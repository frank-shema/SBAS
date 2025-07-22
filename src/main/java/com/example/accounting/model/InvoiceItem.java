package com.example.accounting.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entity representing a line item in an invoice.
 * Each item has a description, quantity, and unit price.
 * The total amount for the item is calculated dynamically by multiplying quantity and unit price.
 * 
 * Unlike other entities, InvoiceItem does not extend BaseEntity as it's considered
 * a child entity of Invoice and its lifecycle is managed through the parent.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invoice_items")
public class InvoiceItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @NotBlank
    private String description;

    @NotNull
    @Positive
    private BigDecimal quantity;

    @NotNull
    @Positive
    private BigDecimal unitPrice;

    @Transient
    public BigDecimal getTotal() {
        return quantity.multiply(unitPrice);
    }
}
