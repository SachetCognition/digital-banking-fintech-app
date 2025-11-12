package com.yourorg.banking.payments.repository;

import com.yourorg.banking.payments.model.BulkPayment;
import com.yourorg.banking.payments.model.BulkPaymentStatus;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BulkPaymentRepository extends CrudRepository<BulkPayment, UUID> {
    
    List<BulkPayment> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    
    List<BulkPayment> findByStatusOrderByCreatedAtAsc(BulkPaymentStatus status);
    
    List<BulkPayment> findByCustomerIdAndStatusOrderByCreatedAtDesc(
        UUID customerId, BulkPaymentStatus status);
    
    @Query("SELECT * FROM bulk_payments WHERE customer_id = :customerId " +
           "AND created_at >= :fromDate AND created_at <= :toDate " +
           "ORDER BY created_at DESC")
    List<BulkPayment> findByCustomerIdAndDateRange(
        @Param("customerId") UUID customerId,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate);
    
    @Query("SELECT * FROM bulk_payments WHERE status = :status " +
           "AND created_at < :beforeDate ORDER BY created_at ASC")
    List<BulkPayment> findStalePendingPayments(
        @Param("status") BulkPaymentStatus status,
        @Param("beforeDate") LocalDateTime beforeDate);
    
    @Query("SELECT COUNT(*) FROM bulk_payments WHERE customer_id = :customerId " +
           "AND status = :status AND DATE(created_at) = CURRENT_DATE")
    long countByCustomerIdAndStatusToday(
        @Param("customerId") UUID customerId,
        @Param("status") BulkPaymentStatus status);
}

