package com.yourorg.banking.account.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AccountOpeningResponse(
    UUID accountId,
    String accountNumber,
    String accountName,
    AccountType accountType,
    String currency,
    BigDecimal initialDeposit,
    AccountStatus status,
    String openingStatus,
    String message,
    List<String> nextSteps,
    List<String> requiredDocuments,
    Instant createdAt,
    Instant estimatedCompletionAt
) {}

