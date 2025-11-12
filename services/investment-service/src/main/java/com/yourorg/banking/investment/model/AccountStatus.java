package com.yourorg.banking.investment.model;

public enum AccountStatus {
    ACTIVE("Active", "Account is active and can be used for trading"),
    INACTIVE("Inactive", "Account is inactive and cannot be used for trading"),
    SUSPENDED("Suspended", "Account is temporarily suspended"),
    CLOSED("Closed", "Account has been closed"),
    PENDING("Pending", "Account is pending approval"),
    RESTRICTED("Restricted", "Account has trading restrictions");
    
    private final String displayName;
    private final String description;
    
    AccountStatus(String displayName, String description) {
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

