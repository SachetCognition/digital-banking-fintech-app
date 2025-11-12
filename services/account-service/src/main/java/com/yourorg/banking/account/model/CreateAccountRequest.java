package com.yourorg.banking.account.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateAccountRequest(
        @NotNull(message = "Customer ID is required")
        UUID customerId,
        
        @NotBlank(message = "Account name is required")
        @Size(max = 50, message = "Account name must not exceed 50 characters")
        String accountName,
        
        @NotNull(message = "Account type is required")
        AccountType type,
        
        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must be 3 characters")
        String currency,
        
        @DecimalMin(value = "0.01", message = "Daily transfer limit must be at least 0.01")
        BigDecimal dailyTransferLimit,
        
        @DecimalMin(value = "0.01", message = "Per transaction limit must be at least 0.01")
        BigDecimal perTransactionLimit
) {}

