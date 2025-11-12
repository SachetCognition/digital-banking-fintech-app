package com.yourorg.banking.card.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CardTransactionRequest(
    @NotBlank(message = "Transaction ID is required")
    @Size(max = 100, message = "Transaction ID must not exceed 100 characters")
    String transactionId,
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    BigDecimal amount,
    
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    String currency,
    
    @NotNull(message = "Transaction type is required")
    CardTransactionType transactionType,
    
    @Size(max = 255, message = "Merchant name must not exceed 255 characters")
    String merchantName,
    
    @Size(max = 4, message = "Merchant category code must not exceed 4 characters")
    String merchantCategoryCode,
    
    @Size(max = 2, message = "Merchant country must not exceed 2 characters")
    String merchantCountry,
    
    @NotNull(message = "Transaction date is required")
    LocalDateTime transactionDate,
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description,
    
    @Size(max = 100, message = "Reference number must not exceed 100 characters")
    String referenceNumber
) {}

