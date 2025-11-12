package com.yourorg.banking.loan.model.dto;

import com.yourorg.banking.loan.model.LoanApplicationStatus;
import com.yourorg.banking.loan.model.RiskLevel;
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
public class LoanApplicationResponse {
    private UUID id;
    private String applicationNumber;
    private String loanType;
    private BigDecimal requestedAmount;
    private Integer requestedTermMonths;
    private String purpose;
    private String employmentStatus;
    private BigDecimal annualIncome;
    private BigDecimal monthlyExpenses;
    private String status;
    private Integer creditScore;
    private String riskLevel;
    private BigDecimal interestRate;
    private BigDecimal approvedAmount;
    private Integer approvedTermMonths;
    private String rejectionReason;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime approvedAt;
}

