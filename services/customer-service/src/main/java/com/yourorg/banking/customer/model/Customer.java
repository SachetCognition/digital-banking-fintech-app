package com.yourorg.banking.customer.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record Customer(
        UUID id,
        String userNo,
        String email,
        String phone,
        String fullName,
        LocalDate dob,
        String country,
        CustomerStatus status,
        String passwordHash,
        boolean emailVerified,
        boolean phoneVerified,
        boolean mfaEnabled,
        Instant lastLoginAt,
        int failedLoginAttempts,
        Instant lockedUntil,
        Instant createdAt,
        Instant updatedAt
) {}
