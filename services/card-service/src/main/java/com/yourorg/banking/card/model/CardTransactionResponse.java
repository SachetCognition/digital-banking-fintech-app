package com.yourorg.banking.card.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CardTransactionResponse(
    UUID id,
    UUID cardId,
    String transactionId,
    BigDecimal amount,
    String currency,
    CardTransactionType transactionType,
    String merchantName,
    String merchantCategoryCode,
    String merchantCountry,
    LocalDateTime transactionDate,
    LocalDateTime settlementDate,
    CardTransactionStatus status,
    String description,
    String referenceNumber,
    String authorizationCode
) {
    public static CardTransactionResponse from(CardTransaction transaction) {
        return new CardTransactionResponse(
            transaction.id(),
            transaction.cardId(),
            transaction.transactionId(),
            transaction.amount(),
            transaction.currency(),
            transaction.transactionType(),
            transaction.merchantName(),
            transaction.merchantCategoryCode(),
            transaction.merchantCountry(),
            transaction.transactionDate(),
            transaction.settlementDate(),
            transaction.status(),
            transaction.description(),
            transaction.referenceNumber(),
            transaction.authorizationCode()
        );
    }
}

