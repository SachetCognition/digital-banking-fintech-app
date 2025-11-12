package com.yourorg.banking.compliance.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "event_id", nullable = false)
    private String eventId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "event_category", nullable = false)
    private EventCategory eventCategory;
    
    @Column(name = "user_id")
    private UUID userId;
    
    @Column(name = "customer_id")
    private UUID customerId;
    
    @Column(name = "entity_type")
    private String entityType;
    
    @Column(name = "entity_id")
    private UUID entityId;
    
    @Column(name = "action", nullable = false)
    private String action;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "old_values", columnDefinition = "JSONB")
    private String oldValues;
    
    @Column(name = "new_values", columnDefinition = "JSONB")
    private String newValues;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(name = "session_id")
    private String sessionId;
    
    @Column(name = "request_id")
    private String requestId;
    
    @Column(name = "response_status")
    private Integer responseStatus;
    
    @Column(name = "processing_time_ms")
    private Long processingTimeMs;
    
    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

