package com.yourorg.banking.card.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateCardRequest(
    @NotNull
    java.util.UUID accountId,
    
    @NotBlank(message = "Card holder name is required")
    @Size(max = 255, message = "Card holder name must not exceed 255 characters")
    String cardHolderName,
    
    @NotNull(message = "Card type is required")
    CardType cardType,
    
    @DecimalMin(value = "0.01", message = "Spending limit must be greater than 0")
    BigDecimal spendingLimit,
    
    @DecimalMin(value = "0.01", message = "Daily limit must be greater than 0")
    BigDecimal dailyLimit,
    
    @DecimalMin(value = "0.01", message = "Monthly limit must be greater than 0")
    BigDecimal monthlyLimit,
    
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    String currency,
    
    boolean isPrimary
) {}

