package com.yourorg.banking.payments.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "beneficiaries")
public class Beneficiary {
    
    @Id
    private UUID id;
    
    @Column(nullable = false)
    private UUID customerId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String accountNumber;
    
    @Column(nullable = false)
    private String bankCode;
    
    @Column(nullable = false)
    private String bankName;
    
    @Column(nullable = false, length = 3)
    private String currency;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BeneficiaryType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BeneficiaryStatus status;
    
    @Column(length = 255)
    private String description;
    
    @Column(nullable = false)
    private Instant createdAt;
    
    @Column(nullable = false)
    private Instant updatedAt;
    
    // Constructors
    public Beneficiary() {}
    
    public Beneficiary(UUID id, UUID customerId, String name, String accountNumber, 
                      String bankCode, String bankName, String currency, 
                      BeneficiaryType type, String description) {
        this.id = id;
        this.customerId = customerId;
        this.name = name;
        this.accountNumber = accountNumber;
        this.bankCode = bankCode;
        this.bankName = bankName;
        this.currency = currency;
        this.type = type;
        this.description = description;
        this.status = BeneficiaryStatus.ACTIVE;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    
    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }
    
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public BeneficiaryType getType() { return type; }
    public void setType(BeneficiaryType type) { this.type = type; }
    
    public BeneficiaryStatus getStatus() { return status; }
    public void setStatus(BeneficiaryStatus status) { this.status = status; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    
    // Business methods
    public void deactivate() {
        this.status = BeneficiaryStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }
    
    public void activate() {
        this.status = BeneficiaryStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }
    
    public void update(String name, String description) {
        this.name = name;
        this.description = description;
        this.updatedAt = Instant.now();
    }
}

