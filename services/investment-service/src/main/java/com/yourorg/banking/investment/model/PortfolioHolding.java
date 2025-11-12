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
@Table(name = "portfolio_holdings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioHolding {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "account_id", nullable = false)
    private UUID accountId;
    
    @Column(name = "symbol", nullable = false)
    private String symbol;
    
    @Column(name = "quantity", nullable = false, precision = 15, scale = 6)
    private BigDecimal quantity;
    
    @Column(name = "average_cost", nullable = false, precision = 15, scale = 4)
    private BigDecimal averageCost;
    
    @Column(name = "current_price", nullable = false, precision = 15, scale = 4)
    private BigDecimal currentPrice;
    
    @Column(name = "market_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal marketValue;
    
    @Column(name = "cost_basis", nullable = false, precision = 15, scale = 2)
    private BigDecimal costBasis;
    
    @Column(name = "unrealized_pnl", nullable = false, precision = 15, scale = 2)
    private BigDecimal unrealizedPnl;
    
    @Column(name = "unrealized_pnl_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal unrealizedPnlPercentage;
    
    @Column(name = "weight_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal weightPercentage;
    
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
    }
    
    public BigDecimal getMarketValue() {
        if (marketValue == null) {
            return quantity.multiply(currentPrice);
        }
        return marketValue;
    }
    
    public BigDecimal getCostBasis() {
        if (costBasis == null) {
            return quantity.multiply(averageCost);
        }
        return costBasis;
    }
    
    public BigDecimal getUnrealizedPnl() {
        if (unrealizedPnl == null) {
            return getMarketValue().subtract(getCostBasis());
        }
        return unrealizedPnl;
    }
    
    public BigDecimal getUnrealizedPnlPercentage() {
        if (unrealizedPnlPercentage == null && getCostBasis().compareTo(BigDecimal.ZERO) != 0) {
            return getUnrealizedPnl().divide(getCostBasis(), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }
        return unrealizedPnlPercentage;
    }
}

