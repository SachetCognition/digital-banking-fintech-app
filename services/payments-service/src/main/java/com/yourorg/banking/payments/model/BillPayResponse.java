package com.yourorg.banking.payments.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BillPayResponse(
        UUID billPayId,
        UUID payerAccountId,
        UUID beneficiaryId,
        String beneficiaryName,
        String beneficiaryAccountNumber,
        BigDecimal amount,
        String currency,
        String status,
        String description,
        Instant createdAt,
        Instant completedAt,
        String idempotencyKey
) {}

