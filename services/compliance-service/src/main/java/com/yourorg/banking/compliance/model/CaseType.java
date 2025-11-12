package com.yourorg.banking.compliance.model;

public enum CaseType {
    SUSPICIOUS_ACTIVITY("Suspicious Activity", "Suspicious transaction patterns"),
    LARGE_TRANSACTION("Large Transaction", "Transaction exceeding reporting thresholds"),
    UNUSUAL_PATTERN("Unusual Pattern", "Unusual customer behavior patterns"),
    HIGH_RISK_CUSTOMER("High Risk Customer", "Customer with high risk profile"),
    SANCTIONS_MATCH("Sanctions Match", "Potential sanctions list match"),
    PEP_MATCH("PEP Match", "Politically Exposed Person match"),
    GEOGRAPHIC_RISK("Geographic Risk", "High-risk geographic location"),
    VELOCITY_ANOMALY("Velocity Anomaly", "Unusual transaction velocity"),
    STRUCTURING("Structuring", "Potential transaction structuring"),
    MONEY_LAUNDERING("Money Laundering", "Suspected money laundering activity");
    
    private final String displayName;
    private final String description;
    
    CaseType(String displayName, String description) {
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

