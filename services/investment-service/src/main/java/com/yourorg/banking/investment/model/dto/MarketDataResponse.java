package com.yourorg.banking.investment.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketDataResponse {
    private String symbol;
    private BigDecimal price;
    private BigDecimal previousClose;
    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private Long volume;
    private Long marketCap;
    private BigDecimal peRatio;
    private BigDecimal dividendYield;
    private BigDecimal priceChange;
    private BigDecimal priceChangePercentage;
    private LocalDateTime lastUpdated;
}

