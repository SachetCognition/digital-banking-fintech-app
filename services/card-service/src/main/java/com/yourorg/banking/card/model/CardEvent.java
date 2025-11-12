package com.yourorg.banking.card.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record CardEvent(
    UUID id,
    UUID cardId,
    String eventType,
    String eventData,
    LocalDateTime createdAt
) {}

