package com.yourorg.banking.loan.model;

public enum TransactionStatus {
    PENDING("Pending", "Transaction is pending processing"),
    PROCESSING("Processing", "Transaction is being processed"),
    COMPLETED("Completed", "Transaction has been completed"),
    FAILED("Failed", "Transaction failed to process"),
    CANCELLED("Cancelled", "Transaction was cancelled"),
    REFUNDED("Refunded", "Transaction has been refunded"),
    DISPUTED("Disputed", "Transaction is under dispute"),
    REVERSED("Reversed", "Transaction has been reversed");
    
    private final String displayName;
    private final String description;
    
    TransactionStatus(String displayName, String description) {
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

