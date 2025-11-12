package com.yourorg.banking.payments.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecurringPaymentRequest(
    @NotNull
    UUID fromAccountId,
    
    @NotBlank(message = "Account number is required")
    @Size(max = 50, message = "Account number must not exceed 50 characters")
    String toAccountNumber,
    
    @Size(max = 20, message = "Bank code must not exceed 20 characters")
    String toBankCode,
    
    @Size(max = 255, message = "Bank name must not exceed 255 characters")
    String toBankName,
    
    @NotBlank(message = "Account name is required")
    @Size(max = 255, message = "Account name must not exceed 255 characters")
    String toAccountName,
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    BigDecimal amount,
    
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    String currency,
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description,
    
    @NotNull(message = "Frequency is required")
    PaymentFrequency frequency,
    
    @Size(min = 1, max = 31, message = "Day of month must be between 1 and 31")
    Integer dayOfMonth,
    
    @Size(min = 1, max = 7, message = "Day of week must be between 1 and 7")
    Integer dayOfWeek,
    
    @NotNull(message = "Start date is required")
    LocalDate startDate,
    
    LocalDate endDate,
    
    @Size(min = 1, message = "Max executions must be at least 1")
    Integer maxExecutions
) {}

