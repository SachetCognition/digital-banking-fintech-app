package com.yourorg.banking.compliance.service;

import com.yourorg.banking.compliance.model.*;
import com.yourorg.banking.compliance.repository.AmlCaseRepository;
import com.yourorg.banking.compliance.repository.AmlRuleRepository;
import com.yourorg.banking.compliance.repository.AmlRuleViolationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AmlMonitoringService {
    
    private final AmlCaseRepository amlCaseRepository;
    private final AmlRuleRepository amlRuleRepository;
    private final AmlRuleViolationRepository amlRuleViolationRepository;
    private final AuditLoggingService auditLoggingService;
    private final CaseNumberGeneratorService caseNumberGeneratorService;
    
    @KafkaListener(topics = "transaction-events", groupId = "compliance-service")
    public void processTransactionEvent(String transactionEvent) {
        log.info("Processing transaction event for AML monitoring: {}", transactionEvent);
        
        // Parse transaction event and check against AML rules
        // This is a simplified implementation - in reality, you'd parse the JSON event
        try {
            // For now, we'll simulate processing
            processTransactionForAml(UUID.randomUUID(), UUID.randomUUID(), 
                "TRANSFER", new BigDecimal("15000"), LocalDateTime.now());
        } catch (Exception e) {
            log.error("Error processing transaction event: {}", e.getMessage());
        }
    }
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void processPendingTransactions() {
        log.info("Processing pending transactions for AML monitoring");
        
        // In a real implementation, this would:
        // 1. Query pending transactions from the database
        // 2. Apply AML rules to each transaction
        // 3. Create cases for violations
        // 4. Update transaction status
        
        // For now, we'll simulate some processing
        List<AmlRule> activeRules = amlRuleRepository.findByIsActiveTrue();
        log.info("Found {} active AML rules", activeRules.size());
    }
    
    @Transactional
    public void processTransactionForAml(UUID transactionId, UUID customerId, 
                                       String transactionType, BigDecimal amount, 
                                       LocalDateTime transactionDate) {
        log.info("Processing transaction {} for AML monitoring", transactionId);
        
        List<AmlRule> activeRules = amlRuleRepository.findByIsActiveTrue();
        
        for (AmlRule rule : activeRules) {
            if (isRuleViolated(rule, amount, transactionDate)) {
                createAmlCase(rule, customerId, transactionId, amount, transactionDate);
            }
        }
    }
    
    private boolean isRuleViolated(AmlRule rule, BigDecimal amount, LocalDateTime transactionDate) {
        // Check amount threshold
        if (rule.getThresholdAmount() != null && 
            amount.compareTo(rule.getThresholdAmount()) >= 0) {
            return true;
        }
        
        // Check time-based rules (simplified)
        if (rule.getTimeWindowMinutes() != null) {
            // In a real implementation, you'd check transaction velocity
            // within the time window
            return false;
        }
        
        return false;
    }
    
    @Transactional
    public void createAmlCase(AmlRule rule, UUID customerId, UUID transactionId, 
                            BigDecimal amount, LocalDateTime transactionDate) {
        log.info("Creating AML case for rule: {} and customer: {}", rule.getRuleName(), customerId);
        
        // Check if case already exists for this transaction
        if (amlCaseRepository.existsByCustomerIdAndDescriptionContaining(
            customerId, transactionId.toString())) {
            log.warn("AML case already exists for transaction: {}", transactionId);
            return;
        }
        
        // Create new AML case
        AmlCase amlCase = AmlCase.builder()
            .caseNumber(caseNumberGeneratorService.generateCaseNumber())
            .customerId(customerId)
            .caseType(determineCaseType(rule))
            .status(CaseStatus.OPEN)
            .priority(determinePriority(rule, amount))
            .riskScore(calculateRiskScore(rule, amount))
            .description(String.format("Transaction %s triggered rule: %s", 
                transactionId, rule.getRuleName()))
            .assignedTo(null) // Will be assigned by compliance team
            .build();
        
        AmlCase savedCase = amlCaseRepository.save(amlCase);
        
        // Create rule violation record
        AmlRuleViolation violation = AmlRuleViolation.builder()
            .caseId(savedCase.getId())
            .ruleId(rule.getId())
            .violationType(rule.getRuleType())
            .violationDescription(String.format("Amount %s exceeded threshold %s", 
                amount, rule.getThresholdAmount()))
            .transactionId(transactionId)
            .amount(amount)
            .violationTimestamp(transactionDate)
            .severity(rule.getSeverity())
            .build();
        
        amlRuleViolationRepository.save(violation);
        
        // Log audit event
        auditLoggingService.logEvent(
            EventType.AML_CASE_CREATED,
            EventCategory.COMPLIANCE,
            null,
            customerId,
            "AML_CASE",
            savedCase.getId().toString(),
            "AML case created for transaction",
            null,
            null
        );
        
        log.info("Created AML case: {} for customer: {}", savedCase.getCaseNumber(), customerId);
    }
    
    public List<AmlCase> getOpenCases() {
        return amlCaseRepository.findByStatusOrderByCreatedAtDesc(CaseStatus.OPEN);
    }
    
    public List<AmlCase> getCasesByCustomer(UUID customerId) {
        return amlCaseRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }
    
    @Transactional
    public void updateCaseStatus(UUID caseId, CaseStatus status, String resolutionNotes) {
        AmlCase amlCase = amlCaseRepository.findById(caseId)
            .orElseThrow(() -> new RuntimeException("AML case not found"));
        
        amlCase.setStatus(status);
        amlCase.setResolutionNotes(resolutionNotes);
        
        if (status == CaseStatus.CLOSED) {
            amlCase.setClosedAt(LocalDateTime.now());
        }
        
        amlCaseRepository.save(amlCase);
        
        // Log audit event
        auditLoggingService.logEvent(
            EventType.AML_CASE_UPDATED,
            EventCategory.COMPLIANCE,
            null,
            amlCase.getCustomerId(),
            "AML_CASE",
            amlCase.getId().toString(),
            "AML case status updated",
            null,
            null
        );
    }
    
    private CaseType determineCaseType(AmlRule rule) {
        // Map rule types to case types
        switch (rule.getRuleType()) {
            case "LARGE_TRANSACTION":
                return CaseType.LARGE_TRANSACTION;
            case "VELOCITY_ANOMALY":
                return CaseType.VELOCITY_ANOMALY;
            case "UNUSUAL_PATTERN":
                return CaseType.UNUSUAL_PATTERN;
            case "STRUCTURING":
                return CaseType.STRUCTURING;
            default:
                return CaseType.SUSPICIOUS_ACTIVITY;
        }
    }
    
    private Priority determinePriority(AmlRule rule, BigDecimal amount) {
        if ("HIGH".equals(rule.getSeverity())) {
            return Priority.HIGH;
        } else if ("MEDIUM".equals(rule.getSeverity())) {
            return Priority.MEDIUM;
        } else {
            return Priority.LOW;
        }
    }
    
    private Integer calculateRiskScore(AmlRule rule, BigDecimal amount) {
        int baseScore = 0;
        
        // Base score from rule severity
        switch (rule.getSeverity()) {
            case "HIGH":
                baseScore = 80;
                break;
            case "MEDIUM":
                baseScore = 50;
                break;
            case "LOW":
                baseScore = 20;
                break;
        }
        
        // Adjust based on amount
        if (amount.compareTo(new BigDecimal("50000")) > 0) {
            baseScore += 20;
        } else if (amount.compareTo(new BigDecimal("10000")) > 0) {
            baseScore += 10;
        }
        
        return Math.min(100, baseScore);
    }
}

