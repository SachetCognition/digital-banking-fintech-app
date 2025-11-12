package com.yourorg.banking.customer.service;

import com.yourorg.banking.customer.model.DocumentType;
import com.yourorg.banking.customer.model.KycLevel;
import com.yourorg.banking.customer.model.SubmitKycRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class KycValidationService {
    
    // Required documents per KYC level
    private static final Map<KycLevel, Set<DocumentType>> REQUIRED_DOCUMENTS = Map.of(
        KycLevel.BASIC, Set.of(DocumentType.NATIONAL_ID),
        KycLevel.STANDARD, Set.of(DocumentType.NATIONAL_ID, DocumentType.PROOF_OF_ADDRESS),
        KycLevel.ENHANCED, Set.of(DocumentType.PASSPORT, DocumentType.UTILITY_BILL, DocumentType.BANK_STATEMENT),
        KycLevel.PREMIUM, Set.of(DocumentType.PASSPORT, DocumentType.UTILITY_BILL, DocumentType.BANK_STATEMENT, DocumentType.PAYSLIP, DocumentType.TAX_DOCUMENT)
    );
    
    // File size limits (in bytes)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long MIN_FILE_SIZE = 1024; // 1KB
    
    // Allowed content types
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "image/jpeg",
        "image/png",
        "image/pdf",
        "application/pdf"
    );
    
    public void validateDocuments(List<SubmitKycRequest.DocumentSubmission> documents, KycLevel level) {
        if (documents == null || documents.isEmpty()) {
            throw new IllegalArgumentException("At least one document is required");
        }
        
        // Check required documents for level
        Set<DocumentType> requiredTypes = REQUIRED_DOCUMENTS.get(level);
        Set<DocumentType> providedTypes = documents.stream()
            .map(SubmitKycRequest.DocumentSubmission::type)
            .collect(java.util.stream.Collectors.toSet());
        
        if (!providedTypes.containsAll(requiredTypes)) {
            Set<DocumentType> missing = requiredTypes.stream()
                .filter(type -> !providedTypes.contains(type))
                .collect(java.util.stream.Collectors.toSet());
            throw new IllegalArgumentException("Missing required documents: " + missing);
        }
        
        // Validate each document
        for (SubmitKycRequest.DocumentSubmission doc : documents) {
            validateDocument(doc);
        }
        
        // Check for duplicate document types
        long uniqueTypes = documents.stream()
            .map(SubmitKycRequest.DocumentSubmission::type)
            .distinct()
            .count();
        
        if (uniqueTypes != documents.size()) {
            throw new IllegalArgumentException("Duplicate document types are not allowed");
        }
    }
    
    private void validateDocument(SubmitKycRequest.DocumentSubmission doc) {
        // Validate file name
        if (doc.fileName() == null || doc.fileName().trim().isEmpty()) {
            throw new IllegalArgumentException("File name is required");
        }
        
        if (doc.fileName().length() > 255) {
            throw new IllegalArgumentException("File name too long (max 255 characters)");
        }
        
        // Validate content type
        if (doc.contentType() == null || !ALLOWED_CONTENT_TYPES.contains(doc.contentType())) {
            throw new IllegalArgumentException("Invalid content type. Allowed: " + ALLOWED_CONTENT_TYPES);
        }
        
        // Validate file size
        if (doc.fileSize() == null || doc.fileSize() < MIN_FILE_SIZE || doc.fileSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Invalid file size. Must be between " + MIN_FILE_SIZE + " and " + MAX_FILE_SIZE + " bytes");
        }
        
        // Validate file hash
        if (doc.fileHash() == null || doc.fileHash().trim().isEmpty()) {
            throw new IllegalArgumentException("File hash is required");
        }
        
        if (!doc.fileHash().matches("^[a-fA-F0-9]{64}$")) {
            throw new IllegalArgumentException("Invalid file hash format (must be SHA-256)");
        }
        
        // Validate storage ID
        if (doc.storageId() == null || doc.storageId().trim().isEmpty()) {
            throw new IllegalArgumentException("Storage ID is required");
        }
    }
}

