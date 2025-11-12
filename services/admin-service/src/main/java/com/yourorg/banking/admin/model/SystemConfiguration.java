package com.yourorg.banking.admin.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "system_configurations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "config_key", unique = true, nullable = false)
    private String configKey;
    
    @Column(name = "config_value", nullable = false, columnDefinition = "TEXT")
    private String configValue;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "config_type", nullable = false)
    private ConfigType configType;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "category", nullable = false)
    private String category;
    
    @Column(name = "is_encrypted", nullable = false)
    private Boolean isEncrypted;
    
    @Column(name = "is_sensitive", nullable = false)
    private Boolean isSensitive;
    
    @Column(name = "is_readonly", nullable = false)
    private Boolean isReadonly;
    
    @Column(name = "validation_rules", columnDefinition = "JSONB")
    private String validationRules;
    
    @Column(name = "created_by")
    private UUID createdBy;
    
    @Column(name = "updated_by")
    private UUID updatedBy;
    
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

