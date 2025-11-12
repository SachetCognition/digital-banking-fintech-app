package com.yourorg.banking.ledger.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BalanceResponse(
        UUID accountId,
        String currency,
        BigDecimal balance,
        Instant lastUpdatedAt
) {}

