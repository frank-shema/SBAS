package com.example.accounting.dto.budget;

import com.example.accounting.model.Budget.BudgetPeriod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BudgetAlertResponse {
    private Long budgetId;
    private String category;
    private BudgetPeriod period;
    private BigDecimal amount;
    private BigDecimal spent;
    private BigDecimal remaining;
    private double percentUsed;
    private AlertLevel alertLevel;
    
    public enum AlertLevel {
        OK,       // < 70% used
        WARNING,  // >= 70% and < 90% used
        DANGER    // >= 90% used
    }
    
    public static BudgetAlertResponse fromBudgetResponse(BudgetResponse budget) {
        AlertLevel alertLevel;
        if (budget.getPercentUsed() >= 90) {
            alertLevel = AlertLevel.DANGER;
        } else if (budget.getPercentUsed() >= 70) {
            alertLevel = AlertLevel.WARNING;
        } else {
            alertLevel = AlertLevel.OK;
        }
        
        return BudgetAlertResponse.builder()
                .budgetId(budget.getId())
                .category(budget.getCategory())
                .period(budget.getPeriod())
                .amount(budget.getAmount())
                .spent(budget.getSpent())
                .remaining(budget.getRemaining())
                .percentUsed(budget.getPercentUsed())
                .alertLevel(alertLevel)
                .build();
    }
}