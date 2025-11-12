package com.yourorg.banking.payments.model;

import jakarta.validation.constraints.Size;

public record UpdateBeneficiaryRequest(
        @Size(max = 255, message = "Name must not exceed 255 characters")
        String name,
        
        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description
) {}

