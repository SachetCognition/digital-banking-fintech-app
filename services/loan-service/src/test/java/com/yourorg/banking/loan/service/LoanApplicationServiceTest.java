package com.yourorg.banking.loan.service;

import com.yourorg.banking.loan.model.*;
import com.yourorg.banking.loan.repository.LoanApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanApplicationServiceTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private CreditScoringService creditScoringService;

    @InjectMocks
    private LoanApplicationService loanApplicationService;

    @Test
    void applyForLoan_highScore_autoApproves() {
        UUID customerId = UUID.randomUUID();
        LoanApplicationRequest request = new LoanApplicationRequest(
            customerId, new BigDecimal("30000"), "PERSONAL", 36, new BigDecimal("80000")
        );

        when(creditScoringService.getScore(customerId)).thenReturn(750);
        when(loanApplicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LoanApplication result = loanApplicationService.applyForLoan(request);

        assertNotNull(result);
        assertEquals(LoanApplicationStatus.APPROVED, result.getStatus());
    }

    @Test
    void applyForLoan_lowScore_autoRejects() {
        UUID customerId = UUID.randomUUID();
        LoanApplicationRequest request = new LoanApplicationRequest(
            customerId, new BigDecimal("50000"), "PERSONAL", 60, new BigDecimal("40000")
        );

        when(creditScoringService.getScore(customerId)).thenReturn(550);
        when(loanApplicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LoanApplication result = loanApplicationService.applyForLoan(request);

        assertNotNull(result);
        assertEquals(LoanApplicationStatus.REJECTED, result.getStatus());
    }

    @Test
    void applyForLoan_midScore_requiresManualReview() {
        UUID customerId = UUID.randomUUID();
        LoanApplicationRequest request = new LoanApplicationRequest(
            customerId, new BigDecimal("75000"), "PERSONAL", 48, new BigDecimal("60000")
        );

        when(creditScoringService.getScore(customerId)).thenReturn(650);
        when(loanApplicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LoanApplication result = loanApplicationService.applyForLoan(request);

        assertNotNull(result);
        assertEquals(LoanApplicationStatus.UNDER_REVIEW, result.getStatus());
    }

    @Test
    void applyForLoan_highAmount_autoRejects() {
        UUID customerId = UUID.randomUUID();
        LoanApplicationRequest request = new LoanApplicationRequest(
            customerId, new BigDecimal("150000"), "PERSONAL", 60, new BigDecimal("100000")
        );

        when(creditScoringService.getScore(customerId)).thenReturn(720);
        when(loanApplicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LoanApplication result = loanApplicationService.applyForLoan(request);

        assertNotNull(result);
        assertEquals(LoanApplicationStatus.REJECTED, result.getStatus());
    }
}
