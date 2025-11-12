package com.yourorg.banking.ledger.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PostJournalRequest(
        @NotBlank(message = "Reference is required")
        @Size(max = 50, message = "Reference must not exceed 50 characters")
        String reference,
        
        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description,
        
        @NotEmpty(message = "Entries are required")
        List<JournalEntryRequest> entries
) {
    public record JournalEntryRequest(
            @NotNull(message = "Account ID is required")
            UUID accountId,
            
            @NotNull(message = "Entry type is required")
            EntryType type,
            
            @NotNull(message = "Amount is required")
            BigDecimal amount,
            
            @NotBlank(message = "Currency is required")
            @Size(min = 3, max = 3, message = "Currency must be 3 characters")
            String currency,
            
            @Size(max = 255, message = "Description must not exceed 255 characters")
            String description,
            
            @Size(max = 255, message = "Reference must not exceed 255 characters")
            String reference
    ) {}
}

