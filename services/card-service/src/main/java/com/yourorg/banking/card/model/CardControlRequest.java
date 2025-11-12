package com.yourorg.banking.card.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CardControlRequest(
    @NotNull(message = "Control type is required")
    CardControlType controlType,
    
    @NotBlank(message = "Control value is required")
    String controlValue,
    
    LocalDateTime expiresAt
) {}

