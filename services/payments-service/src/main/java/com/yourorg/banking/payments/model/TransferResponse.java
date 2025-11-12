package com.yourorg.banking.payments.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
        UUID transferId,
        UUID payerAccountId,
        UUID payeeAccountId,
        BigDecimal amount,
        String currency,
        String status,
        String description,
        Instant createdAt,
        Instant completedAt,
        String idempotencyKey
) {}

