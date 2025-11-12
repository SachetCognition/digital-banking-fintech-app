package com.yourorg.banking.payments.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BulkPaymentItem(
    UUID id,
    UUID bulkPaymentId,
    String recipientName,
    String recipientAccountNumber,
    String recipientBankCode,
    String recipientBankName,
    BigDecimal amount,
    String description,
    BulkPaymentItemStatus status,
    UUID transferId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime processedAt,
    LocalDateTime failedAt,
    String failureReason
) {
    public BulkPaymentItem withStatus(BulkPaymentItemStatus newStatus) {
        return new BulkPaymentItem(
            id, bulkPaymentId, recipientName, recipientAccountNumber, recipientBankCode,
            recipientBankName, amount, description, newStatus, transferId,
            createdAt, LocalDateTime.now(), processedAt, failedAt, failureReason
        );
    }
    
    public BulkPaymentItem withProcessedAt(LocalDateTime processedAt) {
        return new BulkPaymentItem(
            id, bulkPaymentId, recipientName, recipientAccountNumber, recipientBankCode,
            recipientBankName, amount, description, status, transferId,
            createdAt, LocalDateTime.now(), processedAt, failedAt, failureReason
        );
    }
    
    public BulkPaymentItem withFailed(String reason) {
        return new BulkPaymentItem(
            id, bulkPaymentId, recipientName, recipientAccountNumber, recipientBankCode,
            recipientBankName, amount, description, BulkPaymentItemStatus.FAILED, transferId,
            createdAt, LocalDateTime.now(), processedAt, LocalDateTime.now(), reason
        );
    }
}

