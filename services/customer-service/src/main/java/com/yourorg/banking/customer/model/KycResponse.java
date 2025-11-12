package com.yourorg.banking.customer.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record KycResponse(
        UUID kycCaseId,
        UUID customerId,
        KycLevel level,
        KycStatus status,
        String provider,
        String providerRef,
        Integer riskScore,
        String rejectionReason,
        Instant submittedAt,
        Instant reviewedAt,
        Instant expiresAt,
        Instant createdAt,
        Instant updatedAt,
        List<DocumentInfo> documents
) {
    public record DocumentInfo(
            UUID documentId,
            DocumentType type,
            String fileName,
            String contentType,
            Long fileSize,
            String fileHash,
            DocumentStatus status,
            String rejectionReason,
            Instant uploadedAt,
            Instant processedAt
    ) {}
}

