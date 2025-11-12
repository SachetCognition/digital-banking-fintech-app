package com.yourorg.banking.customer.model;

import java.time.Instant;
import java.util.UUID;

public record Role(
    UUID id,
    String name,
    String description,
    Instant createdAt,
    Instant updatedAt
) {}

