package com.yourorg.banking.payments.repository;

import com.yourorg.banking.payments.model.BulkPaymentItem;
import com.yourorg.banking.payments.model.BulkPaymentItemStatus;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BulkPaymentItemRepository extends CrudRepository<BulkPaymentItem, UUID> {
    
    List<BulkPaymentItem> findByBulkPaymentIdOrderByCreatedAtAsc(UUID bulkPaymentId);
    
    List<BulkPaymentItem> findByStatusOrderByCreatedAtAsc(BulkPaymentItemStatus status);
    
    List<BulkPaymentItem> findByBulkPaymentIdAndStatusOrderByCreatedAtAsc(
        UUID bulkPaymentId, BulkPaymentItemStatus status);
    
    @Query("SELECT COUNT(*) FROM bulk_payment_items WHERE bulk_payment_id = :bulkPaymentId " +
           "AND status = :status")
    long countByBulkPaymentIdAndStatus(
        @Param("bulkPaymentId") UUID bulkPaymentId,
        @Param("status") BulkPaymentItemStatus status);
    
    @Query("SELECT SUM(amount) FROM bulk_payment_items WHERE bulk_payment_id = :bulkPaymentId " +
           "AND status = :status")
    Double sumAmountByBulkPaymentIdAndStatus(
        @Param("bulkPaymentId") UUID bulkPaymentId,
        @Param("status") BulkPaymentItemStatus status);
}

