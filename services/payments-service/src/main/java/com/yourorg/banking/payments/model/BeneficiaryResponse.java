package com.yourorg.banking.payments.model;

import java.time.Instant;
import java.util.UUID;

public record BeneficiaryResponse(
        UUID id,
        UUID customerId,
        String name,
        String accountNumber,
        String bankCode,
        String bankName,
        String currency,
        BeneficiaryType type,
        BeneficiaryStatus status,
        String description,
        Instant createdAt,
        Instant updatedAt
) {}

