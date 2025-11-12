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
@Table(name = "loan_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "loan_id", nullable = false)
    private UUID loanId;
    
    @Column(name = "payment_number", nullable = false)
    private Integer paymentNumber;
    
    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;
    
    @Column(name = "principal_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal principalAmount;
    
    @Column(name = "interest_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal interestAmount;
    
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "paid_amount", precision = 15, scale = 2)
    private BigDecimal paidAmount;
    
    @Column(name = "paid_date")
    private LocalDate paidDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;
    
    @Column(name = "late_fee", precision = 15, scale = 2)
    private BigDecimal lateFee;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;
    
    @Column(name = "reference_number")
    private String referenceNumber;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (paidAmount == null) {
            paidAmount = BigDecimal.ZERO;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public boolean isOverdue() {
        if (status == PaymentStatus.PAID || status == PaymentStatus.CANCELLED) {
            return false;
        }
        return LocalDate.now().isAfter(scheduledDate);
    }
    
    public BigDecimal getOutstandingAmount() {
        return totalAmount.subtract(paidAmount != null ? paidAmount : BigDecimal.ZERO);
    }
}

