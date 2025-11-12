package com.yourorg.banking.business.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class LoyaltyProgramService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Enroll customer in loyalty program
     */
    @Transactional
    public LoyaltyAccount enrollCustomer(String customerId) {
        LoyaltyAccount account = new LoyaltyAccount();
        account.setCustomerId(customerId);
        account.setAccountNumber(generateLoyaltyAccountNumber());
        account.setTotalPoints(0L);
        account.setAvailablePoints(0L);
        account.setLifetimePoints(0L);
        account.setTierLevel("bronze");
        account.setTierPoints(0L);
        account.setNextTierPoints(1000L);
        account.setEnrollmentDate(LocalDate.now());
        account.setLastActivityDate(LocalDate.now());
        account.setIsActive(true);

        try {
            // Check if customer is already enrolled
            if (isCustomerEnrolled(customerId)) {
                account.setErrorMessage("Customer already enrolled in loyalty program");
                return account;
            }

            // Record loyalty account
            recordLoyaltyAccount(account);

            // Award welcome bonus
            awardWelcomeBonus(account);

        } catch (Exception e) {
            account.setErrorMessage("Failed to enroll customer: " + e.getMessage());
        }

        return account;
    }

    /**
     * Earn points for transaction
     */
    @Transactional
    public LoyaltyTransaction earnPoints(String customerId, String transactionId, 
                                       BigDecimal transactionAmount, String transactionType) {
        LoyaltyTransaction transaction = new LoyaltyTransaction();
        transaction.setLoyaltyAccountId(getLoyaltyAccountId(customerId));
        transaction.setTransactionType("earned");
        transaction.setTransactionReference(transactionId);
        transaction.setDescription("Points earned for " + transactionType + " transaction");
        transaction.setStatus("pending");
        transaction.setCreatedAt(LocalDateTime.now());

        try {
            // Get loyalty account
            LoyaltyAccount account = getLoyaltyAccount(customerId);
            if (account == null) {
                transaction.setStatus("failed");
                transaction.setErrorMessage("Loyalty account not found");
                return transaction;
            }

            // Calculate points based on transaction amount and type
            long pointsEarned = calculatePointsEarned(transactionAmount, transactionType);
            transaction.setPointsAmount(pointsEarned);

            // Set expiration date (24 months from now)
            transaction.setExpirationDate(LocalDate.now().plusMonths(24));

            // Record transaction
            recordLoyaltyTransaction(transaction);

            // Update account points
            updateAccountPoints(account.getId(), pointsEarned);

            transaction.setStatus("completed");
            transaction.setProcessedAt(LocalDateTime.now());

        } catch (Exception e) {
            transaction.setStatus("failed");
            transaction.setErrorMessage(e.getMessage());
        }

        return transaction;
    }

    /**
     * Redeem points for reward
     */
    @Transactional
    public LoyaltyRedemption redeemPoints(String customerId, String rewardId, long pointsToRedeem) {
        LoyaltyRedemption redemption = new LoyaltyRedemption();
        redemption.setLoyaltyAccountId(getLoyaltyAccountId(customerId));
        redemption.setRewardId(rewardId);
        redemption.setPointsUsed(pointsToRedeem);
        redemption.setStatus("pending");
        redemption.setCreatedAt(LocalDateTime.now());

        try {
            // Get loyalty account
            LoyaltyAccount account = getLoyaltyAccount(customerId);
            if (account == null) {
                redemption.setStatus("failed");
                redemption.setErrorMessage("Loyalty account not found");
                return redemption;
            }

            // Get reward details
            LoyaltyReward reward = getLoyaltyReward(rewardId);
            if (reward == null) {
                redemption.setStatus("failed");
                redemption.setErrorMessage("Reward not found");
                return redemption;
            }

            // Validate redemption
            if (!validateRedemption(account, reward, pointsToRedeem)) {
                redemption.setStatus("failed");
                redemption.setErrorMessage("Invalid redemption request");
                return redemption;
            }

            // Calculate cash value
            BigDecimal cashValue = calculateCashValue(pointsToRedeem);
            redemption.setCashValue(cashValue);

            // Record redemption
            recordLoyaltyRedemption(redemption);

            // Update account points
            updateAccountPoints(account.getId(), -pointsToRedeem);

            // Update reward usage
            updateRewardUsage(rewardId);

            redemption.setStatus("completed");
            redemption.setProcessedAt(LocalDateTime.now());

        } catch (Exception e) {
            redemption.setStatus("failed");
            redemption.setErrorMessage(e.getMessage());
        }

        return redemption;
    }

    /**
     * Get loyalty account details
     */
    public LoyaltyAccount getLoyaltyAccount(String customerId) {
        String sql = "SELECT * FROM loyalty_accounts WHERE customer_id = ? AND is_active = true";
        
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{customerId}, (rs, rowNum) -> {
                LoyaltyAccount account = new LoyaltyAccount();
                account.setId(rs.getString("id"));
                account.setCustomerId(rs.getString("customer_id"));
                account.setAccountNumber(rs.getString("account_number"));
                account.setTotalPoints(rs.getLong("total_points"));
                account.setAvailablePoints(rs.getLong("available_points"));
                account.setLifetimePoints(rs.getLong("lifetime_points"));
                account.setTierLevel(rs.getString("tier_level"));
                account.setTierPoints(rs.getLong("tier_points"));
                account.setNextTierPoints(rs.getLong("next_tier_points"));
                account.setEnrollmentDate(rs.getDate("enrollment_date").toLocalDate());
                account.setLastActivityDate(rs.getDate("last_activity_date") != null ? 
                    rs.getDate("last_activity_date").toLocalDate() : null);
                account.setIsActive(rs.getBoolean("is_active"));
                return account;
            });
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get available rewards
     */
    public List<LoyaltyReward> getAvailableRewards() {
        String sql = """
            SELECT * FROM loyalty_rewards 
            WHERE is_active = true 
            AND start_date <= CURRENT_DATE 
            AND (end_date IS NULL OR end_date >= CURRENT_DATE)
            AND (max_redemptions IS NULL OR current_redemptions < max_redemptions)
            ORDER BY points_required ASC
            """;
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            LoyaltyReward reward = new LoyaltyReward();
            reward.setId(rs.getString("id"));
            reward.setRewardName(rs.getString("reward_name"));
            reward.setDescription(rs.getString("description"));
            reward.setRewardType(rs.getString("reward_type"));
            reward.setPointsRequired(rs.getLong("points_required"));
            reward.setCashValue(rs.getBigDecimal("cash_value"));
            reward.setIsActive(rs.getBoolean("is_active"));
            reward.setStartDate(rs.getDate("start_date").toLocalDate());
            reward.setEndDate(rs.getDate("end_date") != null ? 
                rs.getDate("end_date").toLocalDate() : null);
            reward.setMaxRedemptions(rs.getInt("max_redemptions"));
            reward.setCurrentRedemptions(rs.getInt("current_redemptions"));
            return reward;
        });
    }

    /**
     * Get loyalty summary
     */
    public LoyaltySummary getLoyaltySummary(String customerId) {
        LoyaltySummary summary = new LoyaltySummary();
        summary.setCustomerId(customerId);
        summary.setGeneratedAt(LocalDateTime.now());

        try {
            // Get account details
            LoyaltyAccount account = getLoyaltyAccount(customerId);
            if (account == null) {
                summary.setErrorMessage("Loyalty account not found");
                return summary;
            }

            summary.setTotalPoints(account.getTotalPoints());
            summary.setAvailablePoints(account.getAvailablePoints());
            summary.setLifetimePoints(account.getLifetimePoints());
            summary.setTierLevel(account.getTierLevel());
            summary.setTierPoints(account.getTierPoints());
            summary.setNextTierPoints(account.getNextTierPoints());

            // Get transaction history
            List<LoyaltyTransaction> transactions = getLoyaltyTransactions(customerId, 30);
            summary.setRecentTransactions(transactions);

            // Get redemption history
            List<LoyaltyRedemption> redemptions = getLoyaltyRedemptions(customerId, 30);
            summary.setRecentRedemptions(redemptions);

            // Calculate tier progress
            summary.setTierProgress(calculateTierProgress(account));

        } catch (Exception e) {
            summary.setErrorMessage("Failed to generate loyalty summary: " + e.getMessage());
        }

        return summary;
    }

    /**
     * Update tier level
     */
    @Transactional
    public LoyaltyAccount updateTierLevel(String customerId) {
        try {
            LoyaltyAccount account = getLoyaltyAccount(customerId);
            if (account == null) {
                return null;
            }

            String newTierLevel = calculateTierLevel(account.getLifetimePoints());
            if (!newTierLevel.equals(account.getTierLevel())) {
                // Update tier level
                updateAccountTierLevel(account.getId(), newTierLevel);
                account.setTierLevel(newTierLevel);
                
                // Award tier bonus
                awardTierBonus(account, newTierLevel);
            }

            return account;

        } catch (Exception e) {
            return null;
        }
    }

    // Private helper methods
    private boolean isCustomerEnrolled(String customerId) {
        String sql = "SELECT COUNT(*) FROM loyalty_accounts WHERE customer_id = ? AND is_active = true";
        return jdbcTemplate.queryForObject(sql, new Object[]{customerId}, Integer.class) > 0;
    }

    private String generateLoyaltyAccountNumber() {
        return "LY" + System.currentTimeMillis();
    }

    private long calculatePointsEarned(BigDecimal transactionAmount, String transactionType) {
        // Base points per dollar
        long basePoints = transactionAmount.multiply(new BigDecimal("1.0")).longValue();
        
        // Bonus multiplier based on transaction type
        double multiplier = 1.0;
        switch (transactionType.toLowerCase()) {
            case "purchase":
                multiplier = 1.0;
                break;
            case "bill_pay":
                multiplier = 1.5;
                break;
            case "transfer":
                multiplier = 0.5;
                break;
            case "investment":
                multiplier = 2.0;
                break;
            default:
                multiplier = 1.0;
        }
        
        return (long) (basePoints * multiplier);
    }

    private BigDecimal calculateCashValue(long points) {
        // 1 point = $0.01
        return new BigDecimal(points).multiply(new BigDecimal("0.01"));
    }

    private boolean validateRedemption(LoyaltyAccount account, LoyaltyReward reward, long pointsToRedeem) {
        // Check if account has enough points
        if (account.getAvailablePoints() < pointsToRedeem) {
            return false;
        }
        
        // Check if reward is available
        if (!reward.isActive()) {
            return false;
        }
        
        // Check if reward has reached max redemptions
        if (reward.getMaxRedemptions() != null && 
            reward.getCurrentRedemptions() >= reward.getMaxRedemptions()) {
            return false;
        }
        
        // Check if points match reward requirement
        if (pointsToRedeem != reward.getPointsRequired()) {
            return false;
        }
        
        return true;
    }

    private String calculateTierLevel(long lifetimePoints) {
        if (lifetimePoints >= 50000) {
            return "platinum";
        } else if (lifetimePoints >= 25000) {
            return "gold";
        } else if (lifetimePoints >= 10000) {
            return "silver";
        } else {
            return "bronze";
        }
    }

    private double calculateTierProgress(LoyaltyAccount account) {
        long currentPoints = account.getTierPoints();
        long nextTierPoints = account.getNextTierPoints();
        
        if (nextTierPoints == 0) {
            return 100.0; // Already at highest tier
        }
        
        return (double) currentPoints / nextTierPoints * 100.0;
    }

    private void awardWelcomeBonus(LoyaltyAccount account) {
        // Award 1000 welcome bonus points
        LoyaltyTransaction transaction = new LoyaltyTransaction();
        transaction.setLoyaltyAccountId(account.getId());
        transaction.setTransactionType("earned");
        transaction.setPointsAmount(1000L);
        transaction.setDescription("Welcome bonus");
        transaction.setStatus("completed");
        transaction.setProcessedAt(LocalDateTime.now());
        transaction.setCreatedAt(LocalDateTime.now());
        
        recordLoyaltyTransaction(transaction);
        updateAccountPoints(account.getId(), 1000L);
    }

    private void awardTierBonus(LoyaltyAccount account, String newTierLevel) {
        long bonusPoints = 0;
        switch (newTierLevel) {
            case "silver":
                bonusPoints = 2000;
                break;
            case "gold":
                bonusPoints = 5000;
                break;
            case "platinum":
                bonusPoints = 10000;
                break;
        }
        
        if (bonusPoints > 0) {
            LoyaltyTransaction transaction = new LoyaltyTransaction();
            transaction.setLoyaltyAccountId(account.getId());
            transaction.setTransactionType("earned");
            transaction.setPointsAmount(bonusPoints);
            transaction.setDescription("Tier upgrade bonus - " + newTierLevel);
            transaction.setStatus("completed");
            transaction.setProcessedAt(LocalDateTime.now());
            transaction.setCreatedAt(LocalDateTime.now());
            
            recordLoyaltyTransaction(transaction);
            updateAccountPoints(account.getId(), bonusPoints);
        }
    }

    private String getLoyaltyAccountId(String customerId) {
        String sql = "SELECT id FROM loyalty_accounts WHERE customer_id = ? AND is_active = true";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{customerId}, String.class);
        } catch (Exception e) {
            return null;
        }
    }

    private LoyaltyReward getLoyaltyReward(String rewardId) {
        String sql = "SELECT * FROM loyalty_rewards WHERE id = ?";
        
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{rewardId}, (rs, rowNum) -> {
                LoyaltyReward reward = new LoyaltyReward();
                reward.setId(rs.getString("id"));
                reward.setRewardName(rs.getString("reward_name"));
                reward.setDescription(rs.getString("description"));
                reward.setRewardType(rs.getString("reward_type"));
                reward.setPointsRequired(rs.getLong("points_required"));
                reward.setCashValue(rs.getBigDecimal("cash_value"));
                reward.setIsActive(rs.getBoolean("is_active"));
                reward.setStartDate(rs.getDate("start_date").toLocalDate());
                reward.setEndDate(rs.getDate("end_date") != null ? 
                    rs.getDate("end_date").toLocalDate() : null);
                reward.setMaxRedemptions(rs.getInt("max_redemptions"));
                reward.setCurrentRedemptions(rs.getInt("current_redemptions"));
                return reward;
            });
        } catch (Exception e) {
            return null;
        }
    }

    private List<LoyaltyTransaction> getLoyaltyTransactions(String customerId, int days) {
        String sql = """
            SELECT lt.* FROM loyalty_transactions lt
            JOIN loyalty_accounts la ON lt.loyalty_account_id = la.id
            WHERE la.customer_id = ? 
            AND lt.created_at >= CURRENT_DATE - INTERVAL '%d days'
            ORDER BY lt.created_at DESC
            """.formatted(days);
        
        return jdbcTemplate.query(sql, new Object[]{customerId}, (rs, rowNum) -> {
            LoyaltyTransaction transaction = new LoyaltyTransaction();
            transaction.setId(rs.getString("id"));
            transaction.setLoyaltyAccountId(rs.getString("loyalty_account_id"));
            transaction.setTransactionType(rs.getString("transaction_type"));
            transaction.setPointsAmount(rs.getLong("points_amount"));
            transaction.setTransactionReference(rs.getString("transaction_reference"));
            transaction.setDescription(rs.getString("description"));
            transaction.setExpirationDate(rs.getDate("expiration_date") != null ? 
                rs.getDate("expiration_date").toLocalDate() : null);
            transaction.setStatus(rs.getString("status"));
            transaction.setProcessedAt(rs.getTimestamp("processed_at") != null ? 
                rs.getTimestamp("processed_at").toLocalDateTime() : null);
            transaction.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return transaction;
        });
    }

    private List<LoyaltyRedemption> getLoyaltyRedemptions(String customerId, int days) {
        String sql = """
            SELECT lr.* FROM loyalty_redemptions lr
            JOIN loyalty_accounts la ON lr.loyalty_account_id = la.id
            WHERE la.customer_id = ? 
            AND lr.created_at >= CURRENT_DATE - INTERVAL '%d days'
            ORDER BY lr.created_at DESC
            """.formatted(days);
        
        return jdbcTemplate.query(sql, new Object[]{customerId}, (rs, rowNum) -> {
            LoyaltyRedemption redemption = new LoyaltyRedemption();
            redemption.setId(rs.getString("id"));
            redemption.setLoyaltyAccountId(rs.getString("loyalty_account_id"));
            redemption.setRewardId(rs.getString("reward_id"));
            redemption.setPointsUsed(rs.getLong("points_used"));
            redemption.setCashValue(rs.getBigDecimal("cash_value"));
            redemption.setStatus(rs.getString("status"));
            redemption.setProcessedAt(rs.getTimestamp("processed_at") != null ? 
                rs.getTimestamp("processed_at").toLocalDateTime() : null);
            redemption.setCompletedAt(rs.getTimestamp("completed_at") != null ? 
                rs.getTimestamp("completed_at").toLocalDateTime() : null);
            redemption.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return redemption;
        });
    }

    private void recordLoyaltyAccount(LoyaltyAccount account) {
        String sql = """
            INSERT INTO loyalty_accounts 
            (id, customer_id, account_number, total_points, available_points, lifetime_points, 
             tier_level, tier_points, next_tier_points, enrollment_date, last_activity_date, is_active)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        jdbcTemplate.update(sql,
            UUID.randomUUID().toString(),
            account.getCustomerId(),
            account.getAccountNumber(),
            account.getTotalPoints(),
            account.getAvailablePoints(),
            account.getLifetimePoints(),
            account.getTierLevel(),
            account.getTierPoints(),
            account.getNextTierPoints(),
            account.getEnrollmentDate(),
            account.getLastActivityDate(),
            account.getIsActive()
        );
    }

    private void recordLoyaltyTransaction(LoyaltyTransaction transaction) {
        String sql = """
            INSERT INTO loyalty_transactions 
            (id, loyalty_account_id, transaction_type, points_amount, transaction_reference, 
             description, expiration_date, status, processed_at, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        jdbcTemplate.update(sql,
            UUID.randomUUID().toString(),
            transaction.getLoyaltyAccountId(),
            transaction.getTransactionType(),
            transaction.getPointsAmount(),
            transaction.getTransactionReference(),
            transaction.getDescription(),
            transaction.getExpirationDate(),
            transaction.getStatus(),
            transaction.getProcessedAt(),
            transaction.getCreatedAt()
        );
    }

    private void recordLoyaltyRedemption(LoyaltyRedemption redemption) {
        String sql = """
            INSERT INTO loyalty_redemptions 
            (id, loyalty_account_id, reward_id, points_used, cash_value, status, 
             processed_at, completed_at, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        jdbcTemplate.update(sql,
            UUID.randomUUID().toString(),
            redemption.getLoyaltyAccountId(),
            redemption.getRewardId(),
            redemption.getPointsUsed(),
            redemption.getCashValue(),
            redemption.getStatus(),
            redemption.getProcessedAt(),
            redemption.getCompletedAt(),
            redemption.getCreatedAt()
        );
    }

    private void updateAccountPoints(String accountId, long pointsChange) {
        String sql = """
            UPDATE loyalty_accounts 
            SET total_points = total_points + ?, 
                available_points = available_points + ?, 
                lifetime_points = lifetime_points + ABS(?),
                tier_points = tier_points + ABS(?),
                last_activity_date = CURRENT_DATE
            WHERE id = ?
            """;
        
        jdbcTemplate.update(sql, pointsChange, pointsChange, pointsChange, pointsChange, accountId);
    }

    private void updateAccountTierLevel(String accountId, String newTierLevel) {
        String sql = "UPDATE loyalty_accounts SET tier_level = ? WHERE id = ?";
        jdbcTemplate.update(sql, newTierLevel, accountId);
    }

    private void updateRewardUsage(String rewardId) {
        String sql = "UPDATE loyalty_rewards SET current_redemptions = current_redemptions + 1 WHERE id = ?";
        jdbcTemplate.update(sql, rewardId);
    }

    // Data classes
    public static class LoyaltyAccount {
        private String id;
        private String customerId;
        private String accountNumber;
        private long totalPoints;
        private long availablePoints;
        private long lifetimePoints;
        private String tierLevel;
        private long tierPoints;
        private long nextTierPoints;
        private LocalDate enrollmentDate;
        private LocalDate lastActivityDate;
        private boolean isActive;
        private String errorMessage;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
        public long getTotalPoints() { return totalPoints; }
        public void setTotalPoints(long totalPoints) { this.totalPoints = totalPoints; }
        public long getAvailablePoints() { return availablePoints; }
        public void setAvailablePoints(long availablePoints) { this.availablePoints = availablePoints; }
        public long getLifetimePoints() { return lifetimePoints; }
        public void setLifetimePoints(long lifetimePoints) { this.lifetimePoints = lifetimePoints; }
        public String getTierLevel() { return tierLevel; }
        public void setTierLevel(String tierLevel) { this.tierLevel = tierLevel; }
        public long getTierPoints() { return tierPoints; }
        public void setTierPoints(long tierPoints) { this.tierPoints = tierPoints; }
        public long getNextTierPoints() { return nextTierPoints; }
        public void setNextTierPoints(long nextTierPoints) { this.nextTierPoints = nextTierPoints; }
        public LocalDate getEnrollmentDate() { return enrollmentDate; }
        public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }
        public LocalDate getLastActivityDate() { return lastActivityDate; }
        public void setLastActivityDate(LocalDate lastActivityDate) { this.lastActivityDate = lastActivityDate; }
        public boolean getIsActive() { return isActive; }
        public void setIsActive(boolean isActive) { this.isActive = isActive; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class LoyaltyTransaction {
        private String id;
        private String loyaltyAccountId;
        private String transactionType;
        private long pointsAmount;
        private String transactionReference;
        private String description;
        private LocalDate expirationDate;
        private String status;
        private LocalDateTime processedAt;
        private LocalDateTime createdAt;
        private String errorMessage;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getLoyaltyAccountId() { return loyaltyAccountId; }
        public void setLoyaltyAccountId(String loyaltyAccountId) { this.loyaltyAccountId = loyaltyAccountId; }
        public String getTransactionType() { return transactionType; }
        public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
        public long getPointsAmount() { return pointsAmount; }
        public void setPointsAmount(long pointsAmount) { this.pointsAmount = pointsAmount; }
        public String getTransactionReference() { return transactionReference; }
        public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public LocalDate getExpirationDate() { return expirationDate; }
        public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getProcessedAt() { return processedAt; }
        public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class LoyaltyReward {
        private String id;
        private String rewardName;
        private String description;
        private String rewardType;
        private long pointsRequired;
        private BigDecimal cashValue;
        private boolean isActive;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer maxRedemptions;
        private int currentRedemptions;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getRewardName() { return rewardName; }
        public void setRewardName(String rewardName) { this.rewardName = rewardName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getRewardType() { return rewardType; }
        public void setRewardType(String rewardType) { this.rewardType = rewardType; }
        public long getPointsRequired() { return pointsRequired; }
        public void setPointsRequired(long pointsRequired) { this.pointsRequired = pointsRequired; }
        public BigDecimal getCashValue() { return cashValue; }
        public void setCashValue(BigDecimal cashValue) { this.cashValue = cashValue; }
        public boolean isActive() { return isActive; }
        public void setIsActive(boolean isActive) { this.isActive = isActive; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public Integer getMaxRedemptions() { return maxRedemptions; }
        public void setMaxRedemptions(Integer maxRedemptions) { this.maxRedemptions = maxRedemptions; }
        public int getCurrentRedemptions() { return currentRedemptions; }
        public void setCurrentRedemptions(int currentRedemptions) { this.currentRedemptions = currentRedemptions; }
    }

    public static class LoyaltyRedemption {
        private String id;
        private String loyaltyAccountId;
        private String rewardId;
        private long pointsUsed;
        private BigDecimal cashValue;
        private String status;
        private LocalDateTime processedAt;
        private LocalDateTime completedAt;
        private LocalDateTime createdAt;
        private String errorMessage;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getLoyaltyAccountId() { return loyaltyAccountId; }
        public void setLoyaltyAccountId(String loyaltyAccountId) { this.loyaltyAccountId = loyaltyAccountId; }
        public String getRewardId() { return rewardId; }
        public void setRewardId(String rewardId) { this.rewardId = rewardId; }
        public long getPointsUsed() { return pointsUsed; }
        public void setPointsUsed(long pointsUsed) { this.pointsUsed = pointsUsed; }
        public BigDecimal getCashValue() { return cashValue; }
        public void setCashValue(BigDecimal cashValue) { this.cashValue = cashValue; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getProcessedAt() { return processedAt; }
        public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
        public LocalDateTime getCompletedAt() { return completedAt; }
        public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class LoyaltySummary {
        private String customerId;
        private LocalDateTime generatedAt;
        private long totalPoints;
        private long availablePoints;
        private long lifetimePoints;
        private String tierLevel;
        private long tierPoints;
        private long nextTierPoints;
        private double tierProgress;
        private List<LoyaltyTransaction> recentTransactions;
        private List<LoyaltyRedemption> recentRedemptions;
        private String errorMessage;

        // Getters and setters
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
        public long getTotalPoints() { return totalPoints; }
        public void setTotalPoints(long totalPoints) { this.totalPoints = totalPoints; }
        public long getAvailablePoints() { return availablePoints; }
        public void setAvailablePoints(long availablePoints) { this.availablePoints = availablePoints; }
        public long getLifetimePoints() { return lifetimePoints; }
        public void setLifetimePoints(long lifetimePoints) { this.lifetimePoints = lifetimePoints; }
        public String getTierLevel() { return tierLevel; }
        public void setTierLevel(String tierLevel) { this.tierLevel = tierLevel; }
        public long getTierPoints() { return tierPoints; }
        public void setTierPoints(long tierPoints) { this.tierPoints = tierPoints; }
        public long getNextTierPoints() { return nextTierPoints; }
        public void setNextTierPoints(long nextTierPoints) { this.nextTierPoints = nextTierPoints; }
        public double getTierProgress() { return tierProgress; }
        public void setTierProgress(double tierProgress) { this.tierProgress = tierProgress; }
        public List<LoyaltyTransaction> getRecentTransactions() { return recentTransactions; }
        public void setRecentTransactions(List<LoyaltyTransaction> recentTransactions) { this.recentTransactions = recentTransactions; }
        public List<LoyaltyRedemption> getRecentRedemptions() { return recentRedemptions; }
        public void setRecentRedemptions(List<LoyaltyRedemption> recentRedemptions) { this.recentRedemptions = recentRedemptions; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}

