package com.yourorg.banking.loan.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IslamicLoanApplicationService {

    public enum SsbApplicationStatus {
        PENDING_SSB_APPROVAL,
        APPROVED,
        REJECTED
    }

    public record SsbApplication(
            UUID applicationId,
            UUID loanApplicationId,
            String submitter,
            SsbApplicationStatus status,
            List<AuditEntry> auditTrail
    ) {}

    public record AuditEntry(
            String actor,
            String action,
            String decision,
            LocalDateTime timestamp
    ) {}

    private final Map<UUID, SsbApplication> applications = new ConcurrentHashMap<>();

    public SsbApplication submitForSsbApproval(UUID loanApplicationId, String submitter) {
        UUID applicationId = UUID.randomUUID();
        List<AuditEntry> auditTrail = new ArrayList<>();
        auditTrail.add(new AuditEntry(submitter, "SUBMIT", null, LocalDateTime.now()));

        SsbApplication application = new SsbApplication(
                applicationId,
                loanApplicationId,
                submitter,
                SsbApplicationStatus.PENDING_SSB_APPROVAL,
                auditTrail
        );
        applications.put(applicationId, application);
        return application;
    }

    public SsbApplication reviewApplication(UUID applicationId, String reviewer) {
        SsbApplication existing = applications.get(applicationId);
        if (existing == null) {
            throw new IllegalArgumentException("SSB application not found: " + applicationId);
        }
        if (existing.status() != SsbApplicationStatus.PENDING_SSB_APPROVAL) {
            throw new IllegalStateException("Application is not pending SSB approval");
        }

        List<AuditEntry> updatedAudit = new ArrayList<>(existing.auditTrail());
        updatedAudit.add(new AuditEntry(reviewer, "REVIEW", null, LocalDateTime.now()));

        SsbApplication updated = new SsbApplication(
                existing.applicationId(),
                existing.loanApplicationId(),
                existing.submitter(),
                existing.status(),
                updatedAudit
        );
        applications.put(applicationId, updated);
        return updated;
    }

    public SsbApplication approveApplication(UUID applicationId, String reviewer) {
        SsbApplication existing = applications.get(applicationId);
        if (existing == null) {
            throw new IllegalArgumentException("SSB application not found: " + applicationId);
        }
        if (existing.status() != SsbApplicationStatus.PENDING_SSB_APPROVAL) {
            throw new IllegalStateException("Application is not pending SSB approval");
        }

        List<AuditEntry> updatedAudit = new ArrayList<>(existing.auditTrail());
        updatedAudit.add(new AuditEntry(reviewer, "APPROVE", "APPROVED", LocalDateTime.now()));

        SsbApplication updated = new SsbApplication(
                existing.applicationId(),
                existing.loanApplicationId(),
                existing.submitter(),
                SsbApplicationStatus.APPROVED,
                updatedAudit
        );
        applications.put(applicationId, updated);
        return updated;
    }

    public SsbApplication rejectApplication(UUID applicationId, String reviewer) {
        SsbApplication existing = applications.get(applicationId);
        if (existing == null) {
            throw new IllegalArgumentException("SSB application not found: " + applicationId);
        }
        if (existing.status() != SsbApplicationStatus.PENDING_SSB_APPROVAL) {
            throw new IllegalStateException("Application is not pending SSB approval");
        }

        List<AuditEntry> updatedAudit = new ArrayList<>(existing.auditTrail());
        updatedAudit.add(new AuditEntry(reviewer, "REJECT", "REJECTED", LocalDateTime.now()));

        SsbApplication updated = new SsbApplication(
                existing.applicationId(),
                existing.loanApplicationId(),
                existing.submitter(),
                SsbApplicationStatus.REJECTED,
                updatedAudit
        );
        applications.put(applicationId, updated);
        return updated;
    }

    public SsbApplication getApplication(UUID applicationId) {
        return applications.get(applicationId);
    }
}
