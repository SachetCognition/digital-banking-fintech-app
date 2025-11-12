package com.yourorg.banking.card.model;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record UpdateCardLimitsRequest(
    @DecimalMin(value = "0.01", message = "Spending limit must be greater than 0")
    BigDecimal spendingLimit,
    
    @DecimalMin(value = "0.01", message = "Daily limit must be greater than 0")
    BigDecimal dailyLimit,
    
    @DecimalMin(value = "0.01", message = "Monthly limit must be greater than 0")
    BigDecimal monthlyLimit
) {}

