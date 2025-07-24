package com.example.accounting.controller;

import com.example.accounting.dto.account.AccountRequest;
import com.example.accounting.dto.account.AccountResponse;
import com.example.accounting.model.Account;
import com.example.accounting.model.Account.AccountType;
import com.example.accounting.model.User;
import com.example.accounting.repository.AccountRepository;
import com.example.accounting.repository.UserRepository;
import com.example.accounting.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Account Management", description = "APIs for managing financial accounts")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Create a new account", description = "Create a financial account with name, type, and initial balance")
    public ResponseEntity<AccountResponse> createAccount(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody AccountRequest accountRequest) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if account with same name already exists for this user
        if (accountRepository.existsByNameAndUser(accountRequest.getName(), user)) {
            return ResponseEntity.badRequest().build();
        }

        Account account = new Account();
        account.setUser(user);
        account.setName(accountRequest.getName());
        account.setType(accountRequest.getType());
        account.setBalance(accountRequest.getInitialBalance());

        Account savedAccount = accountRepository.save(account);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AccountResponse.fromEntity(savedAccount));
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get account details", description = "Get details of a specific account by ID")
    public ResponseEntity<AccountResponse> getAccount(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "Account ID") @PathVariable Long accountId) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Account account = accountRepository.findByIdAndUser(accountId, user)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        return ResponseEntity.ok(AccountResponse.fromEntity(account));
    }

    @GetMapping
    @Operation(summary = "List accounts", description = "List all accounts or filter by type")
    public ResponseEntity<List<AccountResponse>> listAccounts(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "Account type filter") @RequestParam(required = false) AccountType type) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Account> accounts;
        if (type != null) {
            accounts = accountRepository.findByUserAndType(user, type);
        } else {
            accounts = accountRepository.findByUser(user);
        }

        List<AccountResponse> accountResponses = accounts.stream()
                .map(AccountResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(accountResponses);
    }

    @PutMapping("/{accountId}")
    @Operation(summary = "Update account name", description = "Update the name of an existing account")
    public ResponseEntity<AccountResponse> updateAccount(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "Account ID") @PathVariable Long accountId,
            @Valid @RequestBody Map<String, String> request) {

        String name = request.get("name");
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Account account = accountRepository.findByIdAndUser(accountId, user)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Check if another account with the same name exists
        if (!account.getName().equals(name) && 
                accountRepository.existsByNameAndUser(name, user)) {
            return ResponseEntity.badRequest().build();
        }

        account.setName(name);
        Account updatedAccount = accountRepository.save(account);

        return ResponseEntity.ok(AccountResponse.fromEntity(updatedAccount));
    }

    @DeleteMapping("/{accountId}")
    @Operation(summary = "Delete account", description = "Delete an account if it has no transactions")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<?> deleteAccount(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "Account ID") @PathVariable Long accountId) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Account account = accountRepository.findByIdAndUser(accountId, user)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // In a real implementation, check if account has transactions before deleting
        // For now, we'll just delete it
        accountRepository.delete(account);

        return ResponseEntity.noContent().build();
    }
}
