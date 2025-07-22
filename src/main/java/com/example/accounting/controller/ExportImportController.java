package com.example.accounting.controller;

import com.example.accounting.dto.export.TransactionExportRequest;
import com.example.accounting.dto.export.TransactionExportRequest.ExportFormat;
import com.example.accounting.dto.export.TransactionImportRequest;
import com.example.accounting.model.Account;
import com.example.accounting.model.Transaction;
import com.example.accounting.model.Transaction.TransactionType;
import com.example.accounting.model.User;
import com.example.accounting.repository.AccountRepository;
import com.example.accounting.repository.TransactionRepository;
import com.example.accounting.repository.UserRepository;
import com.example.accounting.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Data Export/Import", description = "APIs for exporting and importing data")
@SecurityRequirement(name = "bearerAuth")
public class ExportImportController {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String[] CSV_HEADERS = {"ID", "Account", "Amount", "Type", "Category", "Date", "Description"};

    @GetMapping("/export/transactions")
    @Operation(summary = "Export transactions", description = "Export transactions as CSV or PDF with optional filters")
    public void exportTransactions(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid TransactionExportRequest request,
            HttpServletResponse response) throws IOException {

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Account> accounts;
        if (request.getAccountId() != null) {
            accounts = Collections.singletonList(accountRepository.findByIdAndUser(request.getAccountId(), user)
                    .orElseThrow(() -> new RuntimeException("Account not found")));
        } else {
            accounts = accountRepository.findByUser(user);
        }

        LocalDateTime startDate = request.getStartDate() != null ? request.getStartDate() : LocalDateTime.now().minusYears(1);
        LocalDateTime endDate = request.getEndDate() != null ? request.getEndDate() : LocalDateTime.now();

        List<Transaction> transactions = new ArrayList<>();
        for (Account account : accounts) {
            transactions.addAll(transactionRepository.findByAccountAndDateBetweenOrderByDateDesc(
                    account, startDate, endDate));
        }

        if (request.getFormat() == ExportFormat.PDF) {
            exportTransactionsToPdf(transactions, response);
        } else {
            exportTransactionsToCsv(transactions, response);
        }
    }

    @PostMapping("/import/transactions")
    @Transactional
    @Operation(summary = "Import transactions", description = "Import transactions from CSV file")
    public ResponseEntity<?> importTransactions(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @ModelAttribute @Valid TransactionImportRequest request) {

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Account account = accountRepository.findByIdAndUser(request.getAccountId(), user)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        MultipartFile file = request.getFile();
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Please upload a file"));
        }

        if (!Objects.equals(file.getContentType(), "text/csv") && 
            !Objects.equals(file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.') + 1), "csv")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Only CSV files are supported"));
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            List<Transaction> importedTransactions = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            int lineNumber = 1;

            for (CSVRecord record : csvParser) {
                lineNumber++;
                try {
                    BigDecimal amount = new BigDecimal(record.get("Amount"));
                    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                        errors.add("Line " + lineNumber + ": Amount must be positive");
                        continue;
                    }

                    TransactionType type;
                    try {
                        type = TransactionType.valueOf(record.get("Type").toUpperCase());
                    } catch (IllegalArgumentException e) {
                        errors.add("Line " + lineNumber + ": Invalid transaction type. Must be INCOME or EXPENSE");
                        continue;
                    }

                    LocalDateTime date;
                    try {
                        date = LocalDateTime.parse(record.get("Date"), DATE_FORMATTER);
                    } catch (DateTimeParseException e) {
                        errors.add("Line " + lineNumber + ": Invalid date format. Use yyyy-MM-dd HH:mm:ss");
                        continue;
                    }

                    String category = record.get("Category");
                    if (category == null || category.trim().isEmpty()) {
                        errors.add("Line " + lineNumber + ": Category is required");
                        continue;
                    }

                    Transaction transaction = new Transaction();
                    transaction.setAccount(account);
                    transaction.setAmount(amount);
                    transaction.setType(type);
                    transaction.setCategory(category);
                    transaction.setDate(date);
                    transaction.setDescription(record.get("Description"));

                    importedTransactions.add(transaction);

                    // Update account balance
                    if (type == TransactionType.INCOME) {
                        account.setBalance(account.getBalance().add(amount));
                    } else {
                        account.setBalance(account.getBalance().subtract(amount));
                    }

                } catch (Exception e) {
                    errors.add("Line " + lineNumber + ": " + e.getMessage());
                }
            }

            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Import completed with errors",
                        "errors", errors
                ));
            }

            accountRepository.save(account);
            transactionRepository.saveAll(importedTransactions);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Successfully imported " + importedTransactions.size() + " transactions"
            ));

        } catch (IOException e) {
            log.error("Error importing transactions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error importing transactions: " + e.getMessage()));
        }
    }

    private void exportTransactionsToCsv(List<Transaction> transactions, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"transactions.csv\"");

        try (PrintWriter writer = response.getWriter();
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(CSV_HEADERS))) {

            for (Transaction transaction : transactions) {
                csvPrinter.printRecord(
                        transaction.getId(),
                        transaction.getAccount().getName(),
                        transaction.getAmount(),
                        transaction.getType(),
                        transaction.getCategory(),
                        transaction.getDate().format(DATE_FORMATTER),
                        transaction.getDescription()
                );
            }

            csvPrinter.flush();
        }
    }

    private void exportTransactionsToPdf(List<Transaction> transactions, HttpServletResponse response) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        try {
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Transactions Report");
            contentStream.endText();

            contentStream.setFont(PDType1Font.HELVETICA, 10);
            float yPosition = 730;

            // Header
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText(String.join(" | ", CSV_HEADERS));
            contentStream.endText();
            yPosition -= 15;

            // Data
            for (Transaction transaction : transactions) {
                if (yPosition < 50) {
                    contentStream.close();
                    contentStream = createNewPage(document);
                    yPosition = 750;
                }

                contentStream.beginText();
                contentStream.newLineAtOffset(50, yPosition);
                contentStream.showText(
                        transaction.getId() + " | " +
                        transaction.getAccount().getName() + " | " +
                        transaction.getAmount() + " | " +
                        transaction.getType() + " | " +
                        transaction.getCategory() + " | " +
                        transaction.getDate().format(DATE_FORMATTER) + " | " +
                        (transaction.getDescription() != null ? transaction.getDescription() : "")
                );
                contentStream.endText();
                yPosition -= 15;

                if (yPosition < 50) {
                    contentStream.close();
                    contentStream = createNewPage(document);
                    yPosition = 750;
                }
            }

            contentStream.close();
        } catch (IOException e) {
            if (contentStream != null) {
                contentStream.close();
            }
            throw e;
        }

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"transactions.pdf\"");
        document.save(response.getOutputStream());
        document.close();
    }

    private PDPageContentStream createNewPage(PDDocument document) throws IOException {
        PDPage page = new PDPage();
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        return contentStream;
    }
}
