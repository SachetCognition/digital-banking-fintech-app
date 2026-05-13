package com.yourorg.banking.account.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record StatementTransaction(
    UUID transactionId,
    LocalDate transactionDate,
    String description,
    String reference,
    BigDecimal debitAmount,
    BigDecimal creditAmount,
    BigDecimal balance,
    String transactionType,
    String status
) {}
