package com.yourorg.banking.payments.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UaeftsServiceTest {

    @InjectMocks
    private UaeftsService uaeftsService;

    @Test
    void generateRtgsMessage_validInputs_iso20022Structure() {
        // TC-PN-006: RTGS message format validation (ISO 20022 structure)
        String message = uaeftsService.generateRtgsMessage(
                "ABORAEADXXX", "ABORAEADYYY",
                new BigDecimal("250000.00"), "AED", "2025-06-15");

        assertNotNull(message);
        assertTrue(message.contains("pacs.008.001.08"));
        assertTrue(message.contains("<MsgId>"));
        assertTrue(message.contains("<NbOfTxs>1</NbOfTxs>"));
        assertTrue(message.contains("<SttlmMtd>CLRG</SttlmMtd>"));
        assertTrue(message.contains("Ccy=\"AED\""));
        assertTrue(message.contains("250000.00"));
        assertTrue(message.contains("<BICFI>ABORAEADXXX</BICFI>"));
        assertTrue(message.contains("<BICFI>ABORAEADYYY</BICFI>"));
        assertTrue(message.contains("2025-06-15"));
        assertTrue(message.contains("<FIToFICstmrCdtTrf>"));
        assertTrue(message.contains("<CdtTrfTxInf>"));
    }

    @Test
    void submitTransfer_endToEnd_domesticRtgs() {
        // TC-PN-008: End-to-end domestic RTGS transfer
        UaeftsService.TransferResult result = uaeftsService.submitTransfer(
                "ABORAEADXXX", "NBADAEADXXX",
                new BigDecimal("1000000.00"), "AED", "2025-06-15");

        assertTrue(result.success());
        assertNotNull(result.transactionId());
        assertEquals("ACCEPTED", result.status());
        assertNotNull(result.rtgsMessage());
        assertTrue(result.rtgsMessage().contains("pacs.008.001.08"));
        assertTrue(result.message().contains("successfully"));
    }
}
