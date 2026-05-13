package com.yourorg.banking.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class InterestAccrual {
    private UUID id;
    private UUID accountId;
    private BigDecimal amount;
    private BigDecimal rate;
    private LocalDate postingDate;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
    public LocalDate getPostingDate() { return postingDate; }
    public void setPostingDate(LocalDate postingDate) { this.postingDate = postingDate; }
}
