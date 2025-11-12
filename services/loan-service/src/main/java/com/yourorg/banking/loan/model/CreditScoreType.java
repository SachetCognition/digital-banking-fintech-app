package com.yourorg.banking.loan.model;

public enum CreditScoreType {
    FICO("FICO", "Fair Isaac Corporation score"),
    VANTAGE("VantageScore", "VantageScore credit score"),
    INTERNAL("Internal", "Internal bank scoring model"),
    EXPERIAN("Experian", "Experian credit score"),
    EQUIFAX("Equifax", "Equifax credit score"),
    TRANSUNION("TransUnion", "TransUnion credit score"),
    CUSTOM("Custom", "Custom scoring model");
    
    private final String displayName;
    private final String description;
    
    CreditScoreType(String displayName, String description) {
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

