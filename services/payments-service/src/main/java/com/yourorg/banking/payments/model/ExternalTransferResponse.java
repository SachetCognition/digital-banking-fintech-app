package com.yourorg.banking.payments.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ExternalTransferResponse(
    UUID id,
    UUID customerId,
    UUID fromAccountId,
    String toBankCode,
    String toBankName,
    String toAccountNumber,
    String toAccountName,
    String toRoutingNumber,
    BigDecimal amount,
    String currency,
    String description,
    String referenceNumber,
    ExternalTransferStatus status,
    String externalReference,
    BigDecimal processingFee,
    BigDecimal exchangeRate,
    LocalDateTime createdAt,
    LocalDateTime processedAt,
    String failureReason
) {
    public static ExternalTransferResponse from(ExternalTransfer transfer) {
        return new ExternalTransferResponse(
            transfer.id(),
            transfer.customerId(),
            transfer.fromAccountId(),
            transfer.toBankCode(),
            transfer.toBankName(),
            transfer.toAccountNumber(),
            transfer.toAccountName(),
            transfer.toRoutingNumber(),
            transfer.amount(),
            transfer.currency(),
            transfer.description(),
            transfer.referenceNumber(),
            transfer.status(),
            transfer.externalReference(),
            transfer.processingFee(),
            transfer.exchangeRate(),
            transfer.createdAt(),
            transfer.processedAt(),
            transfer.failureReason()
        );
    }
}

