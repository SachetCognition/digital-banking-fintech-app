package com.yourorg.banking.customer.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record SubmitKycRequest(
        @NotNull(message = "Customer ID is required")
        UUID customerId,
        
        @NotNull(message = "KYC level is required")
        KycLevel level,
        
        @NotEmpty(message = "Documents are required")
        List<DocumentSubmission> documents
) {
    public record DocumentSubmission(
            @NotNull(message = "Document type is required")
            DocumentType type,
            
            @NotNull(message = "File name is required")
            String fileName,
            
            @NotNull(message = "Content type is required")
            String contentType,
            
            @NotNull(message = "File size is required")
            Long fileSize,
            
            @NotNull(message = "File hash is required")
            String fileHash,
            
            @NotNull(message = "Storage ID is required")
            String storageId
    ) {}
}

