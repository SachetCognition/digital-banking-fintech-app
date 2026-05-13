package com.yourorg.banking.loan.service;

import com.yourorg.banking.loan.model.*;
import com.yourorg.banking.loan.model.dto.LoanApplicationRequest;
import com.yourorg.banking.loan.model.dto.LoanApplicationResponse;
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

    @Mock
    private LoanNumberGeneratorService loanNumberGeneratorService;

    @InjectMocks
    private LoanApplicationService loanApplicationService;

    @Test
    void submitLoanApplication_highScore_autoApproves() {
        UUID customerId = UUID.randomUUID();
        LoanApplicationRequest request = LoanApplicationRequest.builder()
                .loanType(LoanType.PERSONAL)
                .requestedAmount(new BigDecimal("30000"))
                .requestedTermMonths(36)
                .purpose("Personal use")
                .employmentStatus(EmploymentStatus.EMPLOYED)
                .annualIncome(new BigDecimal("80000"))
                .monthlyExpenses(new BigDecimal("3000"))
                .build();

        CreditScore creditScore = CreditScore.builder()
                .score(750)
                .riskLevel(RiskLevel.LOW)
                .build();

        when(loanNumberGeneratorService.generateApplicationNumber()).thenReturn("APP-001");
        when(creditScoringService.calculateCreditScore(customerId, CreditScoreType.INTERNAL)).thenReturn(creditScore);
        when(loanApplicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LoanApplicationResponse result = loanApplicationService.submitLoanApplication(customerId, request);

        assertNotNull(result);
        assertEquals("Approved", result.getStatus());
    }

    @Test
    void submitLoanApplication_lowScore_autoRejects() {
        UUID customerId = UUID.randomUUID();
        LoanApplicationRequest request = LoanApplicationRequest.builder()
                .loanType(LoanType.PERSONAL)
                .requestedAmount(new BigDecimal("50000"))
                .requestedTermMonths(60)
                .purpose("Personal use")
                .employmentStatus(EmploymentStatus.EMPLOYED)
                .annualIncome(new BigDecimal("40000"))
                .monthlyExpenses(new BigDecimal("2000"))
                .build();

        CreditScore creditScore = CreditScore.builder()
                .score(550)
                .riskLevel(RiskLevel.CRITICAL)
                .build();

        when(loanNumberGeneratorService.generateApplicationNumber()).thenReturn("APP-002");
        when(creditScoringService.calculateCreditScore(customerId, CreditScoreType.INTERNAL)).thenReturn(creditScore);
        when(loanApplicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LoanApplicationResponse result = loanApplicationService.submitLoanApplication(customerId, request);

        assertNotNull(result);
        assertEquals("Rejected", result.getStatus());
    }

    @Test
    void submitLoanApplication_midScore_requiresManualReview() {
        UUID customerId = UUID.randomUUID();
        LoanApplicationRequest request = LoanApplicationRequest.builder()
                .loanType(LoanType.PERSONAL)
                .requestedAmount(new BigDecimal("40000"))
                .requestedTermMonths(48)
                .purpose("Personal use")
                .employmentStatus(EmploymentStatus.EMPLOYED)
                .annualIncome(new BigDecimal("90000"))
                .monthlyExpenses(new BigDecimal("2500"))
                .build();

        CreditScore creditScore = CreditScore.builder()
                .score(650)
                .riskLevel(RiskLevel.MEDIUM)
                .build();

        when(loanNumberGeneratorService.generateApplicationNumber()).thenReturn("APP-003");
        when(creditScoringService.calculateCreditScore(customerId, CreditScoreType.INTERNAL)).thenReturn(creditScore);
        when(loanApplicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LoanApplicationResponse result = loanApplicationService.submitLoanApplication(customerId, request);

        assertNotNull(result);
        assertEquals("Under Review", result.getStatus());
    }
}
