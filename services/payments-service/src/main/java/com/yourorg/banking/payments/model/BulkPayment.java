package com.yourorg.banking.payments.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BulkPayment(
    UUID id,
    UUID customerId,
    String batchName,
    UUID fromAccountId,
    BigDecimal totalAmount,
    String currency,
    Integer totalRecipients,
    BulkPaymentStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime processedAt,
    LocalDateTime completedAt,
    LocalDateTime failedAt,
    String failureReason
) {
    public BulkPayment withStatus(BulkPaymentStatus newStatus) {
        return new BulkPayment(
            id, customerId, batchName, fromAccountId, totalAmount, currency,
            totalRecipients, newStatus, createdAt, LocalDateTime.now(),
            processedAt, completedAt, failedAt, failureReason
        );
    }
    
    public BulkPayment withProcessedAt(LocalDateTime processedAt) {
        return new BulkPayment(
            id, customerId, batchName, fromAccountId, totalAmount, currency,
            totalRecipients, status, createdAt, LocalDateTime.now(),
            processedAt, completedAt, failedAt, failureReason
        );
    }
    
    public BulkPayment withCompletedAt(LocalDateTime completedAt) {
        return new BulkPayment(
            id, customerId, batchName, fromAccountId, totalAmount, currency,
            totalRecipients, status, createdAt, LocalDateTime.now(),
            processedAt, completedAt, failedAt, failureReason
        );
    }
    
    public BulkPayment withFailed(String reason) {
        return new BulkPayment(
            id, customerId, batchName, fromAccountId, totalAmount, currency,
            totalRecipients, BulkPaymentStatus.FAILED, createdAt, LocalDateTime.now(),
            processedAt, completedAt, LocalDateTime.now(), reason
        );
    }
}

