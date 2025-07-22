package com.example.accounting.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entity representing a financial account in the system.
 * Each account belongs to a user and has a type (ASSET or LIABILITY) and a balance.
 * - ASSET: Represents resources owned by the business (e.g., cash, bank accounts)
 * - LIABILITY: Represents obligations or debts owed by the business
 * 
 * This entity extends BaseEntity which provides common fields like id, createdAt, and updatedAt.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "accounts")
public class Account extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AccountType type;

    @NotNull
    private BigDecimal balance = BigDecimal.ZERO;

    public enum AccountType {
        ASSET,
        LIABILITY
    }
}
