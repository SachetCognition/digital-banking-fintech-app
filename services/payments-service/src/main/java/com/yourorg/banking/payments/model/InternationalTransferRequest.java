package com.yourorg.banking.payments.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record InternationalTransferRequest(
    @NotNull
    UUID fromAccountId,
    
    @NotNull(message = "Transfer type is required")
    InternationalTransferType transferType,
    
    @Size(max = 11, message = "SWIFT code must not exceed 11 characters")
    String toSwiftCode,
    
    @Size(max = 34, message = "IBAN must not exceed 34 characters")
    String toIban,
    
    @NotBlank(message = "Bank name is required")
    @Size(max = 255, message = "Bank name must not exceed 255 characters")
    String toBankName,
    
    @Size(max = 500, message = "Bank address must not exceed 500 characters")
    String toBankAddress,
    
    @NotBlank(message = "Account number is required")
    @Size(max = 50, message = "Account number must not exceed 50 characters")
    String toAccountNumber,
    
    @NotBlank(message = "Account name is required")
    @Size(max = 255, message = "Account name must not exceed 255 characters")
    String toAccountName,
    
    @Size(max = 500, message = "Address must not exceed 500 characters")
    String toAddress,
    
    @NotBlank(message = "Country code is required")
    @Size(min = 2, max = 2, message = "Country code must be 2 characters")
    String toCountryCode,
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    BigDecimal amount,
    
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    String currency,
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description,
    
    @Size(max = 100, message = "Reference number must not exceed 100 characters")
    String referenceNumber,
    
    @Size(max = 11, message = "Correspondent bank SWIFT must not exceed 11 characters")
    String correspondentBankSwift,
    
    @Size(max = 255, message = "Correspondent bank name must not exceed 255 characters")
    String correspondentBankName
) {}

