package com.example.accounting.dto.budget;

import com.example.accounting.model.Budget;
import com.example.accounting.model.Budget.BudgetPeriod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BudgetResponse {
    private Long id;
    private String category;
    private BigDecimal amount;
    private BudgetPeriod period;
    private BigDecimal spent;
    private BigDecimal remaining;
    private double percentUsed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static BudgetResponse fromEntity(Budget budget, BigDecimal spent) {
        BigDecimal remaining = budget.getAmount().subtract(spent);
        double percentUsed = spent.doubleValue() / budget.getAmount().doubleValue() * 100;
        
        return BudgetResponse.builder()
                .id(budget.getId())
                .category(budget.getCategory())
                .amount(budget.getAmount())
                .period(budget.getPeriod())
                .spent(spent)
                .remaining(remaining)
                .percentUsed(percentUsed)
                .createdAt(budget.getCreatedAt())
                .updatedAt(budget.getUpdatedAt())
                .build();
    }
}