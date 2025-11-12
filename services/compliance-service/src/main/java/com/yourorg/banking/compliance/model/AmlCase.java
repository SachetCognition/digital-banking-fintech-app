package com.yourorg.banking.compliance.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "aml_cases")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmlCase {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "case_number", unique = true, nullable = false)
    private String caseNumber;
    
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "case_type", nullable = false)
    private CaseType caseType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CaseStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority;
    
    @Column(name = "risk_score", nullable = false)
    private Integer riskScore;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "assigned_to")
    private String assignedTo;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "closed_at")
    private LocalDateTime closedAt;
    
    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;
    
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

