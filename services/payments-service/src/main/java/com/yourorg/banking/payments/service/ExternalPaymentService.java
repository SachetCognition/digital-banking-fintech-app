package com.yourorg.banking.payments.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ExternalPaymentService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private RestTemplate restTemplate;

    /**
     * Process external bank transfer
     */
    @Transactional
    public ExternalBankTransfer processExternalBankTransfer(String customerId, String fromAccountId, 
                                                          String toBankAccount, String toBankName, 
                                                          String toBankCode, BigDecimal amount, 
                                                          String currency, String description) {
        ExternalBankTransfer transfer = new ExternalBankTransfer();
        transfer.setCustomerId(customerId);
        transfer.setFromAccountId(fromAccountId);
        transfer.setToBankAccount(toBankAccount);
        transfer.setToBankName(toBankName);
        transfer.setToBankCode(toBankCode);
        transfer.setAmount(amount);
        transfer.setCurrency(currency);
        transfer.setDescription(description);
        transfer.setStatus("initiated");
        transfer.setInitiatedAt(LocalDateTime.now());

        try {
            // Validate account and limits
            if (!validateAccountAndLimits(customerId, fromAccountId, amount)) {
                transfer.setStatus("failed");
                transfer.setErrorMessage("Account validation failed or insufficient funds");
                return transfer;
            }

            // Calculate fees
            BigDecimal fee = calculateExternalTransferFee(amount, currency);
            transfer.setFee(fee);

            // Reserve funds
            if (!reserveFunds(fromAccountId, amount.add(fee))) {
                transfer.setStatus("failed");
                transfer.setErrorMessage("Failed to reserve funds");
                return transfer;
            }

            // Process external transfer
            ExternalTransferResult result = processExternalTransfer(transfer);
            
            if (result.isSuccess()) {
                transfer.setStatus("completed");
                transfer.setCompletedAt(LocalDateTime.now());
                transfer.setExternalTransactionId(result.getExternalTransactionId());
                transfer.setProcessingFee(result.getProcessingFee());
            } else {
                transfer.setStatus("failed");
                transfer.setErrorMessage(result.getErrorMessage());
                // Release reserved funds
                releaseFunds(fromAccountId, amount.add(fee));
            }

            // Record transfer
            recordExternalBankTransfer(transfer);

        } catch (Exception e) {
            transfer.setStatus("failed");
            transfer.setErrorMessage(e.getMessage());
        }

        return transfer;
    }

    /**
     * Process international transfer
     */
    @Transactional
    public InternationalTransfer processInternationalTransfer(String customerId, String fromAccountId,
                                                           String toAccountNumber, String toBankName,
                                                           String toBankCode, String toCountry,
                                                           BigDecimal amount, String fromCurrency,
                                                           String toCurrency, String description) {
        InternationalTransfer transfer = new InternationalTransfer();
        transfer.setCustomerId(customerId);
        transfer.setFromAccountId(fromAccountId);
        transfer.setToAccountNumber(toAccountNumber);
        transfer.setToBankName(toBankName);
        transfer.setToBankCode(toBankCode);
        transfer.setToCountry(toCountry);
        transfer.setAmount(amount);
        transfer.setFromCurrency(fromCurrency);
        transfer.setToCurrency(toCurrency);
        transfer.setDescription(description);
        transfer.setStatus("initiated");
        transfer.setInitiatedAt(LocalDateTime.now());

        try {
            // Validate account and limits
            if (!validateAccountAndLimits(customerId, fromAccountId, amount)) {
                transfer.setStatus("failed");
                transfer.setErrorMessage("Account validation failed or insufficient funds");
                return transfer;
            }

            // Get exchange rate
            ExchangeRate exchangeRate = getExchangeRate(fromCurrency, toCurrency);
            transfer.setExchangeRate(exchangeRate.getRate());
            transfer.setConvertedAmount(amount.multiply(exchangeRate.getRate()));

            // Calculate fees
            BigDecimal fee = calculateInternationalTransferFee(amount, fromCurrency, toCurrency);
            transfer.setFee(fee);

            // Reserve funds
            if (!reserveFunds(fromAccountId, amount.add(fee))) {
                transfer.setStatus("failed");
                transfer.setErrorMessage("Failed to reserve funds");
                return transfer;
            }

            // Process international transfer
            InternationalTransferResult result = processInternationalTransfer(transfer);
            
            if (result.isSuccess()) {
                transfer.setStatus("completed");
                transfer.setCompletedAt(LocalDateTime.now());
                transfer.setExternalTransactionId(result.getExternalTransactionId());
                transfer.setSwiftCode(result.getSwiftCode());
                transfer.setProcessingFee(result.getProcessingFee());
            } else {
                transfer.setStatus("failed");
                transfer.setErrorMessage(result.getErrorMessage());
                // Release reserved funds
                releaseFunds(fromAccountId, amount.add(fee));
            }

            // Record transfer
            recordInternationalTransfer(transfer);

        } catch (Exception e) {
            transfer.setStatus("failed");
            transfer.setErrorMessage(e.getMessage());
        }

        return transfer;
    }

    /**
     * Get transfer status
     */
    public TransferStatus getTransferStatus(String transferId, String transferType) {
        TransferStatus status = new TransferStatus();
        status.setTransferId(transferId);
        status.setTransferType(transferType);
        status.setCheckedAt(LocalDateTime.now());

        try {
            if ("external".equals(transferType)) {
                ExternalBankTransfer transfer = getExternalBankTransfer(transferId);
                if (transfer != null) {
                    status.setStatus(transfer.getStatus());
                    status.setAmount(transfer.getAmount());
                    status.setCurrency(transfer.getCurrency());
                    status.setFee(transfer.getFee());
                    status.setInitiatedAt(transfer.getInitiatedAt());
                    status.setCompletedAt(transfer.getCompletedAt());
                    status.setErrorMessage(transfer.getErrorMessage());
                }
            } else if ("international".equals(transferType)) {
                InternationalTransfer transfer = getInternationalTransfer(transferId);
                if (transfer != null) {
                    status.setStatus(transfer.getStatus());
                    status.setAmount(transfer.getAmount());
                    status.setCurrency(transfer.getFromCurrency());
                    status.setFee(transfer.getFee());
                    status.setInitiatedAt(transfer.getInitiatedAt());
                    status.setCompletedAt(transfer.getCompletedAt());
                    status.setErrorMessage(transfer.getErrorMessage());
                }
            }

        } catch (Exception e) {
            status.setStatus("error");
            status.setErrorMessage(e.getMessage());
        }

        return status;
    }

    /**
     * Get transfer history
     */
    public List<TransferHistory> getTransferHistory(String customerId, String transferType, int limit) {
        List<TransferHistory> history = new ArrayList<>();
        
        try {
            if ("external".equals(transferType)) {
                String sql = """
                    SELECT * FROM external_bank_transfers 
                    WHERE customer_id = ? 
                    ORDER BY initiated_at DESC 
                    LIMIT ?
                    """;
                
                List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, customerId, limit);
                
                for (Map<String, Object> row : results) {
                    TransferHistory item = new TransferHistory();
                    item.setTransferId((String) row.get("id"));
                    item.setTransferType("external");
                    item.setAmount(((BigDecimal) row.get("amount")));
                    item.setCurrency((String) row.get("currency"));
                    item.setFee(((BigDecimal) row.get("fee")));
                    item.setStatus((String) row.get("status"));
                    item.setDescription((String) row.get("description"));
                    item.setInitiatedAt(((LocalDateTime) row.get("initiated_at")));
                    item.setCompletedAt(((LocalDateTime) row.get("completed_at")));
                    history.add(item);
                }
            } else if ("international".equals(transferType)) {
                String sql = """
                    SELECT * FROM international_transfers 
                    WHERE customer_id = ? 
                    ORDER BY initiated_at DESC 
                    LIMIT ?
                    """;
                
                List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, customerId, limit);
                
                for (Map<String, Object> row : results) {
                    TransferHistory item = new TransferHistory();
                    item.setTransferId((String) row.get("id"));
                    item.setTransferType("international");
                    item.setAmount(((BigDecimal) row.get("amount")));
                    item.setCurrency((String) row.get("from_currency"));
                    item.setFee(((BigDecimal) row.get("fee")));
                    item.setStatus((String) row.get("status"));
                    item.setDescription((String) row.get("description"));
                    item.setInitiatedAt(((LocalDateTime) row.get("initiated_at")));
                    item.setCompletedAt(((LocalDateTime) row.get("completed_at")));
                    history.add(item);
                }
            }

        } catch (Exception e) {
            // Handle error
        }

        return history;
    }

    // Private helper methods
    private boolean validateAccountAndLimits(String customerId, String accountId, BigDecimal amount) {
        // In a real implementation, this would validate:
        // - Account exists and belongs to customer
        // - Account has sufficient balance
        // - Customer has not exceeded daily/monthly limits
        // - Account is active and not frozen
        return true;
    }

    private BigDecimal calculateExternalTransferFee(BigDecimal amount, String currency) {
        // External transfer fee: $25.00
        return new BigDecimal("25.00");
    }

    private BigDecimal calculateInternationalTransferFee(BigDecimal amount, String fromCurrency, String toCurrency) {
        // International transfer fee: $45.00
        return new BigDecimal("45.00");
    }

    private boolean reserveFunds(String accountId, BigDecimal amount) {
        // In a real implementation, this would integrate with the ledger service
        // to reserve funds in the account
        return true;
    }

    private void releaseFunds(String accountId, BigDecimal amount) {
        // In a real implementation, this would integrate with the ledger service
        // to release reserved funds
    }

    private ExternalTransferResult processExternalTransfer(ExternalBankTransfer transfer) {
        // In a real implementation, this would integrate with external payment networks
        // like ACH, Fedwire, or SWIFT
        ExternalTransferResult result = new ExternalTransferResult();
        result.setSuccess(true);
        result.setExternalTransactionId("EXT-" + System.currentTimeMillis());
        result.setProcessingFee(new BigDecimal("5.00"));
        return result;
    }

    private InternationalTransferResult processInternationalTransfer(InternationalTransfer transfer) {
        // In a real implementation, this would integrate with SWIFT network
        InternationalTransferResult result = new InternationalTransferResult();
        result.setSuccess(true);
        result.setExternalTransactionId("INT-" + System.currentTimeMillis());
        result.setSwiftCode("SWIFT-" + transfer.getToBankCode());
        result.setProcessingFee(new BigDecimal("15.00"));
        return result;
    }

    private ExchangeRate getExchangeRate(String fromCurrency, String toCurrency) {
        // In a real implementation, this would fetch real-time exchange rates
        ExchangeRate rate = new ExchangeRate();
        rate.setFromCurrency(fromCurrency);
        rate.setToCurrency(toCurrency);
        rate.setRate(new BigDecimal("1.25")); // Example rate
        rate.setTimestamp(LocalDateTime.now());
        return rate;
    }

    private void recordExternalBankTransfer(ExternalBankTransfer transfer) {
        String sql = """
            INSERT INTO external_bank_transfers 
            (id, customer_id, from_account_id, to_bank_account, to_bank_name, to_bank_code, 
             amount, currency, fee, description, status, initiated_at, completed_at, 
             external_transaction_id, processing_fee, error_message)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        jdbcTemplate.update(sql,
            UUID.randomUUID().toString(),
            transfer.getCustomerId(),
            transfer.getFromAccountId(),
            transfer.getToBankAccount(),
            transfer.getToBankName(),
            transfer.getToBankCode(),
            transfer.getAmount(),
            transfer.getCurrency(),
            transfer.getFee(),
            transfer.getDescription(),
            transfer.getStatus(),
            transfer.getInitiatedAt(),
            transfer.getCompletedAt(),
            transfer.getExternalTransactionId(),
            transfer.getProcessingFee(),
            transfer.getErrorMessage()
        );
    }

    private void recordInternationalTransfer(InternationalTransfer transfer) {
        String sql = """
            INSERT INTO international_transfers 
            (id, customer_id, from_account_id, to_account_number, to_bank_name, to_bank_code, 
             to_country, amount, from_currency, to_currency, converted_amount, exchange_rate, 
             fee, description, status, initiated_at, completed_at, external_transaction_id, 
             swift_code, processing_fee, error_message)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        jdbcTemplate.update(sql,
            UUID.randomUUID().toString(),
            transfer.getCustomerId(),
            transfer.getFromAccountId(),
            transfer.getToAccountNumber(),
            transfer.getToBankName(),
            transfer.getToBankCode(),
            transfer.getToCountry(),
            transfer.getAmount(),
            transfer.getFromCurrency(),
            transfer.getToCurrency(),
            transfer.getConvertedAmount(),
            transfer.getExchangeRate(),
            transfer.getFee(),
            transfer.getDescription(),
            transfer.getStatus(),
            transfer.getInitiatedAt(),
            transfer.getCompletedAt(),
            transfer.getExternalTransactionId(),
            transfer.getSwiftCode(),
            transfer.getProcessingFee(),
            transfer.getErrorMessage()
        );
    }

    private ExternalBankTransfer getExternalBankTransfer(String transferId) {
        String sql = "SELECT * FROM external_bank_transfers WHERE id = ?";
        
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{transferId}, (rs, rowNum) -> {
                ExternalBankTransfer transfer = new ExternalBankTransfer();
                transfer.setId(rs.getString("id"));
                transfer.setCustomerId(rs.getString("customer_id"));
                transfer.setFromAccountId(rs.getString("from_account_id"));
                transfer.setToBankAccount(rs.getString("to_bank_account"));
                transfer.setToBankName(rs.getString("to_bank_name"));
                transfer.setToBankCode(rs.getString("to_bank_code"));
                transfer.setAmount(rs.getBigDecimal("amount"));
                transfer.setCurrency(rs.getString("currency"));
                transfer.setFee(rs.getBigDecimal("fee"));
                transfer.setDescription(rs.getString("description"));
                transfer.setStatus(rs.getString("status"));
                transfer.setInitiatedAt(rs.getTimestamp("initiated_at").toLocalDateTime());
                transfer.setCompletedAt(rs.getTimestamp("completed_at") != null ? 
                    rs.getTimestamp("completed_at").toLocalDateTime() : null);
                transfer.setExternalTransactionId(rs.getString("external_transaction_id"));
                transfer.setProcessingFee(rs.getBigDecimal("processing_fee"));
                transfer.setErrorMessage(rs.getString("error_message"));
                return transfer;
            });
        } catch (Exception e) {
            return null;
        }
    }

    private InternationalTransfer getInternationalTransfer(String transferId) {
        String sql = "SELECT * FROM international_transfers WHERE id = ?";
        
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{transferId}, (rs, rowNum) -> {
                InternationalTransfer transfer = new InternationalTransfer();
                transfer.setId(rs.getString("id"));
                transfer.setCustomerId(rs.getString("customer_id"));
                transfer.setFromAccountId(rs.getString("from_account_id"));
                transfer.setToAccountNumber(rs.getString("to_account_number"));
                transfer.setToBankName(rs.getString("to_bank_name"));
                transfer.setToBankCode(rs.getString("to_bank_code"));
                transfer.setToCountry(rs.getString("to_country"));
                transfer.setAmount(rs.getBigDecimal("amount"));
                transfer.setFromCurrency(rs.getString("from_currency"));
                transfer.setToCurrency(rs.getString("to_currency"));
                transfer.setConvertedAmount(rs.getBigDecimal("converted_amount"));
                transfer.setExchangeRate(rs.getBigDecimal("exchange_rate"));
                transfer.setFee(rs.getBigDecimal("fee"));
                transfer.setDescription(rs.getString("description"));
                transfer.setStatus(rs.getString("status"));
                transfer.setInitiatedAt(rs.getTimestamp("initiated_at").toLocalDateTime());
                transfer.setCompletedAt(rs.getTimestamp("completed_at") != null ? 
                    rs.getTimestamp("completed_at").toLocalDateTime() : null);
                transfer.setExternalTransactionId(rs.getString("external_transaction_id"));
                transfer.setSwiftCode(rs.getString("swift_code"));
                transfer.setProcessingFee(rs.getBigDecimal("processing_fee"));
                transfer.setErrorMessage(rs.getString("error_message"));
                return transfer;
            });
        } catch (Exception e) {
            return null;
        }
    }

    // Data classes
    public static class ExternalBankTransfer {
        private String id;
        private String customerId;
        private String fromAccountId;
        private String toBankAccount;
        private String toBankName;
        private String toBankCode;
        private BigDecimal amount;
        private String currency;
        private BigDecimal fee;
        private String description;
        private String status;
        private LocalDateTime initiatedAt;
        private LocalDateTime completedAt;
        private String externalTransactionId;
        private BigDecimal processingFee;
        private String errorMessage;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        public String getFromAccountId() { return fromAccountId; }
        public void setFromAccountId(String fromAccountId) { this.fromAccountId = fromAccountId; }
        public String getToBankAccount() { return toBankAccount; }
        public void setToBankAccount(String toBankAccount) { this.toBankAccount = toBankAccount; }
        public String getToBankName() { return toBankName; }
        public void setToBankName(String toBankName) { this.toBankName = toBankName; }
        public String getToBankCode() { return toBankCode; }
        public void setToBankCode(String toBankCode) { this.toBankCode = toBankCode; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public BigDecimal getFee() { return fee; }
        public void setFee(BigDecimal fee) { this.fee = fee; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getInitiatedAt() { return initiatedAt; }
        public void setInitiatedAt(LocalDateTime initiatedAt) { this.initiatedAt = initiatedAt; }
        public LocalDateTime getCompletedAt() { return completedAt; }
        public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
        public String getExternalTransactionId() { return externalTransactionId; }
        public void setExternalTransactionId(String externalTransactionId) { this.externalTransactionId = externalTransactionId; }
        public BigDecimal getProcessingFee() { return processingFee; }
        public void setProcessingFee(BigDecimal processingFee) { this.processingFee = processingFee; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class InternationalTransfer {
        private String id;
        private String customerId;
        private String fromAccountId;
        private String toAccountNumber;
        private String toBankName;
        private String toBankCode;
        private String toCountry;
        private BigDecimal amount;
        private String fromCurrency;
        private String toCurrency;
        private BigDecimal convertedAmount;
        private BigDecimal exchangeRate;
        private BigDecimal fee;
        private String description;
        private String status;
        private LocalDateTime initiatedAt;
        private LocalDateTime completedAt;
        private String externalTransactionId;
        private String swiftCode;
        private BigDecimal processingFee;
        private String errorMessage;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        public String getFromAccountId() { return fromAccountId; }
        public void setFromAccountId(String fromAccountId) { this.fromAccountId = fromAccountId; }
        public String getToAccountNumber() { return toAccountNumber; }
        public void setToAccountNumber(String toAccountNumber) { this.toAccountNumber = toAccountNumber; }
        public String getToBankName() { return toBankName; }
        public void setToBankName(String toBankName) { this.toBankName = toBankName; }
        public String getToBankCode() { return toBankCode; }
        public void setToBankCode(String toBankCode) { this.toBankCode = toBankCode; }
        public String getToCountry() { return toCountry; }
        public void setToCountry(String toCountry) { this.toCountry = toCountry; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getFromCurrency() { return fromCurrency; }
        public void setFromCurrency(String fromCurrency) { this.fromCurrency = fromCurrency; }
        public String getToCurrency() { return toCurrency; }
        public void setToCurrency(String toCurrency) { this.toCurrency = toCurrency; }
        public BigDecimal getConvertedAmount() { return convertedAmount; }
        public void setConvertedAmount(BigDecimal convertedAmount) { this.convertedAmount = convertedAmount; }
        public BigDecimal getExchangeRate() { return exchangeRate; }
        public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }
        public BigDecimal getFee() { return fee; }
        public void setFee(BigDecimal fee) { this.fee = fee; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getInitiatedAt() { return initiatedAt; }
        public void setInitiatedAt(LocalDateTime initiatedAt) { this.initiatedAt = initiatedAt; }
        public LocalDateTime getCompletedAt() { return completedAt; }
        public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
        public String getExternalTransactionId() { return externalTransactionId; }
        public void setExternalTransactionId(String externalTransactionId) { this.externalTransactionId = externalTransactionId; }
        public String getSwiftCode() { return swiftCode; }
        public void setSwiftCode(String swiftCode) { this.swiftCode = swiftCode; }
        public BigDecimal getProcessingFee() { return processingFee; }
        public void setProcessingFee(BigDecimal processingFee) { this.processingFee = processingFee; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class ExchangeRate {
        private String fromCurrency;
        private String toCurrency;
        private BigDecimal rate;
        private LocalDateTime timestamp;

        // Getters and setters
        public String getFromCurrency() { return fromCurrency; }
        public void setFromCurrency(String fromCurrency) { this.fromCurrency = fromCurrency; }
        public String getToCurrency() { return toCurrency; }
        public void setToCurrency(String toCurrency) { this.toCurrency = toCurrency; }
        public BigDecimal getRate() { return rate; }
        public void setRate(BigDecimal rate) { this.rate = rate; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    public static class ExternalTransferResult {
        private boolean success;
        private String externalTransactionId;
        private BigDecimal processingFee;
        private String errorMessage;

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getExternalTransactionId() { return externalTransactionId; }
        public void setExternalTransactionId(String externalTransactionId) { this.externalTransactionId = externalTransactionId; }
        public BigDecimal getProcessingFee() { return processingFee; }
        public void setProcessingFee(BigDecimal processingFee) { this.processingFee = processingFee; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class InternationalTransferResult {
        private boolean success;
        private String externalTransactionId;
        private String swiftCode;
        private BigDecimal processingFee;
        private String errorMessage;

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getExternalTransactionId() { return externalTransactionId; }
        public void setExternalTransactionId(String externalTransactionId) { this.externalTransactionId = externalTransactionId; }
        public String getSwiftCode() { return swiftCode; }
        public void setSwiftCode(String swiftCode) { this.swiftCode = swiftCode; }
        public BigDecimal getProcessingFee() { return processingFee; }
        public void setProcessingFee(BigDecimal processingFee) { this.processingFee = processingFee; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class TransferStatus {
        private String transferId;
        private String transferType;
        private String status;
        private BigDecimal amount;
        private String currency;
        private BigDecimal fee;
        private LocalDateTime initiatedAt;
        private LocalDateTime completedAt;
        private String errorMessage;
        private LocalDateTime checkedAt;

        // Getters and setters
        public String getTransferId() { return transferId; }
        public void setTransferId(String transferId) { this.transferId = transferId; }
        public String getTransferType() { return transferType; }
        public void setTransferType(String transferType) { this.transferType = transferType; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public BigDecimal getFee() { return fee; }
        public void setFee(BigDecimal fee) { this.fee = fee; }
        public LocalDateTime getInitiatedAt() { return initiatedAt; }
        public void setInitiatedAt(LocalDateTime initiatedAt) { this.initiatedAt = initiatedAt; }
        public LocalDateTime getCompletedAt() { return completedAt; }
        public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public LocalDateTime getCheckedAt() { return checkedAt; }
        public void setCheckedAt(LocalDateTime checkedAt) { this.checkedAt = checkedAt; }
    }

    public static class TransferHistory {
        private String transferId;
        private String transferType;
        private BigDecimal amount;
        private String currency;
        private BigDecimal fee;
        private String status;
        private String description;
        private LocalDateTime initiatedAt;
        private LocalDateTime completedAt;

        // Getters and setters
        public String getTransferId() { return transferId; }
        public void setTransferId(String transferId) { this.transferId = transferId; }
        public String getTransferType() { return transferType; }
        public void setTransferType(String transferType) { this.transferType = transferType; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public BigDecimal getFee() { return fee; }
        public void setFee(BigDecimal fee) { this.fee = fee; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public LocalDateTime getInitiatedAt() { return initiatedAt; }
        public void setInitiatedAt(LocalDateTime initiatedAt) { this.initiatedAt = initiatedAt; }
        public LocalDateTime getCompletedAt() { return completedAt; }
        public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    }
}

