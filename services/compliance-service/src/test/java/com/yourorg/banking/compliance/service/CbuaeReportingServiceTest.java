package com.yourorg.banking.compliance.service;

import com.yourorg.banking.compliance.model.ReportType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CbuaeReportingServiceTest {

    @InjectMocks
    private CbuaeReportingService cbuaeReportingService;

    @InjectMocks
    private TransactionMonitoringService transactionMonitoringService;

    @Test
    void TC_RC_004_goAmlReportGeneration_containsMandatoryFields() {
        UUID transactionId = UUID.randomUUID();
        String suspicionIndicators = "Unusual pattern of transactions";

        String report = cbuaeReportingService.generateGoAmlReport(transactionId, suspicionIndicators);

        assertNotNull(report);
        assertTrue(report.contains("<reportingEntity>"));
        assertTrue(report.contains("EMIRATES-DIGITAL-BANK"));
        assertTrue(report.contains("<subject>"));
        assertTrue(report.contains(transactionId.toString()));
        assertTrue(report.contains("<transactionDetails>"));
        assertTrue(report.contains("<suspicionIndicators>"));
        assertTrue(report.contains(suspicionIndicators));
    }

    @Test
    void TC_RC_005_prudentialReturn_capitalAdequacyRatioCalculation() {
        BigDecimal totalAssets = new BigDecimal("1000000000");
        BigDecimal riskWeightedAssets = new BigDecimal("500000000");
        BigDecimal tier1Capital = new BigDecimal("60000000");
        BigDecimal tier2Capital = new BigDecimal("20000000");

        String report = cbuaeReportingService.generatePrudentialReturn(
                totalAssets, riskWeightedAssets, tier1Capital, tier2Capital);

        assertNotNull(report);
        // CAR = (tier1 + tier2) / riskWeightedAssets = (60M + 20M) / 500M = 0.16
        assertTrue(report.contains("<capitalAdequacyRatio>0.1600</capitalAdequacyRatio>"));
        assertTrue(report.contains("<tier1Capital>60000000</tier1Capital>"));
        assertTrue(report.contains("<tier2Capital>20000000</tier2Capital>"));
        assertTrue(report.contains("<totalCapital>80000000</totalCapital>"));
    }

    @Test
    void TC_RC_006_amlThreshold_uaeUsesAed35000() {
        BigDecimal uaeThreshold = transactionMonitoringService.getThresholdForJurisdiction("UAE");
        BigDecimal usThreshold = transactionMonitoringService.getThresholdForJurisdiction("US");

        assertEquals(new BigDecimal("35000"), uaeThreshold);
        assertEquals(new BigDecimal("10000"), usThreshold);
    }

    @Test
    void TC_RC_008_endToEndReportSubmission_generateValidateSubmitAuditTrail() {
        UUID transactionId = UUID.randomUUID();
        String suspicionIndicators = "Multiple high-value cash deposits";

        // Generate report
        String reportXml = cbuaeReportingService.generateGoAmlReport(transactionId, suspicionIndicators);
        assertNotNull(reportXml);
        assertFalse(reportXml.isEmpty());

        // Validate report contains required structure
        assertTrue(reportXml.contains("<?xml version=\"1.0\""));
        assertTrue(reportXml.contains("<goAMLReport>"));
        assertTrue(reportXml.contains("<reportMetadata>"));

        // Submit report
        String submissionId = cbuaeReportingService.submitReport(reportXml, ReportType.GOAML);
        assertNotNull(submissionId);
        assertFalse(submissionId.isEmpty());

        // Verify submission ID is a valid UUID (audit trail)
        assertDoesNotThrow(() -> UUID.fromString(submissionId));
    }
}
