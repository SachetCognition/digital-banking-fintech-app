package com.yourorg.banking.investment.model;

public enum RiskTolerance {
    CONSERVATIVE("Conservative", "Low risk, stable returns"),
    MODERATE("Moderate", "Balanced risk and return"),
    AGGRESSIVE("Aggressive", "High risk, high potential returns"),
    VERY_AGGRESSIVE("Very Aggressive", "Very high risk, very high potential returns");
    
    private final String displayName;
    private final String description;
    
    RiskTolerance(String displayName, String description) {
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

