package com.yourorg.banking.loan.service;

import com.yourorg.banking.loan.model.*;
import com.yourorg.banking.loan.repository.InterestCalculationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterestCalculationService {
    
    private final InterestCalculationRepository interestCalculationRepository;
    
    public InterestCalculation calculateDailyInterest(UUID loanId, UUID creditLineId, 
                                                    BigDecimal principalBalance, 
                                                    BigDecimal interestRate, 
                                                    InterestCalculationMethod method) {
        log.info("Calculating daily interest for loan: {} or credit line: {}", loanId, creditLineId);
        
        BigDecimal dailyRate = interestRate.divide(BigDecimal.valueOf(365), 6, RoundingMode.HALF_UP);
        BigDecimal dailyInterest = principalBalance.multiply(dailyRate);
        
        InterestCalculation calculation = InterestCalculation.builder()
            .loanId(loanId)
            .creditLineId(creditLineId)
            .calculationDate(LocalDate.now())
            .principalBalance(principalBalance)
            .interestRate(interestRate)
            .dailyInterest(dailyInterest)
            .monthlyInterest(calculateMonthlyInterest(principalBalance, interestRate, method))
            .accruedInterest(dailyInterest)
            .calculationMethod(method)
            .build();
        
        return interestCalculationRepository.save(calculation);
    }
    
    public BigDecimal calculateMonthlyInterest(BigDecimal principalBalance, 
                                             BigDecimal interestRate, 
                                             InterestCalculationMethod method) {
        switch (method) {
            case SIMPLE:
                return calculateSimpleInterest(principalBalance, interestRate, 1);
            case COMPOUND_DAILY:
                return calculateCompoundInterest(principalBalance, interestRate, 365);
            case COMPOUND_MONTHLY:
                return calculateCompoundInterest(principalBalance, interestRate, 12);
            case COMPOUND_ANNUALLY:
                return calculateCompoundInterest(principalBalance, interestRate, 1);
            case RULE_78:
                return calculateRule78Interest(principalBalance, interestRate);
            case ACTUARIAL:
                return calculateActuarialInterest(principalBalance, interestRate);
            default:
                return calculateSimpleInterest(principalBalance, interestRate, 1);
        }
    }
    
    public BigDecimal calculateLoanPayment(BigDecimal principal, 
                                         BigDecimal annualRate, 
                                         Integer termMonths) {
        if (annualRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);
        }
        
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);
        BigDecimal rateFactor = BigDecimal.ONE.add(monthlyRate);
        BigDecimal power = rateFactor.pow(termMonths);
        
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(power);
        BigDecimal denominator = power.subtract(BigDecimal.ONE);
        
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }
    
    public BigDecimal calculateTotalInterest(BigDecimal principal, 
                                           BigDecimal annualRate, 
                                           Integer termMonths) {
        BigDecimal monthlyPayment = calculateLoanPayment(principal, annualRate, termMonths);
        BigDecimal totalPayments = monthlyPayment.multiply(BigDecimal.valueOf(termMonths));
        return totalPayments.subtract(principal);
    }
    
    public BigDecimal calculateAmortizationSchedule(UUID loanId, 
                                                  BigDecimal principal, 
                                                  BigDecimal annualRate, 
                                                  Integer termMonths) {
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);
        BigDecimal monthlyPayment = calculateLoanPayment(principal, annualRate, termMonths);
        BigDecimal remainingBalance = principal;
        
        for (int month = 1; month <= termMonths; month++) {
            BigDecimal interestPayment = remainingBalance.multiply(monthlyRate);
            BigDecimal principalPayment = monthlyPayment.subtract(interestPayment);
            remainingBalance = remainingBalance.subtract(principalPayment);
            
            // Save each payment calculation
            calculateDailyInterest(loanId, null, remainingBalance, annualRate, 
                                 InterestCalculationMethod.COMPOUND_MONTHLY);
        }
        
        return remainingBalance;
    }
    
    private BigDecimal calculateSimpleInterest(BigDecimal principal, 
                                             BigDecimal annualRate, 
                                             Integer periods) {
        return principal.multiply(annualRate).divide(BigDecimal.valueOf(periods), 2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateCompoundInterest(BigDecimal principal, 
                                               BigDecimal annualRate, 
                                               Integer compoundingPeriods) {
        BigDecimal ratePerPeriod = annualRate.divide(BigDecimal.valueOf(compoundingPeriods), 6, RoundingMode.HALF_UP);
        BigDecimal factor = BigDecimal.ONE.add(ratePerPeriod);
        BigDecimal compoundFactor = factor.pow(compoundingPeriods);
        return principal.multiply(compoundFactor.subtract(BigDecimal.ONE));
    }
    
    private BigDecimal calculateRule78Interest(BigDecimal principal, 
                                             BigDecimal annualRate) {
        // Rule of 78 is a method for calculating interest on loans
        // This is a simplified implementation
        return principal.multiply(annualRate).multiply(BigDecimal.valueOf(0.5));
    }
    
    private BigDecimal calculateActuarialInterest(BigDecimal principal, 
                                                BigDecimal annualRate) {
        // Actuarial method for interest calculation
        // This is a simplified implementation
        return principal.multiply(annualRate).multiply(BigDecimal.valueOf(0.0833)); // 1/12 of annual
    }
    
    public BigDecimal calculateLateFee(BigDecimal paymentAmount, 
                                     BigDecimal lateFeeRate, 
                                     Integer daysOverdue) {
        if (daysOverdue <= 0) {
            return BigDecimal.ZERO;
        }
        return paymentAmount.multiply(lateFeeRate).multiply(BigDecimal.valueOf(daysOverdue));
    }
}

