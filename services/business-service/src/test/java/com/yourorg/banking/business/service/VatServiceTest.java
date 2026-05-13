package com.yourorg.banking.business.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class VatServiceTest {

    @InjectMocks
    private VatService vatService;

    @Test
    void TC_RC_007_vatOnFee_nonExempt_calculatesCorrectly() {
        BigDecimal feeAmount = new BigDecimal("1000");

        VatService.VatResult result = vatService.calculateVat(feeAmount, false);

        assertFalse(result.isExempt());
        assertEquals(new BigDecimal("1000"), result.feeAmount());
        assertEquals(new BigDecimal("50.00"), result.vatAmount());
        assertEquals(new BigDecimal("1050.00"), result.totalAmount());
        assertEquals(new BigDecimal("0.05"), result.vatRate());
    }

    @Test
    void TC_RC_007b_vatExempt_returnsZeroVat() {
        BigDecimal feeAmount = new BigDecimal("1000");

        VatService.VatResult result = vatService.calculateVat(feeAmount, true);

        assertTrue(result.isExempt());
        assertEquals(new BigDecimal("1000"), result.feeAmount());
        assertEquals(new BigDecimal("0.00"), result.vatAmount());
        assertEquals(new BigDecimal("1000"), result.totalAmount());
        assertEquals(new BigDecimal("0.05"), result.vatRate());
    }
}
