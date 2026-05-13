package com.yourorg.banking.compliance.model;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionEvent(
    UUID transactionId,
    UUID customerId,
    BigDecimal amount,
    String currency,
    String transactionType,
    String country
) {
}
