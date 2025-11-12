package com.yourorg.banking.payments.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ExternalTransfer(
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
    LocalDateTime updatedAt,
    LocalDateTime processedAt,
    LocalDateTime failedAt,
    String failureReason
) {
    public ExternalTransfer withStatus(ExternalTransferStatus newStatus) {
        return new ExternalTransfer(
            id, customerId, fromAccountId, toBankCode, toBankName, toAccountNumber,
            toAccountName, toRoutingNumber, amount, currency, description, referenceNumber,
            newStatus, externalReference, processingFee, exchangeRate, createdAt,
            LocalDateTime.now(), processedAt, failedAt, failureReason
        );
    }
    
    public ExternalTransfer withProcessedAt(LocalDateTime processedAt) {
        return new ExternalTransfer(
            id, customerId, fromAccountId, toBankCode, toBankName, toAccountNumber,
            toAccountName, toRoutingNumber, amount, currency, description, referenceNumber,
            status, externalReference, processingFee, exchangeRate, createdAt,
            LocalDateTime.now(), processedAt, failedAt, failureReason
        );
    }
    
    public ExternalTransfer withFailed(String reason) {
        return new ExternalTransfer(
            id, customerId, fromAccountId, toBankCode, toBankName, toAccountNumber,
            toAccountName, toRoutingNumber, amount, currency, description, referenceNumber,
            ExternalTransferStatus.FAILED, externalReference, processingFee, exchangeRate,
            createdAt, LocalDateTime.now(), processedAt, LocalDateTime.now(), reason
        );
    }
}

