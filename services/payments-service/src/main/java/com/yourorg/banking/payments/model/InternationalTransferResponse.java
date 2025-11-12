package com.yourorg.banking.payments.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record InternationalTransferResponse(
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
    LocalDateTime processedAt,
    String failureReason
) {
    public static InternationalTransferResponse from(InternationalTransfer transfer) {
        return new InternationalTransferResponse(
            transfer.id(),
            transfer.customerId(),
            transfer.fromAccountId(),
            transfer.transferType(),
            transfer.toSwiftCode(),
            transfer.toIban(),
            transfer.toBankName(),
            transfer.toBankAddress(),
            transfer.toAccountNumber(),
            transfer.toAccountName(),
            transfer.toAddress(),
            transfer.toCountryCode(),
            transfer.amount(),
            transfer.currency(),
            transfer.description(),
            transfer.referenceNumber(),
            transfer.status(),
            transfer.swiftMessageId(),
            transfer.correspondentBankSwift(),
            transfer.correspondentBankName(),
            transfer.processingFee(),
            transfer.exchangeRate(),
            transfer.createdAt(),
            transfer.processedAt(),
            transfer.failureReason()
        );
    }
}

