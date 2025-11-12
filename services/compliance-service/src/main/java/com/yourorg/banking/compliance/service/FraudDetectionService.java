package com.yourorg.banking.compliance.service;

import com.yourorg.banking.compliance.model.*;
import com.yourorg.banking.compliance.repository.FraudDetectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionService {
    
    private final FraudDetectionRepository fraudDetectionRepository;
    private final AuditLoggingService auditLoggingService;
    
    @Transactional
    public void analyzeTransaction(UUID transactionId, UUID customerId, 
                                 BigDecimal amount, String transactionType) {
        log.info("Analyzing transaction {} for fraud", transactionId);
        
        // Calculate fraud score
        BigDecimal fraudScore = calculateFraudScore(amount, transactionType, customerId);
        
        // Determine fraud type
        FraudType fraudType = determineFraudType(amount, transactionType, fraudScore);
        
        // Check if fraud is detected
        if (fraudScore.compareTo(new BigDecimal("70")) > 0) {
            createFraudDetection(transactionId, customerId, fraudType, fraudScore, 
                               DetectionMethod.RULE_BASED);
        }
    }
    
    @Transactional
    public void createFraudDetection(UUID transactionId, UUID customerId, 
                                   FraudType fraudType, BigDecimal fraudScore, 
                                   DetectionMethod detectionMethod) {
        log.info("Creating fraud detection for transaction: {}", transactionId);
        
        FraudDetection fraudDetection = FraudDetection.builder()
            .transactionId(transactionId)
            .customerId(customerId)
            .fraudType(fraudType)
            .fraudScore(fraudScore)
            .fraudIndicators(generateFraudIndicators(fraudType, fraudScore))
            .detectionMethod(detectionMethod)
            .isConfirmed(false)
            .isFalsePositive(false)
            .investigationStatus(InvestigationStatus.PENDING)
            .build();
        
        FraudDetection savedDetection = fraudDetectionRepository.save(fraudDetection);
        
        // Log audit event
        auditLoggingService.logEvent(
            EventType.SECURITY_VIOLATION,
            EventCategory.SECURITY,
            null,
            customerId,
            "FRAUD_DETECTION",
            savedDetection.getId().toString(),
            "Fraud detection created"
        );
        
        log.info("Fraud detection created: {} for transaction: {}", 
                savedDetection.getId(), transactionId);
    }
    
    public List<FraudDetection> getActiveFraudDetections() {
        return fraudDetectionRepository.findByInvestigationStatusInOrderByCreatedAtDesc(
            List.of(InvestigationStatus.PENDING, InvestigationStatus.ASSIGNED, 
                   InvestigationStatus.IN_PROGRESS));
    }
    
    public List<FraudDetection> getFraudDetectionsByCustomer(UUID customerId) {
        return fraudDetectionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }
    
    @Transactional
    public void updateInvestigationStatus(UUID fraudDetectionId, InvestigationStatus status, 
                                        String investigator, String notes) {
        FraudDetection fraudDetection = fraudDetectionRepository.findById(fraudDetectionId)
            .orElseThrow(() -> new RuntimeException("Fraud detection not found"));
        
        fraudDetection.setInvestigationStatus(status);
        fraudDetection.setInvestigator(investigator);
        fraudDetection.setInvestigationNotes(notes);
        
        fraudDetectionRepository.save(fraudDetection);
        
        // Log audit event
        auditLoggingService.logEvent(
            EventType.SECURITY_VIOLATION,
            EventCategory.SECURITY,
            null,
            fraudDetection.getCustomerId(),
            "FRAUD_DETECTION",
            fraudDetectionId.toString(),
            "Fraud investigation status updated"
        );
    }
    
    @Transactional
    public void confirmFraud(UUID fraudDetectionId, String investigator, String notes) {
        FraudDetection fraudDetection = fraudDetectionRepository.findById(fraudDetectionId)
            .orElseThrow(() -> new RuntimeException("Fraud detection not found"));
        
        fraudDetection.setIsConfirmed(true);
        fraudDetection.setIsFalsePositive(false);
        fraudDetection.setInvestigationStatus(InvestigationStatus.RESOLVED);
        fraudDetection.setInvestigator(investigator);
        fraudDetection.setInvestigationNotes(notes);
        
        fraudDetectionRepository.save(fraudDetection);
        
        // Log audit event
        auditLoggingService.logEvent(
            EventType.SECURITY_VIOLATION,
            EventCategory.SECURITY,
            null,
            fraudDetection.getCustomerId(),
            "FRAUD_DETECTION",
            fraudDetectionId.toString(),
            "Fraud confirmed"
        );
    }
    
    @Transactional
    public void markAsFalsePositive(UUID fraudDetectionId, String investigator, String notes) {
        FraudDetection fraudDetection = fraudDetectionRepository.findById(fraudDetectionId)
            .orElseThrow(() -> new RuntimeException("Fraud detection not found"));
        
        fraudDetection.setIsConfirmed(false);
        fraudDetection.setIsFalsePositive(true);
        fraudDetection.setInvestigationStatus(InvestigationStatus.RESOLVED);
        fraudDetection.setInvestigator(investigator);
        fraudDetection.setInvestigationNotes(notes);
        
        fraudDetectionRepository.save(fraudDetection);
        
        // Log audit event
        auditLoggingService.logEvent(
            EventType.SECURITY_VIOLATION,
            EventCategory.SECURITY,
            null,
            fraudDetection.getCustomerId(),
            "FRAUD_DETECTION",
            fraudDetectionId.toString(),
            "Fraud marked as false positive"
        );
    }
    
    private BigDecimal calculateFraudScore(BigDecimal amount, String transactionType, UUID customerId) {
        double score = 0.0;
        
        // Amount-based scoring
        if (amount.compareTo(new BigDecimal("50000")) > 0) {
            score += 40.0;
        } else if (amount.compareTo(new BigDecimal("10000")) > 0) {
            score += 25.0;
        } else if (amount.compareTo(new BigDecimal("5000")) > 0) {
            score += 15.0;
        }
        
        // Transaction type scoring
        switch (transactionType.toUpperCase()) {
            case "WIRE_TRANSFER":
                score += 30.0;
                break;
            case "CASH_DEPOSIT":
                score += 25.0;
                break;
            case "INTERNATIONAL_TRANSFER":
                score += 35.0;
                break;
            case "CARD_NOT_PRESENT":
                score += 20.0;
                break;
        }
        
        // Add some randomness to simulate ML model
        score += Math.random() * 10.0;
        
        return BigDecimal.valueOf(Math.min(100.0, score));
    }
    
    private FraudType determineFraudType(BigDecimal amount, String transactionType, BigDecimal fraudScore) {
        if (fraudScore.compareTo(new BigDecimal("80")) > 0) {
            return FraudType.MONEY_LAUNDERING;
        } else if (amount.compareTo(new BigDecimal("25000")) > 0) {
            return FraudType.AMOUNT_ANOMALY;
        } else if ("CARD_NOT_PRESENT".equals(transactionType)) {
            return FraudType.CARD_NOT_PRESENT;
        } else {
            return FraudType.PATTERN_ANOMALY;
        }
    }
    
    private String generateFraudIndicators(FraudType fraudType, BigDecimal fraudScore) {
        return String.format("{\"fraudType\": \"%s\", \"score\": %s, \"timestamp\": \"%s\"}", 
                fraudType.name(), fraudScore, LocalDateTime.now());
    }
}

