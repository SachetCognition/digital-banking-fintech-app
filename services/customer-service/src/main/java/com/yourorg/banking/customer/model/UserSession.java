package com.yourorg.banking.customer.model;

import java.net.InetAddress;
import java.time.Instant;
import java.util.UUID;

public record UserSession(
    UUID id,
    UUID customerId,
    String sessionToken,
    String deviceFingerprint,
    InetAddress ipAddress,
    String userAgent,
    Instant expiresAt,
    Instant lastActivityAt,
    Instant createdAt
) {
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}

