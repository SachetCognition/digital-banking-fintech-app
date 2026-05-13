package com.yourorg.banking.business.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Zakat Calculation Service — Shariah-compliant wealth tax computation.
 *
 * Islamic Zakat Rules implemented:
 *   1. Rate:       2.5% (1/40) of net zakatable assets held for one lunar year (hawl).
 *   2. Nisab:      Minimum wealth threshold — the lesser of the gold nisab
 *                   (85 g × current gold price/g) or the silver nisab
 *                   (595 g × current silver price/g), converted to AED.
 *   3. Exclusions: Personal-use assets (primary residence, personal vehicle,
 *                   household furniture), debt deductions, and business
 *                   operating expenses are subtracted before calculation.
 */
@Service
public class ZakatCalculationService {

    private static final BigDecimal ZAKAT_RATE = new BigDecimal("0.025");      // 2.5%
    private static final BigDecimal GOLD_NISAB_GRAMS = new BigDecimal("85");   // 85 grams of gold
    private static final BigDecimal SILVER_NISAB_GRAMS = new BigDecimal("595");// 595 grams of silver

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ──────────────────────────────────────────────────────────
    //  Core calculation
    // ──────────────────────────────────────────────────────────

    /**
     * Calculate Zakat obligation for a customer.
     *
     * @param customerId    UUID of the customer
     * @param assets        breakdown of the customer's wealth
     * @param goldPricePerGramAed  current gold price per gram in AED
     * @param silverPricePerGramAed current silver price per gram in AED
     * @return full Zakat assessment
     */
    @Transactional
    public ZakatAssessment calculateZakat(String customerId,
                                          ZakatAssets assets,
                                          BigDecimal goldPricePerGramAed,
                                          BigDecimal silverPricePerGramAed) {

        ZakatAssessment assessment = new ZakatAssessment();
        assessment.setId(UUID.randomUUID().toString());
        assessment.setCustomerId(customerId);
        assessment.setAssessmentDate(LocalDate.now());
        assessment.setCalculatedAt(LocalDateTime.now());
        assessment.setGoldPricePerGram(goldPricePerGramAed);
        assessment.setSilverPricePerGram(silverPricePerGramAed);

        try {
            // 1. Compute nisab thresholds in AED
            BigDecimal goldNisab = GOLD_NISAB_GRAMS.multiply(goldPricePerGramAed)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal silverNisab = SILVER_NISAB_GRAMS.multiply(silverPricePerGramAed)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal nisabThreshold = goldNisab.min(silverNisab);

            assessment.setGoldNisab(goldNisab);
            assessment.setSilverNisab(silverNisab);
            assessment.setNisabThreshold(nisabThreshold);

            // 2. Sum zakatable assets
            BigDecimal totalAssets = sumZakatableAssets(assets);
            assessment.setTotalAssets(totalAssets);

            // 3. Sum excluded / deductible amounts
            BigDecimal totalExclusions = sumExclusions(assets);
            assessment.setTotalExclusions(totalExclusions);

            // 4. Net zakatable wealth
            BigDecimal netZakatableWealth = totalAssets.subtract(totalExclusions)
                    .max(BigDecimal.ZERO);
            assessment.setNetZakatableWealth(netZakatableWealth);

            // 5. Nisab check
            boolean meetsNisab = netZakatableWealth.compareTo(nisabThreshold) >= 0;
            assessment.setMeetsNisab(meetsNisab);

            if (!meetsNisab) {
                assessment.setZakatDue(BigDecimal.ZERO);
                assessment.setStatus("BELOW_NISAB");
                assessment.setMessage(
                    String.format("Net zakatable wealth (AED %s) is below the nisab threshold (AED %s). No Zakat due.",
                        netZakatableWealth.toPlainString(), nisabThreshold.toPlainString()));
            } else {
                // 6. Apply 2.5% rate
                BigDecimal zakatDue = netZakatableWealth.multiply(ZAKAT_RATE)
                        .setScale(2, RoundingMode.HALF_UP);
                assessment.setZakatDue(zakatDue);
                assessment.setZakatRate(ZAKAT_RATE);
                assessment.setStatus("CALCULATED");
                assessment.setMessage(
                    String.format("Zakat of 2.5%% on AED %s = AED %s",
                        netZakatableWealth.toPlainString(), zakatDue.toPlainString()));
            }

            // 7. Record assessment
            recordAssessment(assessment);

        } catch (Exception e) {
            assessment.setStatus("ERROR");
            assessment.setMessage("Zakat calculation failed: " + e.getMessage());
        }

        return assessment;
    }

    // ──────────────────────────────────────────────────────────
    //  Hawl (one-year holding period) verification
    // ──────────────────────────────────────────────────────────

