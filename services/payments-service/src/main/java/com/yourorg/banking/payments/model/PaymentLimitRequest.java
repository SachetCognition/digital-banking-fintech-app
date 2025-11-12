package com.yourorg.banking.payments.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentLimitRequest(
    UUID accountId,
    
    @NotNull(message = "Limit type is required")
    PaymentLimitType limitType,
    
    @NotNull(message = "Limit value is required")
    @DecimalMin(value = "0.01", message = "Limit value must be greater than 0")
    BigDecimal limitValue,
    
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    String currency,
    
    LocalDateTime expiresAt
) {}

