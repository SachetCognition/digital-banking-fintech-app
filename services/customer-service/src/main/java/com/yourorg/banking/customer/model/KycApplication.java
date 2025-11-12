package com.yourorg.banking.customer.model;

import java.time.Instant;
import java.util.UUID;

public record KycApplication(
        UUID id,
        UUID customerId,
        String status,
        String providerRef,
        String rejectionReason,
        Instant submittedAt,
        Instant updatedAt
) {}
