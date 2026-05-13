package com.yourorg.banking.account.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountClosureRequest(
    UUID id,
    UUID accountId,
    UUID requestedBy,
    String closureReason,
    UUID transferAccountId,
    String comments,
    AccountClosureStatus status,
    UUID approvedBy,
    Instant approvedAt,
    String rejectionReason,
    BigDecimal finalBalance,
    BigDecimal transferAmount,
    boolean finalStatementGenerated,
    boolean finalStatementSent,
    Instant createdAt,
    Instant updatedAt,
    Instant completedAt
) {}