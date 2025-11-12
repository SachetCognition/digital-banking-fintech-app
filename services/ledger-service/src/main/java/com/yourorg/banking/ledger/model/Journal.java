package com.yourorg.banking.ledger.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "journals")
public class Journal {
    
    @Id
    private UUID id;
    
    @Column(nullable = false, length = 50)
    private String reference;
    
    @Column(length = 255)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JournalStatus status;
    
    @Column(nullable = false)
    private Instant createdAt;
    
    @Column
    private Instant postedAt;
    
    // Constructors
    public Journal() {}
    
    public Journal(UUID id, String reference, String description) {
        this.id = id;
        this.reference = reference;
        this.description = description;
        this.status = JournalStatus.PENDING;
        this.createdAt = Instant.now();
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public JournalStatus getStatus() { return status; }
    public void setStatus(JournalStatus status) { this.status = status; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public Instant getPostedAt() { return postedAt; }
    public void setPostedAt(Instant postedAt) { this.postedAt = postedAt; }
    
    // Business methods
    public void post() {
        this.status = JournalStatus.POSTED;
        this.postedAt = Instant.now();
    }
    
    public void cancel() {
        this.status = JournalStatus.CANCELLED;
    }
}

