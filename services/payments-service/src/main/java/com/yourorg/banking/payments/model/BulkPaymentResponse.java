package com.yourorg.banking.payments.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BulkPaymentResponse(
    UUID id,
    UUID customerId,
    String batchName,
    UUID fromAccountId,
    BigDecimal totalAmount,
    String currency,
    Integer totalRecipients,
    BulkPaymentStatus status,
    LocalDateTime createdAt,
    LocalDateTime processedAt,
    LocalDateTime completedAt,
    String failureReason
) {
    public static BulkPaymentResponse from(BulkPayment bulkPayment) {
        return new BulkPaymentResponse(
            bulkPayment.id(),
            bulkPayment.customerId(),
            bulkPayment.batchName(),
            bulkPayment.fromAccountId(),
            bulkPayment.totalAmount(),
            bulkPayment.currency(),
            bulkPayment.totalRecipients(),
            bulkPayment.status(),
            bulkPayment.createdAt(),
            bulkPayment.processedAt(),
            bulkPayment.completedAt(),
            bulkPayment.failureReason()
        );
    }
}

