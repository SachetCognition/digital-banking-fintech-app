package com.yourorg.banking.payments.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentLimitResponse(
    UUID id,
    UUID customerId,
    UUID accountId,
    PaymentLimitType limitType,
    BigDecimal limitValue,
    String currency,
    BigDecimal currentUsage,
    LocalDateTime usagePeriodStart,
    boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime expiresAt
) {
    public static PaymentLimitResponse from(PaymentLimit limit) {
        return new PaymentLimitResponse(
            limit.id(),
            limit.customerId(),
            limit.accountId(),
            limit.limitType(),
            limit.limitValue(),
            limit.currency(),
            limit.currentUsage(),
            limit.usagePeriodStart(),
            limit.isActive(),
            limit.createdAt(),
            limit.updatedAt(),
            limit.expiresAt()
        );
    }
}

