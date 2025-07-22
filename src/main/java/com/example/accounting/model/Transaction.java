package com.example.accounting.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a financial transaction in the system.
 * Each transaction is associated with an account and can be either an INCOME or EXPENSE.
 * Transactions affect the balance of the associated account:
 * - INCOME: Increases the account balance
 * - EXPENSE: Decreases the account balance
 * 
 * Transactions are categorized and dated, allowing for financial reporting and analysis.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "transactions")
public class Transaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @NotBlank
    private String category;

    @NotNull
    @PastOrPresent
    private LocalDateTime date;

    private String description;

    public enum TransactionType {
        INCOME,
        EXPENSE
    }
}
