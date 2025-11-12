package com.yourorg.banking.loan.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditLineRequest {
    private BigDecimal requestedCreditLimit;
    private String purpose;
    private Boolean autoIncrease;
}

