package com.example.accounting.controller;

import com.example.accounting.dto.report.BalanceSheetResponse;
import com.example.accounting.dto.report.CashFlowResponse;
import com.example.accounting.dto.report.ProfitAndLossResponse;
import com.example.accounting.model.Account;
import com.example.accounting.model.Account.AccountType;
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
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Financial Reports", description = "APIs for generating financial reports")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @GetMapping("/balance-sheet")
    @Operation(summary = "Generate balance sheet", description = "Generate balance sheet (assets, liabilities, equity; optional date)")
    public ResponseEntity<BalanceSheetResponse> generateBalanceSheet(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "As of date (optional)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime asOfDate) {
        
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (asOfDate == null) {
            asOfDate = LocalDateTime.now();
        }
        
        // Get all accounts for the user
        List<Account> userAccounts = accountRepository.findByUser(user);
        
        // Separate accounts by type
        List<Account> assetAccounts = userAccounts.stream()
                .filter(account -> account.getType() == AccountType.ASSET)
                .collect(Collectors.toList());
        
        List<Account> liabilityAccounts = userAccounts.stream()
                .filter(account -> account.getType() == AccountType.LIABILITY)
                .collect(Collectors.toList());
        
        // Create account balances for assets
        List<BalanceSheetResponse.AccountBalance> assetBalances = assetAccounts.stream()
                .map(account -> BalanceSheetResponse.AccountBalance.builder()
                        .accountId(account.getId())
                        .accountName(account.getName())
                        .balance(account.getBalance())
                        .build())
                .collect(Collectors.toList());
        
        // Create account balances for liabilities
        List<BalanceSheetResponse.AccountBalance> liabilityBalances = liabilityAccounts.stream()
                .map(account -> BalanceSheetResponse.AccountBalance.builder()
                        .accountId(account.getId())
                        .accountName(account.getName())
                        .balance(account.getBalance())
                        .build())
                .collect(Collectors.toList());
        
        // Calculate totals
        BigDecimal totalAssets = assetBalances.stream()
                .map(BalanceSheetResponse.AccountBalance::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalLiabilities = liabilityBalances.stream()
                .map(BalanceSheetResponse.AccountBalance::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal equity = totalAssets.subtract(totalLiabilities);
        
        // Create response
        BalanceSheetResponse response = BalanceSheetResponse.builder()
                .asOfDate(asOfDate)
                .assets(assetBalances)
                .liabilities(liabilityBalances)
                .totalAssets(totalAssets)
                .totalLiabilities(totalLiabilities)
                .equity(equity)
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/profit-and-loss")
    @Operation(summary = "Generate profit and loss statement", description = "Generate profit and loss statement (revenue, expenses, net profit; requires startDate, endDate)")
    public ResponseEntity<ProfitAndLossResponse> generateProfitAndLoss(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "Start date") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get all accounts for the user
        List<Account> userAccounts = accountRepository.findByUser(user);
        
        // Get all transactions for the user's accounts within the date range
        List<Transaction> transactions = new ArrayList<>();
        for (Account account : userAccounts) {
            transactions.addAll(
                    transactionRepository.findByAccountAndDateBetweenOrderByDateDesc(
                            account, startDate, endDate));
        }
        
        // Separate transactions by type
        List<Transaction> incomeTransactions = transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.INCOME)
                .collect(Collectors.toList());
        
        List<Transaction> expenseTransactions = transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                .collect(Collectors.toList());
        
        // Group income transactions by category and sum amounts
        Map<String, BigDecimal> incomeByCategory = new HashMap<>();
        for (Transaction transaction : incomeTransactions) {
            String category = transaction.getCategory();
            BigDecimal amount = transaction.getAmount();
            
            incomeByCategory.put(category, 
                    incomeByCategory.getOrDefault(category, BigDecimal.ZERO).add(amount));
        }
        
        // Group expense transactions by category and sum amounts
        Map<String, BigDecimal> expensesByCategory = new HashMap<>();
        for (Transaction transaction : expenseTransactions) {
            String category = transaction.getCategory();
            BigDecimal amount = transaction.getAmount();
            
            expensesByCategory.put(category, 
                    expensesByCategory.getOrDefault(category, BigDecimal.ZERO).add(amount));
        }
        
        // Create category amounts for revenue
        List<ProfitAndLossResponse.CategoryAmount> revenue = incomeByCategory.entrySet().stream()
                .map(entry -> ProfitAndLossResponse.CategoryAmount.builder()
                        .category(entry.getKey())
                        .amount(entry.getValue())
                        .build())
                .collect(Collectors.toList());
        
        // Create category amounts for expenses
        List<ProfitAndLossResponse.CategoryAmount> expenses = expensesByCategory.entrySet().stream()
                .map(entry -> ProfitAndLossResponse.CategoryAmount.builder()
                        .category(entry.getKey())
                        .amount(entry.getValue())
                        .build())
                .collect(Collectors.toList());
        
        // Calculate totals
        BigDecimal totalRevenue = revenue.stream()
                .map(ProfitAndLossResponse.CategoryAmount::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalExpenses = expenses.stream()
                .map(ProfitAndLossResponse.CategoryAmount::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal netProfit = totalRevenue.subtract(totalExpenses);
        
        // Create response
        ProfitAndLossResponse response = ProfitAndLossResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .revenue(revenue)
                .expenses(expenses)
                .totalRevenue(totalRevenue)
                .totalExpenses(totalExpenses)
                .netProfit(netProfit)
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/cash-flow")
    @Operation(summary = "Generate cash flow report", description = "Generate cash flow report (inflows, outflows; requires startDate, endDate)")
    public ResponseEntity<CashFlowResponse> generateCashFlow(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "Start date") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get all accounts for the user
        List<Account> userAccounts = accountRepository.findByUser(user);
        
        // Get all transactions for the user's accounts within the date range
        List<Transaction> transactions = new ArrayList<>();
        for (Account account : userAccounts) {
            transactions.addAll(
                    transactionRepository.findByAccountAndDateBetweenOrderByDateDesc(
                            account, startDate, endDate));
        }
        
        // Separate transactions by type
        List<Transaction> inflowTransactions = transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.INCOME)
                .collect(Collectors.toList());
        
        List<Transaction> outflowTransactions = transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                .collect(Collectors.toList());
        
        // Group inflow transactions by category and sum amounts
        Map<String, BigDecimal> inflowsByCategory = new HashMap<>();
        for (Transaction transaction : inflowTransactions) {
            String category = transaction.getCategory();
            BigDecimal amount = transaction.getAmount();
            
            inflowsByCategory.put(category, 
                    inflowsByCategory.getOrDefault(category, BigDecimal.ZERO).add(amount));
        }
        
        // Group outflow transactions by category and sum amounts
        Map<String, BigDecimal> outflowsByCategory = new HashMap<>();
        for (Transaction transaction : outflowTransactions) {
            String category = transaction.getCategory();
            BigDecimal amount = transaction.getAmount();
            
            outflowsByCategory.put(category, 
                    outflowsByCategory.getOrDefault(category, BigDecimal.ZERO).add(amount));
        }
        
        // Create category amounts for inflows
        List<CashFlowResponse.CategoryAmount> inflows = inflowsByCategory.entrySet().stream()
                .map(entry -> CashFlowResponse.CategoryAmount.builder()
                        .category(entry.getKey())
                        .amount(entry.getValue())
                        .build())
                .collect(Collectors.toList());
        
        // Create category amounts for outflows
        List<CashFlowResponse.CategoryAmount> outflows = outflowsByCategory.entrySet().stream()
                .map(entry -> CashFlowResponse.CategoryAmount.builder()
                        .category(entry.getKey())
                        .amount(entry.getValue())
                        .build())
                .collect(Collectors.toList());
        
        // Calculate totals
        BigDecimal totalInflows = inflows.stream()
                .map(CashFlowResponse.CategoryAmount::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalOutflows = outflows.stream()
                .map(CashFlowResponse.CategoryAmount::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal netCashFlow = totalInflows.subtract(totalOutflows);
        
        // Calculate opening and closing balances
        BigDecimal openingBalance = BigDecimal.ZERO;
        BigDecimal closingBalance = BigDecimal.ZERO;
        
        for (Account account : userAccounts) {
            if (account.getType() == AccountType.ASSET) {
                // For asset accounts, add the current balance to the closing balance
                closingBalance = closingBalance.add(account.getBalance());
                
                // Calculate the opening balance by subtracting the net cash flow from the closing balance
                openingBalance = closingBalance.subtract(netCashFlow);
            }
        }
        
        // Create response
        CashFlowResponse response = CashFlowResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .inflows(inflows)
                .outflows(outflows)
                .totalInflows(totalInflows)
                .totalOutflows(totalOutflows)
                .netCashFlow(netCashFlow)
                .openingBalance(openingBalance)
                .closingBalance(closingBalance)
                .build();
        
        return ResponseEntity.ok(response);
    }
}