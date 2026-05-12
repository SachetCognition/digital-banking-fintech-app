package com.yourorg.banking.payments.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Dispute {
    private UUID id;
    private UUID transactionId;
    private UUID customerId;
    private String reason;
    private DisputeStatus status;
    private BigDecimal amount;
    private Instant createdAt;
    private Instant updatedAt;

    public enum DisputeStatus {
        OPEN, UNDER_REVIEW, RESOLVED_CUSTOMER, RESOLVED_MERCHANT, CLOSED
    }

    public Dispute() {
        this.id = UUID.randomUUID();
        this.status = DisputeStatus.OPEN;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTransactionId() { return transactionId; }
    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public DisputeStatus getStatus() { return status; }
    public void setStatus(DisputeStatus status) { this.status = status; this.updatedAt = Instant.now(); }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
