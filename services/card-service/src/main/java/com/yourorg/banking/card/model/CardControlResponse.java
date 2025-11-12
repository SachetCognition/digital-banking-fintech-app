package com.yourorg.banking.card.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record CardControlResponse(
    UUID id,
    UUID cardId,
    CardControlType controlType,
    String controlValue,
    boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime expiresAt
) {
    public static CardControlResponse from(CardControl control) {
        return new CardControlResponse(
            control.id(),
            control.cardId(),
            control.controlType(),
            control.controlValue(),
            control.isActive(),
            control.createdAt(),
            control.expiresAt()
        );
    }
}

