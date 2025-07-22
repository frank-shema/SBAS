package com.example.accounting.dto.export;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionExportRequest {
    private Long accountId; // Optional, if not provided, export all transactions
    
    private LocalDateTime startDate; // Optional, if not provided, use 1 year ago
    
    private LocalDateTime endDate; // Optional, if not provided, use current date
    
    @NotNull(message = "Format is required")
    private ExportFormat format = ExportFormat.CSV;
    
    public enum ExportFormat {
        CSV,
        PDF
    }
}