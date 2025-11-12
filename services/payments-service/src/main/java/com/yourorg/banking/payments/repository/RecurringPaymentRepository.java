package com.yourorg.banking.payments.repository;

import com.yourorg.banking.payments.model.RecurringPayment;
import com.yourorg.banking.payments.model.RecurringPaymentStatus;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface RecurringPaymentRepository extends CrudRepository<RecurringPayment, UUID> {
    
    List<RecurringPayment> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    
    List<RecurringPayment> findByStatusOrderByCreatedAtAsc(RecurringPaymentStatus status);
    
    List<RecurringPayment> findByCustomerIdAndStatusOrderByCreatedAtDesc(
        UUID customerId, RecurringPaymentStatus status);
    
    @Query("SELECT * FROM recurring_payments WHERE status = :status " +
           "AND next_execution_date <= :currentDate ORDER BY next_execution_date ASC")
    List<RecurringPayment> findDueForExecution(
        @Param("status") RecurringPaymentStatus status,
        @Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT * FROM recurring_payments WHERE customer_id = :customerId " +
           "AND status = :status AND next_execution_date <= :currentDate " +
           "ORDER BY next_execution_date ASC")
    List<RecurringPayment> findCustomerDueForExecution(
        @Param("customerId") UUID customerId,
        @Param("status") RecurringPaymentStatus status,
        @Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT COUNT(*) FROM recurring_payments WHERE customer_id = :customerId " +
           "AND status = :status")
    long countByCustomerIdAndStatus(
        @Param("customerId") UUID customerId,
        @Param("status") RecurringPaymentStatus status);
}

