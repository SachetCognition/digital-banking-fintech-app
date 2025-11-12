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
@Table(name = "market_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketData {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "symbol", nullable = false)
    private String symbol;
    
    @Column(name = "price", nullable = false, precision = 15, scale = 4)
    private BigDecimal price;
    
    @Column(name = "previous_close", precision = 15, scale = 4)
    private BigDecimal previousClose;
    
    @Column(name = "open_price", precision = 15, scale = 4)
    private BigDecimal openPrice;
    
    @Column(name = "high_price", precision = 15, scale = 4)
    private BigDecimal highPrice;
    
    @Column(name = "low_price", precision = 15, scale = 4)
    private BigDecimal lowPrice;
    
    @Column(name = "volume")
    private Long volume;
    
    @Column(name = "market_cap")
    private Long marketCap;
    
    @Column(name = "pe_ratio", precision = 10, scale = 2)
    private BigDecimal peRatio;
    
    @Column(name = "dividend_yield", precision = 5, scale = 4)
    private BigDecimal dividendYield;
    
    @Column(name = "price_change", precision = 15, scale = 4)
    private BigDecimal priceChange;
    
    @Column(name = "price_change_percentage", precision = 5, scale = 2)
    private BigDecimal priceChangePercentage;
    
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public BigDecimal getPriceChange() {
        if (priceChange == null && previousClose != null) {
            return price.subtract(previousClose);
        }
        return priceChange;
    }
    
    public BigDecimal getPriceChangePercentage() {
        if (priceChangePercentage == null && previousClose != null && previousClose.compareTo(BigDecimal.ZERO) != 0) {
            return getPriceChange().divide(previousClose, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }
        return priceChangePercentage;
    }
}

