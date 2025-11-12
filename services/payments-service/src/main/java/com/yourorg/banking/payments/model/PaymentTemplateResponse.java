package com.yourorg.banking.payments.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentTemplateResponse(
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
    public static PaymentTemplateResponse from(PaymentTemplate template) {
        return new PaymentTemplateResponse(
            template.id(),
            template.customerId(),
            template.templateName(),
            template.fromAccountId(),
            template.toAccountNumber(),
            template.toBankCode(),
            template.toBankName(),
            template.toAccountName(),
            template.amount(),
            template.currency(),
            template.description(),
            template.isFavorite(),
            template.createdAt(),
            template.updatedAt()
        );
    }
}

