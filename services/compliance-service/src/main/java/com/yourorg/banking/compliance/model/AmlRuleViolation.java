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
@Table(name = "aml_rule_violations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmlRuleViolation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "case_id", nullable = false)
    private UUID caseId;
    
    @Column(name = "rule_id", nullable = false)
    private UUID ruleId;
    
    @Column(name = "violation_type", nullable = false)
    private String violationType;
    
    @Column(name = "violation_description", columnDefinition = "TEXT")
    private String violationDescription;
    
    @Column(name = "transaction_id")
    private UUID transactionId;
    
    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "violation_timestamp", nullable = false)
    private LocalDateTime violationTimestamp;
    
    @Column(name = "severity", nullable = false)
    private String severity;
    
    @Column(name = "is_resolved", nullable = false)
    private Boolean isResolved;
    
    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