    /**
     * Check whether the customer's wealth has been held above nisab
     * for one full lunar year (354 days).
     */
    public boolean verifyHawl(String customerId, BigDecimal nisabThreshold) {
        LocalDate oneYearAgo = LocalDate.now().minusDays(354); // lunar year ≈ 354 days
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM zakat_assessments " +
                "WHERE customer_id = ? AND assessment_date >= ? AND meets_nisab = true",
                Integer.class, customerId, oneYearAgo);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // ──────────────────────────────────────────────────────────
    //  Asset aggregation helpers
    // ──────────────────────────────────────────────────────────

    private BigDecimal sumZakatableAssets(ZakatAssets a) {
        return safe(a.getCashAndBankBalances())
            .add(safe(a.getGoldAndSilverValue()))
            .add(safe(a.getInvestmentPortfolio()))
            .add(safe(a.getTradingInventory()))
            .add(safe(a.getReceivables()))
            .add(safe(a.getRentalIncome()))
            .add(safe(a.getCryptocurrencyValue()))
            .add(safe(a.getSavingsAndDeposits()))
            .add(safe(a.getMutualFunds()))
            .add(safe(a.getSukukAndBonds()));
    }

    /**
     * Amounts excluded from Zakat:
     *   - Outstanding debts / liabilities
     *   - Personal-use assets (primary home, personal vehicle, furniture)
     *   - Business operating expenses
     *   - Taxes already paid or payable
     */
    private BigDecimal sumExclusions(ZakatAssets a) {
        return safe(a.getOutstandingDebts())
            .add(safe(a.getPersonalUseAssets()))
            .add(safe(a.getBusinessOperatingExpenses()))
            .add(safe(a.getTaxesPaid()));
    }

