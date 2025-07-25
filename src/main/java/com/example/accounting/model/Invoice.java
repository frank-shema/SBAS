package com.example.accounting.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an invoice in the system.
 * Invoices are created for clients and can have multiple line items.
 * Each invoice is associated with an account and has a status:
 * - DRAFT: Initial state, can be modified or deleted
 * - SENT: Invoice has been sent to the client
 * - PAID: Invoice has been paid (creates a transaction when marked as paid)
 * - OVERDUE: Invoice is past its due date and not paid
 * 
 * The total amount of the invoice is calculated dynamically from its items.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "invoices")
public class Invoice extends BaseEntity {

    @NotBlank
    private String clientName;

    @NotBlank
    @Email
    private String clientEmail;

    @NotNull
    private LocalDate dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @NotNull
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> items = new ArrayList<>();

    @Transient
    public BigDecimal getTotal() {
        return items.stream()
                .map(InvoiceItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public enum InvoiceStatus {
        DRAFT,
        SENT,
        PAID,
        OVERDUE
    }
}
