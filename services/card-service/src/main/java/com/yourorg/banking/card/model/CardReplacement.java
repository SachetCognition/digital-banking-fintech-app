package com.yourorg.banking.card.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record CardReplacement(
    UUID id,
    UUID originalCardId,
    UUID replacementCardId,
    CardReplacementReason replacementReason,
    CardReplacementStatus replacementStatus,
    LocalDateTime requestedAt,
    LocalDateTime processedAt,
    LocalDateTime completedAt,
    String deliveryMethod,
    String deliveryAddress,
    String trackingNumber,
    String notes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public CardReplacement withStatus(CardReplacementStatus newStatus) {
        return new CardReplacement(
            id, originalCardId, replacementCardId, replacementReason, newStatus,
            requestedAt, processedAt, completedAt, deliveryMethod, deliveryAddress,
            trackingNumber, notes, createdAt, LocalDateTime.now()
        );
    }
    
    public CardReplacement withProcessed() {
        return new CardReplacement(
            id, originalCardId, replacementCardId, replacementReason, CardReplacementStatus.PROCESSING,
            requestedAt, LocalDateTime.now(), completedAt, deliveryMethod, deliveryAddress,
            trackingNumber, notes, createdAt, LocalDateTime.now()
        );
    }
    
    public CardReplacement withCompleted(UUID newReplacementCardId) {
        return new CardReplacement(
            id, originalCardId, newReplacementCardId, replacementReason, CardReplacementStatus.COMPLETED,
            requestedAt, processedAt, LocalDateTime.now(), deliveryMethod, deliveryAddress,
            trackingNumber, notes, createdAt, LocalDateTime.now()
        );
    }
}

