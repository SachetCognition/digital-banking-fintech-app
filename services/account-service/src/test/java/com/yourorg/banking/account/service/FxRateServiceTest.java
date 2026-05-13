package com.yourorg.banking.account.service;

import com.yourorg.banking.account.model.Currency;
import com.yourorg.banking.account.model.FxRate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FxRateServiceTest {

    private MockFxRateService fxRateService;

    @BeforeEach
    void setUp() {
        fxRateService = new MockFxRateService();
        ReflectionTestUtils.setField(fxRateService, "defaultSpread", new BigDecimal("0.005"));
        ReflectionTestUtils.setField(fxRateService, "cacheTtlMinutes", 15L);
    }

    @Test
    void getRate_aedToSar_returnsValidRate() {
        FxRate rate = fxRateService.getRate(Currency.AED, Currency.SAR);

        assertNotNull(rate);
        assertEquals(Currency.AED, rate.from());
        assertEquals(Currency.SAR, rate.to());
        assertTrue(rate.midRate().compareTo(BigDecimal.ZERO) > 0);
        assertEquals("CBUAE", rate.source());
        assertNotNull(rate.timestamp());
    }

    @Test
    void getRate_spreadCalculation_buyLessThanMidLessThanSell() {
        FxRate rate = fxRateService.getRate(Currency.AED, Currency.USD);

        assertTrue(rate.buyRate().compareTo(rate.midRate()) < 0);
        assertTrue(rate.sellRate().compareTo(rate.midRate()) > 0);
        assertEquals(new BigDecimal("0.005"), rate.spread());
    }

    @Test
    void getRate_cachedRate_returnsSameInstance() {
        FxRate first = fxRateService.getRate(Currency.AED, Currency.EUR);
        FxRate second = fxRateService.getRate(Currency.AED, Currency.EUR);

        assertEquals(first.midRate(), second.midRate());
        assertEquals(first.source(), second.source());
        assertEquals(first.timestamp(), second.timestamp());
    }

    @Test
    void getRate_sameCurrency_returnsOneToOne() {
        FxRate rate = fxRateService.getRate(Currency.USD, Currency.USD);

        assertEquals(0, BigDecimal.ONE.compareTo(rate.midRate()));
    }

    @Test
    void getRate_allGccCurrencies_returnValidRates() {
        Currency[] gccCurrencies = {
            Currency.AED, Currency.SAR, Currency.QAR,
            Currency.BHD, Currency.OMR, Currency.KWD
        };

        for (Currency from : gccCurrencies) {
            for (Currency to : gccCurrencies) {
                FxRate rate = fxRateService.getRate(from, to);
                assertNotNull(rate);
                assertTrue(rate.midRate().compareTo(BigDecimal.ZERO) > 0);
                assertEquals("CBUAE", rate.source());
            }
        }
    }
}
