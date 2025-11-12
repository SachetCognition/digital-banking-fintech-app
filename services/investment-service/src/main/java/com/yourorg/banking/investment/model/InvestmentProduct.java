package com.yourorg.banking.investment.model;

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
@Table(name = "investment_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "symbol", unique = true, nullable = false)
    private String symbol;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false)
    private ProductType productType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "asset_class", nullable = false)
    private AssetClass assetClass;
    
    @Column(name = "sector")
    private String sector;
    
    @Column(name = "exchange")
    private String exchange;
    
    @Column(name = "currency", length = 3)
    private String currency;
    
    @Column(name = "is_active")
    private Boolean isActive;
    
    @Column(name = "min_investment", precision = 15, scale = 2)
    private BigDecimal minInvestment;
    
    @Column(name = "expense_ratio", precision = 5, scale = 4)
    private BigDecimal expenseRatio;
    
    @Column(name = "management_fee", precision = 5, scale = 4)
    private BigDecimal managementFee;
    
    @Column(name = "inception_date")
    private LocalDate inceptionDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (currency == null) {
            currency = "USD";
        }
        if (isActive == null) {
            isActive = true;
        }
        if (minInvestment == null) {
            minInvestment = BigDecimal.ONE;
        }
        if (expenseRatio == null) {
            expenseRatio = BigDecimal.ZERO;
        }
        if (managementFee == null) {
            managementFee = BigDecimal.ZERO;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

