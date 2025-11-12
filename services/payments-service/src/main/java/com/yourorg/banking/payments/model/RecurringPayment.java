package com.yourorg.banking.payments.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RecurringPayment(
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
    LocalDateTime updatedAt,
    LocalDateTime lastExecutedAt,
    LocalDateTime cancelledAt,
    String cancellationReason
) {
    public RecurringPayment withStatus(RecurringPaymentStatus newStatus) {
        return new RecurringPayment(
            id, customerId, fromAccountId, toAccountNumber, toBankCode, toBankName,
            toAccountName, amount, currency, description, frequency, dayOfMonth, dayOfWeek,
            startDate, endDate, nextExecutionDate, newStatus, maxExecutions, executionCount,
            createdAt, LocalDateTime.now(), lastExecutedAt, cancelledAt, cancellationReason
        );
    }
    
    public RecurringPayment withNextExecutionDate(LocalDate nextDate) {
        return new RecurringPayment(
            id, customerId, fromAccountId, toAccountNumber, toBankCode, toBankName,
            toAccountName, amount, currency, description, frequency, dayOfMonth, dayOfWeek,
            startDate, endDate, nextDate, status, maxExecutions, executionCount,
            createdAt, LocalDateTime.now(), lastExecutedAt, cancelledAt, cancellationReason
        );
    }
    
    public RecurringPayment withExecuted() {
        return new RecurringPayment(
            id, customerId, fromAccountId, toAccountNumber, toBankCode, toBankName,
            toAccountName, amount, currency, description, frequency, dayOfMonth, dayOfWeek,
            startDate, endDate, nextExecutionDate, status, maxExecutions, executionCount + 1,
            createdAt, LocalDateTime.now(), LocalDateTime.now(), cancelledAt, cancellationReason
        );
    }
    
    public RecurringPayment withCancelled(String reason) {
        return new RecurringPayment(
            id, customerId, fromAccountId, toAccountNumber, toBankCode, toBankName,
            toAccountName, amount, currency, description, frequency, dayOfMonth, dayOfWeek,
            startDate, endDate, nextExecutionDate, RecurringPaymentStatus.CANCELLED,
            maxExecutions, executionCount, createdAt, LocalDateTime.now(),
            lastExecutedAt, LocalDateTime.now(), reason
        );
    }
}

