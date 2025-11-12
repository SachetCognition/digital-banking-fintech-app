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
@Table(name = "fraud_detection")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudDetection {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;
    
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "fraud_type", nullable = false)
    private FraudType fraudType;
    
    @Column(name = "fraud_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal fraudScore;
    
    @Column(name = "fraud_indicators", columnDefinition = "JSONB")
    private String fraudIndicators;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "detection_method", nullable = false)
    private DetectionMethod detectionMethod;
    
    @Column(name = "is_confirmed", nullable = false)
    private Boolean isConfirmed;
    
    @Column(name = "is_false_positive", nullable = false)
    private Boolean isFalsePositive;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "investigation_status", nullable = false)
    private InvestigationStatus investigationStatus;
    
    @Column(name = "investigator")
    private String investigator;
    
    @Column(name = "investigation_notes", columnDefinition = "TEXT")
    private String investigationNotes;
    
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

