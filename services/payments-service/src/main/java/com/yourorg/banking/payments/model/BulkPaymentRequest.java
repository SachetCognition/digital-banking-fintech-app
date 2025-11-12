package com.yourorg.banking.payments.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record BulkPaymentRequest(
    @NotBlank(message = "Batch name is required")
    @Size(max = 255, message = "Batch name must not exceed 255 characters")
    String batchName,
    
    @NotNull
    UUID fromAccountId,
    
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
    BigDecimal totalAmount,
    
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    String currency,
    
    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    List<BulkPaymentItemRequest> items
) {}

