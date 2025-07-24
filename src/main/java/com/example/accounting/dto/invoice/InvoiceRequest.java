package com.example.accounting.dto.invoice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceRequest {
    @NotBlank(message = "Client name is required")
    private String clientName;

    @NotBlank(message = "Client email is required")
    @Email(message = "Client email should be valid")
    private String clientEmail;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    @NotNull(message = "Account ID is required")
    private Long accountId;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<InvoiceItemRequest> items;
}
