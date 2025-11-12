package com.yourorg.banking.customer.repo;

import com.yourorg.banking.customer.model.KycCase;
import com.yourorg.banking.customer.model.KycLevel;
import com.yourorg.banking.customer.model.KycStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KycCaseRepository extends JpaRepository<KycCase, UUID> {
    
    List<KycCase> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    
    Optional<KycCase> findByCustomerIdAndStatus(UUID customerId, KycStatus status);
    
    Optional<KycCase> findByProviderRef(String providerRef);
    
    List<KycCase> findByStatusAndSubmittedAtBefore(KycStatus status, Instant before);
    
    @Query("SELECT k FROM KycCase k WHERE k.customerId = :customerId AND k.level = :level ORDER BY k.createdAt DESC")
    List<KycCase> findByCustomerIdAndLevel(@Param("customerId") UUID customerId, @Param("level") KycLevel level);
    
    @Query("SELECT k FROM KycCase k WHERE k.status = :status AND k.expiresAt < :now")
    List<KycCase> findExpiredCases(@Param("status") KycStatus status, @Param("now") Instant now);
    
    @Query("SELECT COUNT(k) FROM KycCase k WHERE k.customerId = :customerId AND k.status = :status")
    long countByCustomerIdAndStatus(@Param("customerId") UUID customerId, @Param("status") KycStatus status);
}

