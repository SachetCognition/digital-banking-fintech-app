package com.yourorg.banking.compliance.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transaction_monitoring")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionMonitoring {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;
    
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    @Column(name = "transaction_type", nullable = false)
    private String transactionType;
    
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "currency", length = 3)
    private String currency;
    
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;
    
    @Column(name = "risk_score", nullable = false)
    private Integer riskScore;
    
    @Column(name = "risk_factors", columnDefinition = "JSONB")
    private String riskFactors;
    
    @Column(name = "is_flagged", nullable = false)
    private Boolean isFlagged;
    
    @Column(name = "flag_reason", columnDefinition = "TEXT")
    private String flagReason;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", nullable = false)
    private ReviewStatus reviewStatus;
    
    @Column(name = "reviewed_by")
    private String reviewedBy;
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

