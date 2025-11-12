package com.yourorg.banking.customer.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AuthResponse(
    UUID customerId,
    String email,
    String fullName,
    String accessToken,
    String refreshToken,
    String sessionToken,
    Instant expiresAt,
    boolean mfaRequired,
    List<String> roles,
    List<String> permissions
) {}

