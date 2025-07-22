package com.example.accounting.security;

import com.example.accounting.model.User;
import com.example.accounting.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service that loads user-specific data for authentication.
 * This class implements Spring Security's UserDetailsService interface,
 * which is used by the authentication manager to load user details during authentication.
 * 
 * It retrieves user information from the database and converts it to a UserDetailsImpl object
 * that Spring Security can use for authentication and authorization.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return UserDetailsImpl.build(user);
    }
}
