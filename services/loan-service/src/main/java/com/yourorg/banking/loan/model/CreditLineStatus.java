package com.yourorg.banking.loan.model;

public enum CreditLineStatus {
    ACTIVE("Active", "Credit line is active and available for use"),
    SUSPENDED("Suspended", "Credit line is temporarily suspended"),
    CLOSED("Closed", "Credit line has been closed"),
    FROZEN("Frozen", "Credit line is frozen due to suspicious activity"),
    OVER_LIMIT("Over Limit", "Credit line usage exceeds the limit"),
    EXPIRED("Expired", "Credit line has expired"),
    PENDING("Pending", "Credit line is pending approval"),
    CANCELLED("Cancelled", "Credit line application was cancelled");
    
    private final String displayName;
    private final String description;
    
    CreditLineStatus(String displayName, String description) {
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

