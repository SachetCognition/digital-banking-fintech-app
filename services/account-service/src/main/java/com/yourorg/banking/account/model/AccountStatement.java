package com.yourorg.banking.account.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AccountStatement(
    UUID accountId,
    String accountNumber,
    String accountName,
    LocalDate statementDate,
    LocalDate periodStart,
    LocalDate periodEnd,
    BigDecimal openingBalance,
    BigDecimal closingBalance,
    BigDecimal totalDebits,
    BigDecimal totalCredits,
    List<StatementTransaction> transactions,
    String currency,
    Instant generatedAt,
    String statementId
) {}

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

