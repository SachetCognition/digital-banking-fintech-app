package com.yourorg.banking.investment.model.dto;

import com.yourorg.banking.investment.model.AccountType;
import com.yourorg.banking.investment.model.RiskTolerance;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAccountRequest {
    private AccountType accountType;
    private String accountName;
    private BigDecimal initialDeposit;
    private RiskTolerance riskTolerance;
    private String investmentObjective;
}

