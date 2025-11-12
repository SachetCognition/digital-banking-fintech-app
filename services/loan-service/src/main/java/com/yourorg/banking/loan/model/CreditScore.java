package com.yourorg.banking.loan.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "credit_scores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditScore {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    @Column(name = "score", nullable = false)
    private Integer score;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "score_type", nullable = false)
    private CreditScoreType scoreType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;
    
    @Column(name = "factors", columnDefinition = "JSONB")
    private String factors; // JSON string containing scoring factors
    
    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isGoodScore() {
        return score >= 700;
    }
    
    public boolean isExcellentScore() {
        return score >= 750;
    }
}

