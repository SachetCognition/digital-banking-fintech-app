package com.yourorg.banking.customer.model;

import java.time.Instant;
import java.util.UUID;

public record Permission(
    UUID id,
    String name,
    String resource,
    String action,
    Instant createdAt
) {}

