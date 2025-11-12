package com.yourorg.banking.card.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record CardControl(
    UUID id,
    UUID cardId,
    CardControlType controlType,
    String controlValue,
    boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime expiresAt
) {
    public CardControl withActive(boolean active) {
        return new CardControl(
            id, cardId, controlType, controlValue, active,
            createdAt, LocalDateTime.now(), expiresAt
        );
    }
    
    public CardControl withUpdated() {
        return new CardControl(
            id, cardId, controlType, controlValue, isActive,
            createdAt, LocalDateTime.now(), expiresAt
        );
    }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}

