package com.yourorg.banking.payments.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record BillPayRequest(
        @NotNull(message = "Payer account ID is required")
        UUID payerAccountId,
        
        @NotNull(message = "Beneficiary ID is required")
        UUID beneficiaryId,
        
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
        BigDecimal amount,
        
        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must be 3 characters")
        String currency,
        
        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description,
        
        @NotBlank(message = "Idempotency key is required")
        String idempotencyKey
) {}

