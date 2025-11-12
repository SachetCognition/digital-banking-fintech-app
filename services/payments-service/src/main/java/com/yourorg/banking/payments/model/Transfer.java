package com.yourorg.banking.payments.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transfers")
public class Transfer {
    
    @Id
    private UUID id;
    
    @Column(nullable = false)
    private UUID payerAccountId;
    
    @Column(nullable = false)
    private UUID payeeAccountId;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false, length = 3)
    private String currency;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status;
    
    @Column(length = 255)
    private String description;
    
    @Column(nullable = false, unique = true)
    private String idempotencyKey;
    
    @Column(nullable = false)
    private Instant createdAt;
    
    private Instant completedAt;
    
    private String failureReason;
    
    // Saga state
    @Enumerated(EnumType.STRING)
    private SagaStep currentStep;
    
    private Instant lastUpdatedAt;
    
    // Constructors
    public Transfer() {}
    
    public Transfer(UUID id, UUID payerAccountId, UUID payeeAccountId, BigDecimal amount, 
                   String currency, String description, String idempotencyKey) {
        this.id = id;
        this.payerAccountId = payerAccountId;
        this.payeeAccountId = payeeAccountId;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.idempotencyKey = idempotencyKey;
        this.status = TransferStatus.PENDING;
        this.currentStep = SagaStep.VALIDATION;
        this.createdAt = Instant.now();
        this.lastUpdatedAt = Instant.now();
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getPayerAccountId() { return payerAccountId; }
    public void setPayerAccountId(UUID payerAccountId) { this.payerAccountId = payerAccountId; }
    
    public UUID getPayeeAccountId() { return payeeAccountId; }
    public void setPayeeAccountId(UUID payeeAccountId) { this.payeeAccountId = payeeAccountId; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public TransferStatus getStatus() { return status; }
    public void setStatus(TransferStatus status) { this.status = status; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    
    public SagaStep getCurrentStep() { return currentStep; }
    public void setCurrentStep(SagaStep currentStep) { this.currentStep = currentStep; }
    
    public Instant getLastUpdatedAt() { return lastUpdatedAt; }
    public void setLastUpdatedAt(Instant lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }
    
    // Business methods
    public void markAsCompleted() {
        this.status = TransferStatus.COMPLETED;
        this.completedAt = Instant.now();
        this.lastUpdatedAt = Instant.now();
    }
    
    public void markAsFailed(String reason) {
        this.status = TransferStatus.FAILED;
        this.failureReason = reason;
        this.completedAt = Instant.now();
        this.lastUpdatedAt = Instant.now();
    }
    
    public void updateStep(SagaStep step) {
        this.currentStep = step;
        this.lastUpdatedAt = Instant.now();
    }
}

