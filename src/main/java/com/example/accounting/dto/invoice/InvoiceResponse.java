package com.example.accounting.dto.invoice;

import com.example.accounting.model.Invoice;
import com.example.accounting.model.Invoice.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceResponse {
    private Long id;
    private String clientName;
    private String clientEmail;
    private LocalDate dueDate;
    private Long accountId;
    private String accountName;
    private InvoiceStatus status;
    private BigDecimal total;
    private List<InvoiceItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static InvoiceResponse fromEntity(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .clientName(invoice.getClientName())
                .clientEmail(invoice.getClientEmail())
                .dueDate(invoice.getDueDate())
                .accountId(invoice.getAccount().getId())
                .accountName(invoice.getAccount().getName())
                .status(invoice.getStatus())
                .total(invoice.getTotal())
                .items(invoice.getItems().stream()
                        .map(InvoiceItemResponse::fromEntity)
                        .collect(Collectors.toList()))
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .build();
    }
}