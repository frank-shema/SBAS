package com.example.accounting.controller;

import com.example.accounting.dto.invoice.InvoiceRequest;
import com.example.accounting.dto.invoice.InvoiceResponse;
import com.example.accounting.model.Account;
import com.example.accounting.model.Invoice;
import com.example.accounting.model.Invoice.InvoiceStatus;
import com.example.accounting.model.InvoiceItem;
import com.example.accounting.model.Transaction;
import com.example.accounting.model.Transaction.TransactionType;
import com.example.accounting.model.User;
import com.example.accounting.repository.AccountRepository;
import com.example.accounting.repository.InvoiceRepository;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoice Management", description = "APIs for managing invoices")
@SecurityRequirement(name = "bearerAuth")
public class InvoiceController {
    private final InvoiceRepository invoiceRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @PostMapping
    @Transactional
    @Operation(summary = "Create invoice", description = "Create invoice with client details, due date, items, and account")
    public ResponseEntity<InvoiceResponse> createInvoice(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody InvoiceRequest request) {
        
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Account account = accountRepository.findByIdAndUser(request.getAccountId(), user)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        Invoice invoice = new Invoice();
        invoice.setClientName(request.getClientName());
        invoice.setClientEmail(request.getClientEmail());
        invoice.setDueDate(request.getDueDate());
        invoice.setAccount(account);
        invoice.setStatus(InvoiceStatus.DRAFT);
        
        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        // Create invoice items
        request.getItems().forEach(itemRequest -> {
            InvoiceItem item = new InvoiceItem();
            item.setInvoice(savedInvoice);
            item.setDescription(itemRequest.getDescription());
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(itemRequest.getUnitPrice());
            savedInvoice.getItems().add(item);
        });
        
        Invoice finalInvoice = invoiceRepository.save(savedInvoice);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(InvoiceResponse.fromEntity(finalInvoice));
    }
    
    @GetMapping("/{invoiceId}")
    @Operation(summary = "Get invoice details", description = "Get details of a specific invoice by ID")
    public ResponseEntity<InvoiceResponse> getInvoice(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "Invoice ID") @PathVariable Long invoiceId) {
        
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Find accounts owned by the user
        List<Account> userAccounts = accountRepository.findByUser(user);
        
        // Find invoice that belongs to one of the user's accounts
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .filter(i -> userAccounts.contains(i.getAccount()))
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        
        return ResponseEntity.ok(InvoiceResponse.fromEntity(invoice));
    }
    
    @GetMapping
    @Operation(summary = "List invoices", description = "List invoices with filters: status, clientName, date; support pagination")
    public ResponseEntity<Map<String, Object>> listInvoices(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "Invoice status") @RequestParam(required = false) InvoiceStatus status,
            @Parameter(description = "Client name") @RequestParam(required = false) String clientName,
            @Parameter(description = "Start date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Find accounts owned by the user
        List<Account> userAccounts = accountRepository.findByUser(user);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("dueDate").descending());
        Page<Invoice> invoices;
        
        if (status != null && clientName != null && startDate != null && endDate != null) {
            // Filter by status, client name, and date range
            invoices = invoiceRepository.findByAccountInAndStatusAndClientNameContainingIgnoreCaseAndDueDateBetween(
                    userAccounts, status, clientName, startDate, endDate, pageable);
        } else if (status != null && startDate != null && endDate != null) {
            // Filter by status and date range
            invoices = invoiceRepository.findByAccountInAndStatusAndDueDateBetween(
                    userAccounts, status, startDate, endDate, pageable);
        } else if (clientName != null && startDate != null && endDate != null) {
            // Filter by client name and date range
            invoices = invoiceRepository.findByAccountInAndClientNameContainingIgnoreCaseAndDueDateBetween(
                    userAccounts, clientName, startDate, endDate, pageable);
        } else if (startDate != null && endDate != null) {
            // Filter by date range
            invoices = invoiceRepository.findByAccountInAndDueDateBetween(
                    userAccounts, startDate, endDate, pageable);
        } else if (status != null && clientName != null) {
            // Filter by status and client name
            invoices = invoiceRepository.findByAccountInAndStatusAndClientNameContainingIgnoreCase(
                    userAccounts, status, clientName, pageable);
        } else if (status != null) {
            // Filter by status
            invoices = invoiceRepository.findByAccountInAndStatus(userAccounts, status, pageable);
        } else if (clientName != null) {
            // Filter by client name
            invoices = invoiceRepository.findByAccountInAndClientNameContainingIgnoreCase(
                    userAccounts, clientName, pageable);
        } else {
            // No filters
            invoices = invoiceRepository.findByAccountIn(userAccounts, pageable);
        }
        
        List<InvoiceResponse> invoiceResponses = invoices.getContent().stream()
                .map(InvoiceResponse::fromEntity)
                .collect(Collectors.toList());
        
        Map<String, Object> response = Map.of(
                "invoices", invoiceResponses,
                "currentPage", invoices.getNumber(),
                "totalItems", invoices.getTotalElements(),
                "totalPages", invoices.getTotalPages()
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{invoiceId}/status")
    @Transactional
    @Operation(summary = "Update invoice status", description = "Update invoice status (creates transaction if PAID)")
    public ResponseEntity<InvoiceResponse> updateInvoiceStatus(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "Invoice ID") @PathVariable Long invoiceId,
            @Valid @RequestBody Map<String, String> request) {
        
        String statusStr = request.get("status");
        if (statusStr == null) {
            return ResponseEntity.badRequest().build();
        }
        
        InvoiceStatus status;
        try {
            status = InvoiceStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Find accounts owned by the user
        List<Account> userAccounts = accountRepository.findByUser(user);
        
        // Find invoice that belongs to one of the user's accounts
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .filter(i -> userAccounts.contains(i.getAccount()))
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        
        // If status is changing to PAID, create a transaction
        if (status == InvoiceStatus.PAID && invoice.getStatus() != InvoiceStatus.PAID) {
            Account account = invoice.getAccount();
            
            Transaction transaction = new Transaction();
            transaction.setAccount(account);
            transaction.setAmount(invoice.getTotal());
            transaction.setType(TransactionType.INCOME);
            transaction.setCategory("Invoice Payment");
            transaction.setDate(LocalDateTime.now());
            transaction.setDescription("Payment for invoice #" + invoice.getId() + " from " + invoice.getClientName());
            
            // Update account balance
            account.setBalance(account.getBalance().add(invoice.getTotal()));
            
            accountRepository.save(account);
            transactionRepository.save(transaction);
        }
        
        invoice.setStatus(status);
        Invoice updatedInvoice = invoiceRepository.save(invoice);
        
        return ResponseEntity.ok(InvoiceResponse.fromEntity(updatedInvoice));
    }
    
    @DeleteMapping("/{invoiceId}")
    @Operation(summary = "Delete invoice", description = "Delete draft invoice")
    public ResponseEntity<?> deleteInvoice(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "Invoice ID") @PathVariable Long invoiceId) {
        
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Find accounts owned by the user
        List<Account> userAccounts = accountRepository.findByUser(user);
        
        // Find invoice that belongs to one of the user's accounts
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .filter(i -> userAccounts.contains(i.getAccount()))
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        
        // Only draft invoices can be deleted
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Only draft invoices can be deleted"));
        }
        
        invoiceRepository.delete(invoice);
        
        return ResponseEntity.noContent().build();
    }
}