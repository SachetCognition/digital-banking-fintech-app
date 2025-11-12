package com.yourorg.banking.loan.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanResponse {
    private UUID id;
    private String loanNumber;
    private String loanType;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Integer termMonths;
    private BigDecimal monthlyPayment;
    private BigDecimal remainingBalance;
    private String status;
    private LocalDate disbursementDate;
    private LocalDate maturityDate;
    private LocalDate nextPaymentDate;
    private Integer gracePeriodDays;
    private BigDecimal lateFeeRate;
    private LocalDateTime createdAt;
}

