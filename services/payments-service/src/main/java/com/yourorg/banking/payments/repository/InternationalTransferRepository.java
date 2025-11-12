package com.yourorg.banking.payments.repository;

import com.yourorg.banking.payments.model.InternationalTransfer;
import com.yourorg.banking.payments.model.InternationalTransferStatus;
import com.yourorg.banking.payments.model.InternationalTransferType;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InternationalTransferRepository extends CrudRepository<InternationalTransfer, UUID> {
    
    List<InternationalTransfer> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    
    List<InternationalTransfer> findByStatusOrderByCreatedAtAsc(InternationalTransferStatus status);
    
    List<InternationalTransfer> findByCustomerIdAndStatusOrderByCreatedAtDesc(
        UUID customerId, InternationalTransferStatus status);
    
    List<InternationalTransfer> findByTransferTypeOrderByCreatedAtAsc(InternationalTransferType transferType);
    
    @Query("SELECT * FROM international_transfers WHERE customer_id = :customerId " +
           "AND created_at >= :fromDate AND created_at <= :toDate " +
           "ORDER BY created_at DESC")
    List<InternationalTransfer> findByCustomerIdAndDateRange(
        @Param("customerId") UUID customerId,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate);
    
    @Query("SELECT * FROM international_transfers WHERE status = :status " +
           "AND created_at < :beforeDate ORDER BY created_at ASC")
    List<InternationalTransfer> findStalePendingTransfers(
        @Param("status") InternationalTransferStatus status,
        @Param("beforeDate") LocalDateTime beforeDate);
    
    Optional<InternationalTransfer> findByReferenceNumber(String referenceNumber);
    
    Optional<InternationalTransfer> findBySwiftMessageId(String swiftMessageId);
    
    @Query("SELECT COUNT(*) FROM international_transfers WHERE customer_id = :customerId " +
           "AND status = :status AND DATE(created_at) = CURRENT_DATE")
    long countByCustomerIdAndStatusToday(
        @Param("customerId") UUID customerId,
        @Param("status") InternationalTransferStatus status);
    
    @Query("SELECT SUM(amount) FROM international_transfers WHERE customer_id = :customerId " +
           "AND status = :status AND DATE(created_at) = CURRENT_DATE")
    Double sumAmountByCustomerIdAndStatusToday(
        @Param("customerId") UUID customerId,
        @Param("status") InternationalTransferStatus status);
}

