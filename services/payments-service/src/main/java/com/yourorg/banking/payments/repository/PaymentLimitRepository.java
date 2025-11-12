package com.yourorg.banking.payments.repository;

import com.yourorg.banking.payments.model.PaymentLimit;
import com.yourorg.banking.payments.model.PaymentLimitType;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentLimitRepository extends CrudRepository<PaymentLimit, UUID> {
    
    List<PaymentLimit> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    
    List<PaymentLimit> findByAccountIdOrderByCreatedAtDesc(UUID accountId);
    
    List<PaymentLimit> findByCustomerIdAndIsActiveOrderByCreatedAtDesc(
        UUID customerId, boolean isActive);
    
    List<PaymentLimit> findByAccountIdAndIsActiveOrderByCreatedAtDesc(
        UUID accountId, boolean isActive);
    
    @Query("SELECT * FROM payment_limits WHERE customer_id = :customerId " +
           "AND limit_type = :limitType AND is_active = true " +
           "AND (expires_at IS NULL OR expires_at > :currentTime) " +
           "ORDER BY created_at DESC LIMIT 1")
    Optional<PaymentLimit> findActiveByCustomerIdAndType(
        @Param("customerId") UUID customerId,
        @Param("limitType") PaymentLimitType limitType,
        @Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT * FROM payment_limits WHERE account_id = :accountId " +
           "AND limit_type = :limitType AND is_active = true " +
           "AND (expires_at IS NULL OR expires_at > :currentTime) " +
           "ORDER BY created_at DESC LIMIT 1")
    Optional<PaymentLimit> findActiveByAccountIdAndType(
        @Param("accountId") UUID accountId,
        @Param("limitType") PaymentLimitType limitType,
        @Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT * FROM payment_limits WHERE customer_id = :customerId " +
           "AND limit_type = :limitType AND is_active = true " +
           "AND usage_period_start <= :currentTime " +
           "AND (expires_at IS NULL OR expires_at > :currentTime) " +
           "ORDER BY created_at DESC")
    List<PaymentLimit> findActiveByCustomerIdAndTypeAndPeriod(
        @Param("customerId") UUID customerId,
        @Param("limitType") PaymentLimitType limitType,
        @Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT COUNT(*) FROM payment_limits WHERE customer_id = :customerId " +
           "AND is_active = true")
    long countActiveByCustomerId(@Param("customerId") UUID customerId);
}

