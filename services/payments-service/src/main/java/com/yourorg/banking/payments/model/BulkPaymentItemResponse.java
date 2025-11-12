package com.yourorg.banking.payments.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BulkPaymentItemResponse(
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
    LocalDateTime processedAt,
    String failureReason
) {
    public static BulkPaymentItemResponse from(BulkPaymentItem item) {
        return new BulkPaymentItemResponse(
            item.id(),
            item.bulkPaymentId(),
            item.recipientName(),
            item.recipientAccountNumber(),
            item.recipientBankCode(),
            item.recipientBankName(),
            item.amount(),
            item.description(),
            item.status(),
            item.transferId(),
            item.createdAt(),
            item.processedAt(),
            item.failureReason()
        );
    }
}

