package com.yourorg.banking.investment.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioHoldingResponse {
    private UUID id;
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal averageCost;
    private BigDecimal currentPrice;
    private BigDecimal marketValue;
    private BigDecimal costBasis;
    private BigDecimal unrealizedPnl;
    private BigDecimal unrealizedPnlPercentage;
    private BigDecimal weightPercentage;
    private LocalDateTime lastUpdated;
}

