package com.yourorg.banking.account.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        UUID customerId,
        String accountNumber,
        String accountName,
        AccountType type,
        AccountStatus status,
        String currency,
        BigDecimal balance,
        BigDecimal availableBalance,
        BigDecimal dailyTransferLimit,
        BigDecimal perTransactionLimit,
        Instant lastTransactionAt,
        Instant createdAt,
        Instant updatedAt
) {}

