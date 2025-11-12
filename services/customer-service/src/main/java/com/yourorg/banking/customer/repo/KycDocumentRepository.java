package com.yourorg.banking.customer.repo;

import com.yourorg.banking.customer.model.DocumentStatus;
import com.yourorg.banking.customer.model.DocumentType;
import com.yourorg.banking.customer.model.KycDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KycDocumentRepository extends JpaRepository<KycDocument, UUID> {
    
    List<KycDocument> findByKycCaseId(UUID kycCaseId);
    
    List<KycDocument> findByCustomerId(UUID customerId);
    
    List<KycDocument> findByKycCaseIdAndType(UUID kycCaseId, DocumentType type);
    
    List<KycDocument> findByStatus(DocumentStatus status);
    
    @Query("SELECT d FROM KycDocument d WHERE d.kycCaseId = :kycCaseId AND d.status = :status")
    List<KycDocument> findByKycCaseIdAndStatus(@Param("kycCaseId") UUID kycCaseId, @Param("status") DocumentStatus status);
    
    @Query("SELECT d FROM KycDocument d WHERE d.fileHash = :fileHash AND d.customerId = :customerId")
    List<KycDocument> findByFileHashAndCustomerId(@Param("fileHash") String fileHash, @Param("customerId") UUID customerId);
}

