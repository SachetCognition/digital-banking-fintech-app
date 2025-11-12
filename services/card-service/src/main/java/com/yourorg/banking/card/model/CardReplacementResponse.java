package com.yourorg.banking.card.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record CardReplacementResponse(
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
    String notes
) {
    public static CardReplacementResponse from(CardReplacement replacement) {
        return new CardReplacementResponse(
            replacement.id(),
            replacement.originalCardId(),
            replacement.replacementCardId(),
            replacement.replacementReason(),
            replacement.replacementStatus(),
            replacement.requestedAt(),
            replacement.processedAt(),
            replacement.completedAt(),
            replacement.deliveryMethod(),
            replacement.deliveryAddress(),
            replacement.trackingNumber(),
            replacement.notes()
        );
    }
}

