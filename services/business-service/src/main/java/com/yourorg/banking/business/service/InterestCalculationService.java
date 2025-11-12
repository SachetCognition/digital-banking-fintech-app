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

@Service
public class InterestCalculationService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Calculate daily interest for an account
     */
    @Transactional
    public InterestCalculation calculateDailyInterest(String customerId, String accountId, 
                                                    String accountType, BigDecimal accountBalance) {
        InterestCalculation calculation = new InterestCalculation();
        calculation.setCustomerId(customerId);
        calculation.setAccountId(accountId);
        calculation.setCalculationDate(LocalDate.now());
        calculation.setAccountBalance(accountBalance);
        calculation.setAccountType(accountType);
        calculation.setCalculatedAt(LocalDateTime.now());

        try {
            // Get interest rate for account type
            InterestRate interestRate = getInterestRate(accountType, accountBalance);
            if (interestRate == null) {
                calculation.setStatus("no_rate");
                calculation.setErrorMessage("No interest rate found for account type: " + accountType);
                return calculation;
            }

            // Calculate daily interest
            BigDecimal dailyInterest = calculateDailyInterestAmount(accountBalance, interestRate);
            BigDecimal monthlyInterest = dailyInterest.multiply(new BigDecimal("30"));
            BigDecimal yearToDateInterest = getYearToDateInterest(accountId);

            calculation.setInterestRate(interestRate.getInterestRate());
            calculation.setDailyInterest(dailyInterest);
            calculation.setMonthlyInterest(monthlyInterest);
            calculation.setYearToDateInterest(yearToDateInterest);
            calculation.setCompoundingFrequency(interestRate.getCompoundingFrequency());
            calculation.setCalculationMethod("Daily compound interest calculation");
            calculation.setStatus("calculated");

            // Record interest calculation
            recordInterestCalculation(calculation);

        } catch (Exception e) {
            calculation.setStatus("error");
            calculation.setErrorMessage(e.getMessage());
        }

        return calculation;
    }

    /**
     * Process interest payments
     */
    @Transactional
    public InterestPayment processInterestPayment(String customerId, String accountId, 
                                                String paymentMethod) {
        InterestPayment payment = new InterestPayment();
        payment.setCustomerId(customerId);
        payment.setAccountId(accountId);
        payment.setPaymentDate(LocalDate.now());
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus("processing");
        payment.setStartedAt(LocalDateTime.now());

        try {
            // Get pending interest calculations
            List<InterestCalculation> pendingCalculations = getPendingInterestCalculations(accountId);
            if (pendingCalculations.isEmpty()) {
                payment.setStatus("no_pending");
                payment.setErrorMessage("No pending interest calculations found");
                return payment;
            }

            // Calculate total interest amount
            BigDecimal totalInterest = pendingCalculations.stream()
                .map(InterestCalculation::getDailyInterest)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            payment.setInterestAmount(totalInterest);

            // Process payment based on method
            boolean success = false;
            if ("credit".equals(paymentMethod)) {
                success = processInterestCredit(accountId, totalInterest);
            } else if ("compound".equals(paymentMethod)) {
                success = processInterestCompounding(accountId, totalInterest);
            }

            if (success) {
                // Update calculations as processed
                markCalculationsAsProcessed(pendingCalculations);
                
                // Record interest payment
                recordInterestPayment(payment);
                
                payment.setStatus("completed");
                payment.setProcessedAt(LocalDateTime.now());
            } else {
                payment.setStatus("failed");
                payment.setErrorMessage("Interest payment processing failed");
            }

        } catch (Exception e) {
            payment.setStatus("failed");
            payment.setErrorMessage(e.getMessage());
        }

        return payment;
    }

    /**
     * Get interest summary for customer
     */
    public InterestSummary getInterestSummary(String customerId, String period) {
        InterestSummary summary = new InterestSummary();
        summary.setCustomerId(customerId);
        summary.setPeriod(period);
        summary.setGeneratedAt(LocalDateTime.now());

        try {
            String sql = """
                SELECT 
                    account_type,
                    COUNT(*) as total_calculations,
                    SUM(daily_interest) as total_daily_interest,
                    SUM(monthly_interest) as total_monthly_interest,
                    SUM(year_to_date_interest) as total_ytd_interest,
                    AVG(interest_rate) as average_rate,
                    MAX(interest_rate) as max_rate,
                    MIN(interest_rate) as min_rate
                FROM interest_calculations 
                WHERE customer_id = ? AND calculation_date >= ?
                GROUP BY account_type
                """;
            
            LocalDate startDate = getPeriodStartDate(period);
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, customerId, startDate);
            
            List<AccountTypeSummary> accountTypeSummaries = new ArrayList<>();
            BigDecimal totalInterest = BigDecimal.ZERO;
            int totalCalculations = 0;
            
            for (Map<String, Object> row : results) {
                AccountTypeSummary accountSummary = new AccountTypeSummary();
                accountSummary.setAccountType((String) row.get("account_type"));
                accountSummary.setTotalCalculations(((Number) row.get("total_calculations")).intValue());
                accountSummary.setTotalDailyInterest(((BigDecimal) row.get("total_daily_interest")));
                accountSummary.setTotalMonthlyInterest(((BigDecimal) row.get("total_monthly_interest")));
                accountSummary.setTotalYtdInterest(((BigDecimal) row.get("total_ytd_interest")));
                accountSummary.setAverageRate(((BigDecimal) row.get("average_rate")));
                accountSummary.setMaxRate(((BigDecimal) row.get("max_rate")));
                accountSummary.setMinRate(((BigDecimal) row.get("min_rate")));
                
                accountTypeSummaries.add(accountSummary);
                totalInterest = totalInterest.add(accountSummary.getTotalMonthlyInterest());
                totalCalculations += accountSummary.getTotalCalculations();
            }
            
            summary.setAccountTypeSummaries(accountTypeSummaries);
            summary.setTotalInterest(totalInterest);
            summary.setTotalCalculations(totalCalculations);

        } catch (Exception e) {
            summary.setErrorMessage("Failed to generate interest summary: " + e.getMessage());
        }

        return summary;
    }

    /**
     * Get interest rates for all account types
     */
    public List<InterestRate> getInterestRates() {
        String sql = """
            SELECT * FROM interest_rates 
            WHERE is_active = true 
            AND effective_date <= CURRENT_DATE 
            AND (expiration_date IS NULL OR expiration_date >= CURRENT_DATE)
            ORDER BY account_type, effective_date DESC
            """;
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            InterestRate rate = new InterestRate();
            rate.setId(rs.getString("id"));
            rate.setAccountType(rs.getString("account_type"));
            rate.setRateName(rs.getString("rate_name"));
            rate.setAnnualPercentageYield(rs.getBigDecimal("annual_percentage_yield"));
            rate.setInterestRate(rs.getBigDecimal("interest_rate"));
            rate.setCompoundingFrequency(rs.getString("compounding_frequency"));
            rate.setMinimumBalance(rs.getBigDecimal("minimum_balance"));
            rate.setMaximumBalance(rs.getBigDecimal("maximum_balance"));
            rate.setIsActive(rs.getBoolean("is_active"));
            rate.setEffectiveDate(rs.getDate("effective_date").toLocalDate());
            rate.setExpirationDate(rs.getDate("expiration_date") != null ? 
                rs.getDate("expiration_date").toLocalDate() : null);
            return rate;
        });
    }

    /**
     * Update interest rates
     */
    @Transactional
    public InterestRate updateInterestRate(String accountType, BigDecimal newRate, 
                                         String compoundingFrequency) {
        InterestRate rate = new InterestRate();
        rate.setAccountType(accountType);
        rate.setInterestRate(newRate);
        rate.setCompoundingFrequency(compoundingFrequency);
        rate.setEffectiveDate(LocalDate.now());
        rate.setIsActive(true);

        try {
            // Deactivate current rate
            String deactivateSql = """
                UPDATE interest_rates 
                SET is_active = false, expiration_date = CURRENT_DATE 
                WHERE account_type = ? AND is_active = true
                """;
            jdbcTemplate.update(deactivateSql, accountType);

            // Insert new rate
            String insertSql = """
                INSERT INTO interest_rates 
                (id, account_type, rate_name, annual_percentage_yield, interest_rate, 
                 compounding_frequency, minimum_balance, maximum_balance, is_active, 
                 effective_date, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
            
            String rateName = accountType.toUpperCase() + " Rate";
            jdbcTemplate.update(insertSql,
                UUID.randomUUID().toString(),
                accountType,
                rateName,
                newRate,
                newRate,
                compoundingFrequency,
                BigDecimal.ZERO,
                null,
                true,
                LocalDate.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
            );

            rate.setId(UUID.randomUUID().toString());
            rate.setRateName(rateName);
            rate.setAnnualPercentageYield(newRate);
            rate.setMinimumBalance(BigDecimal.ZERO);
            rate.setMaximumBalance(null);

        } catch (Exception e) {
            rate.setErrorMessage("Failed to update interest rate: " + e.getMessage());
        }

        return rate;
    }

    // Private helper methods
    private InterestRate getInterestRate(String accountType, BigDecimal accountBalance) {
        String sql = """
            SELECT * FROM interest_rates 
            WHERE account_type = ? AND is_active = true 
            AND minimum_balance <= ? 
            AND (maximum_balance IS NULL OR maximum_balance >= ?)
            AND effective_date <= CURRENT_DATE 
            AND (expiration_date IS NULL OR expiration_date >= CURRENT_DATE)
            ORDER BY minimum_balance DESC 
            LIMIT 1
            """;
        
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{accountType, accountBalance, accountBalance}, 
                (rs, rowNum) -> {
                    InterestRate rate = new InterestRate();
                    rate.setId(rs.getString("id"));
                    rate.setAccountType(rs.getString("account_type"));
                    rate.setRateName(rs.getString("rate_name"));
                    rate.setAnnualPercentageYield(rs.getBigDecimal("annual_percentage_yield"));
                    rate.setInterestRate(rs.getBigDecimal("interest_rate"));
                    rate.setCompoundingFrequency(rs.getString("compounding_frequency"));
                    rate.setMinimumBalance(rs.getBigDecimal("minimum_balance"));
                    rate.setMaximumBalance(rs.getBigDecimal("maximum_balance"));
                    rate.setIsActive(rs.getBoolean("is_active"));
                    return rate;
                });
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal calculateDailyInterestAmount(BigDecimal accountBalance, InterestRate interestRate) {
        // Calculate daily interest based on compounding frequency
        BigDecimal annualRate = interestRate.getInterestRate();
        BigDecimal dailyRate;
        
        switch (interestRate.getCompoundingFrequency().toLowerCase()) {
            case "daily":
                dailyRate = annualRate.divide(new BigDecimal("365"), 10, RoundingMode.HALF_UP);
                break;
            case "monthly":
                dailyRate = annualRate.divide(new BigDecimal("365"), 10, RoundingMode.HALF_UP);
                break;
            case "quarterly":
                dailyRate = annualRate.divide(new BigDecimal("365"), 10, RoundingMode.HALF_UP);
                break;
            case "annually":
                dailyRate = annualRate.divide(new BigDecimal("365"), 10, RoundingMode.HALF_UP);
                break;
            default:
                dailyRate = annualRate.divide(new BigDecimal("365"), 10, RoundingMode.HALF_UP);
        }
        
        return accountBalance.multiply(dailyRate).setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal getYearToDateInterest(String accountId) {
        String sql = """
            SELECT COALESCE(SUM(daily_interest), 0) 
            FROM interest_calculations 
            WHERE account_id = ? 
            AND calculation_date >= DATE_TRUNC('year', CURRENT_DATE)
            AND is_processed = true
            """;
        
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{accountId}, BigDecimal.class);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private List<InterestCalculation> getPendingInterestCalculations(String accountId) {
        String sql = """
            SELECT * FROM interest_calculations 
            WHERE account_id = ? AND is_processed = false
            ORDER BY calculation_date ASC
            """;
        
        return jdbcTemplate.query(sql, new Object[]{accountId}, (rs, rowNum) -> {
            InterestCalculation calculation = new InterestCalculation();
            calculation.setId(rs.getString("id"));
            calculation.setCustomerId(rs.getString("customer_id"));
            calculation.setAccountId(rs.getString("account_id"));
            calculation.setCalculationDate(rs.getDate("calculation_date").toLocalDate());
            calculation.setAccountBalance(rs.getBigDecimal("account_balance"));
            calculation.setInterestRate(rs.getBigDecimal("interest_rate"));
            calculation.setDailyInterest(rs.getBigDecimal("daily_interest"));
            calculation.setMonthlyInterest(rs.getBigDecimal("monthly_interest"));
            calculation.setYearToDateInterest(rs.getBigDecimal("year_to_date_interest"));
            calculation.setCompoundingFrequency(rs.getString("compounding_frequency"));
            calculation.setCalculationMethod(rs.getString("calculation_method"));
            calculation.setIsProcessed(rs.getBoolean("is_processed"));
            return calculation;
        });
    }

    private boolean processInterestCredit(String accountId, BigDecimal interestAmount) {
        // In a real implementation, this would integrate with the ledger service
        // to credit the customer's account
        return true;
    }

    private boolean processInterestCompounding(String accountId, BigDecimal interestAmount) {
        // In a real implementation, this would add the interest to the principal
        // for compound interest calculation
        return true;
    }

    private void recordInterestCalculation(InterestCalculation calculation) {
        String sql = """
            INSERT INTO interest_calculations 
            (id, customer_id, account_id, calculation_date, account_balance, interest_rate, 
             daily_interest, monthly_interest, year_to_date_interest, compounding_frequency, 
             calculation_method, is_processed, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        jdbcTemplate.update(sql,
            UUID.randomUUID().toString(),
            calculation.getCustomerId(),
            calculation.getAccountId(),
            calculation.getCalculationDate(),
            calculation.getAccountBalance(),
            calculation.getInterestRate(),
            calculation.getDailyInterest(),
            calculation.getMonthlyInterest(),
            calculation.getYearToDateInterest(),
            calculation.getCompoundingFrequency(),
            calculation.getCalculationMethod(),
            calculation.getIsProcessed(),
            calculation.getCalculatedAt()
        );
    }

    private void markCalculationsAsProcessed(List<InterestCalculation> calculations) {
        String sql = "UPDATE interest_calculations SET is_processed = true, processed_at = NOW() WHERE id = ?";
        for (InterestCalculation calculation : calculations) {
            jdbcTemplate.update(sql, calculation.getId());
        }
    }

    private void recordInterestPayment(InterestPayment payment) {
        String sql = """
            INSERT INTO interest_payments 
            (id, customer_id, account_id, payment_date, interest_amount, payment_method, 
             status, processed_at, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        jdbcTemplate.update(sql,
            UUID.randomUUID().toString(),
            payment.getCustomerId(),
            payment.getAccountId(),
            payment.getPaymentDate(),
            payment.getInterestAmount(),
            payment.getPaymentMethod(),
            payment.getStatus(),
            payment.getProcessedAt(),
            payment.getStartedAt()
        );
    }

    private LocalDate getPeriodStartDate(String period) {
        switch (period.toLowerCase()) {
            case "daily":
                return LocalDate.now().minusDays(1);
            case "weekly":
                return LocalDate.now().minusWeeks(1);
            case "monthly":
                return LocalDate.now().minusMonths(1);
            case "quarterly":
                return LocalDate.now().minusMonths(3);
            case "annually":
                return LocalDate.now().minusYears(1);
            default:
                return LocalDate.now().minusMonths(1);
        }
    }

    // Data classes
    public static class InterestCalculation {
        private String id;
        private String customerId;
        private String accountId;
        private LocalDate calculationDate;
        private BigDecimal accountBalance;
        private String accountType;
        private BigDecimal interestRate;
        private BigDecimal dailyInterest;
        private BigDecimal monthlyInterest;
        private BigDecimal yearToDateInterest;
        private String compoundingFrequency;
        private String calculationMethod;
        private boolean isProcessed;
        private String status;
        private String errorMessage;
        private LocalDateTime calculatedAt;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        public String getAccountId() { return accountId; }
        public void setAccountId(String accountId) { this.accountId = accountId; }
        public LocalDate getCalculationDate() { return calculationDate; }
        public void setCalculationDate(LocalDate calculationDate) { this.calculationDate = calculationDate; }
        public BigDecimal getAccountBalance() { return accountBalance; }
        public void setAccountBalance(BigDecimal accountBalance) { this.accountBalance = accountBalance; }
        public String getAccountType() { return accountType; }
        public void setAccountType(String accountType) { this.accountType = accountType; }
        public BigDecimal getInterestRate() { return interestRate; }
        public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }
        public BigDecimal getDailyInterest() { return dailyInterest; }
        public void setDailyInterest(BigDecimal dailyInterest) { this.dailyInterest = dailyInterest; }
        public BigDecimal getMonthlyInterest() { return monthlyInterest; }
        public void setMonthlyInterest(BigDecimal monthlyInterest) { this.monthlyInterest = monthlyInterest; }
        public BigDecimal getYearToDateInterest() { return yearToDateInterest; }
        public void setYearToDateInterest(BigDecimal yearToDateInterest) { this.yearToDateInterest = yearToDateInterest; }
        public String getCompoundingFrequency() { return compoundingFrequency; }
        public void setCompoundingFrequency(String compoundingFrequency) { this.compoundingFrequency = compoundingFrequency; }
        public String getCalculationMethod() { return calculationMethod; }
        public void setCalculationMethod(String calculationMethod) { this.calculationMethod = calculationMethod; }
        public boolean getIsProcessed() { return isProcessed; }
        public void setIsProcessed(boolean isProcessed) { this.isProcessed = isProcessed; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public LocalDateTime getCalculatedAt() { return calculatedAt; }
        public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
    }

    public static class InterestRate {
        private String id;
        private String accountType;
        private String rateName;
        private BigDecimal annualPercentageYield;
        private BigDecimal interestRate;
        private String compoundingFrequency;
        private BigDecimal minimumBalance;
        private BigDecimal maximumBalance;
        private boolean isActive;
        private LocalDate effectiveDate;
        private LocalDate expirationDate;
        private String errorMessage;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getAccountType() { return accountType; }
        public void setAccountType(String accountType) { this.accountType = accountType; }
        public String getRateName() { return rateName; }
        public void setRateName(String rateName) { this.rateName = rateName; }
        public BigDecimal getAnnualPercentageYield() { return annualPercentageYield; }
        public void setAnnualPercentageYield(BigDecimal annualPercentageYield) { this.annualPercentageYield = annualPercentageYield; }
        public BigDecimal getInterestRate() { return interestRate; }
        public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }
        public String getCompoundingFrequency() { return compoundingFrequency; }
        public void setCompoundingFrequency(String compoundingFrequency) { this.compoundingFrequency = compoundingFrequency; }
        public BigDecimal getMinimumBalance() { return minimumBalance; }
        public void setMinimumBalance(BigDecimal minimumBalance) { this.minimumBalance = minimumBalance; }
        public BigDecimal getMaximumBalance() { return maximumBalance; }
        public void setMaximumBalance(BigDecimal maximumBalance) { this.maximumBalance = maximumBalance; }
        public boolean isActive() { return isActive; }
        public void setIsActive(boolean isActive) { this.isActive = isActive; }
        public LocalDate getEffectiveDate() { return effectiveDate; }
        public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
        public LocalDate getExpirationDate() { return expirationDate; }
        public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class InterestPayment {
        private String id;
        private String customerId;
        private String accountId;
        private LocalDate paymentDate;
        private BigDecimal interestAmount;
        private String paymentMethod;
        private String status;
        private LocalDateTime startedAt;
        private LocalDateTime processedAt;
        private String errorMessage;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        public String getAccountId() { return accountId; }
        public void setAccountId(String accountId) { this.accountId = accountId; }
        public LocalDate getPaymentDate() { return paymentDate; }
        public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
        public BigDecimal getInterestAmount() { return interestAmount; }
        public void setInterestAmount(BigDecimal interestAmount) { this.interestAmount = interestAmount; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getStartedAt() { return startedAt; }
        public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
        public LocalDateTime getProcessedAt() { return processedAt; }
        public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class InterestSummary {
        private String customerId;
        private String period;
        private LocalDateTime generatedAt;
        private List<AccountTypeSummary> accountTypeSummaries;
        private BigDecimal totalInterest;
        private int totalCalculations;
        private String errorMessage;

        // Getters and setters
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
        public List<AccountTypeSummary> getAccountTypeSummaries() { return accountTypeSummaries; }
        public void setAccountTypeSummaries(List<AccountTypeSummary> accountTypeSummaries) { this.accountTypeSummaries = accountTypeSummaries; }
        public BigDecimal getTotalInterest() { return totalInterest; }
        public void setTotalInterest(BigDecimal totalInterest) { this.totalInterest = totalInterest; }
        public int getTotalCalculations() { return totalCalculations; }
        public void setTotalCalculations(int totalCalculations) { this.totalCalculations = totalCalculations; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class AccountTypeSummary {
        private String accountType;
        private int totalCalculations;
        private BigDecimal totalDailyInterest;
        private BigDecimal totalMonthlyInterest;
        private BigDecimal totalYtdInterest;
        private BigDecimal averageRate;
        private BigDecimal maxRate;
        private BigDecimal minRate;

        // Getters and setters
        public String getAccountType() { return accountType; }
        public void setAccountType(String accountType) { this.accountType = accountType; }
        public int getTotalCalculations() { return totalCalculations; }
        public void setTotalCalculations(int totalCalculations) { this.totalCalculations = totalCalculations; }
        public BigDecimal getTotalDailyInterest() { return totalDailyInterest; }
        public void setTotalDailyInterest(BigDecimal totalDailyInterest) { this.totalDailyInterest = totalDailyInterest; }
        public BigDecimal getTotalMonthlyInterest() { return totalMonthlyInterest; }
        public void setTotalMonthlyInterest(BigDecimal totalMonthlyInterest) { this.totalMonthlyInterest = totalMonthlyInterest; }
        public BigDecimal getTotalYtdInterest() { return totalYtdInterest; }
        public void setTotalYtdInterest(BigDecimal totalYtdInterest) { this.totalYtdInterest = totalYtdInterest; }
        public BigDecimal getAverageRate() { return averageRate; }
        public void setAverageRate(BigDecimal averageRate) { this.averageRate = averageRate; }
        public BigDecimal getMaxRate() { return maxRate; }
        public void setMaxRate(BigDecimal maxRate) { this.maxRate = maxRate; }
        public BigDecimal getMinRate() { return minRate; }
        public void setMinRate(BigDecimal minRate) { this.minRate = minRate; }
    }
}

