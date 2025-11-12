package com.yourorg.banking.loan.api;

import com.yourorg.banking.loan.model.CreditScoreType;
import com.yourorg.banking.loan.model.dto.CreditScoreResponse;
import com.yourorg.banking.loan.service.CreditScoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/credit-scores")
@RequiredArgsConstructor
@Slf4j
public class CreditScoreController {
    
    private final CreditScoringService creditScoringService;
    
    @PostMapping("/calculate")
    public ResponseEntity<CreditScoreResponse> calculateCreditScore(
            @AuthenticationPrincipal String customerId,
            @RequestParam(defaultValue = "INTERNAL") CreditScoreType scoreType) {
        log.info("Calculating credit score for customer: {}", customerId);
        
        UUID customerUuid = UUID.fromString(customerId);
        var creditScore = creditScoringService.calculateCreditScore(customerUuid, scoreType);
        
        CreditScoreResponse response = CreditScoreResponse.builder()
            .id(creditScore.getId())
            .score(creditScore.getScore())
            .scoreType(creditScore.getScoreType().getDisplayName())
            .riskLevel(creditScore.getRiskLevel().getDisplayName())
            .factors(creditScore.getFactors())
            .calculatedAt(creditScore.getCalculatedAt())
            .expiresAt(creditScore.getExpiresAt())
            .isExpired(creditScore.isExpired())
            .isGoodScore(creditScore.isGoodScore())
            .isExcellentScore(creditScore.isExcellentScore())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<CreditScoreResponse>> getCustomerCreditScores(
            @AuthenticationPrincipal String customerId) {
        log.info("Getting credit scores for customer: {}", customerId);
        
        UUID customerUuid = UUID.fromString(customerId);
        List<CreditScoreResponse> scores = creditScoringService.getCustomerCreditScores(customerUuid)
            .stream()
            .map(creditScore -> CreditScoreResponse.builder()
                .id(creditScore.getId())
                .score(creditScore.getScore())
                .scoreType(creditScore.getScoreType().getDisplayName())
                .riskLevel(creditScore.getRiskLevel().getDisplayName())
                .factors(creditScore.getFactors())
                .calculatedAt(creditScore.getCalculatedAt())
                .expiresAt(creditScore.getExpiresAt())
                .isExpired(creditScore.isExpired())
                .isGoodScore(creditScore.isGoodScore())
                .isExcellentScore(creditScore.isExcellentScore())
                .build())
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(scores);
    }
    
    @GetMapping("/latest")
    public ResponseEntity<CreditScoreResponse> getLatestCreditScore(
            @AuthenticationPrincipal String customerId,
            @RequestParam(defaultValue = "INTERNAL") CreditScoreType scoreType) {
        log.info("Getting latest credit score for customer: {}", customerId);
        
        UUID customerUuid = UUID.fromString(customerId);
        var creditScore = creditScoringService.getLatestCreditScore(customerUuid, scoreType);
        
        if (creditScore == null) {
            return ResponseEntity.notFound().build();
        }
        
        CreditScoreResponse response = CreditScoreResponse.builder()
            .id(creditScore.getId())
            .score(creditScore.getScore())
            .scoreType(creditScore.getScoreType().getDisplayName())
            .riskLevel(creditScore.getRiskLevel().getDisplayName())
            .factors(creditScore.getFactors())
            .calculatedAt(creditScore.getCalculatedAt())
            .expiresAt(creditScore.getExpiresAt())
            .isExpired(creditScore.isExpired())
            .isGoodScore(creditScore.isGoodScore())
            .isExcellentScore(creditScore.isExcellentScore())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/valid")
    public ResponseEntity<CreditScoreResponse> getValidCreditScore(
            @AuthenticationPrincipal String customerId) {
        log.info("Getting valid credit score for customer: {}", customerId);
        
        UUID customerUuid = UUID.fromString(customerId);
        var creditScore = creditScoringService.getValidCreditScore(customerUuid);
        
        if (creditScore == null) {
            return ResponseEntity.notFound().build();
        }
        
        CreditScoreResponse response = CreditScoreResponse.builder()
            .id(creditScore.getId())
            .score(creditScore.getScore())
            .scoreType(creditScore.getScoreType().getDisplayName())
            .riskLevel(creditScore.getRiskLevel().getDisplayName())
            .factors(creditScore.getFactors())
            .calculatedAt(creditScore.getCalculatedAt())
            .expiresAt(creditScore.getExpiresAt())
            .isExpired(creditScore.isExpired())
            .isGoodScore(creditScore.isGoodScore())
            .isExcellentScore(creditScore.isExcellentScore())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Credit Score Service is healthy");
    }
}

