package com.yourorg.banking.account.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AccountOpeningCase(
    UUID id,
    UUID accountId,
    UUID customerId,
    AccountOpeningCaseType caseType,
    AccountOpeningStatus status,
    AccountOpeningPriority priority,
    UUID assignedTo,
    UUID kycCaseId,
    List<String> requiredDocuments,
    List<String> submittedDocuments,
    String reviewNotes,
    String approvalNotes,
    String rejectionReason,
    Instant createdAt,
    Instant updatedAt,
    Instant completedAt
) {
    public enum AccountOpeningCaseType {
        NEW_ACCOUNT,
        JOINT_ACCOUNT,
        ACCOUNT_UPGRADE,
        ACCOUNT_REOPEN
    }

    public enum AccountOpeningPriority {
        LOW,
        NORMAL,
        HIGH,
        URGENT
    }
}

