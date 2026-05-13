package com.yourorg.banking.compliance.service;

import com.yourorg.banking.compliance.model.*;
import com.yourorg.banking.compliance.repository.FraudAlertRepository;
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
class FraudDetectionServiceTest {

    @Mock
    private FraudAlertRepository fraudAlertRepository;

    @InjectMocks
    private FraudDetectionService fraudDetectionService;

    @Test
    void analyzeTransaction_highRiskScore_triggersFraudAlert() {
        TransactionEvent event = new TransactionEvent(
            UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("50000.00"),
            "USD", "INTERNATIONAL_WIRE", "NG"
        );

        when(fraudAlertRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FraudAnalysisResult result = fraudDetectionService.analyzeTransaction(event);

        assertNotNull(result);
        assertTrue(result.getFraudScore() > 70);
        assertTrue(result.isFraudDetected());
        verify(fraudAlertRepository).save(any());
    }

    @Test
    void analyzeTransaction_lowRiskScore_noAlert() {
        TransactionEvent event = new TransactionEvent(
            UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("50.00"),
            "USD", "INTERNAL_TRANSFER", "US"
        );

        FraudAnalysisResult result = fraudDetectionService.analyzeTransaction(event);

        assertNotNull(result);
        assertTrue(result.getFraudScore() < 70);
        assertFalse(result.isFraudDetected());
        verify(fraudAlertRepository, never()).save(any());
    }
}
