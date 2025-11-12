package com.yourorg.banking.card.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePinRequest(
    @NotBlank(message = "Current PIN is required")
    @Pattern(regexp = "\\d{4}", message = "Current PIN must be 4 digits")
    String currentPin,
    
    @NotBlank(message = "New PIN is required")
    @Pattern(regexp = "\\d{4}", message = "New PIN must be 4 digits")
    String newPin,
    
    @NotBlank(message = "Confirm PIN is required")
    @Pattern(regexp = "\\d{4}", message = "Confirm PIN must be 4 digits")
    String confirmPin
) {
    public boolean isPinMatch() {
        return newPin.equals(confirmPin);
    }
}

