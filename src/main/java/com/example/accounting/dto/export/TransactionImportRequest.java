package com.example.accounting.dto.export;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionImportRequest {
    @NotNull(message = "Account ID is required")
    private Long accountId;
    
    @NotNull(message = "File is required")
    private MultipartFile file;
}