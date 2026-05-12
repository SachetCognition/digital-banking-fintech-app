package com.yourorg.banking.loan.service;

import com.yourorg.banking.loan.model.*;
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

    @InjectMocks
    private CreditLineManagementService creditLineManagementService;

    @Test
    void createCreditLine_highScore_getsHigherLimit() {
        UUID customerId = UUID.randomUUID();
        when(creditScoringService.getScore(customerId)).thenReturn(780);
        when(creditLineRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreditLine creditLine = creditLineManagementService.createCreditLine(customerId);

        assertNotNull(creditLine);
        assertTrue(creditLine.getCreditLimit().compareTo(new BigDecimal("20000")) >= 0);
    }

    @Test
    void createCreditLine_lowScore_getsLowerLimit() {
        UUID customerId = UUID.randomUUID();
        when(creditScoringService.getScore(customerId)).thenReturn(620);
        when(creditLineRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreditLine creditLine = creditLineManagementService.createCreditLine(customerId);

        assertNotNull(creditLine);
        assertTrue(creditLine.getCreditLimit().compareTo(new BigDecimal("5000")) <= 0);
    }
}
