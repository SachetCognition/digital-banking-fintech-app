package com.yourorg.banking.investment.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioPerformanceResponse {
    private LocalDate calculationDate;
    private BigDecimal totalValue;
    private BigDecimal totalCost;
    private BigDecimal totalReturn;
    private BigDecimal totalReturnPercentage;
    private BigDecimal dailyReturn;
    private BigDecimal dailyReturnPercentage;
    private BigDecimal benchmarkReturn;
    private BigDecimal alpha;
    private BigDecimal beta;
    private BigDecimal sharpeRatio;
    private BigDecimal maxDrawdown;
    private BigDecimal volatility;
}

