package com.yourorg.banking.compliance.model;

public enum ReviewStatus {
    PENDING("Pending", "Transaction pending review"),
    UNDER_REVIEW("Under Review", "Transaction under review"),
    APPROVED("Approved", "Transaction approved"),
    REJECTED("Rejected", "Transaction rejected"),
    FLAGGED("Flagged", "Transaction flagged for further investigation"),
    CLEARED("Cleared", "Transaction cleared of suspicion");
    
    private final String displayName;
    private final String description;
    
    ReviewStatus(String displayName, String description) {
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

