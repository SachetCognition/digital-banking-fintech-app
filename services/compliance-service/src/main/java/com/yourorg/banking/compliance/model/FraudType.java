package com.yourorg.banking.compliance.model;

public enum FraudType {
    VELOCITY_FRAUD("Velocity Fraud", "Multiple transactions in short time period"),
    AMOUNT_ANOMALY("Amount Anomaly", "Unusual transaction amounts"),
    LOCATION_ANOMALY("Location Anomaly", "Transactions from unusual locations"),
    DEVICE_ANOMALY("Device Anomaly", "Transactions from new or suspicious devices"),
    PATTERN_ANOMALY("Pattern Anomaly", "Unusual transaction patterns"),
    IDENTITY_THEFT("Identity Theft", "Suspected identity theft"),
    MONEY_LAUNDERING("Money Laundering", "Suspected money laundering"),
    ACCOUNT_TAKEOVER("Account Takeover", "Suspected account takeover"),
    CARD_NOT_PRESENT("Card Not Present", "Suspicious card not present transaction"),
    PHISHING("Phishing", "Suspected phishing attack");
    
    private final String displayName;
    private final String description;
    
    FraudType(String displayName, String description) {
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

