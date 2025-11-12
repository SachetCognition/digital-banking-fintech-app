package com.yourorg.banking.payments.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateBeneficiaryRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must not exceed 255 characters")
        String name,
        
        @NotBlank(message = "Account number is required")
        @Size(max = 50, message = "Account number must not exceed 50 characters")
        String accountNumber,
        
        @NotBlank(message = "Bank code is required")
        @Size(max = 20, message = "Bank code must not exceed 20 characters")
        String bankCode,
        
        @NotBlank(message = "Bank name is required")
        @Size(max = 255, message = "Bank name must not exceed 255 characters")
        String bankName,
        
        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must be 3 characters")
        String currency,
        
        @NotNull(message = "Type is required")
        BeneficiaryType type,
        
        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description
) {}

