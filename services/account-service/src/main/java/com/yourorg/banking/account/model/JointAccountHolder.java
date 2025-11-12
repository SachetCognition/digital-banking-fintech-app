package com.yourorg.banking.account.model;

import java.time.Instant;
import java.util.UUID;

public record JointAccountHolder(
    UUID customerId,
    String fullName,
    String email,
    JointAccountRole role,
    boolean isPrimary,
    Instant addedAt,
    Instant approvedAt,
    String status
) {
    public enum JointAccountRole {
        PRIMARY_OWNER,
        JOINT_OWNER,
        AUTHORIZED_USER,
        BENEFICIARY
    }
}

