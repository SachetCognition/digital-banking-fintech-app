package com.yourorg.banking.customer.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MfaSecret(
    UUID id,
    UUID customerId,
    String secretKey,
    List<String> backupCodes,
    Instant createdAt,
    Instant updatedAt
) {}

