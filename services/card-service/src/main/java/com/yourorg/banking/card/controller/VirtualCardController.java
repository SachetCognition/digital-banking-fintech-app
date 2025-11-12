package com.yourorg.banking.card.controller;

import com.yourorg.banking.card.service.VirtualCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cards/virtual")
public class VirtualCardController {

    @Autowired
    private VirtualCardService virtualCardService;

    /**
     * Issue virtual card
     */
    @PostMapping("/issue")
    public ResponseEntity<VirtualCardService.VirtualCard> issueVirtualCard(
            @RequestHeader("X-Request-Id") String requestId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody IssueVirtualCardRequest request) {
        
        try {
            VirtualCardService.VirtualCard card = virtualCardService.issueVirtualCard(
                request.getCustomerId(),
                request.getAccountId(),
                request.getCardType(),
                request.getCardName(),
                request.getSpendingLimit()
            );
            
            return ResponseEntity.ok(card);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Set card controls
     */
    @PutMapping("/{cardId}/controls")
    public ResponseEntity<VirtualCardService.CardControls> setCardControls(
            @PathVariable String cardId,
            @RequestBody SetCardControlsRequest request) {
        
        try {
            VirtualCardService.CardControls controls = virtualCardService.setCardControls(
                cardId,
                request.getDailyLimit(),
                request.getMonthlyLimit(),
                request.getInternationalEnabled(),
                request.getOnlineEnabled(),
                request.getAtmEnabled(),
                request.getContactlessEnabled()
            );
            
            return ResponseEntity.ok(controls);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Block card
     */
    @PostMapping("/{cardId}/block")
    public ResponseEntity<VirtualCardService.CardBlock> blockCard(
            @PathVariable String cardId,
            @RequestBody BlockCardRequest request) {
        
        try {
            VirtualCardService.CardBlock block = virtualCardService.blockCard(cardId, request.getReason());
            return ResponseEntity.ok(block);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Unblock card
     */
    @PostMapping("/{cardId}/unblock")
    public ResponseEntity<VirtualCardService.CardUnblock> unblockCard(
            @PathVariable String cardId,
            @RequestBody UnblockCardRequest request) {
        
        try {
            VirtualCardService.CardUnblock unblock = virtualCardService.unblockCard(cardId, request.getReason());
            return ResponseEntity.ok(unblock);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get card transactions
     */
    @GetMapping("/{cardId}/transactions")
    public ResponseEntity<List<VirtualCardService.CardTransaction>> getCardTransactions(
            @PathVariable String cardId,
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            List<VirtualCardService.CardTransaction> transactions = virtualCardService.getCardTransactions(cardId, limit);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get card spending summary
     */
    @GetMapping("/{cardId}/spending-summary")
    public ResponseEntity<VirtualCardService.CardSpendingSummary> getCardSpendingSummary(
            @PathVariable String cardId,
            @RequestParam(defaultValue = "monthly") String period) {
        
        try {
            VirtualCardService.CardSpendingSummary summary = virtualCardService.getCardSpendingSummary(cardId, period);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get virtual cards for customer
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<VirtualCardService.VirtualCard>> getVirtualCards(
            @PathVariable String customerId) {
        
        try {
            List<VirtualCardService.VirtualCard> cards = virtualCardService.getVirtualCards(customerId);
            return ResponseEntity.ok(cards);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Request DTOs
    public static class IssueVirtualCardRequest {
        private String customerId;
        private String accountId;
        private String cardType;
        private String cardName;
        private BigDecimal spendingLimit;

        // Getters and setters
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        public String getAccountId() { return accountId; }
        public void setAccountId(String accountId) { this.accountId = accountId; }
        public String getCardType() { return cardType; }
        public void setCardType(String cardType) { this.cardType = cardType; }
        public String getCardName() { return cardName; }
        public void setCardName(String cardName) { this.cardName = cardName; }
        public BigDecimal getSpendingLimit() { return spendingLimit; }
        public void setSpendingLimit(BigDecimal spendingLimit) { this.spendingLimit = spendingLimit; }
    }

    public static class SetCardControlsRequest {
        private BigDecimal dailyLimit;
        private BigDecimal monthlyLimit;
        private boolean internationalEnabled;
        private boolean onlineEnabled;
        private boolean atmEnabled;
        private boolean contactlessEnabled;

        // Getters and setters
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
    }

    public static class BlockCardRequest {
        private String reason;

        // Getters and setters
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class UnblockCardRequest {
        private String reason;

        // Getters and setters
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}

