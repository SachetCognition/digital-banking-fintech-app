package com.yourorg.banking.loan.model.dto;

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
public class CreditLineResponse {
    private UUID id;
    private String creditLineNumber;
    private BigDecimal creditLimit;
    private BigDecimal availableCredit;
    private BigDecimal usedCredit;
    private BigDecimal interestRate;
    private String status;
    private Integer creditScore;
    private String riskLevel;
    private BigDecimal annualFee;
    private BigDecimal minimumPaymentRate;
    private Integer gracePeriodDays;
    private BigDecimal utilizationRate;
    private LocalDateTime createdAt;
}

