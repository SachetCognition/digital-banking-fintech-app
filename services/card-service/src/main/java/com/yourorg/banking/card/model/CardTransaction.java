package com.yourorg.banking.card.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CardTransaction(
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
    String authorizationCode,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public CardTransaction withStatus(CardTransactionStatus newStatus) {
        return new CardTransaction(
            id, cardId, transactionId, amount, currency, transactionType,
            merchantName, merchantCategoryCode, merchantCountry, transactionDate,
            settlementDate, newStatus, description, referenceNumber,
            authorizationCode, createdAt, LocalDateTime.now()
        );
    }
    
    public CardTransaction withSettled(LocalDateTime settlementDate) {
        return new CardTransaction(
            id, cardId, transactionId, amount, currency, transactionType,
            merchantName, merchantCategoryCode, merchantCountry, transactionDate,
            settlementDate, CardTransactionStatus.APPROVED, description, referenceNumber,
            authorizationCode, createdAt, LocalDateTime.now()
        );
    }
}

