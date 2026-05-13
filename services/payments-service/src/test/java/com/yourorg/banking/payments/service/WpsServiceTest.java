package com.yourorg.banking.payments.service;

import com.yourorg.banking.payments.model.WpsSifRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WpsServiceTest {

    @InjectMocks
    private WpsService wpsService;

    @Test
    void generateSifFile_threeEmployees_edrPlusThreeSdrRecords() {
        // TC-PN-001: SIF file generation — 3 employees, verify EDR + 3 SDR format, pipe-delimited
        List<WpsSifRecord> records = List.of(
                new WpsSifRecord("EMP001", "Ahmed Ali", "ENBD", "1234567890123456",
                        new BigDecimal("15000"), new BigDecimal("2000"), new BigDecimal("500"),
                        new BigDecimal("16500")),
                new WpsSifRecord("EMP002", "Sara Khan", "ADCB", "2345678901234567",
                        new BigDecimal("12000"), new BigDecimal("1500"), new BigDecimal("400"),
                        new BigDecimal("13100")),
                new WpsSifRecord("EMP003", "John Smith", "FAB", "3456789012345678",
                        new BigDecimal("18000"), new BigDecimal("3000"), new BigDecimal("800"),
                        new BigDecimal("20200"))
        );

        String sifContent = wpsService.generateSifFile("EMPLOYER-001", records);

        assertNotNull(sifContent);
        String[] lines = sifContent.trim().split("\n");
        assertEquals(4, lines.length);

        assertTrue(lines[0].startsWith("EDR|"));
        String[] edrParts = lines[0].split("\\|");
        assertEquals("EDR", edrParts[0]);
        assertEquals("EMPLOYER-001", edrParts[1]);
        assertEquals("3", edrParts[3]);
        assertEquals("49800", edrParts[4]);

        for (int i = 1; i <= 3; i++) {
            assertTrue(lines[i].startsWith("SDR|"));
            String[] sdrParts = lines[i].split("\\|");
            assertEquals(9, sdrParts.length);
            assertEquals("SDR", sdrParts[0]);
        }

        assertTrue(lines[1].contains("EMP001"));
        assertTrue(lines[1].contains("Ahmed Ali"));
        assertTrue(lines[2].contains("EMP002"));
        assertTrue(lines[3].contains("EMP003"));
    }

    @Test
    void validateBatch_fiveRecords_sufficientFunds_allPass() {
        // TC-PN-002: Batch validation — 5 records, sufficient funds, all pass
        List<WpsSifRecord> records = List.of(
                new WpsSifRecord("EMP001", "Employee 1", "ENBD", "1234567890123456",
                        new BigDecimal("10000"), new BigDecimal("1000"), new BigDecimal("200"),
                        new BigDecimal("10800")),
                new WpsSifRecord("EMP002", "Employee 2", "ADCB", "2345678901234567",
                        new BigDecimal("10000"), new BigDecimal("1000"), new BigDecimal("200"),
                        new BigDecimal("10800")),
                new WpsSifRecord("EMP003", "Employee 3", "FAB", "3456789012345678",
                        new BigDecimal("10000"), new BigDecimal("1000"), new BigDecimal("200"),
                        new BigDecimal("10800")),
                new WpsSifRecord("EMP004", "Employee 4", "DIB", "4567890123456789",
                        new BigDecimal("10000"), new BigDecimal("1000"), new BigDecimal("200"),
                        new BigDecimal("10800")),
                new WpsSifRecord("EMP005", "Employee 5", "CBD", "5678901234567890",
                        new BigDecimal("10000"), new BigDecimal("1000"), new BigDecimal("200"),
                        new BigDecimal("10800"))
        );

        WpsService.BatchResult result = wpsService.validateBatch("BATCH-001", records,
                new BigDecimal("100000"));

        assertEquals(WpsService.BatchStatus.VALIDATED, result.status());
        assertNotNull(result.message());
    }

    @Test
    void validateBatch_insufficientFunds_rejected() {
        // TC-PN-003: Insufficient funds — balance 10K, batch 50K -> INSUFFICIENT_FUNDS
        List<WpsSifRecord> records = List.of(
                new WpsSifRecord("EMP001", "Employee 1", "ENBD", "1234567890123456",
                        new BigDecimal("10000"), new BigDecimal("1000"), new BigDecimal("200"),
                        new BigDecimal("10000")),
                new WpsSifRecord("EMP002", "Employee 2", "ADCB", "2345678901234567",
                        new BigDecimal("10000"), new BigDecimal("1000"), new BigDecimal("200"),
                        new BigDecimal("10000")),
                new WpsSifRecord("EMP003", "Employee 3", "FAB", "3456789012345678",
                        new BigDecimal("10000"), new BigDecimal("1000"), new BigDecimal("200"),
                        new BigDecimal("10000")),
                new WpsSifRecord("EMP004", "Employee 4", "DIB", "4567890123456789",
                        new BigDecimal("10000"), new BigDecimal("1000"), new BigDecimal("200"),
                        new BigDecimal("10000")),
                new WpsSifRecord("EMP005", "Employee 5", "CBD", "5678901234567890",
                        new BigDecimal("10000"), new BigDecimal("1000"), new BigDecimal("200"),
                        new BigDecimal("10000"))
        );

        WpsService.BatchResult result = wpsService.validateBatch("BATCH-002", records,
                new BigDecimal("10000"));

        assertEquals(WpsService.BatchStatus.INSUFFICIENT_FUNDS, result.status());
        assertTrue(result.message().contains("exceeds"));
    }
}
