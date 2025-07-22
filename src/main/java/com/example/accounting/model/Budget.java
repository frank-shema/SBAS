package com.example.accounting.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entity representing a budget in the system.
 * Budgets are set by users to track and limit spending in specific categories.
 * Each budget has:
 * - A category (e.g., "Office Supplies", "Marketing")
 * - A maximum amount that can be spent
 * - A time period (DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY)
 * 
 * The system tracks actual spending against budgets and can generate alerts
 * when spending approaches or exceeds budget limits.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "budgets")
public class Budget extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    private String category;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    private BudgetPeriod period;

    public enum BudgetPeriod {
        DAILY,
        WEEKLY,
        MONTHLY,
        QUARTERLY,
        YEARLY
    }
}
