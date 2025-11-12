package com.yourorg.banking.loan.service;

import com.yourorg.banking.loan.model.*;
import com.yourorg.banking.loan.repository.CreditScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditScoringService {
    
    private final CreditScoreRepository creditScoreRepository;
    
    public CreditScore calculateCreditScore(UUID customerId, CreditScoreType scoreType) {
        log.info("Calculating credit score for customer: {} with type: {}", customerId, scoreType);
        
        // Simulate credit score calculation based on various factors
        Map<String, Object> factors = new HashMap<>();
        int baseScore = 650; // Base score
        
        // Simulate different scoring factors
        factors.put("payment_history", calculatePaymentHistoryScore());
        factors.put("credit_utilization", calculateCreditUtilizationScore());
        factors.put("credit_history_length", calculateCreditHistoryLengthScore());
        factors.put("credit_mix", calculateCreditMixScore());
        factors.put("new_credit", calculateNewCreditScore());
        
        // Calculate weighted score
        int paymentHistoryScore = (Integer) factors.get("payment_history");
        int utilizationScore = (Integer) factors.get("credit_utilization");
        int historyLengthScore = (Integer) factors.get("credit_history_length");
        int creditMixScore = (Integer) factors.get("credit_mix");
        int newCreditScore = (Integer) factors.get("new_credit");
        
        // Weighted calculation (simplified)
        int finalScore = (int) (baseScore + 
            (paymentHistoryScore * 0.35) + 
            (utilizationScore * 0.30) + 
            (historyLengthScore * 0.15) + 
            (creditMixScore * 0.10) + 
            (newCreditScore * 0.10));
        
        // Ensure score is within valid range
        finalScore = Math.max(300, Math.min(850, finalScore));
        
        RiskLevel riskLevel = RiskLevel.fromCreditScore(finalScore);
        
        CreditScore creditScore = CreditScore.builder()
            .customerId(customerId)
            .score(finalScore)
            .scoreType(scoreType)
            .riskLevel(riskLevel)
            .factors(convertFactorsToJson(factors))
            .calculatedAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusMonths(6)) // Valid for 6 months
            .build();
        
        return creditScoreRepository.save(creditScore);
    }
    
    public CreditScore getLatestCreditScore(UUID customerId, CreditScoreType scoreType) {
        return creditScoreRepository.findTopByCustomerIdAndScoreTypeOrderByCalculatedAtDesc(customerId, scoreType)
            .orElse(null);
    }
    
    public CreditScore getValidCreditScore(UUID customerId) {
        return creditScoreRepository.findValidScoresByCustomerId(customerId, LocalDateTime.now())
            .stream()
            .findFirst()
            .orElse(null);
    }
    
    private int calculatePaymentHistoryScore() {
        // Simulate payment history score (0-100)
        return 75 + (int) (Math.random() * 25);
    }
    
    private int calculateCreditUtilizationScore() {
        // Simulate credit utilization score (0-100)
        return 60 + (int) (Math.random() * 40);
    }
    
    private int calculateCreditHistoryLengthScore() {
        // Simulate credit history length score (0-100)
        return 50 + (int) (Math.random() * 50);
    }
    
    private int calculateCreditMixScore() {
        // Simulate credit mix score (0-100)
        return 40 + (int) (Math.random() * 60);
    }
    
    private int calculateNewCreditScore() {
        // Simulate new credit score (0-100)
        return 30 + (int) (Math.random() * 70);
    }
    
    private String convertFactorsToJson(Map<String, Object> factors) {
        // Simple JSON conversion (in production, use Jackson or Gson)
        StringBuilder json = new StringBuilder("{");
        factors.forEach((key, value) -> {
            json.append("\"").append(key).append("\":").append(value).append(",");
        });
        if (json.length() > 1) {
            json.setLength(json.length() - 1); // Remove last comma
        }
        json.append("}");
        return json.toString();
    }
    
    public boolean isGoodCreditScore(Integer score) {
        return score != null && score >= 700;
    }
    
    public boolean isExcellentCreditScore(Integer score) {
        return score != null && score >= 750;
    }
    
    public List<CreditScore> getCustomerCreditScores(UUID customerId) {
        return creditScoreRepository.findByCustomerIdOrderByCalculatedAtDesc(customerId);
    }
}
