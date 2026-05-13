package com.yourorg.banking.loan.api;

import com.yourorg.banking.loan.model.dto.LoanResponse;
import com.yourorg.banking.loan.model.dto.PaymentRequest;
import com.yourorg.banking.loan.service.LoanManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
@Slf4j
public class LoanController {
    
    private final LoanManagementService loanManagementService;
    
    @GetMapping
    public ResponseEntity<List<LoanResponse>> getCustomerLoans(
            @AuthenticationPrincipal String customerId) {
        log.info("Getting loans for customer: {}", customerId);
        
        UUID customerUuid = UUID.fromString(customerId);
        List<LoanResponse> loans = loanManagementService.getCustomerLoans(customerUuid);
        
        return ResponseEntity.ok(loans);
    }
    
    @GetMapping("/{loanNumber}")
    public ResponseEntity<LoanResponse> getLoan(@PathVariable String loanNumber) {
        log.info("Getting loan: {}", loanNumber);
        
        LoanResponse loan = loanManagementService.getLoan(loanNumber);
        if (loan == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(loan);
    }
    
    @PostMapping("/{loanId}/payments")
    public ResponseEntity<String> processPayment(
            @PathVariable UUID loanId,
            @RequestBody PaymentRequest paymentRequest) {
        log.info("Processing payment for loan: {}", loanId);
        
        loanManagementService.processPayment(loanId, paymentRequest);
        
        return ResponseEntity.ok("Payment processed successfully");
    }
    
    @GetMapping("/{loanId}/payments")
    public ResponseEntity<List<Object>> getLoanPayments(@PathVariable UUID loanId) {
        log.info("Getting payments for loan: {}", loanId);
        
        List<Object> payments = new java.util.ArrayList<>(loanManagementService.getLoanPayments(loanId));
        
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/overdue")
    public ResponseEntity<List<LoanResponse>> getOverdueLoans() {
        log.info("Getting overdue loans");
        
        List<LoanResponse> overdueLoans = loanManagementService.getOverdueLoans()
            .stream()
            .map(loan -> {
                // Convert Loan to LoanResponse
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
            })
            .toList();
        
        return ResponseEntity.ok(overdueLoans);
    }
    
    @PostMapping("/process-overdue")
    public ResponseEntity<String> processOverdueLoans() {
        log.info("Processing overdue loans");
        
        loanManagementService.processOverdueLoans();
        
        return ResponseEntity.ok("Overdue loans processed successfully");
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Loan Service is healthy");
    }
}

