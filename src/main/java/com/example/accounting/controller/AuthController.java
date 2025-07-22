package com.example.accounting.controller;

import com.example.accounting.dto.auth.*;
import com.example.accounting.model.PasswordResetToken;
import com.example.accounting.model.User;
import com.example.accounting.repository.PasswordResetTokenRepository;
import com.example.accounting.repository.UserRepository;
import com.example.accounting.security.JwtTokenProvider;
import com.example.accounting.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication API")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Register a new user with username, email, password, and role")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username is already taken"));
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is already in use"));
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(registerRequest.getRole());

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User registered successfully"));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Login with username and password to get JWT token")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        return ResponseEntity.ok(new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getAuthorities().stream()
                        .map(item -> {
                            String authority = item.getAuthority();
                            if (authority.startsWith("ROLE_")) {
                                authority = authority.substring(5);
                            }
                            return User.Role.valueOf(authority);
                        })
                        .findFirst()
                        .orElse(User.Role.ACCOUNTANT)
        ));
    }

    @PostMapping("/password/reset-request")
    @Operation(summary = "Request password reset", description = "Request password reset via email")
    public ResponseEntity<?> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        
        if (userOptional.isEmpty()) {
            // Don't reveal that the email doesn't exist
            return ResponseEntity.ok(Map.of("message", "If your email is registered, you will receive a password reset link"));
        }
        
        User user = userOptional.get();
        
        // Delete any existing token for this user
        passwordResetTokenRepository.findByUser(user).ifPresent(passwordResetTokenRepository::delete);
        
        // Create a new token
        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setUser(user);
        passwordResetToken.setToken(token);
        passwordResetToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        
        passwordResetTokenRepository.save(passwordResetToken);
        
        // In a real application, send an email with the token
        // For this example, we'll just return the token in the response
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset link has been sent to your email");
        response.put("token", token); // In a real app, don't return this
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/password/reset")
    @Operation(summary = "Reset password", description = "Reset password using token")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetTokenRequest request) {
        Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByToken(request.getToken());
        
        if (tokenOptional.isEmpty() || tokenOptional.get().isExpired()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired token"));
        }
        
        PasswordResetToken passwordResetToken = tokenOptional.get();
        User user = passwordResetToken.getUser();
        
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        
        passwordResetTokenRepository.delete(passwordResetToken);
        
        return ResponseEntity.ok(Map.of("message", "Password has been reset successfully"));
    }
}