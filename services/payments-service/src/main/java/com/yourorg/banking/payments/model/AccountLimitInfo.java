package com.yourorg.banking.payments.model;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountLimitInfo(
        UUID accountId,
        BigDecimal dailyAmount,
        BigDecimal perTxnAmount,
        String currency
) {}