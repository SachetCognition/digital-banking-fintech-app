package com.yourorg.banking.ledger.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "journal_entries")
public class JournalEntry {
    
    @Id
    private UUID id;
    
    @Column(nullable = false)
    private UUID journalId;
    
    @Column(nullable = false)
    private UUID accountId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryType type;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false, length = 3)
    private String currency;
    
    @Column(length = 255)
    private String description;
    
    @Column(length = 255)
    private String reference;
    
    @Column(nullable = false)
    private Instant createdAt;
    
    // Constructors
    public JournalEntry() {}
    
    public JournalEntry(UUID id, UUID journalId, UUID accountId, EntryType type, 
                       BigDecimal amount, String currency, String description, String reference) {
        this.id = id;
        this.journalId = journalId;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.reference = reference;
        this.createdAt = Instant.now();
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getJournalId() { return journalId; }
    public void setJournalId(UUID journalId) { this.journalId = journalId; }
    
    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }
    
    public EntryType getType() { return type; }
    public void setType(EntryType type) { this.type = type; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

