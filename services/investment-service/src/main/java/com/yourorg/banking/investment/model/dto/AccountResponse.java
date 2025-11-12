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
public class AccountResponse {
    private UUID id;
    private String accountNumber;
    private String accountType;
    private String accountName;
    private String status;
    private BigDecimal initialDeposit;
    private BigDecimal currentBalance;
    private BigDecimal availableCash;
    private BigDecimal investedAmount;
    private BigDecimal unrealizedPnl;
    private BigDecimal realizedPnl;
    private BigDecimal totalReturn;
    private BigDecimal totalReturnPercentage;
    private String riskTolerance;
    private String investmentObjective;
    private BigDecimal totalValue;
    private BigDecimal totalPnl;
    private LocalDateTime createdAt;
}

