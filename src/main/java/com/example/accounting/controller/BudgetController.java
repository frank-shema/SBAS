package com.example.accounting.controller;

import com.example.accounting.dto.budget.BudgetAlertResponse;
import com.example.accounting.dto.budget.BudgetRequest;
import com.example.accounting.dto.budget.BudgetResponse;
import com.example.accounting.model.Account;
import com.example.accounting.model.Budget;
import com.example.accounting.model.Budget.BudgetPeriod;
import com.example.accounting.model.Transaction;
import com.example.accounting.model.Transaction.TransactionType;
import com.example.accounting.model.User;
import com.example.accounting.repository.AccountRepository;
import com.example.accounting.repository.BudgetRepository;
import com.example.accounting.repository.TransactionRepository;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@Tag(name = "Budget Management", description = "APIs for managing budgets")
@SecurityRequirement(name = "bearerAuth")
public class BudgetController {
    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @PostMapping
    @Operation(summary = "Create budget", description = "Create budget with category, amount, and period")
    public ResponseEntity<BudgetResponse> createBudget(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody BudgetRequest request) {

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if budget with same category and period already exists
        if (budgetRepository.existsByUserAndCategoryAndPeriod(user, request.getCategory(), request.getPeriod())) {
            return ResponseEntity.badRequest().build();
        }

        Budget budget = new Budget();
        budget.setUser(user);
        budget.setCategory(request.getCategory());
        budget.setAmount(request.getAmount());
        budget.setPeriod(request.getPeriod());

        Budget savedBudget = budgetRepository.save(budget);

        // Calculate spent amount for the current period
        BigDecimal spent = calculateSpentAmount(user, savedBudget.getCategory(), savedBudget.getPeriod());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BudgetResponse.fromEntity(savedBudget, spent));
    }

    @GetMapping
    @Operation(summary = "List budgets", description = "List all budgets with current spending")
    public ResponseEntity<List<BudgetResponse>> listBudgets(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "Budget period filter") @RequestParam(required = false) BudgetPeriod period) {

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Budget> budgets;
        if (period != null) {
            budgets = budgetRepository.findByUserAndPeriod(user, period);
        } else {
            budgets = budgetRepository.findByUser(user);
        }

        // Calculate spent amount for each budget
        List<BudgetResponse> budgetResponses = new ArrayList<>();
        for (Budget budget : budgets) {
            BigDecimal spent = calculateSpentAmount(user, budget.getCategory(), budget.getPeriod());
            budgetResponses.add(BudgetResponse.fromEntity(budget, spent));
        }

        return ResponseEntity.ok(budgetResponses);
    }

    @GetMapping("/alerts")
    @Operation(summary = "Get budget alerts", description = "Get alerts for budgets nearing/exceeding limits")
    public ResponseEntity<List<BudgetAlertResponse>> getBudgetAlerts(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Budget> budgets = budgetRepository.findByUser(user);

        // Calculate spent amount and create alerts for each budget
        List<BudgetAlertResponse> alerts = new ArrayList<>();
        for (Budget budget : budgets) {
            BigDecimal spent = calculateSpentAmount(user, budget.getCategory(), budget.getPeriod());
            BudgetResponse budgetResponse = BudgetResponse.fromEntity(budget, spent);

            // Only include budgets with warning or danger alert levels
            if (budgetResponse.getPercentUsed() >= 70) {
                alerts.add(BudgetAlertResponse.fromBudgetResponse(budgetResponse));
            }
        }

        return ResponseEntity.ok(alerts);
    }

    private BigDecimal calculateSpentAmount(User user, String category, BudgetPeriod period) {
        LocalDateTime startDate;
        LocalDateTime endDate = LocalDateTime.now();

        // Calculate start date based on budget period
        switch (period) {
            case DAILY:
                startDate = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
                break;
            case WEEKLY:
                startDate = LocalDateTime.of(
                        LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)),
                        LocalTime.MIDNIGHT);
                break;
            case MONTHLY:
                startDate = LocalDateTime.of(
                        LocalDate.now().withDayOfMonth(1),
                        LocalTime.MIDNIGHT);
                break;
            case QUARTERLY:
                int currentMonth = LocalDate.now().getMonthValue();
                int quarterStartMonth = ((currentMonth - 1) / 3) * 3 + 1;
                startDate = LocalDateTime.of(
                        LocalDate.now().withMonth(quarterStartMonth).withDayOfMonth(1),
                        LocalTime.MIDNIGHT);
                break;
            case YEARLY:
                startDate = LocalDateTime.of(
                        LocalDate.now().withDayOfYear(1),
                        LocalTime.MIDNIGHT);
                break;
            default:
                startDate = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
        }

        // Get all accounts for the user
        List<Account> userAccounts = accountRepository.findByUser(user);

        // Get all transactions for the user's accounts within the date range and category
        BigDecimal totalSpent = BigDecimal.ZERO;

        // Sum up expenses for the category across all accounts
        for (Account account : userAccounts) {
            BigDecimal categorySpent = transactionRepository.sumAmountByAccountAndTypeAndCategoryAndDateBetween(
                    account, TransactionType.EXPENSE, category, startDate, endDate);

            if (categorySpent != null) {
                totalSpent = totalSpent.add(categorySpent);
            }
        }

        return totalSpent;
    }
}
