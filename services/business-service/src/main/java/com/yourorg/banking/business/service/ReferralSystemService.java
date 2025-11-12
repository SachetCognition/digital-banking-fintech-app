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
public class ReferralSystemService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Generate referral code for customer
     */
    @Transactional
    public ReferralCode generateReferralCode(String customerId) {
        ReferralCode code = new ReferralCode();
        code.setCustomerId(customerId);
        code.setReferralCode(generateUniqueReferralCode());
        code.setIsActive(true);
        code.setUsageCount(0);
        code.setMaxUsage(10);
        code.setCreatedAt(LocalDateTime.now());
        code.setExpiresAt(LocalDateTime.now().plusYears(1));

        try {
            // Check if customer already has an active referral code
            if (hasActiveReferralCode(customerId)) {
                code.setErrorMessage("Customer already has an active referral code");
                return code;
            }

            // Record referral code
            recordReferralCode(code);

        } catch (Exception e) {
            code.setErrorMessage("Failed to generate referral code: " + e.getMessage());
        }

        return code;
    }

    /**
     * Process referral
     */
    @Transactional
    public Referral processReferral(String referralCode, String refereeCustomerId, 
                                  BigDecimal depositAmount) {
        Referral referral = new Referral();
        referral.setReferralCode(referralCode);
        referral.setRefereeCustomerId(refereeCustomerId);
        referral.setStatus("pending");
        referral.setRefereeDepositAmount(depositAmount);
        referral.setCreatedAt(LocalDateTime.now());

        try {
            // Get referral code details
            ReferralCode code = getReferralCode(referralCode);
            if (code == null) {
                referral.setStatus("failed");
                referral.setErrorMessage("Invalid referral code");
                return referral;
            }

            // Check if referral code is active
            if (!code.getIsActive() || code.getExpiresAt().isBefore(LocalDateTime.now())) {
                referral.setStatus("failed");
                referral.setErrorMessage("Referral code expired or inactive");
                return referral;
            }

            // Get referrer customer ID
            String referrerCustomerId = code.getCustomerId();
            referral.setReferrerCustomerId(referrerCustomerId);

            // Check if customer is referring themselves
            if (referrerCustomerId.equals(refereeCustomerId)) {
                referral.setStatus("failed");
                referral.setErrorMessage("Cannot refer yourself");
                return referral;
            }

            // Get active referral program
            ReferralProgram program = getActiveReferralProgram();
            if (program == null) {
                referral.setStatus("failed");
                referral.setErrorMessage("No active referral program");
                return referral;
            }

            referral.setProgramId(program.getId());

            // Check minimum deposit requirement
            if (depositAmount.compareTo(program.getMinimumDeposit()) < 0) {
                referral.setStatus("failed");
                referral.setErrorMessage("Deposit amount below minimum requirement");
                return referral;
            }

            // Check maximum referrals limit
            if (hasReachedMaxReferrals(referrerCustomerId, program)) {
                referral.setStatus("failed");
                referral.setErrorMessage("Maximum referrals limit reached");
                return referral;
            }

            // Calculate bonus amounts
            referral.setReferrerBonusAmount(program.getReferrerBonus());
            referral.setRefereeBonusAmount(program.getRefereeBonus());

            // Record referral
            recordReferral(referral);

            // Update referral code usage
            updateReferralCodeUsage(referralCode);

            referral.setStatus("qualified");
            referral.setQualificationDate(LocalDate.now());

        } catch (Exception e) {
            referral.setStatus("failed");
            referral.setErrorMessage(e.getMessage());
        }

        return referral;
    }

    /**
     * Process referral bonus payment
     */
    @Transactional
    public ReferralBonus processReferralBonus(String referralId) {
        ReferralBonus bonus = new ReferralBonus();
        bonus.setReferralId(referralId);
        bonus.setStatus("processing");
        bonus.setStartedAt(LocalDateTime.now());

        try {
            // Get referral details
            Referral referral = getReferral(referralId);
            if (referral == null) {
                bonus.setStatus("failed");
                bonus.setErrorMessage("Referral not found");
                return bonus;
            }

            // Check if referral is qualified
            if (!"qualified".equals(referral.getStatus())) {
                bonus.setStatus("failed");
                bonus.setErrorMessage("Referral not qualified for bonus");
                return bonus;
            }

            // Check if bonus payment delay has passed
            ReferralProgram program = getReferralProgram(referral.getProgramId());
            if (program == null) {
                bonus.setStatus("failed");
                bonus.setErrorMessage("Referral program not found");
                return bonus;
            }

            LocalDate paymentDate = referral.getQualificationDate().plusDays(program.getBonusPaymentDelay());
            if (LocalDate.now().isBefore(paymentDate)) {
                bonus.setStatus("pending");
                bonus.setErrorMessage("Bonus payment delay not yet passed");
                return bonus;
            }

            // Process referrer bonus
            boolean referrerBonusProcessed = processReferrerBonus(referral);
            if (!referrerBonusProcessed) {
                bonus.setStatus("failed");
                bonus.setErrorMessage("Failed to process referrer bonus");
                return bonus;
            }

            // Process referee bonus
            boolean refereeBonusProcessed = processRefereeBonus(referral);
            if (!refereeBonusProcessed) {
                bonus.setStatus("failed");
                bonus.setErrorMessage("Failed to process referee bonus");
                return bonus;
            }

            // Update referral status
            updateReferralStatus(referralId, "paid", LocalDate.now());

            bonus.setStatus("completed");
            bonus.setCompletedAt(LocalDateTime.now());
            bonus.setReferrerBonusAmount(referral.getReferrerBonusAmount());
            bonus.setRefereeBonusAmount(referral.getRefereeBonusAmount());

        } catch (Exception e) {
            bonus.setStatus("failed");
            bonus.setErrorMessage(e.getMessage());
        }

        return bonus;
    }

    /**
     * Get referral summary for customer
     */
    public ReferralSummary getReferralSummary(String customerId) {
        ReferralSummary summary = new ReferralSummary();
        summary.setCustomerId(customerId);
        summary.setGeneratedAt(LocalDateTime.now());

        try {
            // Get referral code
            ReferralCode code = getReferralCodeByCustomer(customerId);
            summary.setReferralCode(code);

            // Get referral statistics
            String sql = """
                SELECT 
                    COUNT(*) as total_referrals,
                    COUNT(CASE WHEN status = 'qualified' THEN 1 END) as qualified_referrals,
                    COUNT(CASE WHEN status = 'paid' THEN 1 END) as paid_referrals,
                    SUM(CASE WHEN status = 'paid' THEN referrer_bonus_amount ELSE 0 END) as total_bonus_earned,
                    SUM(CASE WHEN status = 'qualified' THEN referee_deposit_amount ELSE 0 END) as total_deposits
                FROM referrals 
                WHERE referrer_customer_id = ?
                """;
            
            Map<String, Object> stats = jdbcTemplate.queryForMap(sql, customerId);
            
            summary.setTotalReferrals(((Number) stats.get("total_referrals")).intValue());
            summary.setQualifiedReferrals(((Number) stats.get("qualified_referrals")).intValue());
            summary.setPaidReferrals(((Number) stats.get("paid_referrals")).intValue());
            summary.setTotalBonusEarned(((BigDecimal) stats.get("total_bonus_earned")));
            summary.setTotalDeposits(((BigDecimal) stats.get("total_deposits")));

            // Get recent referrals
            List<Referral> recentReferrals = getRecentReferrals(customerId, 10);
            summary.setRecentReferrals(recentReferrals);

        } catch (Exception e) {
            summary.setErrorMessage("Failed to generate referral summary: " + e.getMessage());
        }

        return summary;
    }

    /**
     * Get active referral program
     */
    public ReferralProgram getActiveReferralProgram() {
        String sql = """
            SELECT * FROM referral_programs 
            WHERE is_active = true 
            AND start_date <= CURRENT_DATE 
            AND (end_date IS NULL OR end_date >= CURRENT_DATE)
            ORDER BY start_date DESC 
            LIMIT 1
            """;
        
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                ReferralProgram program = new ReferralProgram();
                program.setId(rs.getString("id"));
                program.setProgramName(rs.getString("program_name"));
                program.setDescription(rs.getString("description"));
                program.setReferrerBonus(rs.getBigDecimal("referrer_bonus"));
                program.setRefereeBonus(rs.getBigDecimal("referee_bonus"));
                program.setMinimumDeposit(rs.getBigDecimal("minimum_deposit"));
                program.setMaximumReferrals(rs.getInt("maximum_referrals"));
                program.setBonusPaymentDelay(rs.getInt("bonus_payment_delay"));
                program.setIsActive(rs.getBoolean("is_active"));
                program.setStartDate(rs.getDate("start_date").toLocalDate());
                program.setEndDate(rs.getDate("end_date") != null ? 
                    rs.getDate("end_date").toLocalDate() : null);
                return program;
            });
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Create referral program
     */
    @Transactional
    public ReferralProgram createReferralProgram(String programName, String description, 
                                               BigDecimal referrerBonus, BigDecimal refereeBonus,
                                               BigDecimal minimumDeposit, Integer maximumReferrals,
                                               int bonusPaymentDelay) {
        ReferralProgram program = new ReferralProgram();
        program.setProgramName(programName);
        program.setDescription(description);
        program.setReferrerBonus(referrerBonus);
        program.setRefereeBonus(refereeBonus);
        program.setMinimumDeposit(minimumDeposit);
        program.setMaximumReferrals(maximumReferrals);
        program.setBonusPaymentDelay(bonusPaymentDelay);
        program.setIsActive(true);
        program.setStartDate(LocalDate.now());

        try {
            // Deactivate current program
            String deactivateSql = "UPDATE referral_programs SET is_active = false WHERE is_active = true";
            jdbcTemplate.update(deactivateSql);

            // Record new program
            recordReferralProgram(program);

        } catch (Exception e) {
            program.setErrorMessage("Failed to create referral program: " + e.getMessage());
        }

        return program;
    }

    // Private helper methods
    private String generateUniqueReferralCode() {
        String code;
        do {
            code = generateRandomCode();
        } while (isReferralCodeExists(code));
        return code;
    }

    private String generateRandomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < 8; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return code.toString();
    }

    private boolean isReferralCodeExists(String code) {
        String sql = "SELECT COUNT(*) FROM referral_codes WHERE referral_code = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{code}, Integer.class) > 0;
    }

    private boolean hasActiveReferralCode(String customerId) {
        String sql = """
            SELECT COUNT(*) FROM referral_codes 
            WHERE customer_id = ? AND is_active = true 
            AND expires_at > NOW()
            """;
        return jdbcTemplate.queryForObject(sql, new Object[]{customerId}, Integer.class) > 0;
    }

    private boolean hasReachedMaxReferrals(String customerId, ReferralProgram program) {
        if (program.getMaximumReferrals() == null) {
            return false;
        }

        String sql = """
            SELECT COUNT(*) FROM referrals 
            WHERE referrer_customer_id = ? AND program_id = ?
            """;
        int referralCount = jdbcTemplate.queryForObject(sql, 
            new Object[]{customerId, program.getId()}, Integer.class);
        
        return referralCount >= program.getMaximumReferrals();
    }

    private boolean processReferrerBonus(Referral referral) {
        // In a real implementation, this would integrate with the ledger service
        // to credit the referrer's account
        return true;
    }

    private boolean processRefereeBonus(Referral referral) {
        // In a real implementation, this would integrate with the ledger service
        // to credit the referee's account
        return true;
    }

    private ReferralCode getReferralCode(String referralCode) {
        String sql = "SELECT * FROM referral_codes WHERE referral_code = ?";
        
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{referralCode}, (rs, rowNum) -> {
                ReferralCode code = new ReferralCode();
                code.setId(rs.getString("id"));
                code.setCustomerId(rs.getString("customer_id"));
                code.setReferralCode(rs.getString("referral_code"));
                code.setIsActive(rs.getBoolean("is_active"));
                code.setUsageCount(rs.getInt("usage_count"));
                code.setMaxUsage(rs.getInt("max_usage"));
                code.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                code.setExpiresAt(rs.getTimestamp("expires_at").toLocalDateTime());
                return code;
            });
        } catch (Exception e) {
            return null;
        }
    }

    private ReferralCode getReferralCodeByCustomer(String customerId) {
        String sql = """
            SELECT * FROM referral_codes 
            WHERE customer_id = ? AND is_active = true 
            ORDER BY created_at DESC 
            LIMIT 1
            """;
        
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{customerId}, (rs, rowNum) -> {
                ReferralCode code = new ReferralCode();
                code.setId(rs.getString("id"));
                code.setCustomerId(rs.getString("customer_id"));
                code.setReferralCode(rs.getString("referral_code"));
                code.setIsActive(rs.getBoolean("is_active"));
                code.setUsageCount(rs.getInt("usage_count"));
                code.setMaxUsage(rs.getInt("max_usage"));
                code.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                code.setExpiresAt(rs.getTimestamp("expires_at").toLocalDateTime());
                return code;
            });
        } catch (Exception e) {
            return null;
        }
    }

    private Referral getReferral(String referralId) {
        String sql = "SELECT * FROM referrals WHERE id = ?";
        
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{referralId}, (rs, rowNum) -> {
                Referral referral = new Referral();
                referral.setId(rs.getString("id"));
                referral.setReferrerCustomerId(rs.getString("referrer_customer_id"));
                referral.setRefereeCustomerId(rs.getString("referee_customer_id"));
                referral.setReferralCode(rs.getString("referral_code"));
                referral.setProgramId(rs.getString("program_id"));
                referral.setStatus(rs.getString("status"));
                referral.setRefereeDepositAmount(rs.getBigDecimal("referee_deposit_amount"));
                referral.setReferrerBonusAmount(rs.getBigDecimal("referrer_bonus_amount"));
                referral.setRefereeBonusAmount(rs.getBigDecimal("referee_bonus_amount"));
                referral.setQualificationDate(rs.getDate("qualification_date") != null ? 
                    rs.getDate("qualification_date").toLocalDate() : null);
                referral.setPaymentDate(rs.getDate("payment_date") != null ? 
                    rs.getDate("payment_date").toLocalDate() : null);
                referral.setExpirationDate(rs.getDate("expiration_date") != null ? 
                    rs.getDate("expiration_date").toLocalDate() : null);
                referral.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                return referral;
            });
        } catch (Exception e) {
            return null;
        }
    }

    private ReferralProgram getReferralProgram(String programId) {
        String sql = "SELECT * FROM referral_programs WHERE id = ?";
        
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{programId}, (rs, rowNum) -> {
                ReferralProgram program = new ReferralProgram();
                program.setId(rs.getString("id"));
                program.setProgramName(rs.getString("program_name"));
                program.setDescription(rs.getString("description"));
                program.setReferrerBonus(rs.getBigDecimal("referrer_bonus"));
                program.setRefereeBonus(rs.getBigDecimal("referee_bonus"));
                program.setMinimumDeposit(rs.getBigDecimal("minimum_deposit"));
                program.setMaximumReferrals(rs.getInt("maximum_referrals"));
                program.setBonusPaymentDelay(rs.getInt("bonus_payment_delay"));
                program.setIsActive(rs.getBoolean("is_active"));
                program.setStartDate(rs.getDate("start_date").toLocalDate());
                program.setEndDate(rs.getDate("end_date") != null ? 
                    rs.getDate("end_date").toLocalDate() : null);
                return program;
            });
        } catch (Exception e) {
            return null;
        }
    }

    private List<Referral> getRecentReferrals(String customerId, int limit) {
        String sql = """
            SELECT * FROM referrals 
            WHERE referrer_customer_id = ? 
            ORDER BY created_at DESC 
            LIMIT ?
            """;
        
        return jdbcTemplate.query(sql, new Object[]{customerId, limit}, (rs, rowNum) -> {
            Referral referral = new Referral();
            referral.setId(rs.getString("id"));
            referral.setReferrerCustomerId(rs.getString("referrer_customer_id"));
            referral.setRefereeCustomerId(rs.getString("referee_customer_id"));
            referral.setReferralCode(rs.getString("referral_code"));
            referral.setProgramId(rs.getString("program_id"));
            referral.setStatus(rs.getString("status"));
            referral.setRefereeDepositAmount(rs.getBigDecimal("referee_deposit_amount"));
            referral.setReferrerBonusAmount(rs.getBigDecimal("referrer_bonus_amount"));
            referral.setRefereeBonusAmount(rs.getBigDecimal("referee_bonus_amount"));
            referral.setQualificationDate(rs.getDate("qualification_date") != null ? 
                rs.getDate("qualification_date").toLocalDate() : null);
            referral.setPaymentDate(rs.getDate("payment_date") != null ? 
                rs.getDate("payment_date").toLocalDate() : null);
            referral.setExpirationDate(rs.getDate("expiration_date") != null ? 
                rs.getDate("expiration_date").toLocalDate() : null);
            referral.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return referral;
        });
    }

    private void recordReferralCode(ReferralCode code) {
        String sql = """
            INSERT INTO referral_codes 
            (id, customer_id, referral_code, is_active, usage_count, max_usage, created_at, expires_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        jdbcTemplate.update(sql,
            UUID.randomUUID().toString(),
            code.getCustomerId(),
            code.getReferralCode(),
            code.getIsActive(),
            code.getUsageCount(),
            code.getMaxUsage(),
            code.getCreatedAt(),
            code.getExpiresAt()
        );
    }

    private void recordReferral(Referral referral) {
        String sql = """
            INSERT INTO referrals 
            (id, referrer_customer_id, referee_customer_id, referral_code, program_id, status, 
             referee_deposit_amount, referrer_bonus_amount, referee_bonus_amount, qualification_date, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        jdbcTemplate.update(sql,
            UUID.randomUUID().toString(),
            referral.getReferrerCustomerId(),
            referral.getRefereeCustomerId(),
            referral.getReferralCode(),
            referral.getProgramId(),
            referral.getStatus(),
            referral.getRefereeDepositAmount(),
            referral.getReferrerBonusAmount(),
            referral.getRefereeBonusAmount(),
            referral.getQualificationDate(),
            referral.getCreatedAt()
        );
    }

    private void recordReferralProgram(ReferralProgram program) {
        String sql = """
            INSERT INTO referral_programs 
            (id, program_name, description, referrer_bonus, referee_bonus, minimum_deposit, 
             maximum_referrals, bonus_payment_delay, is_active, start_date, end_date, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        jdbcTemplate.update(sql,
            UUID.randomUUID().toString(),
            program.getProgramName(),
            program.getDescription(),
            program.getReferrerBonus(),
            program.getRefereeBonus(),
            program.getMinimumDeposit(),
            program.getMaximumReferrals(),
            program.getBonusPaymentDelay(),
            program.getIsActive(),
            program.getStartDate(),
            program.getEndDate(),
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    private void updateReferralCodeUsage(String referralCode) {
        String sql = "UPDATE referral_codes SET usage_count = usage_count + 1 WHERE referral_code = ?";
        jdbcTemplate.update(sql, referralCode);
    }

    private void updateReferralStatus(String referralId, String status, LocalDate paymentDate) {
        String sql = "UPDATE referrals SET status = ?, payment_date = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, paymentDate, referralId);
    }

    // Data classes
    public static class ReferralCode {
        private String id;
        private String customerId;
        private String referralCode;
        private boolean isActive;
        private int usageCount;
        private int maxUsage;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private String errorMessage;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        public String getReferralCode() { return referralCode; }
        public void setReferralCode(String referralCode) { this.referralCode = referralCode; }
        public boolean getIsActive() { return isActive; }
        public void setIsActive(boolean isActive) { this.isActive = isActive; }
        public int getUsageCount() { return usageCount; }
        public void setUsageCount(int usageCount) { this.usageCount = usageCount; }
        public int getMaxUsage() { return maxUsage; }
        public void setMaxUsage(int maxUsage) { this.maxUsage = maxUsage; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class Referral {
        private String id;
        private String referrerCustomerId;
        private String refereeCustomerId;
        private String referralCode;
        private String programId;
        private String status;
        private BigDecimal refereeDepositAmount;
        private BigDecimal referrerBonusAmount;
        private BigDecimal refereeBonusAmount;
        private LocalDate qualificationDate;
        private LocalDate paymentDate;
        private LocalDate expirationDate;
        private LocalDateTime createdAt;
        private String errorMessage;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getReferrerCustomerId() { return referrerCustomerId; }
        public void setReferrerCustomerId(String referrerCustomerId) { this.referrerCustomerId = referrerCustomerId; }
        public String getRefereeCustomerId() { return refereeCustomerId; }
        public void setRefereeCustomerId(String refereeCustomerId) { this.refereeCustomerId = refereeCustomerId; }
        public String getReferralCode() { return referralCode; }
        public void setReferralCode(String referralCode) { this.referralCode = referralCode; }
        public String getProgramId() { return programId; }
        public void setProgramId(String programId) { this.programId = programId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public BigDecimal getRefereeDepositAmount() { return refereeDepositAmount; }
        public void setRefereeDepositAmount(BigDecimal refereeDepositAmount) { this.refereeDepositAmount = refereeDepositAmount; }
        public BigDecimal getReferrerBonusAmount() { return referrerBonusAmount; }
        public void setReferrerBonusAmount(BigDecimal referrerBonusAmount) { this.referrerBonusAmount = referrerBonusAmount; }
        public BigDecimal getRefereeBonusAmount() { return refereeBonusAmount; }
        public void setRefereeBonusAmount(BigDecimal refereeBonusAmount) { this.refereeBonusAmount = refereeBonusAmount; }
        public LocalDate getQualificationDate() { return qualificationDate; }
        public void setQualificationDate(LocalDate qualificationDate) { this.qualificationDate = qualificationDate; }
        public LocalDate getPaymentDate() { return paymentDate; }
        public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
        public LocalDate getExpirationDate() { return expirationDate; }
        public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class ReferralProgram {
        private String id;
        private String programName;
        private String description;
        private BigDecimal referrerBonus;
        private BigDecimal refereeBonus;
        private BigDecimal minimumDeposit;
        private Integer maximumReferrals;
        private int bonusPaymentDelay;
        private boolean isActive;
        private LocalDate startDate;
        private LocalDate endDate;
        private String errorMessage;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getProgramName() { return programName; }
        public void setProgramName(String programName) { this.programName = programName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public BigDecimal getReferrerBonus() { return referrerBonus; }
        public void setReferrerBonus(BigDecimal referrerBonus) { this.referrerBonus = referrerBonus; }
        public BigDecimal getRefereeBonus() { return refereeBonus; }
        public void setRefereeBonus(BigDecimal refereeBonus) { this.refereeBonus = refereeBonus; }
        public BigDecimal getMinimumDeposit() { return minimumDeposit; }
        public void setMinimumDeposit(BigDecimal minimumDeposit) { this.minimumDeposit = minimumDeposit; }
        public Integer getMaximumReferrals() { return maximumReferrals; }
        public void setMaximumReferrals(Integer maximumReferrals) { this.maximumReferrals = maximumReferrals; }
        public int getBonusPaymentDelay() { return bonusPaymentDelay; }
        public void setBonusPaymentDelay(int bonusPaymentDelay) { this.bonusPaymentDelay = bonusPaymentDelay; }
        public boolean getIsActive() { return isActive; }
        public void setIsActive(boolean isActive) { this.isActive = isActive; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class ReferralBonus {
        private String referralId;
        private String status;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private BigDecimal referrerBonusAmount;
        private BigDecimal refereeBonusAmount;
        private String errorMessage;

        // Getters and setters
        public String getReferralId() { return referralId; }
        public void setReferralId(String referralId) { this.referralId = referralId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getStartedAt() { return startedAt; }
        public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
        public LocalDateTime getCompletedAt() { return completedAt; }
        public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
        public BigDecimal getReferrerBonusAmount() { return referrerBonusAmount; }
        public void setReferrerBonusAmount(BigDecimal referrerBonusAmount) { this.referrerBonusAmount = referrerBonusAmount; }
        public BigDecimal getRefereeBonusAmount() { return refereeBonusAmount; }
        public void setRefereeBonusAmount(BigDecimal refereeBonusAmount) { this.refereeBonusAmount = refereeBonusAmount; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class ReferralSummary {
        private String customerId;
        private LocalDateTime generatedAt;
        private ReferralCode referralCode;
        private int totalReferrals;
        private int qualifiedReferrals;
        private int paidReferrals;
        private BigDecimal totalBonusEarned;
        private BigDecimal totalDeposits;
        private List<Referral> recentReferrals;
        private String errorMessage;

        // Getters and setters
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
        public ReferralCode getReferralCode() { return referralCode; }
        public void setReferralCode(ReferralCode referralCode) { this.referralCode = referralCode; }
        public int getTotalReferrals() { return totalReferrals; }
        public void setTotalReferrals(int totalReferrals) { this.totalReferrals = totalReferrals; }
        public int getQualifiedReferrals() { return qualifiedReferrals; }
        public void setQualifiedReferrals(int qualifiedReferrals) { this.qualifiedReferrals = qualifiedReferrals; }
        public int getPaidReferrals() { return paidReferrals; }
        public void setPaidReferrals(int paidReferrals) { this.paidReferrals = paidReferrals; }
        public BigDecimal getTotalBonusEarned() { return totalBonusEarned; }
        public void setTotalBonusEarned(BigDecimal totalBonusEarned) { this.totalBonusEarned = totalBonusEarned; }
        public BigDecimal getTotalDeposits() { return totalDeposits; }
        public void setTotalDeposits(BigDecimal totalDeposits) { this.totalDeposits = totalDeposits; }
        public List<Referral> getRecentReferrals() { return recentReferrals; }
        public void setRecentReferrals(List<Referral> recentReferrals) { this.recentReferrals = recentReferrals; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}

