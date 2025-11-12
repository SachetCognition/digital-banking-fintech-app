package com.yourorg.banking.card.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record VirtualCard(
    UUID id,
    UUID customerId,
    UUID accountId,
    String cardNumberEncrypted,
    String cardNumberMasked,
    String cardHolderName,
    Integer expiryMonth,
    Integer expiryYear,
    String cvvEncrypted,
    CardType cardType,
    CardStatus cardStatus,
    boolean isPrimary,
    BigDecimal spendingLimit,
    BigDecimal dailyLimit,
    BigDecimal monthlyLimit,
    String currency,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime activatedAt,
    LocalDateTime blockedAt,
    LocalDateTime cancelledAt,
    LocalDate expiryDate,
    LocalDateTime lastUsedAt
) {
    public VirtualCard withStatus(CardStatus newStatus) {
        return new VirtualCard(
            id, customerId, accountId, cardNumberEncrypted, cardNumberMasked,
            cardHolderName, expiryMonth, expiryYear, cvvEncrypted, cardType,
            newStatus, isPrimary, spendingLimit, dailyLimit, monthlyLimit,
            currency, createdAt, LocalDateTime.now(), activatedAt, blockedAt,
            cancelledAt, expiryDate, lastUsedAt
        );
    }
    
    public VirtualCard withActivated() {
        return new VirtualCard(
            id, customerId, accountId, cardNumberEncrypted, cardNumberMasked,
            cardHolderName, expiryMonth, expiryYear, cvvEncrypted, cardType,
            CardStatus.ACTIVE, isPrimary, spendingLimit, dailyLimit, monthlyLimit,
            currency, createdAt, LocalDateTime.now(), LocalDateTime.now(),
            blockedAt, cancelledAt, expiryDate, lastUsedAt
        );
    }
    
    public VirtualCard withBlocked() {
        return new VirtualCard(
            id, customerId, accountId, cardNumberEncrypted, cardNumberMasked,
            cardHolderName, expiryMonth, expiryYear, cvvEncrypted, cardType,
            CardStatus.BLOCKED, isPrimary, spendingLimit, dailyLimit, monthlyLimit,
            currency, createdAt, LocalDateTime.now(), activatedAt,
            LocalDateTime.now(), cancelledAt, expiryDate, lastUsedAt
        );
    }
    
    public VirtualCard withCancelled() {
        return new VirtualCard(
            id, customerId, accountId, cardNumberEncrypted, cardNumberMasked,
            cardHolderName, expiryMonth, expiryYear, cvvEncrypted, cardType,
            CardStatus.CANCELLED, isPrimary, spendingLimit, dailyLimit, monthlyLimit,
            currency, createdAt, LocalDateTime.now(), activatedAt, blockedAt,
            LocalDateTime.now(), expiryDate, lastUsedAt
        );
    }
    
    public VirtualCard withLimits(BigDecimal spendingLimit, BigDecimal dailyLimit, BigDecimal monthlyLimit) {
        return new VirtualCard(
            id, customerId, accountId, cardNumberEncrypted, cardNumberMasked,
            cardHolderName, expiryMonth, expiryYear, cvvEncrypted, cardType,
            cardStatus, isPrimary, spendingLimit, dailyLimit, monthlyLimit,
            currency, createdAt, LocalDateTime.now(), activatedAt, blockedAt,
            cancelledAt, expiryDate, lastUsedAt
        );
    }
    
    public VirtualCard withLastUsed() {
        return new VirtualCard(
            id, customerId, accountId, cardNumberEncrypted, cardNumberMasked,
            cardHolderName, expiryMonth, expiryYear, cvvEncrypted, cardType,
            cardStatus, isPrimary, spendingLimit, dailyLimit, monthlyLimit,
            currency, createdAt, LocalDateTime.now(), activatedAt, blockedAt,
            cancelledAt, expiryDate, LocalDateTime.now()
        );
    }
    
    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }
    
    public boolean isActive() {
        return cardStatus == CardStatus.ACTIVE && !isExpired();
    }
}

