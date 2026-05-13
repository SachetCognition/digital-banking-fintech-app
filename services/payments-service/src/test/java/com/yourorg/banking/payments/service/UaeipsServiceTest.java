package com.yourorg.banking.payments.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UaeipsServiceTest {

    @InjectMocks
    private UaeipsService uaeipsService;

    @Test
    void initiateInstantTransfer_validIbans_succeeds() {
        // TC-PN-004: Instant transfer completes with valid IBANs
        String senderIban = "AE070331234567890123456";
        String receiverIban = "AE460261234567890123456";

        UaeipsService.TransferResult result = uaeipsService.initiateInstantTransfer(
                senderIban, receiverIban, new BigDecimal("5000.00"), "AED");

        assertTrue(result.success());
        assertNotNull(result.transactionId());
        assertEquals("SETTLED", result.status());
        assertNotNull(result.settlementTime());
        assertTrue(result.message().contains("successfully"));
    }

    @Test
    void initiateInstantTransfer_invalidIban_rejected() {
        // TC-PN-005: Invalid IBAN rejected
        String invalidIban = "AE12345";
        String validIban = "AE070331234567890123456";

        UaeipsService.TransferResult result = uaeipsService.initiateInstantTransfer(
                invalidIban, validIban, new BigDecimal("1000.00"), "AED");

        assertFalse(result.success());
        assertTrue(result.message().contains("Invalid sender IBAN"));
        assertNull(result.transactionId());
    }
}
