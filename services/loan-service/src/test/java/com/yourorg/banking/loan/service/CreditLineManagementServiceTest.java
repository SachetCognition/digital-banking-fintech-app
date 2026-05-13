package com.yourorg.banking.loan.service;

import com.yourorg.banking.loan.model.*;
import com.yourorg.banking.loan.model.dto.CreditLineRequest;
import com.yourorg.banking.loan.model.dto.CreditLineResponse;
import com.yourorg.banking.loan.repository.CreditLineRepository;
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
class CreditLineManagementServiceTest {

    @Mock
    private CreditLineRepository creditLineRepository;

    @Mock
    private CreditScoringService creditScoringService;

    @Mock
    private LoanNumberGeneratorService loanNumberGeneratorService;

    @InjectMocks
    private CreditLineManagementService creditLineManagementService;

    @Test
    void createCreditLine_highScore_getsHigherLimit() {
        UUID customerId = UUID.randomUUID();
        CreditLineRequest request = CreditLineRequest.builder()
                .requestedCreditLimit(new BigDecimal("50000"))
                .purpose("General")
                .autoIncrease(true)
                .build();

        CreditScore creditScore = CreditScore.builder()
                .score(780)
                .riskLevel(RiskLevel.LOW)
                .build();

        when(creditScoringService.calculateCreditScore(customerId, CreditScoreType.INTERNAL)).thenReturn(creditScore);
        when(loanNumberGeneratorService.generateCreditLineNumber()).thenReturn("CL-001");
        when(creditLineRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreditLineResponse result = creditLineManagementService.createCreditLine(customerId, request);

        assertNotNull(result);
    }

    @Test
    void createCreditLine_lowScore_getsLowerLimit() {
        UUID customerId = UUID.randomUUID();
        CreditLineRequest request = CreditLineRequest.builder()
                .requestedCreditLimit(new BigDecimal("10000"))
                .purpose("Emergency")
                .autoIncrease(false)
                .build();

        CreditScore creditScore = CreditScore.builder()
                .score(620)
                .riskLevel(RiskLevel.HIGH)
                .build();

        when(creditScoringService.calculateCreditScore(customerId, CreditScoreType.INTERNAL)).thenReturn(creditScore);
        when(loanNumberGeneratorService.generateCreditLineNumber()).thenReturn("CL-002");
        when(creditLineRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreditLineResponse result = creditLineManagementService.createCreditLine(customerId, request);

        assertNotNull(result);
    }
}
