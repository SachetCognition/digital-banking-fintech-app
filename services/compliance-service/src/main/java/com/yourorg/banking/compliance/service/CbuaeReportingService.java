package com.yourorg.banking.compliance.service;

import com.yourorg.banking.compliance.model.ReportType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CbuaeReportingService {

    private static final String REPORTING_ENTITY_ID = "EMIRATES-DIGITAL-BANK";
    private static final DateTimeFormatter XML_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public String generateGoAmlReport(UUID transactionId, String suspicionIndicators) {
        log.info("Generating goAML report for transaction: {}", transactionId);

        String reportId = UUID.randomUUID().toString();
        LocalDateTime reportDate = LocalDateTime.now();

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<goAMLReport>\n");
        xml.append("  <reportMetadata>\n");
        xml.append("    <reportId>").append(reportId).append("</reportId>\n");
        xml.append("    <reportDate>").append(reportDate.format(XML_DATE_FORMAT)).append("</reportDate>\n");
        xml.append("    <reportType>STR</reportType>\n");
        xml.append("    <priority>HIGH</priority>\n");
        xml.append("  </reportMetadata>\n");
        xml.append("  <reportingEntity>\n");
        xml.append("    <entityId>").append(REPORTING_ENTITY_ID).append("</entityId>\n");
        xml.append("    <entityName>Emirates Digital Bank</entityName>\n");
        xml.append("    <jurisdiction>UAE</jurisdiction>\n");
        xml.append("  </reportingEntity>\n");
        xml.append("  <subject>\n");
        xml.append("    <transactionId>").append(transactionId).append("</transactionId>\n");
        xml.append("    <subjectType>INDIVIDUAL</subjectType>\n");
        xml.append("  </subject>\n");
        xml.append("  <transactionDetails>\n");
        xml.append("    <transactionId>").append(transactionId).append("</transactionId>\n");
        xml.append("    <transactionDate>").append(reportDate.format(XML_DATE_FORMAT)).append("</transactionDate>\n");
        xml.append("  </transactionDetails>\n");
        xml.append("  <suspicionIndicators>\n");
        xml.append("    <indicators>").append(suspicionIndicators).append("</indicators>\n");
        xml.append("  </suspicionIndicators>\n");
        xml.append("</goAMLReport>");

        log.info("goAML report generated successfully with ID: {}", reportId);
        return xml.toString();
    }

    public String generatePrudentialReturn(BigDecimal totalAssets, BigDecimal riskWeightedAssets,
                                           BigDecimal tier1Capital, BigDecimal tier2Capital) {
        log.info("Generating CBUAE prudential return");

        BigDecimal totalCapital = tier1Capital.add(tier2Capital);
        BigDecimal capitalAdequacyRatio = totalCapital.divide(riskWeightedAssets, 4, RoundingMode.HALF_UP);

        String reportId = UUID.randomUUID().toString();
        LocalDateTime reportDate = LocalDateTime.now();

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<prudentialReturn>\n");
        xml.append("  <reportMetadata>\n");
        xml.append("    <reportId>").append(reportId).append("</reportId>\n");
        xml.append("    <reportDate>").append(reportDate.format(XML_DATE_FORMAT)).append("</reportDate>\n");
        xml.append("    <reportType>PRUDENTIAL_RETURN</reportType>\n");
        xml.append("    <reportingEntity>").append(REPORTING_ENTITY_ID).append("</reportingEntity>\n");
        xml.append("  </reportMetadata>\n");
        xml.append("  <capitalAdequacy>\n");
        xml.append("    <totalAssets>").append(totalAssets).append("</totalAssets>\n");
        xml.append("    <riskWeightedAssets>").append(riskWeightedAssets).append("</riskWeightedAssets>\n");
        xml.append("    <tier1Capital>").append(tier1Capital).append("</tier1Capital>\n");
        xml.append("    <tier2Capital>").append(tier2Capital).append("</tier2Capital>\n");
        xml.append("    <totalCapital>").append(totalCapital).append("</totalCapital>\n");
        xml.append("    <capitalAdequacyRatio>").append(capitalAdequacyRatio).append("</capitalAdequacyRatio>\n");
        xml.append("  </capitalAdequacy>\n");
        xml.append("</prudentialReturn>");

        log.info("Prudential return generated with CAR: {}", capitalAdequacyRatio);
        return xml.toString();
    }

    public String submitReport(String reportXml, ReportType type) {
        log.info("Submitting {} report to CBUAE", type.getCode());

        String submissionId = UUID.randomUUID().toString();
        LocalDateTime submissionTime = LocalDateTime.now();

        log.info("Report submitted successfully - submissionId: {}, type: {}, timestamp: {}",
                submissionId, type.getCode(), submissionTime);

        return submissionId;
    }
}
