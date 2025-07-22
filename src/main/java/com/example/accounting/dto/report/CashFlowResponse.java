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
public class CashFlowResponse {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<CategoryAmount> inflows;
    private List<CategoryAmount> outflows;
    private BigDecimal totalInflows;
    private BigDecimal totalOutflows;
    private BigDecimal netCashFlow;
    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryAmount {
        private String category;
        private BigDecimal amount;
    }
}