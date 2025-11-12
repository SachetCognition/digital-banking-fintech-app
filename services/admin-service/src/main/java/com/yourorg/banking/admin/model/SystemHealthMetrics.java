package com.yourorg.banking.admin.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "system_health_metrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemHealthMetrics {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "service_name", nullable = false)
    private String serviceName;
    
    @Column(name = "metric_name", nullable = false)
    private String metricName;
    
    @Column(name = "metric_value", nullable = false, precision = 15, scale = 4)
    private BigDecimal metricValue;
    
    @Column(name = "metric_unit")
    private String metricUnit;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private HealthStatus status;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "metadata", columnDefinition = "JSONB")
    private String metadata;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

