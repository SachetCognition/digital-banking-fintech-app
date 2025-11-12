package com.yourorg.banking.payments.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentLimit(
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
    public PaymentLimit withUpdatedUsage(BigDecimal newUsage) {
        return new PaymentLimit(
            id, customerId, accountId, limitType, limitValue, currency,
            newUsage, usagePeriodStart, isActive, createdAt, LocalDateTime.now(), expiresAt
        );
    }
    
    public PaymentLimit withResetUsage() {
        return new PaymentLimit(
            id, customerId, accountId, limitType, limitValue, currency,
            BigDecimal.ZERO, LocalDateTime.now(), isActive, createdAt, LocalDateTime.now(), expiresAt
        );
    }
    
    public PaymentLimit withActive(boolean active) {
        return new PaymentLimit(
            id, customerId, accountId, limitType, limitValue, currency,
            currentUsage, usagePeriodStart, active, createdAt, LocalDateTime.now(), expiresAt
        );
    }
    
    public boolean isExceeded() {
        return currentUsage.compareTo(limitValue) > 0;
    }
    
    public BigDecimal getRemainingLimit() {
        return limitValue.subtract(currentUsage);
    }
}

