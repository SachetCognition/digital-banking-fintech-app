package com.yourorg.banking.payments.model;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountInfo(
        UUID accountId,
        UUID customerId,
        String accountNumber,
        BigDecimal balance,
        String currency,
        boolean active,
        BigDecimal dailyTransferLimit,
        BigDecimal perTransactionLimit
) {}

