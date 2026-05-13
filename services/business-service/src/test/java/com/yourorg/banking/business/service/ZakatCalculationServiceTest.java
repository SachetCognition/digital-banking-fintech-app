package com.yourorg.banking.business.service;

import com.yourorg.banking.business.service.ZakatCalculationService.ZakatAssets;
import com.yourorg.banking.business.service.ZakatCalculationService.ZakatAssessment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZakatCalculationServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ZakatCalculationService zakatService;

    // Reference prices (AED) — realistic 2024 approximations
    private static final BigDecimal GOLD_PRICE = new BigDecimal("245.00");   // AED/gram
    private static final BigDecimal SILVER_PRICE = new BigDecimal("2.80");   // AED/gram

    // Nisab thresholds derived from prices
    //   Gold: 85 g × 245 = AED 20,825
    //   Silver: 595 g × 2.80 = AED 1,666  ← lower, so this is the nisab
    private static final BigDecimal EXPECTED_GOLD_NISAB = new BigDecimal("20825.00");
    private static final BigDecimal EXPECTED_SILVER_NISAB = new BigDecimal("1666.00");
    private static final BigDecimal EXPECTED_NISAB = EXPECTED_SILVER_NISAB; // min of the two

    private ZakatAssets baseAssets;

    @BeforeEach
    void setUp() {
        baseAssets = new ZakatAssets();
        // suppress DB insert during unit tests
        lenient().when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);
    }

    // ───────────── 2.5 % rate ─────────────

    @Test
    void zakatRate_isExactly_2_5_percent() {
        baseAssets.setCashAndBankBalances(new BigDecimal("100000"));

        ZakatAssessment result = zakatService.calculateZakat(
            "cust-1", baseAssets, GOLD_PRICE, SILVER_PRICE);

        assertEquals("CALCULATED", result.getStatus());
        // 100 000 × 0.025 = 2 500
        assertEquals(new BigDecimal("2500.00"), result.getZakatDue());
        assertEquals(new BigDecimal("0.025"), result.getZakatRate());
    }

    // ───────────── Nisab check ─────────────

    @Test
    void nisab_usesLesserOfGoldAndSilver() {
        baseAssets.setCashAndBankBalances(new BigDecimal("1700"));

        ZakatAssessment result = zakatService.calculateZakat(
            "cust-2", baseAssets, GOLD_PRICE, SILVER_PRICE);

        assertEquals(EXPECTED_GOLD_NISAB, result.getGoldNisab());
        assertEquals(EXPECTED_SILVER_NISAB, result.getSilverNisab());
        assertEquals(EXPECTED_NISAB, result.getNisabThreshold());
    }

    @Test
    void wealthAboveNisab_producesZakat() {
        baseAssets.setCashAndBankBalances(new BigDecimal("5000")); // > 1 666

        ZakatAssessment result = zakatService.calculateZakat(
            "cust-3", baseAssets, GOLD_PRICE, SILVER_PRICE);

        assertTrue(result.isMeetsNisab());
        assertEquals("CALCULATED", result.getStatus());
        assertEquals(
            new BigDecimal("5000").multiply(new BigDecimal("0.025")).setScale(2, RoundingMode.HALF_UP),
            result.getZakatDue());
    }

    @Test
    void wealthBelowNisab_noZakatDue() {
        baseAssets.setCashAndBankBalances(new BigDecimal("1000")); // < 1 666

        ZakatAssessment result = zakatService.calculateZakat(
            "cust-4", baseAssets, GOLD_PRICE, SILVER_PRICE);

        assertFalse(result.isMeetsNisab());
        assertEquals("BELOW_NISAB", result.getStatus());
        assertEquals(BigDecimal.ZERO, result.getZakatDue());
    }

    @Test
    void wealthExactlyAtNisab_zakatIsDue() {
        baseAssets.setCashAndBankBalances(EXPECTED_NISAB); // exactly 1 666

        ZakatAssessment result = zakatService.calculateZakat(
            "cust-5", baseAssets, GOLD_PRICE, SILVER_PRICE);

        assertTrue(result.isMeetsNisab());
        assertEquals("CALCULATED", result.getStatus());
        BigDecimal expected = EXPECTED_NISAB.multiply(new BigDecimal("0.025"))
                .setScale(2, RoundingMode.HALF_UP);
        assertEquals(expected, result.getZakatDue());
    }

    // ───────────── Asset exclusions ─────────────

    @Test
    void personalUseAssets_areExcluded() {
        baseAssets.setCashAndBankBalances(new BigDecimal("50000"));
        baseAssets.setPersonalUseAssets(new BigDecimal("48500")); // home, car, furniture
        // Net = 50 000 − 48 500 = 1 500 < 1 666 nisab

        ZakatAssessment result = zakatService.calculateZakat(
            "cust-6", baseAssets, GOLD_PRICE, SILVER_PRICE);

        assertFalse(result.isMeetsNisab());
        assertEquals(new BigDecimal("1500"), result.getNetZakatableWealth()
                .setScale(0, RoundingMode.UNNECESSARY));
    }

    @Test
    void outstandingDebts_areDeducted() {
        baseAssets.setCashAndBankBalances(new BigDecimal("20000"));
        baseAssets.setOutstandingDebts(new BigDecimal("15000"));
        // Net = 5 000

        ZakatAssessment result = zakatService.calculateZakat(
            "cust-7", baseAssets, GOLD_PRICE, SILVER_PRICE);

        assertEquals(new BigDecimal("5000"), result.getNetZakatableWealth()
                .setScale(0, RoundingMode.UNNECESSARY));
        assertEquals(
            new BigDecimal("5000").multiply(new BigDecimal("0.025")).setScale(2, RoundingMode.HALF_UP),
            result.getZakatDue());
    }

    @Test
    void businessOperatingExpenses_areExcluded() {
        baseAssets.setCashAndBankBalances(new BigDecimal("100000"));
        baseAssets.setBusinessOperatingExpenses(new BigDecimal("30000"));
        // Net = 70 000

        ZakatAssessment result = zakatService.calculateZakat(
            "cust-8", baseAssets, GOLD_PRICE, SILVER_PRICE);

        assertEquals(new BigDecimal("70000"), result.getNetZakatableWealth()
                .setScale(0, RoundingMode.UNNECESSARY));
        assertEquals(new BigDecimal("1750.00"), result.getZakatDue());
    }

    @Test
    void taxesPaid_areExcluded() {
        baseAssets.setCashAndBankBalances(new BigDecimal("40000"));
        baseAssets.setTaxesPaid(new BigDecimal("5000"));
        // Net = 35 000

        ZakatAssessment result = zakatService.calculateZakat(
            "cust-9", baseAssets, GOLD_PRICE, SILVER_PRICE);

        assertEquals(new BigDecimal("35000"), result.getNetZakatableWealth()
                .setScale(0, RoundingMode.UNNECESSARY));
        assertEquals(new BigDecimal("875.00"), result.getZakatDue());
    }

    @Test
    void allExclusions_stackCorrectly() {
        baseAssets.setCashAndBankBalances(new BigDecimal("200000"));
        baseAssets.setGoldAndSilverValue(new BigDecimal("30000"));
        baseAssets.setInvestmentPortfolio(new BigDecimal("50000"));

        baseAssets.setOutstandingDebts(new BigDecimal("40000"));
        baseAssets.setPersonalUseAssets(new BigDecimal("80000"));
        baseAssets.setBusinessOperatingExpenses(new BigDecimal("20000"));
        baseAssets.setTaxesPaid(new BigDecimal("10000"));

        // Total assets = 200k + 30k + 50k = 280k
        // Total exclusions = 40k + 80k + 20k + 10k = 150k
        // Net = 130k
        ZakatAssessment result = zakatService.calculateZakat(
            "cust-10", baseAssets, GOLD_PRICE, SILVER_PRICE);

        assertEquals(new BigDecimal("280000"), result.getTotalAssets()
                .setScale(0, RoundingMode.UNNECESSARY));
        assertEquals(new BigDecimal("150000"), result.getTotalExclusions()
                .setScale(0, RoundingMode.UNNECESSARY));
        assertEquals(new BigDecimal("130000"), result.getNetZakatableWealth()
                .setScale(0, RoundingMode.UNNECESSARY));
        assertEquals(new BigDecimal("3250.00"), result.getZakatDue());
    }

    @Test
    void exclusionsExceedAssets_netIsZero_noZakat() {
        baseAssets.setCashAndBankBalances(new BigDecimal("5000"));
        baseAssets.setOutstandingDebts(new BigDecimal("10000")); // debts > assets

        ZakatAssessment result = zakatService.calculateZakat(
            "cust-11", baseAssets, GOLD_PRICE, SILVER_PRICE);

        assertEquals(BigDecimal.ZERO, result.getNetZakatableWealth());
        assertFalse(result.isMeetsNisab());
        assertEquals(BigDecimal.ZERO, result.getZakatDue());
    }

    // ───────────── Multiple asset types ─────────────

    @Test
    void allZakatableAssetTypes_areSummed() {
        baseAssets.setCashAndBankBalances(new BigDecimal("10000"));
        baseAssets.setGoldAndSilverValue(new BigDecimal("5000"));
        baseAssets.setInvestmentPortfolio(new BigDecimal("8000"));
        baseAssets.setTradingInventory(new BigDecimal("3000"));
        baseAssets.setReceivables(new BigDecimal("2000"));
        baseAssets.setRentalIncome(new BigDecimal("1000"));
        baseAssets.setCryptocurrencyValue(new BigDecimal("4000"));
        baseAssets.setSavingsAndDeposits(new BigDecimal("6000"));
        baseAssets.setMutualFunds(new BigDecimal("7000"));
        baseAssets.setSukukAndBonds(new BigDecimal("9000"));
        // Total = 55 000

        ZakatAssessment result = zakatService.calculateZakat(
            "cust-12", baseAssets, GOLD_PRICE, SILVER_PRICE);

        assertEquals(new BigDecimal("55000"), result.getTotalAssets()
                .setScale(0, RoundingMode.UNNECESSARY));
        assertEquals(new BigDecimal("1375.00"), result.getZakatDue()); // 55k × 2.5%
    }

    // ───────────── Null / empty asset fields ─────────────

    @Test
    void nullAssetFields_treatedAsZero() {
        // Only set cash; everything else remains null
        baseAssets.setCashAndBankBalances(new BigDecimal("10000"));

        ZakatAssessment result = zakatService.calculateZakat(
            "cust-13", baseAssets, GOLD_PRICE, SILVER_PRICE);

        assertEquals(new BigDecimal("10000"), result.getTotalAssets()
                .setScale(0, RoundingMode.UNNECESSARY));
        assertEquals("CALCULATED", result.getStatus());
    }

    @Test
    void emptyAssets_belowNisab() {
        // All fields null → total = 0
        ZakatAssessment result = zakatService.calculateZakat(
            "cust-14", new ZakatAssets(), GOLD_PRICE, SILVER_PRICE);

        assertEquals(BigDecimal.ZERO, result.getNetZakatableWealth());
        assertEquals("BELOW_NISAB", result.getStatus());
    }

    // ───────────── Gold-only nisab scenario ─────────────

    @Test
    void whenGoldNisabIsLower_itIsUsed() {
        // If silver were very expensive, gold nisab would be lower
        BigDecimal expensiveSilver = new BigDecimal("50.00"); // 595 × 50 = 29 750
        // Gold nisab stays 85 × 245 = 20 825 → lower

        baseAssets.setCashAndBankBalances(new BigDecimal("21000"));

        ZakatAssessment result = zakatService.calculateZakat(
            "cust-15", baseAssets, GOLD_PRICE, expensiveSilver);

        assertEquals(EXPECTED_GOLD_NISAB, result.getNisabThreshold());
        assertTrue(result.isMeetsNisab()); // 21 000 > 20 825
    }
}
