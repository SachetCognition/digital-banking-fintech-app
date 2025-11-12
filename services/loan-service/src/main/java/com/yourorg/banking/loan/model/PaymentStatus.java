package com.yourorg.banking.loan.model;

public enum PaymentStatus {
    PENDING("Pending", "Payment is scheduled but not yet due"),
    DUE("Due", "Payment is due and should be made"),
    OVERDUE("Overdue", "Payment is past due date"),
    PAID("Paid", "Payment has been completed"),
    PARTIAL("Partial", "Partial payment has been made"),
    CANCELLED("Cancelled", "Payment has been cancelled"),
    FAILED("Failed", "Payment attempt failed"),
    REFUNDED("Refunded", "Payment has been refunded");
    
    private final String displayName;
    private final String description;
    
    PaymentStatus(String displayName, String description) {
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

