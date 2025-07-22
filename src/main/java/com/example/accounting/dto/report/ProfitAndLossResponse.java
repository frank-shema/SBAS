package com.example.accounting.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfitAndLossResponse {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<CategoryAmount> revenue;
    private List<CategoryAmount> expenses;
    private BigDecimal totalRevenue;
    private BigDecimal totalExpenses;
    private BigDecimal netProfit;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryAmount {
        private String category;
        private BigDecimal amount;
    }
}