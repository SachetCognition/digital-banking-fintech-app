package com.yourorg.banking.compliance.service;

import com.yourorg.banking.compliance.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TransactionMonitoringServiceTest {

    @InjectMocks
    private TransactionMonitoringService monitoringService;

    @Test
    void calculateRiskScore_highAmount_highRisk() {
        TransactionEvent event = new TransactionEvent(
            UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("100000.00"),
            "USD", "WIRE_TRANSFER", "US"
        );

        int riskScore = monitoringService.calculateRiskScore(event);

        assertTrue(riskScore >= 70);
    }

    @Test
    void calculateRiskScore_lowAmount_lowRisk() {
        TransactionEvent event = new TransactionEvent(
            UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("25.00"),
            "USD", "DEBIT_CARD", "US"
        );

        int riskScore = monitoringService.calculateRiskScore(event);

        assertTrue(riskScore < 30);
    }

    @Test
    void calculateRiskScore_internationalTransfer_elevatedRisk() {
        TransactionEvent event = new TransactionEvent(
            UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("5000.00"),
            "USD", "INTERNATIONAL_WIRE", "RU"
        );

        int riskScore = monitoringService.calculateRiskScore(event);

        assertTrue(riskScore >= 50);
    }
}
