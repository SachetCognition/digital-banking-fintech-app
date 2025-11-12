package com.yourorg.banking.payments.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RecurringPaymentResponse(
    UUID id,
    UUID customerId,
    UUID fromAccountId,
    String toAccountNumber,
    String toBankCode,
    String toBankName,
    String toAccountName,
    BigDecimal amount,
    String currency,
    String description,
    PaymentFrequency frequency,
    Integer dayOfMonth,
    Integer dayOfWeek,
    LocalDate startDate,
    LocalDate endDate,
    LocalDate nextExecutionDate,
    RecurringPaymentStatus status,
    Integer maxExecutions,
    Integer executionCount,
    LocalDateTime createdAt,
    LocalDateTime lastExecutedAt,
    String cancellationReason
) {
    public static RecurringPaymentResponse from(RecurringPayment payment) {
        return new RecurringPaymentResponse(
            payment.id(),
            payment.customerId(),
            payment.fromAccountId(),
            payment.toAccountNumber(),
            payment.toBankCode(),
            payment.toBankName(),
            payment.toAccountName(),
            payment.amount(),
            payment.currency(),
            payment.description(),
            payment.frequency(),
            payment.dayOfMonth(),
            payment.dayOfWeek(),
            payment.startDate(),
            payment.endDate(),
            payment.nextExecutionDate(),
            payment.status(),
            payment.maxExecutions(),
            payment.executionCount(),
            payment.createdAt(),
            payment.lastExecutedAt(),
            payment.cancellationReason()
        );
    }
}

