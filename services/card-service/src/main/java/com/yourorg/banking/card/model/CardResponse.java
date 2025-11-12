package com.yourorg.banking.card.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CardResponse(
    UUID id,
    UUID customerId,
    UUID accountId,
    String cardNumberMasked,
    String cardHolderName,
    Integer expiryMonth,
    Integer expiryYear,
    CardType cardType,
    CardStatus cardStatus,
    boolean isPrimary,
    BigDecimal spendingLimit,
    BigDecimal dailyLimit,
    BigDecimal monthlyLimit,
    String currency,
    LocalDateTime createdAt,
    LocalDateTime activatedAt,
    LocalDateTime lastUsedAt,
    LocalDate expiryDate
) {
    public static CardResponse from(VirtualCard card) {
        return new CardResponse(
            card.id(),
            card.customerId(),
            card.accountId(),
            card.cardNumberMasked(),
            card.cardHolderName(),
            card.expiryMonth(),
            card.expiryYear(),
            card.cardType(),
            card.cardStatus(),
            card.isPrimary(),
            card.spendingLimit(),
            card.dailyLimit(),
            card.monthlyLimit(),
            card.currency(),
            card.createdAt(),
            card.activatedAt(),
            card.lastUsedAt(),
            card.expiryDate()
        );
    }
}

