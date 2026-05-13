package com.yourorg.banking.account.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Wallet(
    UUID id,
    UUID customerId,
    Currency currency,
    BigDecimal balance,
    Instant createdAt,
    Instant updatedAt
) {}
