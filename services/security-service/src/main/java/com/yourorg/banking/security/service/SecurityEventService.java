package com.yourorg.banking.security.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Value;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SecurityEventService {

    @Autowired
    private SecurityEventRepository securityEventRepository;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${app.security.monitoring.security-events:true}")
    private boolean securityEventsEnabled;

    /**
     * Log a security event
     */
    public void logSecurityEvent(String eventType, String severity, String sourceIp, 
                                String description, String userId) {
        if (!securityEventsEnabled) {
            return;
        }

        SecurityEvent event = new SecurityEvent();
        event.setEventType(eventType);
        event.setSeverity(severity);
        event.setSourceIp(sourceIp);
        event.setDescription(description);
        event.setUserId(userId != null ? UUID.fromString(userId) : null);
        event.setEventData(createEventData(eventType, sourceIp, description));
        event.setRiskScore(calculateRiskScore(severity, eventType));
        event.setCreatedAt(LocalDateTime.now());

        securityEventRepository.save(event);

        // Publish to Kafka for real-time monitoring
        kafkaTemplate.send("security-events", event);
    }

    /**
     * Log authentication event
     */
    public void logAuthenticationEvent(String action, String userId, String sourceIp, 
                                     boolean success, String failureReason) {
        String eventType = "AUTHENTICATION_" + action.toUpperCase();
        String severity = success ? "INFO" : "HIGH";
        String description = success ? 
            "User " + action + " successful" : 
            "User " + action + " failed: " + failureReason;

        logSecurityEvent(eventType, severity, sourceIp, description, userId);
    }

    /**
     * Log authorization event
     */
    public void logAuthorizationEvent(String action, String userId, String resource, 
                                    boolean success, String failureReason) {
        String eventType = "AUTHORIZATION_" + action.toUpperCase();
        String severity = success ? "INFO" : "HIGH";
        String description = success ? 
            "User " + action + " access to " + resource + " successful" : 
            "User " + action + " access to " + resource + " denied: " + failureReason;

        logSecurityEvent(eventType, severity, null, description, userId);
    }

    /**
     * Log data access event
     */
    public void logDataAccessEvent(String action, String userId, String dataType, 
                                 String resourceId, boolean success) {
        String eventType = "DATA_ACCESS_" + action.toUpperCase();
        String severity = success ? "INFO" : "MEDIUM";
        String description = success ? 
            "User " + action + " " + dataType + " data (ID: " + resourceId + ")" : 
            "User " + action + " " + dataType + " data failed";

        logSecurityEvent(eventType, severity, null, description, userId);
    }

    /**
     * Log system event
     */
    public void logSystemEvent(String eventType, String description, String severity) {
        logSecurityEvent(eventType, severity, null, description, null);
    }

    /**
     * Get security events by user
     */
    public List<SecurityEvent> getSecurityEventsByUser(String userId) {
        return securityEventRepository.findByUserIdOrderByCreatedAtDesc(UUID.fromString(userId));
    }

    /**
     * Get security events by severity
     */
    public List<SecurityEvent> getSecurityEventsBySeverity(String severity) {
        return securityEventRepository.findBySeverityOrderByCreatedAtDesc(severity);
    }

    /**
     * Get recent security events
     */
    public List<SecurityEvent> getRecentSecurityEvents(int limit) {
        return securityEventRepository.findTopByOrderByCreatedAtDesc(limit);
    }

    /**
     * Get security events by time range
     */
    public List<SecurityEvent> getSecurityEventsByTimeRange(LocalDateTime start, LocalDateTime end) {
        return securityEventRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
    }

    /**
     * Get unresolved security events
     */
    public List<SecurityEvent> getUnresolvedSecurityEvents() {
        return securityEventRepository.findByIsResolvedFalseOrderByCreatedAtDesc();
    }

    /**
     * Resolve security event
     */
    public void resolveSecurityEvent(UUID eventId, String resolvedBy, String resolutionNotes) {
        SecurityEvent event = securityEventRepository.findById(eventId).orElse(null);
        if (event != null) {
            event.setIsResolved(true);
            event.setResolvedAt(LocalDateTime.now());
            event.setResolvedBy(UUID.fromString(resolvedBy));
            event.setResolutionNotes(resolutionNotes);
            securityEventRepository.save(event);
        }
    }

    /**
     * Get security event statistics
     */
    public SecurityEventStatistics getSecurityEventStatistics() {
        long totalEvents = securityEventRepository.count();
        long criticalEvents = securityEventRepository.countBySeverity("CRITICAL");
        long highEvents = securityEventRepository.countBySeverity("HIGH");
        long mediumEvents = securityEventRepository.countBySeverity("MEDIUM");
        long lowEvents = securityEventRepository.countBySeverity("LOW");
        long unresolvedEvents = securityEventRepository.countByIsResolvedFalse();

        return new SecurityEventStatistics(
            totalEvents, criticalEvents, highEvents, mediumEvents, lowEvents, unresolvedEvents
        );
    }

    private String createEventData(String eventType, String sourceIp, String description) {
        return String.format(
            "{\"eventType\":\"%s\",\"sourceIp\":\"%s\",\"description\":\"%s\",\"timestamp\":\"%s\"}",
            eventType, sourceIp, description, LocalDateTime.now()
        );
    }

    private int calculateRiskScore(String severity, String eventType) {
        int baseScore = switch (severity) {
            case "CRITICAL" -> 100;
            case "HIGH" -> 75;
            case "MEDIUM" -> 50;
            case "LOW" -> 25;
            default -> 0;
        };

        // Add additional risk based on event type
        if (eventType.contains("FAILED") || eventType.contains("DENIED")) {
            baseScore += 10;
        }
        if (eventType.contains("UNAUTHORIZED") || eventType.contains("BREACH")) {
            baseScore += 20;
        }

        return Math.min(baseScore, 100);
    }

    @Entity
    @Table(name = "security_events")
    public static class SecurityEvent {
        @Id
        private UUID id = UUID.randomUUID();
        
        @Column(name = "event_type", nullable = false)
        private String eventType;
        
        @Column(name = "severity", nullable = false)
        private String severity;
        
        @Column(name = "source_ip")
        private String sourceIp;
        
        @Column(name = "user_agent")
        private String userAgent;
        
        @Column(name = "user_id")
        private UUID userId;
        
        @Column(name = "session_id")
        private String sessionId;
        
        @Column(name = "event_data", columnDefinition = "jsonb")
        private String eventData;
        
        @Column(name = "risk_score")
        private Integer riskScore = 0;
        
        @Column(name = "is_resolved")
        private Boolean isResolved = false;
        
        @Column(name = "resolved_at")
        private LocalDateTime resolvedAt;
        
        @Column(name = "resolved_by")
        private UUID resolvedBy;
        
        @Column(name = "resolution_notes")
        private String resolutionNotes;
        
        @Column(name = "created_at")
        private LocalDateTime createdAt;

        // Getters and setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        
        public String getSourceIp() { return sourceIp; }
        public void setSourceIp(String sourceIp) { this.sourceIp = sourceIp; }
        
        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
        
        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        
        public String getEventData() { return eventData; }
        public void setEventData(String eventData) { this.eventData = eventData; }
        
        public Integer getRiskScore() { return riskScore; }
        public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }
        
        public Boolean getIsResolved() { return isResolved; }
        public void setIsResolved(Boolean isResolved) { this.isResolved = isResolved; }
        
        public LocalDateTime getResolvedAt() { return resolvedAt; }
        public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
        
        public UUID getResolvedBy() { return resolvedBy; }
        public void setResolvedBy(UUID resolvedBy) { this.resolvedBy = resolvedBy; }
        
        public String getResolutionNotes() { return resolutionNotes; }
        public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    @Repository
    public interface SecurityEventRepository extends JpaRepository<SecurityEvent, UUID> {
        List<SecurityEvent> findByUserIdOrderByCreatedAtDesc(UUID userId);
        List<SecurityEvent> findBySeverityOrderByCreatedAtDesc(String severity);
        List<SecurityEvent> findByIsResolvedFalseOrderByCreatedAtDesc();
        List<SecurityEvent> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
        
        @Query("SELECT s FROM SecurityEvent s ORDER BY s.createdAt DESC")
        List<SecurityEvent> findTopByOrderByCreatedAtDesc(int limit);
        
        long countBySeverity(String severity);
        long countByIsResolvedFalse();
    }

    public static class SecurityEventStatistics {
        private final long totalEvents;
        private final long criticalEvents;
        private final long highEvents;
        private final long mediumEvents;
        private final long lowEvents;
        private final long unresolvedEvents;

        public SecurityEventStatistics(long totalEvents, long criticalEvents, long highEvents, 
                                     long mediumEvents, long lowEvents, long unresolvedEvents) {
            this.totalEvents = totalEvents;
            this.criticalEvents = criticalEvents;
            this.highEvents = highEvents;
            this.mediumEvents = mediumEvents;
            this.lowEvents = lowEvents;
            this.unresolvedEvents = unresolvedEvents;
        }

        // Getters
        public long getTotalEvents() { return totalEvents; }
        public long getCriticalEvents() { return criticalEvents; }
        public long getHighEvents() { return highEvents; }
        public long getMediumEvents() { return mediumEvents; }
        public long getLowEvents() { return lowEvents; }
        public long getUnresolvedEvents() { return unresolvedEvents; }
    }
}

