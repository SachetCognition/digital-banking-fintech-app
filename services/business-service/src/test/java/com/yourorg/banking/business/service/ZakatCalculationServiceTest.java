package com.yourorg.banking.business.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ZakatCalculationServiceTest {

    @InjectMocks
    private ZakatCalculationService zakatCalculationService;

    @Test
    void TC_RC_001_zakatAboveNisab_calculatesCorrectly() {
        BigDecimal totalAssets = new BigDecimal("500000");
        BigDecimal nonZakatableAssets = BigDecimal.ZERO;
        BigDecimal nisabThreshold = new BigDecimal("20000");

        ZakatCalculationService.ZakatResult result = zakatCalculationService.calculateZakat(
                totalAssets, nonZakatableAssets, nisabThreshold);

        assertTrue(result.isAboveNisab());
        assertEquals(new BigDecimal("500000"), result.eligibleAssets());
        assertEquals(new BigDecimal("12500.00"), result.zakatAmount());
    }

    @Test
    void TC_RC_002_zakatBelowNisab_returnsZero() {
        BigDecimal totalAssets = new BigDecimal("15000");
        BigDecimal nonZakatableAssets = BigDecimal.ZERO;
        BigDecimal nisabThreshold = new BigDecimal("20000");

        ZakatCalculationService.ZakatResult result = zakatCalculationService.calculateZakat(
                totalAssets, nonZakatableAssets, nisabThreshold);

        assertFalse(result.isAboveNisab());
        assertEquals(new BigDecimal("15000"), result.eligibleAssets());
        assertEquals(new BigDecimal("0.00"), result.zakatAmount());
    }

    @Test
    void TC_RC_003_zakatExcludesNonZakatable_calculatesOnEligibleOnly() {
        BigDecimal totalAssets = new BigDecimal("600000");
        BigDecimal nonZakatableAssets = new BigDecimal("400000");
        BigDecimal nisabThreshold = new BigDecimal("20000");

        ZakatCalculationService.ZakatResult result = zakatCalculationService.calculateZakat(
                totalAssets, nonZakatableAssets, nisabThreshold);

        assertTrue(result.isAboveNisab());
        assertEquals(new BigDecimal("200000"), result.eligibleAssets());
        assertEquals(new BigDecimal("5000.00"), result.zakatAmount());
    }
}
