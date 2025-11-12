package com.yourorg.banking.loan.api;

import com.yourorg.banking.loan.model.CreditTransactionType;
import com.yourorg.banking.loan.model.dto.CreditLineRequest;
import com.yourorg.banking.loan.model.dto.CreditLineResponse;
import com.yourorg.banking.loan.service.CreditLineManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/credit-lines")
@RequiredArgsConstructor
@Slf4j
public class CreditLineController {
    
    private final CreditLineManagementService creditLineManagementService;
    
    @PostMapping
    public ResponseEntity<CreditLineResponse> createCreditLine(
            @AuthenticationPrincipal String customerId,
            @RequestBody CreditLineRequest request) {
        log.info("Creating credit line for customer: {}", customerId);
        
        UUID customerUuid = UUID.fromString(customerId);
        CreditLineResponse response = creditLineManagementService.createCreditLine(customerUuid, request);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<CreditLineResponse>> getCustomerCreditLines(
            @AuthenticationPrincipal String customerId) {
        log.info("Getting credit lines for customer: {}", customerId);
        
        UUID customerUuid = UUID.fromString(customerId);
        List<CreditLineResponse> creditLines = creditLineManagementService.getCustomerCreditLines(customerUuid);
        
        return ResponseEntity.ok(creditLines);
    }
    
    @GetMapping("/{creditLineNumber}")
    public ResponseEntity<CreditLineResponse> getCreditLine(@PathVariable String creditLineNumber) {
        log.info("Getting credit line: {}", creditLineNumber);
        
        CreditLineResponse creditLine = creditLineManagementService.getCreditLine(creditLineNumber);
        if (creditLine == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(creditLine);
    }
    
    @PostMapping("/{creditLineId}/transactions")
    public ResponseEntity<String> processTransaction(
            @PathVariable UUID creditLineId,
            @RequestParam CreditTransactionType transactionType,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String merchantName) {
        log.info("Processing credit transaction for credit line: {}", creditLineId);
        
        creditLineManagementService.processCreditTransaction(
            creditLineId, transactionType, amount, description, merchantName);
        
        return ResponseEntity.ok("Transaction processed successfully");
    }
    
    @PostMapping("/{creditLineId}/payments")
    public ResponseEntity<String> processPayment(
            @PathVariable UUID creditLineId,
            @RequestParam BigDecimal paymentAmount) {
        log.info("Processing credit payment for credit line: {}", creditLineId);
        
        creditLineManagementService.processCreditPayment(creditLineId, paymentAmount);
        
        return ResponseEntity.ok("Payment processed successfully");
    }
    
    @GetMapping("/{creditLineId}/transactions")
    public ResponseEntity<List<Object>> getCreditTransactions(@PathVariable UUID creditLineId) {
        log.info("Getting transactions for credit line: {}", creditLineId);
        
        List<Object> transactions = (List<Object>) creditLineManagementService.getCreditTransactions(creditLineId);
        
        return ResponseEntity.ok(transactions);
    }
    
    @PutMapping("/{creditLineId}/limit")
    public ResponseEntity<String> updateCreditLimit(
            @PathVariable UUID creditLineId,
            @RequestParam BigDecimal newLimit) {
        log.info("Updating credit limit for credit line: {}", creditLineId);
        
        creditLineManagementService.updateCreditLimit(creditLineId, newLimit);
        
        return ResponseEntity.ok("Credit limit updated successfully");
    }
    
    @GetMapping("/over-limit")
    public ResponseEntity<List<CreditLineResponse>> getOverLimitCreditLines() {
        log.info("Getting over-limit credit lines");
        
        List<CreditLineResponse> overLimitCreditLines = creditLineManagementService.getOverLimitCreditLines()
            .stream()
            .map(creditLine -> {
                // Convert CreditLine to CreditLineResponse
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
            })
            .toList();
        
        return ResponseEntity.ok(overLimitCreditLines);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Credit Line Service is healthy");
    }
}

