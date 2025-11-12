package com.yourorg.banking.customer.model;

import java.util.List;
import java.util.UUID;

public record KycCallbackRequest(
        String provider,
        String providerRef,
        KycStatus status,
        Integer riskScore,
        String rejectionReason,
        List<DocumentCallback> documents
) {
    public record DocumentCallback(
            String documentId,
            DocumentStatus status,
            String rejectionReason
    ) {}
}

