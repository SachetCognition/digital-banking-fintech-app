package com.yourorg.banking.compliance.model;

public enum EventCategory {
    AUTHENTICATION("Authentication", "User authentication related events"),
    AUTHORIZATION("Authorization", "User authorization and access control events"),
    ACCOUNT_MANAGEMENT("Account Management", "Account lifecycle management events"),
    TRANSACTION_PROCESSING("Transaction Processing", "Financial transaction processing events"),
    COMPLIANCE("Compliance", "Regulatory compliance and AML events"),
    RISK_MANAGEMENT("Risk Management", "Risk assessment and management events"),
    DATA_MANAGEMENT("Data Management", "Data access, modification, and retention events"),
    SYSTEM_ADMINISTRATION("System Administration", "System configuration and administration events"),
    SECURITY("Security", "Security-related events and violations"),
    AUDIT("Audit", "Audit trail and logging events");
    
    private final String displayName;
    private final String description;
    
    EventCategory(String displayName, String description) {
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

