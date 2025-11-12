package com.yourorg.banking.compliance.model;

public enum DetectionMethod {
    RULE_BASED("Rule Based", "Detection based on predefined rules"),
    MACHINE_LEARNING("Machine Learning", "Detection using ML models"),
    ANOMALY_DETECTION("Anomaly Detection", "Statistical anomaly detection"),
    BEHAVIORAL_ANALYSIS("Behavioral Analysis", "Analysis of user behavior patterns"),
    VELOCITY_CHECK("Velocity Check", "Transaction velocity analysis"),
    AMOUNT_ANALYSIS("Amount Analysis", "Transaction amount analysis"),
    LOCATION_ANALYSIS("Location Analysis", "Geographic location analysis"),
    DEVICE_ANALYSIS("Device Analysis", "Device fingerprinting analysis"),
    NETWORK_ANALYSIS("Network Analysis", "Network traffic analysis"),
    MANUAL_REVIEW("Manual Review", "Manual review by compliance team");
    
    private final String displayName;
    private final String description;
    
    DetectionMethod(String displayName, String description) {
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

