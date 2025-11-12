package com.yourorg.banking.account.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record AccountOpeningRequest(
    @NotBlank(message = "Account name is required")
    @Size(min = 2, max = 50, message = "Account name must be between 2 and 50 characters")
    String accountName,
    
    @NotNull(message = "Account type is required")
    AccountType accountType,
    
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter code")
    String currency,
    
    @NotNull(message = "Initial deposit is required")
    BigDecimal initialDeposit,
    
    String description,
    
    // For joint accounts
    List<UUID> jointAccountHolders,
    
    // KYC requirements
    boolean kycRequired,
    String kycLevel,
    
    // Account preferences
    boolean paperlessStatements,
    boolean emailNotifications,
    String preferredLanguage
) {}

