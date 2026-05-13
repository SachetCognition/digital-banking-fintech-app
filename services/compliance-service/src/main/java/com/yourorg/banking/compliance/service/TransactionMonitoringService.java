package com.yourorg.banking.compliance.service;

import com.yourorg.banking.compliance.model.*;
import com.yourorg.banking.compliance.repository.TransactionMonitoringRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionMonitoringService {

    private static final BigDecimal US_THRESHOLD = new BigDecimal("10000");
    private static final BigDecimal UAE_THRESHOLD_AED = new BigDecimal("35000");
    
    private final TransactionMonitoringRepository transactionMonitoringRepository;
    private final AuditLoggingService auditLoggingService;
    private final FraudDetectionService fraudDetectionService;
    
    @KafkaListener(topics = "transaction-events", groupId = "compliance-service")
    public void processTransactionEvent(String transactionEvent) {
        log.info("Processing transaction event for monitoring: {}", transactionEvent);
        
        // Parse transaction event and create monitoring record
        // This is a simplified implementation - in reality, you'd parse the JSON event
        try {
            processTransaction(UUID.randomUUID(), UUID.randomUUID(), 
                "TRANSFER", new BigDecimal("5000"), "USD", LocalDateTime.now());
        } catch (Exception e) {
            log.error("Error processing transaction event: {}", e.getMessage());
        }
    }
    
    @Transactional
    public void processTransaction(UUID transactionId, UUID customerId, 
                                 String transactionType, BigDecimal amount, 
                                 String currency, LocalDateTime transactionDate) {
        log.info("Processing transaction {} for monitoring", transactionId);
        
        // Calculate risk score
        Integer riskScore = calculateRiskScore(amount, transactionType, transactionDate);
        
        // Determine if transaction should be flagged
        boolean isFlagged = shouldFlagTransaction(amount, riskScore);
        String flagReason = isFlagged ? generateFlagReason(amount, riskScore) : null;
        
        // Create monitoring record
        TransactionMonitoring monitoring = TransactionMonitoring.builder()
            .transactionId(transactionId)
            .customerId(customerId)
            .transactionType(transactionType)
            .amount(amount)
            .currency(currency)
            .transactionDate(transactionDate)
            .riskScore(riskScore)
            .riskFactors(generateRiskFactors(amount, transactionType, transactionDate))
            .isFlagged(isFlagged)
            .flagReason(flagReason)
            .reviewStatus(ReviewStatus.PENDING)
            .build();
        
        TransactionMonitoring savedMonitoring = transactionMonitoringRepository.save(monitoring);
        
        // If flagged, trigger fraud detection
        if (isFlagged) {
            fraudDetectionService.analyzeTransaction(transactionId, customerId, amount, transactionType);
        }
        
        // Log audit event
        auditLoggingService.logEvent(
            EventType.TRANSACTION_CREATED,
            EventCategory.TRANSACTION_PROCESSING,
            null,
            customerId,
            "TRANSACTION",
            transactionId.toString(),
            "Transaction processed for monitoring"
        );
        
        log.info("Transaction monitoring record created: {} with risk score: {}", 
                savedMonitoring.getId(), riskScore);
    }
    
    public List<TransactionMonitoring> getFlaggedTransactions() {
        return transactionMonitoringRepository.findByIsFlaggedTrueOrderByCreatedAtDesc();
    }
    
    public List<TransactionMonitoring> getTransactionsByCustomer(UUID customerId) {
        return transactionMonitoringRepository.findByCustomerIdOrderByTransactionDateDesc(customerId);
    }
    
    @Transactional
    public void updateReviewStatus(UUID monitoringId, ReviewStatus status, 
                                 String reviewedBy, String reviewNotes) {
        TransactionMonitoring monitoring = transactionMonitoringRepository.findById(monitoringId)
            .orElseThrow(() -> new RuntimeException("Transaction monitoring record not found"));
        
        monitoring.setReviewStatus(status);
        monitoring.setReviewedBy(reviewedBy);
        monitoring.setReviewedAt(LocalDateTime.now());
        monitoring.setReviewNotes(reviewNotes);
        
        transactionMonitoringRepository.save(monitoring);
        
        // Log audit event
        auditLoggingService.logEvent(
            EventType.TRANSACTION_UPDATED,
            EventCategory.TRANSACTION_PROCESSING,
            null,
            monitoring.getCustomerId(),
            "TRANSACTION_MONITORING",
            monitoringId.toString(),
            "Transaction review status updated"
        );
    }
    
    public BigDecimal getThresholdForJurisdiction(String jurisdiction) {
        if ("UAE".equalsIgnoreCase(jurisdiction) || "AE".equalsIgnoreCase(jurisdiction)) {
            return UAE_THRESHOLD_AED;
        }
        return US_THRESHOLD;
    }

    public int calculateRiskScore(TransactionEvent event) {
        int score = 0;
        BigDecimal amount = event.amount();

        if (amount.compareTo(new BigDecimal("10000")) > 0) {
            score += 40;
        } else if (amount.compareTo(new BigDecimal("5000")) > 0) {
            score += 20;
        } else if (amount.compareTo(new BigDecimal("1000")) > 0) {
            score += 10;
        }

        switch (event.transactionType().toUpperCase()) {
            case "WIRE_TRANSFER", "INTERNATIONAL_WIRE":
                score += 35;
                break;
            case "CASH_DEPOSIT":
                score += 25;
                break;
            case "INTERNATIONAL_TRANSFER":
                score += 35;
                break;
            case "TRANSFER":
                score += 15;
                break;
            default:
                score += 5;
        }

        String country = event.country();
        if (country != null && !country.equalsIgnoreCase("US") && !country.equalsIgnoreCase("GB")
                && !country.equalsIgnoreCase("AE") && !country.equalsIgnoreCase("CA")) {
            score += 20;
        }

        return Math.min(100, score);
    }

    private Integer calculateRiskScore(BigDecimal amount, String transactionType, LocalDateTime transactionDate) {
        int score = 0;
        
        // Amount-based scoring
        if (amount.compareTo(new BigDecimal("10000")) > 0) {
            score += 40;
        } else if (amount.compareTo(new BigDecimal("5000")) > 0) {
            score += 20;
        } else if (amount.compareTo(new BigDecimal("1000")) > 0) {
            score += 10;
        }
        
        // Transaction type scoring
        switch (transactionType.toUpperCase()) {
            case "WIRE_TRANSFER":
                score += 30;
                break;
            case "CASH_DEPOSIT":
                score += 25;
                break;
            case "INTERNATIONAL_TRANSFER":
                score += 35;
                break;
            case "TRANSFER":
                score += 15;
                break;
            default:
                score += 5;
        }
        
        // Time-based scoring (transactions outside business hours)
        int hour = transactionDate.getHour();
        if (hour < 6 || hour > 22) {
            score += 15;
        }
        
        return Math.min(100, score);
    }
    
    private boolean shouldFlagTransaction(BigDecimal amount, Integer riskScore) {
        // Flag if amount exceeds threshold or risk score is high
        return amount.compareTo(new BigDecimal("10000")) > 0 || riskScore > 70;
    }
    
    private String generateFlagReason(BigDecimal amount, Integer riskScore) {
        StringBuilder reason = new StringBuilder();
        
        if (amount.compareTo(new BigDecimal("10000")) > 0) {
            reason.append("Large transaction amount. ");
        }
        
        if (riskScore > 70) {
            reason.append("High risk score. ");
        }
        
        return reason.toString().trim();
    }
    
    private String generateRiskFactors(BigDecimal amount, String transactionType, LocalDateTime transactionDate) {
        // In a real implementation, this would return JSON with detailed risk factors
        return String.format("{\"amount\": %s, \"type\": \"%s\", \"hour\": %d}", 
                amount, transactionType, transactionDate.getHour());
    }
}

