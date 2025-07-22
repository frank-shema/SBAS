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
public class BalanceSheetResponse {
    private LocalDateTime asOfDate;
    private List<AccountBalance> assets;
    private List<AccountBalance> liabilities;
    private BigDecimal totalAssets;
    private BigDecimal totalLiabilities;
    private BigDecimal equity;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AccountBalance {
        private Long accountId;
        private String accountName;
        private BigDecimal balance;
    }
}