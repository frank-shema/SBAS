package com.example.accounting.controller;

import com.example.accounting.dto.transaction.TransactionRequest;
import com.example.accounting.dto.transaction.TransactionResponse;
import com.example.accounting.model.Account;
import com.example.accounting.model.Transaction;
import com.example.accounting.model.Transaction.TransactionType;
import com.example.accounting.model.User;
import com.example.accounting.repository.AccountRepository;
import com.example.accounting.repository.TransactionRepository;
import com.example.accounting.repository.UserRepository;
import com.example.accounting.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Management", description = "APIs for managing transactions")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @PostMapping
    @Transactional
    @Operation(summary = "Record a transaction", description = "Record income/expense transaction with account, amount, type, category, date, and description")
    public ResponseEntity<TransactionResponse> createTransaction(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody TransactionRequest request) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Account account = accountRepository.findByIdAndUser(request.getAccountId(), user)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setCategory(request.getCategory());
        transaction.setDate(request.getDate());
        transaction.setDescription(request.getDescription());

        // Update account balance
        if (request.getType() == TransactionType.INCOME) {
            account.setBalance(account.getBalance().add(request.getAmount()));
        } else {
            account.setBalance(account.getBalance().subtract(request.getAmount()));
        }

        accountRepository.save(account);
        Transaction savedTransaction = transactionRepository.save(transaction);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TransactionResponse.fromEntity(savedTransaction));
    }

    @GetMapping("/{transactionId}")
    @Operation(summary = "Get transaction details", description = "Get details of a specific transaction by ID")
    public ResponseEntity<TransactionResponse> getTransaction(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "Transaction ID") @PathVariable Long transactionId) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find accounts owned by the user
        List<Account> userAccounts = accountRepository.findByUser(user);

        // Find transaction that belongs to one of the user's accounts
        Transaction transaction = transactionRepository.findById(transactionId)
                .filter(t -> userAccounts.contains(t.getAccount()))
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        return ResponseEntity.ok(TransactionResponse.fromEntity(transaction));
    }

    @GetMapping
    @Operation(summary = "List transactions", description = "List transactions with filters: accountId, startDate, endDate, category, type; support pagination")
    public ResponseEntity<Map<String, Object>> listTransactions(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "Account ID") @RequestParam(required = false) Long accountId,
            @Parameter(description = "Start date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Category") @RequestParam(required = false) String category,
            @Parameter(description = "Transaction type") @RequestParam(required = false) TransactionType type,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        Page<Transaction> transactions;

        if (accountId != null) {
            Account account = accountRepository.findByIdAndUser(accountId, user)
                    .orElseThrow(() -> new RuntimeException("Account not found"));

            if (startDate != null && endDate != null) {
                if (category != null && type != null) {
                    // Filter by account, date range, category, and type
                    transactions = transactionRepository.findByAccountAndCategoryAndTypeAndDateBetween(
                            account, category, type, startDate, endDate, pageable);
                } else if (category != null) {
                    // Filter by account, date range, and category
                    transactions = transactionRepository.findByAccountAndCategoryAndDateBetween(
                            account, category, startDate, endDate, pageable);
                } else if (type != null) {
                    // Filter by account, date range, and type
                    transactions = transactionRepository.findByAccountAndTypeAndDateBetween(
                            account, type, startDate, endDate, pageable);
                } else {
                    // Filter by account and date range
                    transactions = transactionRepository.findByAccountAndDateBetween(
                            account, startDate, endDate, pageable);
                }
            } else if (category != null) {
                // Filter by account and category
                transactions = transactionRepository.findByAccountAndCategory(account, category, pageable);
            } else if (type != null) {
                // Filter by account and type
                transactions = transactionRepository.findByAccountAndType(account, type, pageable);
            } else {
                // Filter by account only
                transactions = transactionRepository.findByAccount(account, pageable);
            }
        } else {
            // Find all accounts owned by the user
            List<Account> userAccounts = accountRepository.findByUser(user);

            // Find all transactions for these accounts
            transactions = transactionRepository.findByAccountIn(userAccounts, pageable);
        }

        List<TransactionResponse> transactionResponses = transactions.getContent().stream()
                .map(TransactionResponse::fromEntity)
                .collect(Collectors.toList());

        Map<String, Object> response = Map.of(
                "transactions", transactionResponses,
                "currentPage", transactions.getNumber(),
                "totalItems", transactions.getTotalElements(),
                "totalPages", transactions.getTotalPages()
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{transactionId}")
    @Operation(summary = "Update transaction", description = "Update transaction with new details")
    @PreAuthorize("hasAnyRole('OWNER', 'ACCOUNTANT')")
    @Transactional
    public ResponseEntity<TransactionResponse> updateTransaction(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "Transaction ID") @PathVariable Long transactionId,
            @Valid @RequestBody TransactionRequest request) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find accounts owned by the user
        List<Account> userAccounts = accountRepository.findByUser(user);

        // Find transaction that belongs to one of the user's accounts
        Transaction transaction = transactionRepository.findById(transactionId)
                .filter(t -> userAccounts.contains(t.getAccount()))
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // Get the account for the updated transaction
        Account account = null;
        if (!transaction.getAccount().getId().equals(request.getAccountId())) {
            // Account is being changed, verify the new account belongs to the user
            account = accountRepository.findByIdAndUser(request.getAccountId(), user)
                    .orElseThrow(() -> new RuntimeException("Account not found"));

            // Revert the balance change from the old account
            if (transaction.getType() == TransactionType.INCOME) {
                transaction.getAccount().setBalance(transaction.getAccount().getBalance().subtract(transaction.getAmount()));
            } else {
                transaction.getAccount().setBalance(transaction.getAccount().getBalance().add(transaction.getAmount()));
            }

            // Save the old account with updated balance
            accountRepository.save(transaction.getAccount());

            // Set the new account
            transaction.setAccount(account);
        } else {
            account = transaction.getAccount();
        }

        // Check if amount or type has changed
        boolean amountChanged = !transaction.getAmount().equals(request.getAmount());
        boolean typeChanged = transaction.getType() != request.getType();

        if (amountChanged || typeChanged) {
            // Update account balance

            // First, revert the effect of the old transaction if amount or type changed
            if (transaction.getType() == TransactionType.INCOME) {
                account.setBalance(account.getBalance().subtract(transaction.getAmount()));
            } else {
                account.setBalance(account.getBalance().add(transaction.getAmount()));
            }

            // Then, apply the effect of the new transaction
            if (request.getType() == TransactionType.INCOME) {
                account.setBalance(account.getBalance().add(request.getAmount()));
            } else {
                account.setBalance(account.getBalance().subtract(request.getAmount()));
            }

            // Save the account with updated balance
            accountRepository.save(account);
        }

        // Update transaction fields
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setCategory(request.getCategory());
        transaction.setDate(request.getDate());
        transaction.setDescription(request.getDescription());

        Transaction updatedTransaction = transactionRepository.save(transaction);

        return ResponseEntity.ok(TransactionResponse.fromEntity(updatedTransaction));
    }

    @DeleteMapping("/{transactionId}")
    @Transactional
    @Operation(summary = "Delete transaction", description = "Delete transaction and update balance")
    public ResponseEntity<?> deleteTransaction(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "Transaction ID") @PathVariable Long transactionId) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find accounts owned by the user
        List<Account> userAccounts = accountRepository.findByUser(user);

        // Find transaction that belongs to one of the user's accounts
        Transaction transaction = transactionRepository.findById(transactionId)
                .filter(t -> userAccounts.contains(t.getAccount()))
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        Account account = transaction.getAccount();

        // Update account balance
        if (transaction.getType() == TransactionType.INCOME) {
            account.setBalance(account.getBalance().subtract(transaction.getAmount()));
        } else {
            account.setBalance(account.getBalance().add(transaction.getAmount()));
        }

        accountRepository.save(account);
        transactionRepository.delete(transaction);

        return ResponseEntity.noContent().build();
    }
}
