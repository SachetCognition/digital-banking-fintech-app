package com.yourorg.banking.ledger.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "account_balances")
public class AccountBalance {
    
    @Id
    private UUID id;
    
    @Column(nullable = false)
    private UUID accountId;
    
    @Column(nullable = false, length = 3)
    private String currency;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;
    
    @Column(nullable = false)
    private Instant lastUpdatedAt;
    
    // Constructors
    public AccountBalance() {}
    
    public AccountBalance(UUID id, UUID accountId, String currency, BigDecimal balance) {
        this.id = id;
        this.accountId = accountId;
        this.currency = currency;
        this.balance = balance;
        this.lastUpdatedAt = Instant.now();
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    
    public Instant getLastUpdatedAt() { return lastUpdatedAt; }
    public void setLastUpdatedAt(Instant lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }
    
    // Business methods
    public void updateBalance(BigDecimal newBalance) {
        this.balance = newBalance;
        this.lastUpdatedAt = Instant.now();
    }
    
    public void addAmount(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        this.lastUpdatedAt = Instant.now();
    }
    
    public void subtractAmount(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
        this.lastUpdatedAt = Instant.now();
    }
}

