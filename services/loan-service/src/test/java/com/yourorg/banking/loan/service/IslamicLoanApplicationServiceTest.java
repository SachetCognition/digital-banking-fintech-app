package com.yourorg.banking.loan.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class IslamicLoanApplicationServiceTest {

    @InjectMocks
    private IslamicLoanApplicationService service;

    @Test
    void tcIF007_ssbApprovalWorkflow_endToEnd() {
        // TC-IF-007: SSB approval workflow end-to-end
        UUID loanApplicationId = UUID.randomUUID();
        String submitter = "loan-officer-1";
        String reviewer = "ssb-reviewer-1";

        // Step 1: Submit for SSB approval
        var application = service.submitForSsbApproval(loanApplicationId, submitter);
        assertNotNull(application);
        assertNotNull(application.applicationId());
        assertEquals(loanApplicationId, application.loanApplicationId());
        assertEquals(submitter, application.submitter());
        assertEquals(IslamicLoanApplicationService.SsbApplicationStatus.PENDING_SSB_APPROVAL, application.status());
        assertEquals(1, application.auditTrail().size());
        assertEquals("SUBMIT", application.auditTrail().get(0).action());

        // Step 2: Review
        var reviewed = service.reviewApplication(application.applicationId(), reviewer);
        assertEquals(IslamicLoanApplicationService.SsbApplicationStatus.PENDING_SSB_APPROVAL, reviewed.status());
        assertEquals(2, reviewed.auditTrail().size());
        assertEquals("REVIEW", reviewed.auditTrail().get(1).action());
        assertEquals(reviewer, reviewed.auditTrail().get(1).actor());

        // Step 3: Approve
        var approved = service.approveApplication(application.applicationId(), reviewer);
        assertEquals(IslamicLoanApplicationService.SsbApplicationStatus.APPROVED, approved.status());
        assertEquals(3, approved.auditTrail().size());
        assertEquals("APPROVE", approved.auditTrail().get(2).action());
        assertEquals("APPROVED", approved.auditTrail().get(2).decision());

        // Verify retrieval
        var retrieved = service.getApplication(application.applicationId());
        assertNotNull(retrieved);
        assertEquals(IslamicLoanApplicationService.SsbApplicationStatus.APPROVED, retrieved.status());
    }

    @Test
    void ssbRejectionWorkflow() {
        UUID loanApplicationId = UUID.randomUUID();
        String submitter = "loan-officer-2";
        String reviewer = "ssb-reviewer-2";

        var application = service.submitForSsbApproval(loanApplicationId, submitter);
        var rejected = service.rejectApplication(application.applicationId(), reviewer);

        assertEquals(IslamicLoanApplicationService.SsbApplicationStatus.REJECTED, rejected.status());
        assertEquals(2, rejected.auditTrail().size());
        assertEquals("REJECT", rejected.auditTrail().get(1).action());
        assertEquals("REJECTED", rejected.auditTrail().get(1).decision());
    }
}
