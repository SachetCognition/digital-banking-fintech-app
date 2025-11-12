package com.yourorg.banking.customer.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "kyc_cases")
public class KycCase {
    
    @Id
    private UUID id;
    
    @Column(nullable = false)
    private UUID customerId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycLevel level;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycStatus status;
    
    @Column(length = 64)
    private String provider;
    
    @Column(length = 128)
    private String providerRef;
    
    @Column
    private Integer riskScore;
    
    @Column(columnDefinition = "TEXT")
    private String rejectionReason;
    
    @Column
    private Instant submittedAt;
    
    @Column
    private Instant reviewedAt;
    
    @Column
    private Instant expiresAt;
    
    @Column(nullable = false)
    private Instant createdAt;
    
    @Column(nullable = false)
    private Instant updatedAt;
    
    // Constructors
    public KycCase() {}
    
    public KycCase(UUID id, UUID customerId, KycLevel level) {
        this.id = id;
        this.customerId = customerId;
        this.level = level;
        this.status = KycStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    
    public KycLevel getLevel() { return level; }
    public void setLevel(KycLevel level) { this.level = level; }
    
    public KycStatus getStatus() { return status; }
    public void setStatus(KycStatus status) { this.status = status; }
    
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    
    public String getProviderRef() { return providerRef; }
    public void setProviderRef(String providerRef) { this.providerRef = providerRef; }
    
    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }
    
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    
    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
    
    public Instant getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }
    
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    
    // Business methods
    public void submit(String provider, String providerRef) {
        this.status = KycStatus.SUBMITTED;
        this.provider = provider;
        this.providerRef = providerRef;
        this.submittedAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    public void approve(Integer riskScore) {
        this.status = KycStatus.APPROVED;
        this.riskScore = riskScore;
        this.reviewedAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    public void reject(String reason) {
        this.status = KycStatus.REJECTED;
        this.rejectionReason = reason;
        this.reviewedAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    public void expire() {
        this.status = KycStatus.EXPIRED;
        this.updatedAt = Instant.now();
    }
    
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }
}

