package com.yourorg.banking.payments.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentTemplate(
    UUID id,
    UUID customerId,
    String templateName,
    UUID fromAccountId,
    String toAccountNumber,
    String toBankCode,
    String toBankName,
    String toAccountName,
    BigDecimal amount,
    String currency,
    String description,
    boolean isFavorite,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public PaymentTemplate withFavorite(boolean favorite) {
        return new PaymentTemplate(
            id, customerId, templateName, fromAccountId, toAccountNumber, toBankCode,
            toBankName, toAccountName, amount, currency, description, favorite,
            createdAt, LocalDateTime.now()
        );
    }
    
    public PaymentTemplate withUpdated() {
        return new PaymentTemplate(
            id, customerId, templateName, fromAccountId, toAccountNumber, toBankCode,
            toBankName, toAccountName, amount, currency, description, isFavorite,
            createdAt, LocalDateTime.now()
        );
    }
}

