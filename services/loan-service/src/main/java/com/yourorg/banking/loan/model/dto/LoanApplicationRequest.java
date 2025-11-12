package com.yourorg.banking.loan.model.dto;

import com.yourorg.banking.loan.model.EmploymentStatus;
import com.yourorg.banking.loan.model.LoanType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplicationRequest {
    private LoanType loanType;
    private BigDecimal requestedAmount;
    private Integer requestedTermMonths;
    private String purpose;
    private EmploymentStatus employmentStatus;
    private BigDecimal annualIncome;
    private BigDecimal monthlyExpenses;
}

