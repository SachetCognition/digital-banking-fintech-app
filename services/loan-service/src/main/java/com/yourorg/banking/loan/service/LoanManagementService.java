package com.yourorg.banking.loan.service;

import com.yourorg.banking.loan.model.*;
import com.yourorg.banking.loan.model.dto.LoanResponse;
import com.yourorg.banking.loan.model.dto.PaymentRequest;
import com.yourorg.banking.loan.repository.LoanRepository;
import com.yourorg.banking.loan.repository.LoanPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanManagementService {
    
    private final LoanRepository loanRepository;
    private final LoanPaymentRepository loanPaymentRepository;
    private final InterestCalculationService interestCalculationService;
    private final LoanNumberGeneratorService loanNumberGeneratorService;
    
    @Transactional
    public LoanResponse createLoanFromApplication(UUID customerId, UUID applicationId, 
                                                BigDecimal approvedAmount, 
                                                BigDecimal interestRate, 
                                                Integer termMonths) {
        log.info("Creating loan from application for customer: {}", customerId);
        
        String loanNumber = loanNumberGeneratorService.generateLoanNumber();
        BigDecimal monthlyPayment = interestCalculationService.calculateLoanPayment(
            approvedAmount, interestRate, termMonths);
        
        LocalDate disbursementDate = LocalDate.now();
        LocalDate maturityDate = disbursementDate.plusMonths(termMonths);
        LocalDate nextPaymentDate = disbursementDate.plusMonths(1);
        
        Loan loan = Loan.builder()
            .customerId(customerId)
            .loanApplicationId(applicationId)
            .loanNumber(loanNumber)
            .loanType(LoanType.PERSONAL) // Default type, should come from application
            .principalAmount(approvedAmount)
            .interestRate(interestRate)
            .termMonths(termMonths)
            .monthlyPayment(monthlyPayment)
            .remainingBalance(approvedAmount)
            .status(LoanStatus.ACTIVE)
            .disbursementDate(disbursementDate)
            .maturityDate(maturityDate)
            .nextPaymentDate(nextPaymentDate)
            .gracePeriodDays(15)
            .lateFeeRate(BigDecimal.valueOf(0.05))
            .build();
        
        Loan savedLoan = loanRepository.save(loan);
        
        // Generate payment schedule
        generatePaymentSchedule(savedLoan);
        
        return convertToResponse(savedLoan);
    }
    
    public List<LoanResponse> getCustomerLoans(UUID customerId) {
        return loanRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
            .stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    public LoanResponse getLoan(String loanNumber) {
        return loanRepository.findByLoanNumber(loanNumber)
            .map(this::convertToResponse)
            .orElse(null);
    }
    
    @Transactional
    public void processPayment(UUID loanId, PaymentRequest paymentRequest) {
        log.info("Processing payment for loan: {}", loanId);
        
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found"));
        
        // Find the next due payment
        List<LoanPayment> duePayments = loanPaymentRepository.findByLoanIdAndStatus(
            loanId, PaymentStatus.DUE);
        
        if (duePayments.isEmpty()) {
            throw new RuntimeException("No due payments found for this loan");
        }
        
        LoanPayment payment = duePayments.get(0);
        
        // Process payment
        BigDecimal paidAmount = payment.getPaidAmount() != null ? 
            payment.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal newPaidAmount = paidAmount.add(paymentRequest.getAmount());
        
        payment.setPaidAmount(newPaidAmount);
        payment.setPaidDate(paymentRequest.getPaymentDate());
        payment.setPaymentMethod(paymentRequest.getPaymentMethod());
        payment.setReferenceNumber(paymentRequest.getReferenceNumber());
        
        if (newPaidAmount.compareTo(payment.getTotalAmount()) >= 0) {
            payment.setStatus(PaymentStatus.PAID);
            updateLoanBalance(loan, payment.getPrincipalAmount());
        } else {
            payment.setStatus(PaymentStatus.PARTIAL);
        }
        
        loanPaymentRepository.save(payment);
        
        // Update next payment date
        updateNextPaymentDate(loan);
    }
    
    public List<LoanPayment> getLoanPayments(UUID loanId) {
        return loanPaymentRepository.findByLoanIdOrderByScheduledDateAsc(loanId);
    }
    
    public List<Loan> getOverdueLoans() {
        return loanRepository.findOverdueLoans(LocalDate.now());
    }
    
    @Transactional
    public void processOverdueLoans() {
        List<Loan> overdueLoans = getOverdueLoans();
        
        for (Loan loan : overdueLoans) {
            // Calculate late fees
            BigDecimal lateFee = loan.calculateLateFee();
            if (lateFee.compareTo(BigDecimal.ZERO) > 0) {
                // Add late fee to next payment
                addLateFeeToNextPayment(loan, lateFee);
            }
            
            // Update loan status if severely overdue
            if (isSeverelyOverdue(loan)) {
                loan.setStatus(LoanStatus.DEFAULTED);
                loanRepository.save(loan);
            }
        }
    }
    
    private void generatePaymentSchedule(Loan loan) {
        BigDecimal monthlyPayment = loan.getMonthlyPayment();
        BigDecimal remainingBalance = loan.getRemainingBalance();
        BigDecimal monthlyRate = loan.getInterestRate().divide(BigDecimal.valueOf(12), 6, BigDecimal.ROUND_HALF_UP);
        
        for (int i = 1; i <= loan.getTermMonths(); i++) {
            LocalDate scheduledDate = loan.getDisbursementDate().plusMonths(i);
            
            BigDecimal interestAmount = remainingBalance.multiply(monthlyRate);
            BigDecimal principalAmount = monthlyPayment.subtract(interestAmount);
            
            // Ensure last payment covers remaining balance
            if (i == loan.getTermMonths()) {
                principalAmount = remainingBalance;
            }
            
            LoanPayment payment = LoanPayment.builder()
                .loanId(loan.getId())
                .paymentNumber(i)
                .scheduledDate(scheduledDate)
                .principalAmount(principalAmount)
                .interestAmount(interestAmount)
                .totalAmount(monthlyPayment)
                .status(PaymentStatus.PENDING)
                .build();
            
            loanPaymentRepository.save(payment);
            
            remainingBalance = remainingBalance.subtract(principalAmount);
        }
    }
    
    private void updateLoanBalance(Loan loan, BigDecimal principalPaid) {
        BigDecimal newBalance = loan.getRemainingBalance().subtract(principalPaid);
        loan.setRemainingBalance(newBalance);
        
        if (newBalance.compareTo(BigDecimal.ZERO) <= 0) {
            loan.setStatus(LoanStatus.PAID_OFF);
        }
        
        loanRepository.save(loan);
    }
    
    private void updateNextPaymentDate(Loan loan) {
        List<LoanPayment> pendingPayments = loanPaymentRepository.findByLoanIdAndStatus(
            loan.getId(), PaymentStatus.PENDING);
        
        if (!pendingPayments.isEmpty()) {
            loan.setNextPaymentDate(pendingPayments.get(0).getScheduledDate());
        } else {
            loan.setStatus(LoanStatus.PAID_OFF);
        }
        
        loanRepository.save(loan);
    }
    
    private void addLateFeeToNextPayment(Loan loan, BigDecimal lateFee) {
        List<LoanPayment> pendingPayments = loanPaymentRepository.findByLoanIdAndStatus(
            loan.getId(), PaymentStatus.PENDING);
        
        if (!pendingPayments.isEmpty()) {
            LoanPayment nextPayment = pendingPayments.get(0);
            nextPayment.setLateFee(lateFee);
            nextPayment.setTotalAmount(nextPayment.getTotalAmount().add(lateFee));
            loanPaymentRepository.save(nextPayment);
        }
    }
    
    private boolean isSeverelyOverdue(Loan loan) {
        if (loan.getNextPaymentDate() == null) {
            return false;
        }
        return LocalDate.now().isAfter(loan.getNextPaymentDate().plusDays(90));
    }
    
    private LoanResponse convertToResponse(Loan loan) {
        return LoanResponse.builder()
            .id(loan.getId())
            .loanNumber(loan.getLoanNumber())
            .loanType(loan.getLoanType().getDisplayName())
            .principalAmount(loan.getPrincipalAmount())
            .interestRate(loan.getInterestRate())
            .termMonths(loan.getTermMonths())
            .monthlyPayment(loan.getMonthlyPayment())
            .remainingBalance(loan.getRemainingBalance())
            .status(loan.getStatus().getDisplayName())
            .disbursementDate(loan.getDisbursementDate())
            .maturityDate(loan.getMaturityDate())
            .nextPaymentDate(loan.getNextPaymentDate())
            .gracePeriodDays(loan.getGracePeriodDays())
            .lateFeeRate(loan.getLateFeeRate())
            .createdAt(loan.getCreatedAt())
            .build();
    }
}

