package com.yourorg.banking.loan.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "interest_calculations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterestCalculation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "loan_id")
    private UUID loanId;
    
    @Column(name = "credit_line_id")
    private UUID creditLineId;
    
    @Column(name = "calculation_date", nullable = false)
    private LocalDate calculationDate;
    
    @Column(name = "principal_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal principalBalance;
    
    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;
    
    @Column(name = "daily_interest", nullable = false, precision = 15, scale = 2)
    private BigDecimal dailyInterest;
    
    @Column(name = "monthly_interest", nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyInterest;
    
    @Column(name = "accrued_interest", nullable = false, precision = 15, scale = 2)
    private BigDecimal accruedInterest;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_method", nullable = false)
    private InterestCalculationMethod calculationMethod;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

