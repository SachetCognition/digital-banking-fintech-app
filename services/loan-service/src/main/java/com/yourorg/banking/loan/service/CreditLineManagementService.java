package com.yourorg.banking.loan.service;

import com.yourorg.banking.loan.model.*;
import com.yourorg.banking.loan.model.dto.CreditLineRequest;
import com.yourorg.banking.loan.model.dto.CreditLineResponse;
import com.yourorg.banking.loan.repository.CreditLineRepository;
import com.yourorg.banking.loan.repository.CreditTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditLineManagementService {
    
    private final CreditLineRepository creditLineRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final CreditScoringService creditScoringService;
    private final LoanNumberGeneratorService loanNumberGeneratorService;
    
    @Transactional
    public CreditLineResponse createCreditLine(UUID customerId, CreditLineRequest request) {
        log.info("Creating credit line for customer: {}", customerId);
        
        // Get customer's credit score
        CreditScore creditScore = creditScoringService.getValidCreditScore(customerId);
        if (creditScore == null) {
            creditScore = creditScoringService.calculateCreditScore(customerId, CreditScoreType.INTERNAL);
        }
        
        // Calculate credit limit based on score and income
        BigDecimal creditLimit = calculateCreditLimit(request.getRequestedCreditLimit(), creditScore.getScore());
        BigDecimal interestRate = calculateCreditLineInterestRate(creditScore.getScore());
        
        String creditLineNumber = loanNumberGeneratorService.generateCreditLineNumber();
        
        CreditLine creditLine = CreditLine.builder()
            .customerId(customerId)
            .creditLineNumber(creditLineNumber)
            .creditLimit(creditLimit)
            .availableCredit(creditLimit)
            .usedCredit(BigDecimal.ZERO)
            .interestRate(interestRate)
            .status(CreditLineStatus.ACTIVE)
            .creditScore(creditScore.getScore())
            .riskLevel(creditScore.getRiskLevel())
            .annualFee(calculateAnnualFee(creditLimit, creditScore.getScore()))
            .minimumPaymentRate(BigDecimal.valueOf(0.02)) // 2% minimum payment
            .gracePeriodDays(25)
            .build();
        
        CreditLine savedCreditLine = creditLineRepository.save(creditLine);
        return convertToResponse(savedCreditLine);
    }
    
    public List<CreditLineResponse> getCustomerCreditLines(UUID customerId) {
        return creditLineRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
            .stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    public CreditLineResponse getCreditLine(String creditLineNumber) {
        return creditLineRepository.findByCreditLineNumber(creditLineNumber)
            .map(this::convertToResponse)
            .orElse(null);
    }
    
    @Transactional
    public void processCreditTransaction(UUID creditLineId, 
                                       CreditTransactionType transactionType, 
                                       BigDecimal amount, 
                                       String description, 
                                       String merchantName) {
        log.info("Processing credit transaction for credit line: {}", creditLineId);
        
        CreditLine creditLine = creditLineRepository.findById(creditLineId)
            .orElseThrow(() -> new RuntimeException("Credit line not found"));
        
        // Check if transaction is allowed
        if (transactionType == CreditTransactionType.PURCHASE || 
            transactionType == CreditTransactionType.CASH_ADVANCE) {
            if (creditLine.getAvailableCredit().compareTo(amount) < 0) {
                throw new RuntimeException("Insufficient available credit");
            }
        }
        
        // Create transaction
        CreditTransaction transaction = CreditTransaction.builder()
            .creditLineId(creditLineId)
            .transactionType(transactionType)
            .amount(amount)
            .description(description)
            .merchantName(merchantName)
            .transactionDate(LocalDateTime.now())
            .status(TransactionStatus.COMPLETED)
            .build();
        
        creditTransactionRepository.save(transaction);
        
        // Update credit line balances
        updateCreditLineBalances(creditLine, transactionType, amount);
    }
    
    @Transactional
    public void processCreditPayment(UUID creditLineId, BigDecimal paymentAmount) {
        log.info("Processing credit payment for credit line: {}", creditLineId);
        
        CreditLine creditLine = creditLineRepository.findById(creditLineId)
            .orElseThrow(() -> new RuntimeException("Credit line not found"));
        
        // Create payment transaction
        CreditTransaction payment = CreditTransaction.builder()
            .creditLineId(creditLineId)
            .transactionType(CreditTransactionType.PAYMENT)
            .amount(paymentAmount)
            .description("Credit line payment")
            .transactionDate(LocalDateTime.now())
            .status(TransactionStatus.COMPLETED)
            .build();
        
        creditTransactionRepository.save(payment);
        
        // Update credit line balances
        updateCreditLineBalances(creditLine, CreditTransactionType.PAYMENT, paymentAmount);
    }
    
    public List<CreditTransaction> getCreditTransactions(UUID creditLineId) {
        return creditTransactionRepository.findByCreditLineIdOrderByTransactionDateDesc(creditLineId);
    }
    
    @Transactional
    public void updateCreditLimit(UUID creditLineId, BigDecimal newLimit) {
        CreditLine creditLine = creditLineRepository.findById(creditLineId)
            .orElseThrow(() -> new RuntimeException("Credit line not found"));
        
        BigDecimal oldLimit = creditLine.getCreditLimit();
        creditLine.setCreditLimit(newLimit);
        creditLine.setAvailableCredit(newLimit.subtract(creditLine.getUsedCredit()));
        
        creditLineRepository.save(creditLine);
        
        log.info("Updated credit limit for credit line {} from {} to {}", 
                creditLineId, oldLimit, newLimit);
    }
    
    public List<CreditLine> getOverLimitCreditLines() {
        return creditLineRepository.findOverLimitCreditLines();
    }
    
    private BigDecimal calculateCreditLimit(BigDecimal requestedLimit, Integer creditScore) {
        // Base limit calculation based on credit score
        BigDecimal baseLimit;
        if (creditScore >= 750) {
            baseLimit = BigDecimal.valueOf(50000);
        } else if (creditScore >= 700) {
            baseLimit = BigDecimal.valueOf(25000);
        } else if (creditScore >= 650) {
            baseLimit = BigDecimal.valueOf(10000);
        } else {
            baseLimit = BigDecimal.valueOf(5000);
        }
        
        // Use requested limit if it's within reasonable bounds
        if (requestedLimit != null && 
            requestedLimit.compareTo(BigDecimal.ZERO) > 0 && 
            requestedLimit.compareTo(baseLimit.multiply(BigDecimal.valueOf(2))) <= 0) {
            return requestedLimit;
        }
        
        return baseLimit;
    }
    
    private BigDecimal calculateCreditLineInterestRate(Integer creditScore) {
        if (creditScore >= 750) {
            return BigDecimal.valueOf(12.99); // Prime + 7.99
        } else if (creditScore >= 700) {
            return BigDecimal.valueOf(15.99); // Prime + 10.99
        } else if (creditScore >= 650) {
            return BigDecimal.valueOf(19.99); // Prime + 14.99
        } else {
            return BigDecimal.valueOf(24.99); // Prime + 19.99
        }
    }
    
    private BigDecimal calculateAnnualFee(BigDecimal creditLimit, Integer creditScore) {
        if (creditScore >= 750) {
            return BigDecimal.ZERO; // No annual fee for excellent credit
        } else if (creditScore >= 700) {
            return BigDecimal.valueOf(25); // Low annual fee
        } else {
            return BigDecimal.valueOf(95); // Standard annual fee
        }
    }
    
    private void updateCreditLineBalances(CreditLine creditLine, 
                                        CreditTransactionType transactionType, 
                                        BigDecimal amount) {
        switch (transactionType) {
            case PURCHASE:
            case CASH_ADVANCE:
            case BALANCE_TRANSFER:
                creditLine.setUsedCredit(creditLine.getUsedCredit().add(amount));
                creditLine.setAvailableCredit(creditLine.getCreditLimit().subtract(creditLine.getUsedCredit()));
                break;
            case PAYMENT:
            case REFUND:
                creditLine.setUsedCredit(creditLine.getUsedCredit().subtract(amount));
                creditLine.setAvailableCredit(creditLine.getCreditLimit().subtract(creditLine.getUsedCredit()));
                break;
        }
        
        // Check for over-limit status
        if (creditLine.getUsedCredit().compareTo(creditLine.getCreditLimit()) > 0) {
            creditLine.setStatus(CreditLineStatus.OVER_LIMIT);
        } else if (creditLine.getStatus() == CreditLineStatus.OVER_LIMIT) {
            creditLine.setStatus(CreditLineStatus.ACTIVE);
        }
        
        creditLineRepository.save(creditLine);
    }
    
    private CreditLineResponse convertToResponse(CreditLine creditLine) {
        return CreditLineResponse.builder()
            .id(creditLine.getId())
            .creditLineNumber(creditLine.getCreditLineNumber())
            .creditLimit(creditLine.getCreditLimit())
            .availableCredit(creditLine.getAvailableCredit())
            .usedCredit(creditLine.getUsedCredit())
            .interestRate(creditLine.getInterestRate())
            .status(creditLine.getStatus().getDisplayName())
            .creditScore(creditLine.getCreditScore())
            .riskLevel(creditLine.getRiskLevel() != null ? 
                creditLine.getRiskLevel().getDisplayName() : null)
            .annualFee(creditLine.getAnnualFee())
            .minimumPaymentRate(creditLine.getMinimumPaymentRate())
            .gracePeriodDays(creditLine.getGracePeriodDays())
            .utilizationRate(creditLine.getUtilizationRate())
            .createdAt(creditLine.getCreatedAt())
            .build();
    }
}

