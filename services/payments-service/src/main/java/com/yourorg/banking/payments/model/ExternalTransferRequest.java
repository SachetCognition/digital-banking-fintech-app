package com.yourorg.banking.payments.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ExternalTransferRequest(
    @NotNull
    UUID fromAccountId,
    
    @NotBlank(message = "Bank code is required")
    @Size(max = 20, message = "Bank code must not exceed 20 characters")
    String toBankCode,
    
    @NotBlank(message = "Bank name is required")
    @Size(max = 255, message = "Bank name must not exceed 255 characters")
    String toBankName,
    
    @NotBlank(message = "Account number is required")
    @Size(max = 50, message = "Account number must not exceed 50 characters")
    String toAccountNumber,
    
    @NotBlank(message = "Account name is required")
    @Size(max = 255, message = "Account name must not exceed 255 characters")
    String toAccountName,
    
    @Size(max = 20, message = "Routing number must not exceed 20 characters")
    String toRoutingNumber,
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    BigDecimal amount,
    
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    String currency,
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description,
    
    @Size(max = 100, message = "Reference number must not exceed 100 characters")
    String referenceNumber
) {}

