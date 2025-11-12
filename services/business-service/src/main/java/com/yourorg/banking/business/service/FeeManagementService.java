package com.yourorg.banking.business.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class FeeManagementService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Calculate transaction fee
     */
    public FeeCalculation calculateTransactionFee(String customerId, String accountId, String transactionId, 
                                                BigDecimal transactionAmount, String feeType) {
        FeeCalculation calculation = new FeeCalculation();
        calculation.setCustomerId(customerId);
        calculation.setAccountId(accountId);
        calculation.setTransactionId(transactionId);
        calculation.setTransactionAmount(transactionAmount);
        calculation.setFeeType(feeType);
        calculation.setCalculatedAt(LocalDateTime.now());

        try {
            // Get fee schedule
            FeeSchedule feeSchedule = getFeeSchedule(feeType);
            if (feeSchedule == null) {
                calculation.setFeeAmount(BigDecimal.ZERO);
                calculation.setStatus("no_fee_schedule");
                return calculation;
            }

            // Calculate fee based on structure
            BigDecimal feeAmount = BigDecimal.ZERO;
            String calculationMethod = "";

            switch (feeSchedule.getFeeStructure()) {
                case "fixed":
                    feeAmount = feeSchedule.getBaseAmount();
                    calculationMethod = "Fixed fee: " + feeSchedule.getBaseAmount();
                    break;
                case "percentage":
                    feeAmount = transactionAmount.multiply(feeSchedule.getPercentageRate());
                    calculationMethod = String.format("Percentage: %.4f%% of %s", 
                        feeSchedule.getPercentageRate().multiply(new BigDecimal("100")), transactionAmount);
                    break;
                case "tiered":
                    feeAmount = calculateTieredFee(transactionAmount, feeSchedule);
                    calculationMethod = "Tiered calculation based on amount";
                    break;
            }

            // Apply minimum and maximum limits
            if (feeAmount.compareTo(feeSchedule.getMinimumFee()) < 0) {
                feeAmount = feeSchedule.getMinimumFee();
                calculationMethod += " (applied minimum fee)";
            }
            if (feeAmount.compareTo(feeSchedule.getMaximumFee()) > 0) {
                feeAmount = feeSchedule.getMaximumFee();
                calculationMethod += " (applied maximum fee)";
            }

            calculation.setFeeAmount(feeAmount);
            calculation.setBaseAmount(transactionAmount);
            calculation.setFeeRate(feeSchedule.getPercentageRate());
            calculation.setCalculationMethod(calculationMethod);
            calculation.setStatus("calculated");

            // Record fee transaction
            recordFeeTransaction(calculation);

        } catch (Exception e) {
            calculation.setStatus("error");
            calculation.setErrorMessage(e.getMessage());
        }

        return calculation;
    }

    /**
     * Collect fee
     */
    @Transactional
    public FeeCollection collectFee(String feeTransactionId) {
        FeeCollection collection = new FeeCollection();
        collection.setFeeTransactionId(feeTransactionId);
        collection.setStatus("collecting");
        collection.setStartedAt(LocalDateTime.now());

        try {
            // Get fee transaction
            FeeTransaction feeTransaction = getFeeTransaction(feeTransactionId);
            if (feeTransaction == null) {
                collection.setStatus("failed");
                collection.setErrorMessage("Fee transaction not found");
                return collection;
            }

            // Check if already collected
            if ("collected".equals(feeTransaction.getStatus())) {
                collection.setStatus("already_collected");
                collection.setErrorMessage("Fee already collected");
                return collection;
            }

            // Process fee collection
            boolean success = processFeeCollection(feeTransaction);
            
            if (success) {
                // Update fee transaction status
                updateFeeTransactionStatus(feeTransactionId, "collected", LocalDateTime.now());
                collection.setStatus("collected");
                collection.setCollectedAt(LocalDateTime.now());
                collection.setFeeAmount(feeTransaction.getFeeAmount());
            } else {
                collection.setStatus("failed");
                collection.setErrorMessage("Fee collection failed");
            }

        } catch (Exception e) {
            collection.setStatus("failed");
            collection.setErrorMessage(e.getMessage());
        }

        return collection;
    }

    /**
     * Waive fee
     */
    @Transactional
    public FeeWaiver waiveFee(String feeTransactionId, String reason) {
        FeeWaiver waiver = new FeeWaiver();
        waiver.setFeeTransactionId(feeTransactionId);
        waiver.setReason(reason);
        waiver.setStatus("waiving");
        waiver.setStartedAt(LocalDateTime.now());

        try {
            // Get fee transaction
            FeeTransaction feeTransaction = getFeeTransaction(feeTransactionId);
            if (feeTransaction == null) {
                waiver.setStatus("failed");
                waiver.setErrorMessage("Fee transaction not found");
                return waiver;
            }

            // Check if already processed
            if (!"pending".equals(feeTransaction.getStatus())) {
                waiver.setStatus("failed");
                waiver.setErrorMessage("Fee transaction already processed");
                return waiver;
            }

            // Update fee transaction status
            updateFeeTransactionStatus(feeTransactionId, "waived", LocalDateTime.now());
            updateFeeTransactionWaivedReason(feeTransactionId, reason);

            waiver.setStatus("waived");
            waiver.setWaivedAt(LocalDateTime.now());
            waiver.setFeeAmount(feeTransaction.getFeeAmount());

        } catch (Exception e) {
            waiver.setStatus("failed");
            waiver.setErrorMessage(e.getMessage());
        }

        return waiver;
    }

    /**
     * Refund fee
     */
    @Transactional
    public FeeRefund refundFee(String feeTransactionId, String reason) {
        FeeRefund refund = new FeeRefund();
        refund.setFeeTransactionId(feeTransactionId);
        refund.setReason(reason);
        refund.setStatus("refunding");
        refund.setStartedAt(LocalDateTime.now());

        try {
            // Get fee transaction
            FeeTransaction feeTransaction = getFeeTransaction(feeTransactionId);
            if (feeTransaction == null) {
                refund.setStatus("failed");
                refund.setErrorMessage("Fee transaction not found");
                return refund;
            }

            // Check if fee was collected
            if (!"collected".equals(feeTransaction.getStatus())) {
                refund.setStatus("failed");
                refund.setErrorMessage("Fee not collected, cannot refund");
                return refund;
            }

            // Process refund
            boolean success = processFeeRefund(feeTransaction);
            
            if (success) {
                // Update fee transaction status
                updateFeeTransactionStatus(feeTransactionId, "refunded", LocalDateTime.now());
                updateFeeTransactionRefundedReason(feeTransactionId, reason);
                refund.setStatus("refunded");
                refund.setRefundedAt(LocalDateTime.now());
                refund.setFeeAmount(feeTransaction.getFeeAmount());
            } else {
                refund.setStatus("failed");
                refund.setErrorMessage("Fee refund failed");
            }

        } catch (Exception e) {
            refund.setStatus("failed");
            refund.setErrorMessage(e.getMessage());
        }

        return refund;
    }

    /**
     * Get fee summary
     */
    public FeeSummary getFeeSummary(String customerId, String period) {
        FeeSummary summary = new FeeSummary();
        summary.setCustomerId(customerId);
        summary.setPeriod(period);
        summary.setGeneratedAt(LocalDateTime.now());

        try {
            String sql = """
                SELECT 
                    fee_type,
                    COUNT(*) as total_fees,
                    SUM(fee_amount) as total_amount,
                    AVG(fee_amount) as average_fee,
                    COUNT(CASE WHEN status = 'collected' THEN 1 END) as collected_fees,
                    COUNT(CASE WHEN status = 'waived' THEN 1 END) as waived_fees,
                    COUNT(CASE WHEN status = 'refunded' THEN 1 END) as refunded_fees
                FROM fee_transactions 
                WHERE customer_id = ? AND created_at >= ?
                GROUP BY fee_type
                """;
            
            LocalDateTime startDate = getPeriodStartDate(period);
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, customerId, startDate);
            
            List<FeeTypeSummary> feeTypeSummaries = new ArrayList<>();
            BigDecimal totalFees = BigDecimal.ZERO;
            int totalTransactions = 0;
            
            for (Map<String, Object> row : results) {
                FeeTypeSummary feeTypeSummary = new FeeTypeSummary();
                feeTypeSummary.setFeeType((String) row.get("fee_type"));
                feeTypeSummary.setTotalFees(((Number) row.get("total_fees")).intValue());
                feeTypeSummary.setTotalAmount(((BigDecimal) row.get("total_amount")));
                feeTypeSummary.setAverageFee(((BigDecimal) row.get("average_fee")));
                feeTypeSummary.setCollectedFees(((Number) row.get("collected_fees")).intValue());
                feeTypeSummary.setWaivedFees(((Number) row.get("waived_fees")).intValue());
                feeTypeSummary.setRefundedFees(((Number) row.get("refunded_fees")).intValue());
                
                feeTypeSummaries.add(feeTypeSummary);
                totalFees = totalFees.add(feeTypeSummary.getTotalAmount());
                totalTransactions += feeTypeSummary.getTotalFees();
            }
            
            summary.setFeeTypeSummaries(feeTypeSummaries);
            summary.setTotalFees(totalFees);
            summary.setTotalTransactions(totalTransactions);

        } catch (Exception e) {
            summary.setErrorMessage("Failed to generate fee summary: " + e.getMessage());
        }

        return summary;
    }

    // Private helper methods
    private FeeSchedule getFeeSchedule(String feeType) {
        String sql = """
            SELECT * FROM fee_schedules 
            WHERE fee_type = ? AND is_active = true 
            AND effective_date <= CURRENT_DATE 
            AND (expiration_date IS NULL OR expiration_date >= CURRENT_DATE)
            ORDER BY effective_date DESC 
            LIMIT 1
            """;
        
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{feeType}, (rs, rowNum) -> {
                FeeSchedule schedule = new FeeSchedule();
                schedule.setId(rs.getString("id"));
                schedule.setFeeType(rs.getString("fee_type"));
                schedule.setFeeName(rs.getString("fee_name"));
                schedule.setDescription(rs.getString("description"));
                schedule.setFeeStructure(rs.getString("fee_structure"));
                schedule.setBaseAmount(rs.getBigDecimal("base_amount"));
                schedule.setPercentageRate(rs.getBigDecimal("percentage_rate"));
                schedule.setMinimumFee(rs.getBigDecimal("minimum_fee"));
                schedule.setMaximumFee(rs.getBigDecimal("maximum_fee"));
                schedule.setCurrency(rs.getString("currency"));
                schedule.setIsActive(rs.getBoolean("is_active"));
                return schedule;
            });
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal calculateTieredFee(BigDecimal amount, FeeSchedule schedule) {
        // Simplified tiered calculation
        // In a real implementation, this would use tier_1_balance, tier_1_rate, etc.
        if (amount.compareTo(new BigDecimal("1000")) <= 0) {
            return amount.multiply(schedule.getPercentageRate());
        } else if (amount.compareTo(new BigDecimal("5000")) <= 0) {
            return amount.multiply(schedule.getPercentageRate().multiply(new BigDecimal("0.8")));
        } else {
            return amount.multiply(schedule.getPercentageRate().multiply(new BigDecimal("0.6")));
        }
    }

    private boolean processFeeCollection(FeeTransaction feeTransaction) {
        // In a real implementation, this would integrate with the ledger service
        // to debit the customer's account
        return true;
    }

    private boolean processFeeRefund(FeeTransaction feeTransaction) {
        // In a real implementation, this would integrate with the ledger service
        // to credit the customer's account
        return true;
    }

    private void recordFeeTransaction(FeeCalculation calculation) {
        String sql = """
            INSERT INTO fee_transactions 
            (id, customer_id, account_id, transaction_id, fee_type, fee_amount, base_amount, 
             fee_rate, calculation_method, currency, status, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        jdbcTemplate.update(sql,
            UUID.randomUUID().toString(),
            calculation.getCustomerId(),
            calculation.getAccountId(),
            calculation.getTransactionId(),
            calculation.getFeeType(),
            calculation.getFeeAmount(),
            calculation.getBaseAmount(),
            calculation.getFeeRate(),
            calculation.getCalculationMethod(),
            "USD",
            calculation.getStatus(),
            calculation.getCalculatedAt()
        );
    }

    private FeeTransaction getFeeTransaction(String feeTransactionId) {
        String sql = "SELECT * FROM fee_transactions WHERE id = ?";
        
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{feeTransactionId}, (rs, rowNum) -> {
                FeeTransaction transaction = new FeeTransaction();
                transaction.setId(rs.getString("id"));
                transaction.setCustomerId(rs.getString("customer_id"));
                transaction.setAccountId(rs.getString("account_id"));
                transaction.setTransactionId(rs.getString("transaction_id"));
                transaction.setFeeType(rs.getString("fee_type"));
                transaction.setFeeAmount(rs.getBigDecimal("fee_amount"));
                transaction.setBaseAmount(rs.getBigDecimal("base_amount"));
                transaction.setFeeRate(rs.getBigDecimal("fee_rate"));
                transaction.setCalculationMethod(rs.getString("calculation_method"));
                transaction.setCurrency(rs.getString("currency"));
                transaction.setStatus(rs.getString("status"));
                transaction.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                return transaction;
            });
        } catch (Exception e) {
            return null;
        }
    }

    private void updateFeeTransactionStatus(String feeTransactionId, String status, LocalDateTime timestamp) {
        String sql = "UPDATE fee_transactions SET status = ?, collected_at = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, timestamp, feeTransactionId);
    }

    private void updateFeeTransactionWaivedReason(String feeTransactionId, String reason) {
        String sql = "UPDATE fee_transactions SET waived_reason = ?, waived_at = NOW() WHERE id = ?";
        jdbcTemplate.update(sql, reason, feeTransactionId);
    }

    private void updateFeeTransactionRefundedReason(String feeTransactionId, String reason) {
        String sql = "UPDATE fee_transactions SET refunded_reason = ?, refunded_at = NOW() WHERE id = ?";
        jdbcTemplate.update(sql, reason, feeTransactionId);
    }

    private LocalDateTime getPeriodStartDate(String period) {
        switch (period.toLowerCase()) {
            case "daily":
                return LocalDateTime.now().minusDays(1);
            case "weekly":
                return LocalDateTime.now().minusWeeks(1);
            case "monthly":
                return LocalDateTime.now().minusMonths(1);
            case "quarterly":
                return LocalDateTime.now().minusMonths(3);
            case "annually":
                return LocalDateTime.now().minusYears(1);
            default:
                return LocalDateTime.now().minusMonths(1);
        }
    }

    // Data classes
    public static class FeeCalculation {
        private String customerId;
        private String accountId;
        private String transactionId;
        private BigDecimal transactionAmount;
        private String feeType;
        private BigDecimal feeAmount;
        private BigDecimal baseAmount;
        private BigDecimal feeRate;
        private String calculationMethod;
        private String status;
        private String errorMessage;
        private LocalDateTime calculatedAt;

        // Getters and setters
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        public String getAccountId() { return accountId; }
        public void setAccountId(String accountId) { this.accountId = accountId; }
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        public BigDecimal getTransactionAmount() { return transactionAmount; }
        public void setTransactionAmount(BigDecimal transactionAmount) { this.transactionAmount = transactionAmount; }
        public String getFeeType() { return feeType; }
        public void setFeeType(String feeType) { this.feeType = feeType; }
        public BigDecimal getFeeAmount() { return feeAmount; }
        public void setFeeAmount(BigDecimal feeAmount) { this.feeAmount = feeAmount; }
        public BigDecimal getBaseAmount() { return baseAmount; }
        public void setBaseAmount(BigDecimal baseAmount) { this.baseAmount = baseAmount; }
        public BigDecimal getFeeRate() { return feeRate; }
        public void setFeeRate(BigDecimal feeRate) { this.feeRate = feeRate; }
        public String getCalculationMethod() { return calculationMethod; }
        public void setCalculationMethod(String calculationMethod) { this.calculationMethod = calculationMethod; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public LocalDateTime getCalculatedAt() { return calculatedAt; }
        public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
    }

    public static class FeeSchedule {
        private String id;
        private String feeType;
        private String feeName;
        private String description;
        private String feeStructure;
        private BigDecimal baseAmount;
        private BigDecimal percentageRate;
        private BigDecimal minimumFee;
        private BigDecimal maximumFee;
        private String currency;
        private boolean isActive;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getFeeType() { return feeType; }
        public void setFeeType(String feeType) { this.feeType = feeType; }
        public String getFeeName() { return feeName; }
        public void setFeeName(String feeName) { this.feeName = feeName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getFeeStructure() { return feeStructure; }
        public void setFeeStructure(String feeStructure) { this.feeStructure = feeStructure; }
        public BigDecimal getBaseAmount() { return baseAmount; }
        public void setBaseAmount(BigDecimal baseAmount) { this.baseAmount = baseAmount; }
        public BigDecimal getPercentageRate() { return percentageRate; }
        public void setPercentageRate(BigDecimal percentageRate) { this.percentageRate = percentageRate; }
        public BigDecimal getMinimumFee() { return minimumFee; }
        public void setMinimumFee(BigDecimal minimumFee) { this.minimumFee = minimumFee; }
        public BigDecimal getMaximumFee() { return maximumFee; }
        public void setMaximumFee(BigDecimal maximumFee) { this.maximumFee = maximumFee; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public boolean isActive() { return isActive; }
        public void setIsActive(boolean isActive) { this.isActive = isActive; }
    }

    public static class FeeTransaction {
        private String id;
        private String customerId;
        private String accountId;
        private String transactionId;
        private String feeType;
        private BigDecimal feeAmount;
        private BigDecimal baseAmount;
        private BigDecimal feeRate;
        private String calculationMethod;
        private String currency;
        private String status;
        private LocalDateTime createdAt;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        public String getAccountId() { return accountId; }
        public void setAccountId(String accountId) { this.accountId = accountId; }
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        public String getFeeType() { return feeType; }
        public void setFeeType(String feeType) { this.feeType = feeType; }
        public BigDecimal getFeeAmount() { return feeAmount; }
        public void setFeeAmount(BigDecimal feeAmount) { this.feeAmount = feeAmount; }
        public BigDecimal getBaseAmount() { return baseAmount; }
        public void setBaseAmount(BigDecimal baseAmount) { this.baseAmount = baseAmount; }
        public BigDecimal getFeeRate() { return feeRate; }
        public void setFeeRate(BigDecimal feeRate) { this.feeRate = feeRate; }
        public String getCalculationMethod() { return calculationMethod; }
        public void setCalculationMethod(String calculationMethod) { this.calculationMethod = calculationMethod; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class FeeCollection {
        private String feeTransactionId;
        private String status;
        private LocalDateTime startedAt;
        private LocalDateTime collectedAt;
        private BigDecimal feeAmount;
        private String errorMessage;

        // Getters and setters
        public String getFeeTransactionId() { return feeTransactionId; }
        public void setFeeTransactionId(String feeTransactionId) { this.feeTransactionId = feeTransactionId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getStartedAt() { return startedAt; }
        public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
        public LocalDateTime getCollectedAt() { return collectedAt; }
        public void setCollectedAt(LocalDateTime collectedAt) { this.collectedAt = collectedAt; }
        public BigDecimal getFeeAmount() { return feeAmount; }
        public void setFeeAmount(BigDecimal feeAmount) { this.feeAmount = feeAmount; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class FeeWaiver {
        private String feeTransactionId;
        private String reason;
        private String status;
        private LocalDateTime startedAt;
        private LocalDateTime waivedAt;
        private BigDecimal feeAmount;
        private String errorMessage;

        // Getters and setters
        public String getFeeTransactionId() { return feeTransactionId; }
        public void setFeeTransactionId(String feeTransactionId) { this.feeTransactionId = feeTransactionId; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getStartedAt() { return startedAt; }
        public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
        public LocalDateTime getWaivedAt() { return waivedAt; }
        public void setWaivedAt(LocalDateTime waivedAt) { this.waivedAt = waivedAt; }
        public BigDecimal getFeeAmount() { return feeAmount; }
        public void setFeeAmount(BigDecimal feeAmount) { this.feeAmount = feeAmount; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class FeeRefund {
        private String feeTransactionId;
        private String reason;
        private String status;
        private LocalDateTime startedAt;
        private LocalDateTime refundedAt;
        private BigDecimal feeAmount;
        private String errorMessage;

        // Getters and setters
        public String getFeeTransactionId() { return feeTransactionId; }
        public void setFeeTransactionId(String feeTransactionId) { this.feeTransactionId = feeTransactionId; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getStartedAt() { return startedAt; }
        public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
        public LocalDateTime getRefundedAt() { return refundedAt; }
        public void setRefundedAt(LocalDateTime refundedAt) { this.refundedAt = refundedAt; }
        public BigDecimal getFeeAmount() { return feeAmount; }
        public void setFeeAmount(BigDecimal feeAmount) { this.feeAmount = feeAmount; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class FeeSummary {
        private String customerId;
        private String period;
        private LocalDateTime generatedAt;
        private List<FeeTypeSummary> feeTypeSummaries;
        private BigDecimal totalFees;
        private int totalTransactions;
        private String errorMessage;

        // Getters and setters
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
        public List<FeeTypeSummary> getFeeTypeSummaries() { return feeTypeSummaries; }
        public void setFeeTypeSummaries(List<FeeTypeSummary> feeTypeSummaries) { this.feeTypeSummaries = feeTypeSummaries; }
        public BigDecimal getTotalFees() { return totalFees; }
        public void setTotalFees(BigDecimal totalFees) { this.totalFees = totalFees; }
        public int getTotalTransactions() { return totalTransactions; }
        public void setTotalTransactions(int totalTransactions) { this.totalTransactions = totalTransactions; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class FeeTypeSummary {
        private String feeType;
        private int totalFees;
        private BigDecimal totalAmount;
        private BigDecimal averageFee;
        private int collectedFees;
        private int waivedFees;
        private int refundedFees;

        // Getters and setters
        public String getFeeType() { return feeType; }
        public void setFeeType(String feeType) { this.feeType = feeType; }
        public int getTotalFees() { return totalFees; }
        public void setTotalFees(int totalFees) { this.totalFees = totalFees; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public BigDecimal getAverageFee() { return averageFee; }
        public void setAverageFee(BigDecimal averageFee) { this.averageFee = averageFee; }
        public int getCollectedFees() { return collectedFees; }
        public void setCollectedFees(int collectedFees) { this.collectedFees = collectedFees; }
        public int getWaivedFees() { return waivedFees; }
        public void setWaivedFees(int waivedFees) { this.waivedFees = waivedFees; }
        public int getRefundedFees() { return refundedFees; }
        public void setRefundedFees(int refundedFees) { this.refundedFees = refundedFees; }
    }
}

