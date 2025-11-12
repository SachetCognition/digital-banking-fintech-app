package com.yourorg.banking.payments.controller;

import com.yourorg.banking.payments.service.ExternalPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments/external")
public class ExternalPaymentController {

    @Autowired
    private ExternalPaymentService externalPaymentService;

    /**
     * Process external bank transfer
     */
    @PostMapping("/bank-transfer")
    public ResponseEntity<ExternalPaymentService.ExternalBankTransfer> processExternalBankTransfer(
            @RequestHeader("X-Request-Id") String requestId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody ExternalBankTransferRequest request) {
        
        try {
            ExternalPaymentService.ExternalBankTransfer transfer = externalPaymentService.processExternalBankTransfer(
                request.getCustomerId(),
                request.getFromAccountId(),
                request.getToBankAccount(),
                request.getToBankName(),
                request.getToBankCode(),
                request.getAmount(),
                request.getCurrency(),
                request.getDescription()
            );
            
            return ResponseEntity.ok(transfer);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Process international transfer
     */
    @PostMapping("/international-transfer")
    public ResponseEntity<ExternalPaymentService.InternationalTransfer> processInternationalTransfer(
            @RequestHeader("X-Request-Id") String requestId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody InternationalTransferRequest request) {
        
        try {
            ExternalPaymentService.InternationalTransfer transfer = externalPaymentService.processInternationalTransfer(
                request.getCustomerId(),
                request.getFromAccountId(),
                request.getToAccountNumber(),
                request.getToBankName(),
                request.getToBankCode(),
                request.getToCountry(),
                request.getAmount(),
                request.getFromCurrency(),
                request.getToCurrency(),
                request.getDescription()
            );
            
            return ResponseEntity.ok(transfer);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get transfer status
     */
    @GetMapping("/status/{transferId}")
    public ResponseEntity<ExternalPaymentService.TransferStatus> getTransferStatus(
            @PathVariable String transferId,
            @RequestParam String transferType) {
        
        try {
            ExternalPaymentService.TransferStatus status = externalPaymentService.getTransferStatus(transferId, transferType);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get transfer history
     */
    @GetMapping("/history")
    public ResponseEntity<List<ExternalPaymentService.TransferHistory>> getTransferHistory(
            @RequestParam String customerId,
            @RequestParam String transferType,
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            List<ExternalPaymentService.TransferHistory> history = externalPaymentService.getTransferHistory(customerId, transferType, limit);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Request DTOs
    public static class ExternalBankTransferRequest {
        private String customerId;
        private String fromAccountId;
        private String toBankAccount;
        private String toBankName;
        private String toBankCode;
        private BigDecimal amount;
        private String currency;
        private String description;

        // Getters and setters
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
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class InternationalTransferRequest {
        private String customerId;
        private String fromAccountId;
        private String toAccountNumber;
        private String toBankName;
        private String toBankCode;
        private String toCountry;
        private BigDecimal amount;
        private String fromCurrency;
        private String toCurrency;
        private String description;

        // Getters and setters
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
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}

