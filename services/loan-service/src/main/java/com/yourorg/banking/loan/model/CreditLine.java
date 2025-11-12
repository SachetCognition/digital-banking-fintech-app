package com.yourorg.banking.loan.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "credit_lines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditLine {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    @Column(name = "credit_line_number", unique = true, nullable = false)
    private String creditLineNumber;
    
    @Column(name = "credit_limit", nullable = false, precision = 15, scale = 2)
    private BigDecimal creditLimit;
    
    @Column(name = "available_credit", nullable = false, precision = 15, scale = 2)
    private BigDecimal availableCredit;
    
    @Column(name = "used_credit", precision = 15, scale = 2)
    private BigDecimal usedCredit;
    
    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CreditLineStatus status;
    
    @Column(name = "credit_score")
    private Integer creditScore;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level")
    private RiskLevel riskLevel;
    
    @Column(name = "annual_fee", precision = 15, scale = 2)
    private BigDecimal annualFee;
    
    @Column(name = "minimum_payment_rate", precision = 5, scale = 2)
    private BigDecimal minimumPaymentRate;
    
    @Column(name = "grace_period_days")
    private Integer gracePeriodDays;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (usedCredit == null) {
            usedCredit = BigDecimal.ZERO;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public BigDecimal getUtilizationRate() {
        if (creditLimit.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return usedCredit.divide(creditLimit, 4, BigDecimal.ROUND_HALF_UP);
    }
    
    public boolean isOverLimit() {
        return usedCredit.compareTo(creditLimit) > 0;
    }
    
    public BigDecimal getMinimumPayment() {
        if (minimumPaymentRate == null || usedCredit == null) {
            return BigDecimal.ZERO;
        }
        return usedCredit.multiply(minimumPaymentRate);
    }
}

