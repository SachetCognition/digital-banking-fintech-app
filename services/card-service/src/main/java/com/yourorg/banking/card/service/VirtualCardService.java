package com.yourorg.banking.card.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class VirtualCardService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Issue virtual card
     */
    @Transactional
    public VirtualCard issueVirtualCard(String customerId, String accountId, String cardType, 
                                      String cardName, BigDecimal spendingLimit) {
        VirtualCard card = new VirtualCard();
        card.setCustomerId(customerId);
        card.setAccountId(accountId);
        card.setCardType(cardType);
        card.setCardName(cardName);
        card.setSpendingLimit(spendingLimit);
        card.setStatus("active");
        card.setIssuedAt(LocalDateTime.now());

        try {
            // Generate virtual card details
            card.setCardNumber(generateVirtualCardNumber());
            card.setCvv(generateCVV());
            card.setExpiryDate(generateExpiryDate());
            card.setCardToken(generateCardToken());

            // Set default controls
            card.setDailyLimit(spendingLimit.divide(new BigDecimal("30")));
            card.setMonthlyLimit(spendingLimit);
            card.setInternationalEnabled(true);
            card.setOnlineEnabled(true);
            card.setAtmEnabled(false);
            card.setContactlessEnabled(true);

            // Record virtual card
            recordVirtualCard(card);

        } catch (Exception e) {
            card.setStatus("failed");
            card.setErrorMessage("Failed to issue virtual card: " + e.getMessage());
        }

        return card;
    }

    /**
     * Set card controls
     */
    @Transactional
    public CardControls setCardControls(String cardId, BigDecimal dailyLimit, BigDecimal monthlyLimit,
                                      boolean internationalEnabled, boolean onlineEnabled,
                                      boolean atmEnabled, boolean contactlessEnabled) {
        CardControls controls = new CardControls();
        controls.setCardId(cardId);
        controls.setDailyLimit(dailyLimit);
        controls.setMonthlyLimit(monthlyLimit);
        controls.setInternationalEnabled(internationalEnabled);
        controls.setOnlineEnabled(onlineEnabled);
        controls.setAtmEnabled(atmEnabled);
        controls.setContactlessEnabled(contactlessEnabled);
        controls.setUpdatedAt(LocalDateTime.now());

        try {
            // Update card controls
            String sql = """
                UPDATE virtual_cards 
                SET daily_limit = ?, monthly_limit = ?, international_enabled = ?, 
                    online_enabled = ?, atm_enabled = ?, contactless_enabled = ?, 
                    updated_at = ?
                WHERE id = ?
                """;
            
            jdbcTemplate.update(sql, dailyLimit, monthlyLimit, internationalEnabled,
                onlineEnabled, atmEnabled, contactlessEnabled, LocalDateTime.now(), cardId);

            // Record control change
            recordCardControlChange(controls);

        } catch (Exception e) {
            controls.setErrorMessage("Failed to update card controls: " + e.getMessage());
        }

        return controls;
    }

    /**
     * Block/unblock card
     */
    @Transactional
    public CardBlock blockCard(String cardId, String reason) {
        CardBlock block = new CardBlock();
        block.setCardId(cardId);
        block.setReason(reason);
        block.setStatus("blocking");
        block.setBlockedAt(LocalDateTime.now());

        try {
            // Update card status
            String sql = "UPDATE virtual_cards SET status = 'blocked', updated_at = ? WHERE id = ?";
            jdbcTemplate.update(sql, LocalDateTime.now(), cardId);

            // Record block
            recordCardBlock(block);

            block.setStatus("blocked");

        } catch (Exception e) {
            block.setStatus("failed");
            block.setErrorMessage(e.getMessage());
        }

        return block;
    }

    @Transactional
    public CardUnblock unblockCard(String cardId, String reason) {
        CardUnblock unblock = new CardUnblock();
        unblock.setCardId(cardId);
        unblock.setReason(reason);
        unblock.setStatus("unblocking");
        unblock.setUnblockedAt(LocalDateTime.now());

        try {
            // Update card status
            String sql = "UPDATE virtual_cards SET status = 'active', updated_at = ? WHERE id = ?";
            jdbcTemplate.update(sql, LocalDateTime.now(), cardId);

            // Record unblock
            recordCardUnblock(unblock);

            unblock.setStatus("unblocked");

        } catch (Exception e) {
            unblock.setStatus("failed");
            unblock.setErrorMessage(e.getMessage());
        }

        return unblock;
    }

    /**
     * Get card transactions
     */
    public List<CardTransaction> getCardTransactions(String cardId, int limit) {
        String sql = """
            SELECT * FROM card_transactions 
            WHERE card_id = ? 
            ORDER BY transaction_date DESC 
            LIMIT ?
            """;
        
        return jdbcTemplate.query(sql, new Object[]{cardId, limit}, (rs, rowNum) -> {
            CardTransaction transaction = new CardTransaction();
            transaction.setId(rs.getString("id"));
            transaction.setCardId(rs.getString("card_id"));
            transaction.setTransactionDate(rs.getTimestamp("transaction_date").toLocalDateTime());
            transaction.setAmount(rs.getBigDecimal("amount"));
            transaction.setCurrency(rs.getString("currency"));
            transaction.setMerchantName(rs.getString("merchant_name"));
            transaction.setMerchantCategory(rs.getString("merchant_category"));
            transaction.setTransactionType(rs.getString("transaction_type"));
            transaction.setStatus(rs.getString("status"));
            transaction.setDescription(rs.getString("description"));
            return transaction;
        });
    }

    /**
     * Get card spending summary
     */
    public CardSpendingSummary getCardSpendingSummary(String cardId, String period) {
        CardSpendingSummary summary = new CardSpendingSummary();
        summary.setCardId(cardId);
        summary.setPeriod(period);
        summary.setGeneratedAt(LocalDateTime.now());

        try {
            String sql = """
                SELECT 
                    COUNT(*) as total_transactions,
                    SUM(amount) as total_spent,
                    AVG(amount) as average_transaction,
                    MAX(amount) as largest_transaction,
                    COUNT(CASE WHEN transaction_type = 'online' THEN 1 END) as online_transactions,
                    COUNT(CASE WHEN transaction_type = 'pos' THEN 1 END) as pos_transactions,
                    COUNT(CASE WHEN transaction_type = 'atm' THEN 1 END) as atm_transactions
                FROM card_transactions 
                WHERE card_id = ? AND transaction_date >= ?
                """;
            
            LocalDateTime startDate = getPeriodStartDate(period);
            Map<String, Object> result = jdbcTemplate.queryForMap(sql, cardId, startDate);
            
            summary.setTotalTransactions(((Number) result.get("total_transactions")).intValue());
            summary.setTotalSpent(((BigDecimal) result.get("total_spent")));
            summary.setAverageTransaction(((BigDecimal) result.get("average_transaction")));
            summary.setLargestTransaction(((BigDecimal) result.get("largest_transaction")));
            summary.setOnlineTransactions(((Number) result.get("online_transactions")).intValue());
            summary.setPosTransactions(((Number) result.get("pos_transactions")).intValue());
            summary.setAtmTransactions(((Number) result.get("atm_transactions")).intValue());

        } catch (Exception e) {
            summary.setErrorMessage("Failed to generate spending summary: " + e.getMessage());
        }

        return summary;
    }

    /**
     * Get virtual cards for customer
     */
    public List<VirtualCard> getVirtualCards(String customerId) {
        String sql = """
            SELECT * FROM virtual_cards 
            WHERE customer_id = ? 
            ORDER BY issued_at DESC
            """;
        
        return jdbcTemplate.query(sql, new Object[]{customerId}, (rs, rowNum) -> {
            VirtualCard card = new VirtualCard();
            card.setId(rs.getString("id"));
            card.setCustomerId(rs.getString("customer_id"));
            card.setAccountId(rs.getString("account_id"));
            card.setCardNumber(rs.getString("card_number"));
            card.setCvv(rs.getString("cvv"));
            card.setExpiryDate(rs.getString("expiry_date"));
            card.setCardToken(rs.getString("card_token"));
            card.setCardType(rs.getString("card_type"));
            card.setCardName(rs.getString("card_name"));
            card.setSpendingLimit(rs.getBigDecimal("spending_limit"));
            card.setDailyLimit(rs.getBigDecimal("daily_limit"));
            card.setMonthlyLimit(rs.getBigDecimal("monthly_limit"));
            card.setInternationalEnabled(rs.getBoolean("international_enabled"));
            card.setOnlineEnabled(rs.getBoolean("online_enabled"));
            card.setAtmEnabled(rs.getBoolean("atm_enabled"));
            card.setContactlessEnabled(rs.getBoolean("contactless_enabled"));
            card.setStatus(rs.getString("status"));
            card.setIssuedAt(rs.getTimestamp("issued_at").toLocalDateTime());
            card.setUpdatedAt(rs.getTimestamp("updated_at") != null ? 
                rs.getTimestamp("updated_at").toLocalDateTime() : null);
            return card;
        });
    }

    // Private helper methods
    private String generateVirtualCardNumber() {
        // Generate a virtual card number (16 digits)
        Random random = new Random();
        StringBuilder cardNumber = new StringBuilder();
        
        // Start with a valid BIN (Bank Identification Number)
        cardNumber.append("4"); // Visa
        
        // Generate remaining 15 digits
        for (int i = 0; i < 15; i++) {
            cardNumber.append(random.nextInt(10));
        }
        
        return cardNumber.toString();
    }

    private String generateCVV() {
        Random random = new Random();
        return String.format("%03d", random.nextInt(1000));
    }

    private String generateExpiryDate() {
        // Generate expiry date 3 years from now
        LocalDateTime expiry = LocalDateTime.now().plusYears(3);
        return String.format("%02d/%02d", expiry.getMonthValue(), expiry.getYear() % 100);
    }

    private String generateCardToken() {
        return "tok_" + UUID.randomUUID().toString().replace("-", "");
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

    private void recordVirtualCard(VirtualCard card) {
        String sql = """
            INSERT INTO virtual_cards 
            (id, customer_id, account_id, card_number, cvv, expiry_date, card_token, 
             card_type, card_name, spending_limit, daily_limit, monthly_limit, 
             international_enabled, online_enabled, atm_enabled, contactless_enabled, 
             status, issued_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        jdbcTemplate.update(sql,
            UUID.randomUUID().toString(),
            card.getCustomerId(),
            card.getAccountId(),
            card.getCardNumber(),
            card.getCvv(),
            card.getExpiryDate(),
            card.getCardToken(),
            card.getCardType(),
            card.getCardName(),
            card.getSpendingLimit(),
            card.getDailyLimit(),
            card.getMonthlyLimit(),
            card.getInternationalEnabled(),
            card.getOnlineEnabled(),
            card.getAtmEnabled(),
            card.getContactlessEnabled(),
            card.getStatus(),
            card.getIssuedAt(),
            card.getUpdatedAt()
        );
    }

    private void recordCardControlChange(CardControls controls) {
        String sql = """
            INSERT INTO card_control_changes 
            (id, card_id, daily_limit, monthly_limit, international_enabled, 
             online_enabled, atm_enabled, contactless_enabled, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        jdbcTemplate.update(sql,
            UUID.randomUUID().toString(),
            controls.getCardId(),
            controls.getDailyLimit(),
            controls.getMonthlyLimit(),
            controls.getInternationalEnabled(),
            controls.getOnlineEnabled(),
            controls.getAtmEnabled(),
            controls.getContactlessEnabled(),
            controls.getUpdatedAt()
        );
    }

    private void recordCardBlock(CardBlock block) {
        String sql = """
            INSERT INTO card_blocks 
            (id, card_id, reason, status, blocked_at)
            VALUES (?, ?, ?, ?, ?)
            """;
        
        jdbcTemplate.update(sql,
            UUID.randomUUID().toString(),
            block.getCardId(),
            block.getReason(),
            block.getStatus(),
            block.getBlockedAt()
        );
    }

    private void recordCardUnblock(CardUnblock unblock) {
        String sql = """
            INSERT INTO card_unblocks 
            (id, card_id, reason, status, unblocked_at)
            VALUES (?, ?, ?, ?, ?)
            """;
        
        jdbcTemplate.update(sql,
            UUID.randomUUID().toString(),
            unblock.getCardId(),
            unblock.getReason(),
            unblock.getStatus(),
            unblock.getUnblockedAt()
        );
    }

    // Data classes
    public static class VirtualCard {
        private String id;
        private String customerId;
        private String accountId;
        private String cardNumber;
        private String cvv;
        private String expiryDate;
        private String cardToken;
        private String cardType;
        private String cardName;
        private BigDecimal spendingLimit;
        private BigDecimal dailyLimit;
        private BigDecimal monthlyLimit;
        private boolean internationalEnabled;
        private boolean onlineEnabled;
        private boolean atmEnabled;
        private boolean contactlessEnabled;
        private String status;
        private LocalDateTime issuedAt;
        private LocalDateTime updatedAt;
        private String errorMessage;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        public String getAccountId() { return accountId; }
        public void setAccountId(String accountId) { this.accountId = accountId; }
        public String getCardNumber() { return cardNumber; }
        public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
        public String getCvv() { return cvv; }
        public void setCvv(String cvv) { this.cvv = cvv; }
        public String getExpiryDate() { return expiryDate; }
        public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
        public String getCardToken() { return cardToken; }
        public void setCardToken(String cardToken) { this.cardToken = cardToken; }
        public String getCardType() { return cardType; }
        public void setCardType(String cardType) { this.cardType = cardType; }
        public String getCardName() { return cardName; }
        public void setCardName(String cardName) { this.cardName = cardName; }
        public BigDecimal getSpendingLimit() { return spendingLimit; }
        public void setSpendingLimit(BigDecimal spendingLimit) { this.spendingLimit = spendingLimit; }
        public BigDecimal getDailyLimit() { return dailyLimit; }
        public void setDailyLimit(BigDecimal dailyLimit) { this.dailyLimit = dailyLimit; }
        public BigDecimal getMonthlyLimit() { return monthlyLimit; }
        public void setMonthlyLimit(BigDecimal monthlyLimit) { this.monthlyLimit = monthlyLimit; }
        public boolean getInternationalEnabled() { return internationalEnabled; }
        public void setInternationalEnabled(boolean internationalEnabled) { this.internationalEnabled = internationalEnabled; }
        public boolean getOnlineEnabled() { return onlineEnabled; }
        public void setOnlineEnabled(boolean onlineEnabled) { this.onlineEnabled = onlineEnabled; }
        public boolean getAtmEnabled() { return atmEnabled; }
        public void setAtmEnabled(boolean atmEnabled) { this.atmEnabled = atmEnabled; }
        public boolean getContactlessEnabled() { return contactlessEnabled; }
        public void setContactlessEnabled(boolean contactlessEnabled) { this.contactlessEnabled = contactlessEnabled; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getIssuedAt() { return issuedAt; }
        public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class CardControls {
        private String cardId;
        private BigDecimal dailyLimit;
        private BigDecimal monthlyLimit;
        private boolean internationalEnabled;
        private boolean onlineEnabled;
        private boolean atmEnabled;
        private boolean contactlessEnabled;
        private LocalDateTime updatedAt;
        private String errorMessage;

        // Getters and setters
        public String getCardId() { return cardId; }
        public void setCardId(String cardId) { this.cardId = cardId; }
        public BigDecimal getDailyLimit() { return dailyLimit; }
        public void setDailyLimit(BigDecimal dailyLimit) { this.dailyLimit = dailyLimit; }
        public BigDecimal getMonthlyLimit() { return monthlyLimit; }
        public void setMonthlyLimit(BigDecimal monthlyLimit) { this.monthlyLimit = monthlyLimit; }
        public boolean getInternationalEnabled() { return internationalEnabled; }
        public void setInternationalEnabled(boolean internationalEnabled) { this.internationalEnabled = internationalEnabled; }
        public boolean getOnlineEnabled() { return onlineEnabled; }
        public void setOnlineEnabled(boolean onlineEnabled) { this.onlineEnabled = onlineEnabled; }
        public boolean getAtmEnabled() { return atmEnabled; }
        public void setAtmEnabled(boolean atmEnabled) { this.atmEnabled = atmEnabled; }
        public boolean getContactlessEnabled() { return contactlessEnabled; }
        public void setContactlessEnabled(boolean contactlessEnabled) { this.contactlessEnabled = contactlessEnabled; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class CardBlock {
        private String cardId;
        private String reason;
        private String status;
        private LocalDateTime blockedAt;
        private String errorMessage;

        // Getters and setters
        public String getCardId() { return cardId; }
        public void setCardId(String cardId) { this.cardId = cardId; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getBlockedAt() { return blockedAt; }
        public void setBlockedAt(LocalDateTime blockedAt) { this.blockedAt = blockedAt; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class CardUnblock {
        private String cardId;
        private String reason;
        private String status;
        private LocalDateTime unblockedAt;
        private String errorMessage;

        // Getters and setters
        public String getCardId() { return cardId; }
        public void setCardId(String cardId) { this.cardId = cardId; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getUnblockedAt() { return unblockedAt; }
        public void setUnblockedAt(LocalDateTime unblockedAt) { this.unblockedAt = unblockedAt; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class CardTransaction {
        private String id;
        private String cardId;
        private LocalDateTime transactionDate;
        private BigDecimal amount;
        private String currency;
        private String merchantName;
        private String merchantCategory;
        private String transactionType;
        private String status;
        private String description;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getCardId() { return cardId; }
        public void setCardId(String cardId) { this.cardId = cardId; }
        public LocalDateTime getTransactionDate() { return transactionDate; }
        public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public String getMerchantName() { return merchantName; }
        public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
        public String getMerchantCategory() { return merchantCategory; }
        public void setMerchantCategory(String merchantCategory) { this.merchantCategory = merchantCategory; }
        public String getTransactionType() { return transactionType; }
        public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class CardSpendingSummary {
        private String cardId;
        private String period;
        private LocalDateTime generatedAt;
        private int totalTransactions;
        private BigDecimal totalSpent;
        private BigDecimal averageTransaction;
        private BigDecimal largestTransaction;
        private int onlineTransactions;
        private int posTransactions;
        private int atmTransactions;
        private String errorMessage;

        // Getters and setters
        public String getCardId() { return cardId; }
        public void setCardId(String cardId) { this.cardId = cardId; }
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
        public int getTotalTransactions() { return totalTransactions; }
        public void setTotalTransactions(int totalTransactions) { this.totalTransactions = totalTransactions; }
        public BigDecimal getTotalSpent() { return totalSpent; }
        public void setTotalSpent(BigDecimal totalSpent) { this.totalSpent = totalSpent; }
        public BigDecimal getAverageTransaction() { return averageTransaction; }
        public void setAverageTransaction(BigDecimal averageTransaction) { this.averageTransaction = averageTransaction; }
        public BigDecimal getLargestTransaction() { return largestTransaction; }
        public void setLargestTransaction(BigDecimal largestTransaction) { this.largestTransaction = largestTransaction; }
        public int getOnlineTransactions() { return onlineTransactions; }
        public void setOnlineTransactions(int onlineTransactions) { this.onlineTransactions = onlineTransactions; }
        public int getPosTransactions() { return posTransactions; }
        public void setPosTransactions(int posTransactions) { this.posTransactions = posTransactions; }
        public int getAtmTransactions() { return atmTransactions; }
        public void setAtmTransactions(int atmTransactions) { this.atmTransactions = atmTransactions; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}

