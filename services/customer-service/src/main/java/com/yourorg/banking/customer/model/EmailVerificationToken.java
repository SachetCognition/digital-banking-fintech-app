package com.yourorg.banking.customer.model;

import java.time.Instant;
import java.util.UUID;

public record EmailVerificationToken(
    UUID id,
    UUID customerId,
    String token,
    TokenType type,
    Instant expiresAt,
    Instant usedAt,
    Instant createdAt
) {
    public enum TokenType {
        EMAIL_VERIFICATION,
        PASSWORD_RESET,
        MFA_VERIFICATION
    }
    
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
    
    public boolean isUsed() {
        return usedAt != null;
    }
}

