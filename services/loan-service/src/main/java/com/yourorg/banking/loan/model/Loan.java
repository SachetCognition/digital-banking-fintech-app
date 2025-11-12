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
@Table(name = "loans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    @Column(name = "loan_application_id")
    private UUID loanApplicationId;
    
    @Column(name = "loan_number", unique = true, nullable = false)
    private String loanNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "loan_type", nullable = false)
    private LoanType loanType;
    
    @Column(name = "principal_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal principalAmount;
    
    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;
    
    @Column(name = "term_months", nullable = false)
    private Integer termMonths;
    
    @Column(name = "monthly_payment", nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyPayment;
    
    @Column(name = "remaining_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal remainingBalance;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LoanStatus status;
    
    @Column(name = "disbursement_date")
    private LocalDate disbursementDate;
    
    @Column(name = "maturity_date")
    private LocalDate maturityDate;
    
    @Column(name = "next_payment_date")
    private LocalDate nextPaymentDate;
    
    @Column(name = "grace_period_days")
    private Integer gracePeriodDays;
    
    @Column(name = "late_fee_rate", precision = 5, scale = 2)
    private BigDecimal lateFeeRate;
    
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
    
    public boolean isOverdue() {
        if (nextPaymentDate == null || status != LoanStatus.ACTIVE) {
            return false;
        }
        return LocalDate.now().isAfter(nextPaymentDate.plusDays(gracePeriodDays != null ? gracePeriodDays : 15));
    }
    
    public BigDecimal calculateLateFee() {
        if (!isOverdue() || lateFeeRate == null) {
            return BigDecimal.ZERO;
        }
        return monthlyPayment.multiply(lateFeeRate);
    }
}

