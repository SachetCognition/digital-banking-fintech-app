package com.yourorg.banking.payments.repository;

import com.yourorg.banking.payments.model.ExternalTransfer;
import com.yourorg.banking.payments.model.ExternalTransferStatus;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExternalTransferRepository extends CrudRepository<ExternalTransfer, UUID> {
    
    List<ExternalTransfer> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    
    List<ExternalTransfer> findByStatusOrderByCreatedAtAsc(ExternalTransferStatus status);
    
    List<ExternalTransfer> findByCustomerIdAndStatusOrderByCreatedAtDesc(
        UUID customerId, ExternalTransferStatus status);
    
    @Query("SELECT * FROM external_transfers WHERE customer_id = :customerId " +
           "AND created_at >= :fromDate AND created_at <= :toDate " +
           "ORDER BY created_at DESC")
    List<ExternalTransfer> findByCustomerIdAndDateRange(
        @Param("customerId") UUID customerId,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate);
    
    @Query("SELECT * FROM external_transfers WHERE status = :status " +
           "AND created_at < :beforeDate ORDER BY created_at ASC")
    List<ExternalTransfer> findStalePendingTransfers(
        @Param("status") ExternalTransferStatus status,
        @Param("beforeDate") LocalDateTime beforeDate);
    
    Optional<ExternalTransfer> findByReferenceNumber(String referenceNumber);
    
    @Query("SELECT COUNT(*) FROM external_transfers WHERE customer_id = :customerId " +
           "AND status = :status AND DATE(created_at) = CURRENT_DATE")
    long countByCustomerIdAndStatusToday(
        @Param("customerId") UUID customerId,
        @Param("status") ExternalTransferStatus status);
    
    @Query("SELECT SUM(amount) FROM external_transfers WHERE customer_id = :customerId " +
           "AND status = :status AND DATE(created_at) = CURRENT_DATE")
    Double sumAmountByCustomerIdAndStatusToday(
        @Param("customerId") UUID customerId,
        @Param("status") ExternalTransferStatus status);
}

