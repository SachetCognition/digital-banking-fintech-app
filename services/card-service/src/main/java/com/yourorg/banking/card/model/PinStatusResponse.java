package com.yourorg.banking.card.model;

import java.time.LocalDateTime;

public record PinStatusResponse(
    boolean isLocked,
    LocalDateTime lockedUntil,
    Integer pinAttempts,
    LocalDateTime lastChangedAt
) {}

