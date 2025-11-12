package com.yourorg.banking.card.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record CardPin(
    UUID id,
    UUID cardId,
    String pinHash,
    Integer pinAttempts,
    boolean isLocked,
    LocalDateTime lockedUntil,
    LocalDateTime lastChangedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public CardPin withPinChanged(String newPinHash) {
        return new CardPin(
            id, cardId, newPinHash, 0, false, null,
            LocalDateTime.now(), createdAt, LocalDateTime.now()
        );
    }
    
    public CardPin withFailedAttempt() {
        int newAttempts = pinAttempts + 1;
        boolean shouldLock = newAttempts >= 3;
        return new CardPin(
            id, cardId, pinHash, newAttempts, shouldLock,
            shouldLock ? LocalDateTime.now().plusHours(24) : lockedUntil,
            lastChangedAt, createdAt, LocalDateTime.now()
        );
    }
    
    public CardPin withResetAttempts() {
        return new CardPin(
            id, cardId, pinHash, 0, false, null,
            lastChangedAt, createdAt, LocalDateTime.now()
        );
    }
    
    public boolean isCurrentlyLocked() {
        return isLocked && (lockedUntil == null || LocalDateTime.now().isBefore(lockedUntil));
    }
}

