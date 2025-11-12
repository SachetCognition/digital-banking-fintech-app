package com.yourorg.banking.compliance.repository;

import com.yourorg.banking.compliance.model.AuditLog;
import com.yourorg.banking.compliance.model.EventCategory;
import com.yourorg.banking.compliance.model.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    Page<AuditLog> findByEventTypeOrderByEventTimestampDesc(EventType eventType, Pageable pageable);
    Page<AuditLog> findByEventCategoryOrderByEventTimestampDesc(EventCategory eventCategory, Pageable pageable);
    Page<AuditLog> findByUserIdOrderByEventTimestampDesc(UUID userId, Pageable pageable);
    Page<AuditLog> findByCustomerIdOrderByEventTimestampDesc(UUID customerId, Pageable pageable);
    Page<AuditLog> findByEntityTypeAndEntityIdOrderByEventTimestampDesc(String entityType, UUID entityId, Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a WHERE a.eventTimestamp BETWEEN :startDate AND :endDate ORDER BY a.eventTimestamp DESC")
    Page<AuditLog> findByEventTimestampBetweenOrderByEventTimestampDesc(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate, 
        Pageable pageable
    );
    
    @Query("SELECT a FROM AuditLog a WHERE a.customerId = :customerId AND a.eventTimestamp BETWEEN :startDate AND :endDate ORDER BY a.eventTimestamp DESC")
    List<AuditLog> findByCustomerIdAndEventTimestampBetweenOrderByEventTimestampDesc(
        @Param("customerId") UUID customerId,
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.eventType = :eventType AND a.eventTimestamp BETWEEN :startDate AND :endDate")
    Long countByEventTypeAndEventTimestampBetween(
        @Param("eventType") EventType eventType,
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
}