    private BigDecimal safe(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    // ──────────────────────────────────────────────────────────
    //  History
    // ──────────────────────────────────────────────────────────

    public List<ZakatAssessment> getAssessmentHistory(String customerId) {
        try {
            return jdbcTemplate.query(
                "SELECT * FROM zakat_assessments WHERE customer_id = ? ORDER BY assessment_date DESC",
                (rs, rowNum) -> {
                    ZakatAssessment a = new ZakatAssessment();
                    a.setId(rs.getString("id"));
                    a.setCustomerId(rs.getString("customer_id"));
                    a.setAssessmentDate(rs.getDate("assessment_date").toLocalDate());
                    a.setTotalAssets(rs.getBigDecimal("total_assets"));
                    a.setTotalExclusions(rs.getBigDecimal("total_exclusions"));
                    a.setNetZakatableWealth(rs.getBigDecimal("net_zakatable_wealth"));
                    a.setNisabThreshold(rs.getBigDecimal("nisab_threshold"));
                    a.setMeetsNisab(rs.getBoolean("meets_nisab"));
                    a.setZakatDue(rs.getBigDecimal("zakat_due"));
                    a.setStatus(rs.getString("status"));
                    a.setMessage(rs.getString("message"));
                    return a;
                },
                customerId);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // ──────────────────────────────────────────────────────────
    //  Persistence
    // ──────────────────────────────────────────────────────────

    private void recordAssessment(ZakatAssessment a) {
        jdbcTemplate.update(
            "INSERT INTO zakat_assessments " +
            "(id, customer_id, assessment_date, total_assets, total_exclusions, " +
            " net_zakatable_wealth, nisab_threshold, gold_nisab, silver_nisab, " +
            " meets_nisab, zakat_rate, zakat_due, status, message, " +
            " gold_price_per_gram, silver_price_per_gram, calculated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            a.getId(), a.getCustomerId(), a.getAssessmentDate(),
            a.getTotalAssets(), a.getTotalExclusions(),
            a.getNetZakatableWealth(), a.getNisabThreshold(),
            a.getGoldNisab(), a.getSilverNisab(),
            a.isMeetsNisab(), a.getZakatRate(), a.getZakatDue(),
            a.getStatus(), a.getMessage(),
            a.getGoldPricePerGram(), a.getSilverPricePerGram(),
            a.getCalculatedAt());
    }

    // ──────────────────────────────────────────────────────────
    //  Inner DTOs
    // ──────────────────────────────────────────────────────────

    /** Breakdown of a customer's assets for Zakat computation. */
    public static class ZakatAssets {
        // Zakatable wealth
        private BigDecimal cashAndBankBalances;
        private BigDecimal goldAndSilverValue;
        private BigDecimal investmentPortfolio;
        private BigDecimal tradingInventory;
        private BigDecimal receivables;
        private BigDecimal rentalIncome;
        private BigDecimal cryptocurrencyValue;
        private BigDecimal savingsAndDeposits;
        private BigDecimal mutualFunds;
        private BigDecimal sukukAndBonds;

        // Exclusions / deductions
        private BigDecimal outstandingDebts;
        private BigDecimal personalUseAssets;
        private BigDecimal businessOperatingExpenses;
        private BigDecimal taxesPaid;

        public BigDecimal getCashAndBankBalances() { return cashAndBankBalances; }
        public void setCashAndBankBalances(BigDecimal v) { this.cashAndBankBalances = v; }
        public BigDecimal getGoldAndSilverValue() { return goldAndSilverValue; }
        public void setGoldAndSilverValue(BigDecimal v) { this.goldAndSilverValue = v; }
        public BigDecimal getInvestmentPortfolio() { return investmentPortfolio; }
        public void setInvestmentPortfolio(BigDecimal v) { this.investmentPortfolio = v; }
        public BigDecimal getTradingInventory() { return tradingInventory; }
        public void setTradingInventory(BigDecimal v) { this.tradingInventory = v; }
        public BigDecimal getReceivables() { return receivables; }
        public void setReceivables(BigDecimal v) { this.receivables = v; }
        public BigDecimal getRentalIncome() { return rentalIncome; }
        public void setRentalIncome(BigDecimal v) { this.rentalIncome = v; }
        public BigDecimal getCryptocurrencyValue() { return cryptocurrencyValue; }
        public void setCryptocurrencyValue(BigDecimal v) { this.cryptocurrencyValue = v; }
        public BigDecimal getSavingsAndDeposits() { return savingsAndDeposits; }
        public void setSavingsAndDeposits(BigDecimal v) { this.savingsAndDeposits = v; }
        public BigDecimal getMutualFunds() { return mutualFunds; }
        public void setMutualFunds(BigDecimal v) { this.mutualFunds = v; }
        public BigDecimal getSukukAndBonds() { return sukukAndBonds; }
        public void setSukukAndBonds(BigDecimal v) { this.sukukAndBonds = v; }
        public BigDecimal getOutstandingDebts() { return outstandingDebts; }
        public void setOutstandingDebts(BigDecimal v) { this.outstandingDebts = v; }
        public BigDecimal getPersonalUseAssets() { return personalUseAssets; }
        public void setPersonalUseAssets(BigDecimal v) { this.personalUseAssets = v; }
        public BigDecimal getBusinessOperatingExpenses() { return businessOperatingExpenses; }
        public void setBusinessOperatingExpenses(BigDecimal v) { this.businessOperatingExpenses = v; }
        public BigDecimal getTaxesPaid() { return taxesPaid; }
        public void setTaxesPaid(BigDecimal v) { this.taxesPaid = v; }
    }

    /** Result of a Zakat assessment. */
    public static class ZakatAssessment {
        private String id;
        private String customerId;
        private LocalDate assessmentDate;
        private LocalDateTime calculatedAt;

        private BigDecimal totalAssets;
        private BigDecimal totalExclusions;
        private BigDecimal netZakatableWealth;

        private BigDecimal nisabThreshold;
        private BigDecimal goldNisab;
        private BigDecimal silverNisab;
        private BigDecimal goldPricePerGram;
        private BigDecimal silverPricePerGram;

        private boolean meetsNisab;
        private BigDecimal zakatRate;
        private BigDecimal zakatDue;

        private String status;   // CALCULATED | BELOW_NISAB | ERROR
        private String message;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        public LocalDate getAssessmentDate() { return assessmentDate; }
        public void setAssessmentDate(LocalDate assessmentDate) { this.assessmentDate = assessmentDate; }
        public LocalDateTime getCalculatedAt() { return calculatedAt; }
        public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
        public BigDecimal getTotalAssets() { return totalAssets; }
        public void setTotalAssets(BigDecimal totalAssets) { this.totalAssets = totalAssets; }
        public BigDecimal getTotalExclusions() { return totalExclusions; }
        public void setTotalExclusions(BigDecimal totalExclusions) { this.totalExclusions = totalExclusions; }
        public BigDecimal getNetZakatableWealth() { return netZakatableWealth; }
        public void setNetZakatableWealth(BigDecimal netZakatableWealth) { this.netZakatableWealth = netZakatableWealth; }
        public BigDecimal getNisabThreshold() { return nisabThreshold; }
        public void setNisabThreshold(BigDecimal nisabThreshold) { this.nisabThreshold = nisabThreshold; }
        public BigDecimal getGoldNisab() { return goldNisab; }
        public void setGoldNisab(BigDecimal goldNisab) { this.goldNisab = goldNisab; }
        public BigDecimal getSilverNisab() { return silverNisab; }
        public void setSilverNisab(BigDecimal silverNisab) { this.silverNisab = silverNisab; }
        public BigDecimal getGoldPricePerGram() { return goldPricePerGram; }
        public void setGoldPricePerGram(BigDecimal goldPricePerGram) { this.goldPricePerGram = goldPricePerGram; }
        public BigDecimal getSilverPricePerGram() { return silverPricePerGram; }
        public void setSilverPricePerGram(BigDecimal silverPricePerGram) { this.silverPricePerGram = silverPricePerGram; }
        public boolean isMeetsNisab() { return meetsNisab; }
        public void setMeetsNisab(boolean meetsNisab) { this.meetsNisab = meetsNisab; }
        public BigDecimal getZakatRate() { return zakatRate; }
        public void setZakatRate(BigDecimal zakatRate) { this.zakatRate = zakatRate; }
        public BigDecimal getZakatDue() { return zakatDue; }
        public void setZakatDue(BigDecimal zakatDue) { this.zakatDue = zakatDue; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
