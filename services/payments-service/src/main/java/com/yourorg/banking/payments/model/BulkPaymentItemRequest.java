package com.yourorg.banking.payments.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record BulkPaymentItemRequest(
    @NotBlank(message = "Recipient name is required")
    @Size(max = 255, message = "Recipient name must not exceed 255 characters")
    String recipientName,
    
    @NotBlank(message = "Account number is required")
    @Size(max = 50, message = "Account number must not exceed 50 characters")
    String recipientAccountNumber,
    
    @Size(max = 20, message = "Bank code must not exceed 20 characters")
    String recipientBankCode,
    
    @Size(max = 255, message = "Bank name must not exceed 255 characters")
    String recipientBankName,
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    BigDecimal amount,
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description
) {}

