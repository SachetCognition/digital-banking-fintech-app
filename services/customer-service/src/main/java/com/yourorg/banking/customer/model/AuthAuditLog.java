package com.yourorg.banking.customer.model;

import com.fasterxml.jackson.annotation.JsonRawValue;
import java.net.InetAddress;
import java.time.Instant;
import java.util.UUID;

public record AuthAuditLog(
    UUID id,
    UUID customerId,
    String eventType,
    @JsonRawValue String eventData,
    InetAddress ipAddress,
    String userAgent,
    boolean success,
    Instant createdAt
) {}

