package com.yourorg.banking.customer.service;

import com.yourorg.banking.customer.model.*;
import com.yourorg.banking.customer.repo.KycCaseRepository;
import com.yourorg.banking.customer.repo.KycDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class KycService {
    
    private final KycCaseRepository kycCaseRepository;
    private final KycDocumentRepository kycDocumentRepository;
    private final KycValidationService validationService;
    private final KycCallbackService callbackService;
    
    public KycService(KycCaseRepository kycCaseRepository,
                     KycDocumentRepository kycDocumentRepository,
                     KycValidationService validationService,
                     KycCallbackService callbackService) {
        this.kycCaseRepository = kycCaseRepository;
        this.kycDocumentRepository = kycDocumentRepository;
        this.validationService = validationService;
        this.callbackService = callbackService;
    }
    
    @Transactional
    public KycResponse submitKyc(SubmitKycRequest request) {
        // Check if customer already has pending KYC case
        Optional<KycCase> existingCase = kycCaseRepository.findByCustomerIdAndStatus(
            request.customerId(), KycStatus.PENDING);
        if (existingCase.isPresent()) {
            throw new IllegalArgumentException("Customer already has a pending KYC case");
        }
        
        // Validate documents
        validationService.validateDocuments(request.documents(), request.level());
        
        // Create KYC case
        KycCase kycCase = new KycCase(UUID.randomUUID(), request.customerId(), request.level());
        kycCaseRepository.save(kycCase);
        
        // Create documents
        List<KycDocument> documents = request.documents().stream()
            .map(doc -> new KycDocument(
                UUID.randomUUID(),
                kycCase.getId(),
                request.customerId(),
                doc.type(),
                doc.fileName(),
                doc.contentType(),
                doc.fileSize(),
                doc.fileHash(),
                doc.storageId()
            ))
            .collect(Collectors.toList());
        
        kycDocumentRepository.saveAll(documents);
        
        // Submit to provider (simulated)
        String providerRef = "KYC_" + kycCase.getId().toString().substring(0, 8);
        kycCase.submit("KYC_PROVIDER", providerRef);
        kycCaseRepository.save(kycCase);
        
        // Simulate async callback after delay
        callbackService.scheduleCallback(kycCase.getId(), providerRef);
        
        return createKycResponse(kycCase, documents);
    }
    
    public KycResponse getKycStatus(UUID customerId, UUID kycCaseId) {
        KycCase kycCase = kycCaseRepository.findById(kycCaseId)
            .filter(case_ -> case_.getCustomerId().equals(customerId))
            .orElseThrow(() -> new IllegalArgumentException("KYC case not found"));
        
        List<KycDocument> documents = kycDocumentRepository.findByKycCaseId(kycCaseId);
        
        return createKycResponse(kycCase, documents);
    }
    
    public List<KycResponse> getCustomerKycHistory(UUID customerId) {
        List<KycCase> cases = kycCaseRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        
        return cases.stream()
            .map(case_ -> {
                List<KycDocument> documents = kycDocumentRepository.findByKycCaseId(case_.getId());
                return createKycResponse(case_, documents);
            })
            .collect(Collectors.toList());
    }
    
    @Transactional
    public void processCallback(UUID kycCaseId, KycCallbackRequest callback) {
        KycCase kycCase = kycCaseRepository.findById(kycCaseId)
            .orElseThrow(() -> new IllegalArgumentException("KYC case not found"));
        
        // Update KYC case status
        if (callback.status() == KycStatus.APPROVED) {
            kycCase.approve(callback.riskScore());
        } else if (callback.status() == KycStatus.REJECTED) {
            kycCase.reject(callback.rejectionReason());
        }
        kycCaseRepository.save(kycCase);
        
        // Update document statuses
        if (callback.documents() != null) {
            for (KycCallbackRequest.DocumentCallback docCallback : callback.documents()) {
                Optional<KycDocument> document = kycDocumentRepository.findById(UUID.fromString(docCallback.documentId()));
                if (document.isPresent()) {
                    KycDocument doc = document.get();
                    if (docCallback.status() == DocumentStatus.APPROVED) {
                        doc.approve();
                    } else if (docCallback.status() == DocumentStatus.REJECTED) {
                        doc.reject(docCallback.rejectionReason());
                    }
                    kycDocumentRepository.save(doc);
                }
            }
        }
    }
    
    @Transactional
    public void processExpiredCases() {
        List<KycCase> expiredCases = kycCaseRepository.findExpiredCases(KycStatus.SUBMITTED, Instant.now());
        
        for (KycCase kycCase : expiredCases) {
            kycCase.expire();
            kycCaseRepository.save(kycCase);
        }
    }
    
    private KycResponse createKycResponse(KycCase kycCase, List<KycDocument> documents) {
        List<KycResponse.DocumentInfo> documentInfos = documents.stream()
            .map(doc -> new KycResponse.DocumentInfo(
                doc.getId(),
                doc.getType(),
                doc.getFileName(),
                doc.getContentType(),
                doc.getFileSize(),
                doc.getFileHash(),
                doc.getStatus(),
                doc.getRejectionReason(),
                doc.getUploadedAt(),
                doc.getProcessedAt()
            ))
            .collect(Collectors.toList());
        
        return new KycResponse(
            kycCase.getId(),
            kycCase.getCustomerId(),
            kycCase.getLevel(),
            kycCase.getStatus(),
            kycCase.getProvider(),
            kycCase.getProviderRef(),
            kycCase.getRiskScore(),
            kycCase.getRejectionReason(),
            kycCase.getSubmittedAt(),
            kycCase.getReviewedAt(),
            kycCase.getExpiresAt(),
            kycCase.getCreatedAt(),
            kycCase.getUpdatedAt(),
            documentInfos
        );
    }
}