package com.yourorg.banking.loan.api;

import com.yourorg.banking.loan.model.dto.LoanApplicationRequest;
import com.yourorg.banking.loan.model.dto.LoanApplicationResponse;
import com.yourorg.banking.loan.service.LoanApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/loan-applications")
@RequiredArgsConstructor
@Slf4j
public class LoanApplicationController {
    
    private final LoanApplicationService loanApplicationService;
    
    @PostMapping
    public ResponseEntity<LoanApplicationResponse> submitApplication(
            @AuthenticationPrincipal String customerId,
            @RequestBody LoanApplicationRequest request) {
        log.info("Submitting loan application for customer: {}", customerId);
        
        UUID customerUuid = UUID.fromString(customerId);
        LoanApplicationResponse response = loanApplicationService.submitLoanApplication(customerUuid, request);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<LoanApplicationResponse>> getCustomerApplications(
            @AuthenticationPrincipal String customerId) {
        log.info("Getting loan applications for customer: {}", customerId);
        
        UUID customerUuid = UUID.fromString(customerId);
        List<LoanApplicationResponse> applications = loanApplicationService.getCustomerApplications(customerUuid);
        
        return ResponseEntity.ok(applications);
    }
    
    @GetMapping("/{applicationNumber}")
    public ResponseEntity<LoanApplicationResponse> getApplication(
            @PathVariable String applicationNumber) {
        log.info("Getting loan application: {}", applicationNumber);
        
        LoanApplicationResponse application = loanApplicationService.getApplication(applicationNumber);
        if (application == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(application);
    }
    
    @PostMapping("/{applicationNumber}/approve")
    public ResponseEntity<LoanApplicationResponse> approveApplication(
            @PathVariable String applicationNumber,
            @RequestParam BigDecimal approvedAmount,
            @RequestParam Integer approvedTermMonths) {
        log.info("Approving loan application: {}", applicationNumber);
        
        LoanApplicationResponse response = loanApplicationService.approveApplication(
            applicationNumber, approvedAmount, approvedTermMonths);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{applicationNumber}/reject")
    public ResponseEntity<LoanApplicationResponse> rejectApplication(
            @PathVariable String applicationNumber,
            @RequestParam String rejectionReason) {
        log.info("Rejecting loan application: {}", applicationNumber);
        
        LoanApplicationResponse response = loanApplicationService.rejectApplication(
            applicationNumber, rejectionReason);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Loan Application Service is healthy");
    }
}

