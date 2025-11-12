package com.yourorg.banking.customer.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "kyc_documents")
public class KycDocument {
    
    @Id
    private UUID id;
    
    @Column(nullable = false)
    private UUID kycCaseId;
    
    @Column(nullable = false)
    private UUID customerId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType type;
    
    @Column(nullable = false)
    private String fileName;
    
    @Column(nullable = false)
    private String contentType;
    
    @Column(nullable = false)
    private Long fileSize;
    
    @Column(nullable = false)
    private String fileHash; // SHA-256 hash for integrity
    
    @Column(nullable = false)
    private String storageId; // Reference to file storage (S3, etc.)
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;
    
    @Column(columnDefinition = "TEXT")
    private String rejectionReason;
    
    @Column
    private Instant uploadedAt;
    
    @Column
    private Instant processedAt;
    
    @Column(nullable = false)
    private Instant createdAt;
    
    @Column(nullable = false)
    private Instant updatedAt;
    
    // Constructors
    public KycDocument() {}
    
    public KycDocument(UUID id, UUID kycCaseId, UUID customerId, DocumentType type, 
                      String fileName, String contentType, Long fileSize, 
                      String fileHash, String storageId) {
        this.id = id;
        this.kycCaseId = kycCaseId;
        this.customerId = customerId;
        this.type = type;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.fileHash = fileHash;
        this.storageId = storageId;
        this.status = DocumentStatus.UPLOADED;
        this.uploadedAt = Instant.now();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getKycCaseId() { return kycCaseId; }
    public void setKycCaseId(UUID kycCaseId) { this.kycCaseId = kycCaseId; }
    
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    
    public DocumentType getType() { return type; }
    public void setType(DocumentType type) { this.type = type; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    
    public String getFileHash() { return fileHash; }
    public void setFileHash(String fileHash) { this.fileHash = fileHash; }
    
    public String getStorageId() { return storageId; }
    public void setStorageId(String storageId) { this.storageId = storageId; }
    
    public DocumentStatus getStatus() { return status; }
    public void setStatus(DocumentStatus status) { this.status = status; }
    
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    
    public Instant getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; }
    
    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    
    // Business methods
    public void approve() {
        this.status = DocumentStatus.APPROVED;
        this.processedAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    public void reject(String reason) {
        this.status = DocumentStatus.REJECTED;
        this.rejectionReason = reason;
        this.processedAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    public void process() {
        this.status = DocumentStatus.PROCESSING;
        this.updatedAt = Instant.now();
    }
}

