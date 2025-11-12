package com.yourorg.banking.loan.service;

import com.yourorg.banking.loan.model.*;
import com.yourorg.banking.loan.model.dto.LoanApplicationRequest;
import com.yourorg.banking.loan.model.dto.LoanApplicationResponse;
import com.yourorg.banking.loan.repository.LoanApplicationRepository;
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
public class LoanApplicationService {
    
    private final LoanApplicationRepository loanApplicationRepository;
    private final CreditScoringService creditScoringService;
    private final LoanNumberGeneratorService loanNumberGeneratorService;
    
    @Transactional
    public LoanApplicationResponse submitLoanApplication(UUID customerId, LoanApplicationRequest request) {
        log.info("Submitting loan application for customer: {}", customerId);
        
        // Generate application number
        String applicationNumber = loanNumberGeneratorService.generateApplicationNumber();
        
        // Create loan application
        LoanApplication application = LoanApplication.builder()
            .customerId(customerId)
            .applicationNumber(applicationNumber)
            .loanType(request.getLoanType())
            .requestedAmount(request.getRequestedAmount())
            .requestedTermMonths(request.getRequestedTermMonths())
            .purpose(request.getPurpose())
            .employmentStatus(request.getEmploymentStatus())
            .annualIncome(request.getAnnualIncome())
            .monthlyExpenses(request.getMonthlyExpenses())
            .status(LoanApplicationStatus.PENDING)
            .submittedAt(LocalDateTime.now())
            .build();
        
        // Calculate credit score
        CreditScore creditScore = creditScoringService.calculateCreditScore(customerId, CreditScoreType.INTERNAL);
        application.setCreditScore(creditScore.getScore());
        application.setRiskLevel(creditScore.getRiskLevel());
        
        // Auto-approve or reject based on simple rules
        if (shouldAutoApprove(application, creditScore)) {
            application.setStatus(LoanApplicationStatus.APPROVED);
            application.setApprovedAmount(request.getRequestedAmount());
            application.setApprovedTermMonths(request.getRequestedTermMonths());
            application.setInterestRate(calculateInterestRate(creditScore.getScore()));
            application.setApprovedAt(LocalDateTime.now());
        } else if (shouldAutoReject(application, creditScore)) {
            application.setStatus(LoanApplicationStatus.REJECTED);
            application.setRejectionReason("Insufficient credit score or income requirements not met");
            application.setReviewedAt(LocalDateTime.now());
        } else {
            application.setStatus(LoanApplicationStatus.UNDER_REVIEW);
        }
        
        LoanApplication savedApplication = loanApplicationRepository.save(application);
        return convertToResponse(savedApplication);
    }
    
    public List<LoanApplicationResponse> getCustomerApplications(UUID customerId) {
        return loanApplicationRepository.findByCustomerIdOrderBySubmittedAtDesc(customerId)
            .stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    public LoanApplicationResponse getApplication(String applicationNumber) {
        return loanApplicationRepository.findByApplicationNumber(applicationNumber)
            .map(this::convertToResponse)
            .orElse(null);
    }
    
    @Transactional
    public LoanApplicationResponse approveApplication(String applicationNumber, 
                                                   BigDecimal approvedAmount, 
                                                   Integer approvedTermMonths) {
        LoanApplication application = loanApplicationRepository.findByApplicationNumber(applicationNumber)
            .orElseThrow(() -> new RuntimeException("Application not found"));
        
        application.setStatus(LoanApplicationStatus.APPROVED);
        application.setApprovedAmount(approvedAmount);
        application.setApprovedTermMonths(approvedTermMonths);
        application.setInterestRate(calculateInterestRate(application.getCreditScore()));
        application.setApprovedAt(LocalDateTime.now());
        application.setReviewedAt(LocalDateTime.now());
        
        LoanApplication savedApplication = loanApplicationRepository.save(application);
        return convertToResponse(savedApplication);
    }
    
    @Transactional
    public LoanApplicationResponse rejectApplication(String applicationNumber, String rejectionReason) {
        LoanApplication application = loanApplicationRepository.findByApplicationNumber(applicationNumber)
            .orElseThrow(() -> new RuntimeException("Application not found"));
        
        application.setStatus(LoanApplicationStatus.REJECTED);
        application.setRejectionReason(rejectionReason);
        application.setReviewedAt(LocalDateTime.now());
        
        LoanApplication savedApplication = loanApplicationRepository.save(application);
        return convertToResponse(savedApplication);
    }
    
    private boolean shouldAutoApprove(LoanApplication application, CreditScore creditScore) {
        // Simple auto-approval rules
        return creditScore.getScore() >= 700 && 
               application.getRequestedAmount().compareTo(BigDecimal.valueOf(50000)) <= 0 &&
               application.getAnnualIncome() != null &&
               application.getAnnualIncome().compareTo(application.getRequestedAmount().multiply(BigDecimal.valueOf(2))) >= 0;
    }
    
    private boolean shouldAutoReject(LoanApplication application, CreditScore creditScore) {
        // Simple auto-rejection rules
        return creditScore.getScore() < 600 || 
               application.getRequestedAmount().compareTo(BigDecimal.valueOf(100000)) > 0 ||
               (application.getAnnualIncome() != null && 
                application.getAnnualIncome().compareTo(application.getRequestedAmount()) < 0);
    }
    
    private BigDecimal calculateInterestRate(Integer creditScore) {
        if (creditScore >= 750) {
            return BigDecimal.valueOf(4.5); // Excellent rate
        } else if (creditScore >= 700) {
            return BigDecimal.valueOf(5.5); // Good rate
        } else if (creditScore >= 650) {
            return BigDecimal.valueOf(7.0); // Fair rate
        } else {
            return BigDecimal.valueOf(9.0); // Higher rate for lower scores
        }
    }
    
    private LoanApplicationResponse convertToResponse(LoanApplication application) {
        return LoanApplicationResponse.builder()
            .id(application.getId())
            .applicationNumber(application.getApplicationNumber())
            .loanType(application.getLoanType().getDisplayName())
            .requestedAmount(application.getRequestedAmount())
            .requestedTermMonths(application.getRequestedTermMonths())
            .purpose(application.getPurpose())
            .employmentStatus(application.getEmploymentStatus() != null ? 
                application.getEmploymentStatus().getDisplayName() : null)
            .annualIncome(application.getAnnualIncome())
            .monthlyExpenses(application.getMonthlyExpenses())
            .status(application.getStatus().getDisplayName())
            .creditScore(application.getCreditScore())
            .riskLevel(application.getRiskLevel() != null ? 
                application.getRiskLevel().getDisplayName() : null)
            .interestRate(application.getInterestRate())
            .approvedAmount(application.getApprovedAmount())
            .approvedTermMonths(application.getApprovedTermMonths())
            .rejectionReason(application.getRejectionReason())
            .submittedAt(application.getSubmittedAt())
            .reviewedAt(application.getReviewedAt())
            .approvedAt(application.getApprovedAt())
            .build();
    }
}

