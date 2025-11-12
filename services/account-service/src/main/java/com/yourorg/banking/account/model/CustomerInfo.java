package com.yourorg.banking.account.model;

import java.time.Instant;
import java.util.UUID;

public record CustomerInfo(
    UUID id,
    String userNo,
    String email,
    String phone,
    String fullName,
    String country,
    String status,
    boolean emailVerified,
    boolean phoneVerified,
    boolean mfaEnabled,
    Instant lastLoginAt,
    Instant createdAt
) {}

