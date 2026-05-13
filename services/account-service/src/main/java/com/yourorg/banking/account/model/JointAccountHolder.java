package com.yourorg.banking.account.model;

import java.time.Instant;
import java.util.UUID;

public record JointAccountHolder(
    UUID id,
    UUID accountId,
    UUID customerId,
    JointAccountRole role,
    boolean isPrimary,
    Instant addedAt,
    Instant approvedAt,
    UUID approvedBy,
    String status,
    Instant createdAt,
    Instant updatedAt
) {
    public enum JointAccountRole {
        PRIMARY_OWNER,
        JOINT_OWNER,
        AUTHORIZED_USER,
        BENEFICIARY
    }
}
