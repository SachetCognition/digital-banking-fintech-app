package com.yourorg.banking.account.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AccountStatement(
    UUID id,
    UUID accountId,
    String accountNumber,
    String accountName,
    String statementId,
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
    String generatedBy,
    String filePath,
    boolean emailSent,
    Instant emailSentAt,
    Instant createdAt
) {}
