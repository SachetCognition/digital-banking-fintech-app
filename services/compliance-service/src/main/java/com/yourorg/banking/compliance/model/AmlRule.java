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
@Table(name = "aml_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmlRule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "rule_name", nullable = false)
    private String ruleName;
    
    @Column(name = "rule_description", columnDefinition = "TEXT")
    private String ruleDescription;
    
    @Column(name = "rule_type", nullable = false)
    private String ruleType;
    
    @Column(name = "severity", nullable = false)
    private String severity;
    
    @Column(name = "threshold_amount", precision = 15, scale = 2)
    private BigDecimal thresholdAmount;
    
    @Column(name = "threshold_count")
    private Integer thresholdCount;
    
    @Column(name = "time_window_minutes")
    private Integer timeWindowMinutes;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

