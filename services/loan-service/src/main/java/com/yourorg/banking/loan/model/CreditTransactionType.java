package com.yourorg.banking.loan.model;

public enum CreditTransactionType {
    PURCHASE("Purchase", "Credit purchase transaction"),
    CASH_ADVANCE("Cash Advance", "Cash advance from credit line"),
    BALANCE_TRANSFER("Balance Transfer", "Transfer from another credit account"),
    PAYMENT("Payment", "Payment made to credit line"),
    REFUND("Refund", "Refund or credit adjustment"),
    FEE("Fee", "Fee charged to credit line"),
    INTEREST("Interest", "Interest charge"),
    LATE_FEE("Late Fee", "Late payment fee"),
    ANNUAL_FEE("Annual Fee", "Annual membership fee"),
    OVER_LIMIT_FEE("Over Limit Fee", "Fee for exceeding credit limit"),
    RETURN("Return", "Returned item"),
    DISPUTE("Dispute", "Disputed transaction"),
    ADJUSTMENT("Adjustment", "Account adjustment");
    
    private final String displayName;
    private final String description;
    
    CreditTransactionType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}

