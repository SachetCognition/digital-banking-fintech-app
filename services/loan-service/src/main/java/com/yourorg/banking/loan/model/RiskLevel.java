package com.yourorg.banking.loan.model;

public enum RiskLevel {
    LOW("Low Risk", "Low risk borrower with excellent credit profile"),
    MEDIUM("Medium Risk", "Medium risk borrower with good credit profile"),
    HIGH("High Risk", "High risk borrower with fair credit profile"),
    VERY_HIGH("Very High Risk", "Very high risk borrower with poor credit profile"),
    CRITICAL("Critical Risk", "Critical risk borrower requiring special consideration");
    
    private final String displayName;
    private final String description;
    
    RiskLevel(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static RiskLevel fromCreditScore(Integer creditScore) {
        if (creditScore == null) return MEDIUM;
        
        if (creditScore >= 750) return LOW;
        if (creditScore >= 700) return MEDIUM;
        if (creditScore >= 650) return HIGH;
        if (creditScore >= 600) return VERY_HIGH;
        return CRITICAL;
    }
}

