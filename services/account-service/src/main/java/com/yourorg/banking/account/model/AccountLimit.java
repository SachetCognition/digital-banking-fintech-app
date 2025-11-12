package com.digitalbank.fintech.account.model;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountLimit(
        UUID id,
        UUID accountId,
        String limitType,
        BigDecimal dailyAmount,
        BigDecimal perTxnAmount
) {}
