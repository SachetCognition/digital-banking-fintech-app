package com.yourorg.banking.compliance.service;

import com.yourorg.banking.compliance.model.*;
import com.yourorg.banking.compliance.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLoggingService {
    
    private final AuditLogRepository auditLogRepository;
    
    @Transactional
    public void logEvent(EventType eventType, EventCategory eventCategory, 
                        UUID userId, UUID customerId, String entityType, 
                        String entityId, String description, String oldValues, 
                        String newValues) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .eventId(generateEventId())
                .eventType(eventType)
                .eventCategory(eventCategory)
                .userId(userId)
                .customerId(customerId)
                .entityType(entityType)
                .entityId(entityId != null ? UUID.fromString(entityId) : null)
                .action(eventType.getDisplayName())
                .description(description)
                .oldValues(oldValues)
                .newValues(newValues)
                .eventTimestamp(LocalDateTime.now())
                .build();
            
            auditLogRepository.save(auditLog);
            
            log.debug("Audit event logged: {} - {}", eventType, description);
        } catch (Exception e) {
            log.error("Failed to log audit event: {}", e.getMessage(), e);
        }
    }
    
    @Transactional
    public void logEvent(EventType eventType, EventCategory eventCategory, 
                        UUID userId, UUID customerId, String entityType, 
                        String entityId, String description) {
        logEvent(eventType, eventCategory, userId, customerId, entityType, 
                entityId, description, null, null);
    }
    
    @Transactional
    public void logEvent(EventType eventType, EventCategory eventCategory, 
                        String description) {
        logEvent(eventType, eventCategory, null, null, null, null, description);
    }
    
    @Transactional
    public void logDataAccess(UUID userId, UUID customerId, String entityType, 
                            String entityId, String description) {
        logEvent(EventType.DATA_ACCESSED, EventCategory.DATA_MANAGEMENT, 
                userId, customerId, entityType, entityId, description);
    }
    
    @Transactional
    public void logDataModification(UUID userId, UUID customerId, String entityType, 
                                  String entityId, String description, 
                                  String oldValues, String newValues) {
        logEvent(EventType.DATA_EXPORTED, EventCategory.DATA_MANAGEMENT, 
                userId, customerId, entityType, entityId, description, 
                oldValues, newValues);
    }
    
    @Transactional
    public void logSecurityEvent(EventType eventType, UUID userId, String description) {
        logEvent(eventType, EventCategory.SECURITY, userId, null, null, null, description);
    }
    
    @Transactional
    public void logComplianceEvent(EventType eventType, UUID customerId, 
                                 String entityType, String entityId, String description) {
        logEvent(eventType, EventCategory.COMPLIANCE, null, customerId, 
                entityType, entityId, description);
    }
    
    private String generateEventId() {
        return "EVT-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}

