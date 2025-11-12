package com.yourorg.banking.card.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CardReplacementRequest(
    @NotNull(message = "Replacement reason is required")
    CardReplacementReason replacementReason,
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    String notes,
    
    String deliveryMethod,
    
    @Size(max = 1000, message = "Delivery address must not exceed 1000 characters")
    String deliveryAddress
) {}

