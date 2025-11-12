package com.yourorg.banking.payments.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record InternationalTransfer(
    UUID id,
    UUID customerId,
    UUID fromAccountId,
    InternationalTransferType transferType,
    String toSwiftCode,
    String toIban,
    String toBankName,
    String toBankAddress,
    String toAccountNumber,
    String toAccountName,
    String toAddress,
    String toCountryCode,
    BigDecimal amount,
    String currency,
    String description,
    String referenceNumber,
    InternationalTransferStatus status,
    String swiftMessageId,
    String correspondentBankSwift,
    String correspondentBankName,
    BigDecimal processingFee,
    BigDecimal exchangeRate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime processedAt,
    LocalDateTime failedAt,
    String failureReason
) {
    public InternationalTransfer withStatus(InternationalTransferStatus newStatus) {
        return new InternationalTransfer(
            id, customerId, fromAccountId, transferType, toSwiftCode, toIban,
            toBankName, toBankAddress, toAccountNumber, toAccountName, toAddress,
            toCountryCode, amount, currency, description, referenceNumber, newStatus,
            swiftMessageId, correspondentBankSwift, correspondentBankName, processingFee,
            exchangeRate, createdAt, LocalDateTime.now(), processedAt, failedAt, failureReason
        );
    }
    
    public InternationalTransfer withProcessedAt(LocalDateTime processedAt) {
        return new InternationalTransfer(
            id, customerId, fromAccountId, transferType, toSwiftCode, toIban,
            toBankName, toBankAddress, toAccountNumber, toAccountName, toAddress,
            toCountryCode, amount, currency, description, referenceNumber, status,
            swiftMessageId, correspondentBankSwift, correspondentBankName, processingFee,
            exchangeRate, createdAt, LocalDateTime.now(), processedAt, failedAt, failureReason
        );
    }
    
    public InternationalTransfer withFailed(String reason) {
        return new InternationalTransfer(
            id, customerId, fromAccountId, transferType, toSwiftCode, toIban,
            toBankName, toBankAddress, toAccountNumber, toAccountName, toAddress,
            toCountryCode, amount, currency, description, referenceNumber,
            InternationalTransferStatus.FAILED, swiftMessageId, correspondentBankSwift,
            correspondentBankName, processingFee, exchangeRate, createdAt,
            LocalDateTime.now(), processedAt, LocalDateTime.now(), reason
        );
    }
}

