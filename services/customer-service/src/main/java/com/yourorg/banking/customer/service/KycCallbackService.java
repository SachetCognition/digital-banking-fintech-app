package com.yourorg.banking.customer.service;

import com.yourorg.banking.customer.model.DocumentStatus;
import com.yourorg.banking.customer.model.KycStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class KycCallbackService {
    
    private final KycService kycService;
    private final Random random = new Random();
    
    public KycCallbackService(KycService kycService) {
        this.kycService = kycService;
    }
    
    @Async
    public CompletableFuture<Void> scheduleCallback(UUID kycCaseId, String providerRef) {
        // Simulate processing delay (2-10 seconds)
        int delaySeconds = 2 + random.nextInt(8);
        
        try {
            Thread.sleep(delaySeconds * 1000);
            
            // Simulate provider decision with simple rules
            KycCallbackRequest callback = simulateProviderDecision(kycCaseId, providerRef);
            
            // Process the callback
            kycService.processCallback(kycCaseId, callback);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    private KycCallbackRequest simulateProviderDecision(UUID kycCaseId, String providerRef) {
        // Simple rule-based decision making
        boolean approved = random.nextDouble() < 0.7; // 70% approval rate
        
        if (approved) {
            // Simulate approved case
            int riskScore = 20 + random.nextInt(60); // Risk score 20-80
            
            return new KycCallbackRequest(
                "KYC_PROVIDER",
                providerRef,
                KycStatus.APPROVED,
                riskScore,
                null,
                null // No document-level rejections
            );
        } else {
            // Simulate rejected case
            String[] rejectionReasons = {
                "Document quality insufficient",
                "Document authenticity could not be verified",
                "Information mismatch detected",
                "Document expired",
                "Incomplete documentation"
            };
            
            String reason = rejectionReasons[random.nextInt(rejectionReasons.length)];
            
            return new KycCallbackRequest(
                "KYC_PROVIDER",
                providerRef,
                KycStatus.REJECTED,
                null,
                reason,
                null
            );
        }
    }
}

