package com.yourorg.banking.compliance.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "regulatory_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegulatoryReport {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private ReportType reportType;
    
    @Column(name = "report_name", nullable = false)
    private String reportName;
    
    @Column(name = "reporting_period_start", nullable = false)
    private LocalDate reportingPeriodStart;
    
    @Column(name = "reporting_period_end", nullable = false)
    private LocalDate reportingPeriodEnd;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status;
    
    @Column(name = "generated_by")
    private String generatedBy;
    
    @Column(name = "generated_at")
    private LocalDateTime generatedAt;
    
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    
    @Column(name = "due_date")
    private LocalDate dueDate;
    
    @Column(name = "file_path")
    private String filePath;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "record_count")
    private Integer recordCount;
    
    @Column(name = "report_data", columnDefinition = "JSONB")
    private String reportData;
    
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

