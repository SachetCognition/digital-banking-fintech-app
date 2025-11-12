package com.yourorg.banking.investment.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "investment_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;
    
    @Column(name = "account_name", nullable = false)
    private String accountName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountStatus status;
    
    @Column(name = "initial_deposit", nullable = false, precision = 15, scale = 2)
    private BigDecimal initialDeposit;
    
    @Column(name = "current_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal currentBalance;
    
    @Column(name = "available_cash", nullable = false, precision = 15, scale = 2)
    private BigDecimal availableCash;
    
    @Column(name = "invested_amount", precision = 15, scale = 2)
    private BigDecimal investedAmount;
    
    @Column(name = "unrealized_pnl", precision = 15, scale = 2)
    private BigDecimal unrealizedPnl;
    
    @Column(name = "realized_pnl", precision = 15, scale = 2)
    private BigDecimal realizedPnl;
    
    @Column(name = "total_return", precision = 15, scale = 2)
    private BigDecimal totalReturn;
    
    @Column(name = "total_return_percentage", precision = 5, scale = 2)
    private BigDecimal totalReturnPercentage;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_tolerance")
    private RiskTolerance riskTolerance;
    
    @Column(name = "investment_objective")
    private String investmentObjective;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (investedAmount == null) {
            investedAmount = BigDecimal.ZERO;
        }
        if (unrealizedPnl == null) {
            unrealizedPnl = BigDecimal.ZERO;
        }
        if (realizedPnl == null) {
            realizedPnl = BigDecimal.ZERO;
        }
        if (totalReturn == null) {
            totalReturn = BigDecimal.ZERO;
        }
        if (totalReturnPercentage == null) {
            totalReturnPercentage = BigDecimal.ZERO;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public BigDecimal getTotalValue() {
        return currentBalance.add(investedAmount != null ? investedAmount : BigDecimal.ZERO);
    }
    
    public BigDecimal getTotalPnl() {
        return (unrealizedPnl != null ? unrealizedPnl : BigDecimal.ZERO)
            .add(realizedPnl != null ? realizedPnl : BigDecimal.ZERO);
    }
}

