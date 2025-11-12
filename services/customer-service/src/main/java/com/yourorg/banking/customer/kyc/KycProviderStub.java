package com.yourorg.banking.customer.kyc;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Very simple provider stub that simulates an external KYC provider.
 * It issues a providerRef and returns an initial status. Subsequent checks
 * may move PENDING into APPROVED after a small delay.
 */
@Component
public class KycProviderStub {

    public record ProviderResult(String providerRef, String status, String rejectionReason) {}

    public ProviderResult submit(UUID customerId) {
        // Generate a provider reference; initial status PENDING
        String ref = "KYC-" + customerId.toString().substring(0, 8) + "-" + System.currentTimeMillis();
        return new ProviderResult(ref, "PENDING_VERIFICATION", null);
    }

    public ProviderResult poll(String providerRef, Instant submittedAt) {
        // After 5 seconds, approve; otherwise remain pending
        if (submittedAt != null && Duration.between(submittedAt, Instant.now()).getSeconds() >= 5) {
            return new ProviderResult(providerRef, "APPROVED", null);
        }
        return new ProviderResult(providerRef, "PENDING_VERIFICATION", null);
    }
}
