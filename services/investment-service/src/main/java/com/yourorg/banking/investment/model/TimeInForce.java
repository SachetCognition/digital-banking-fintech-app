package com.yourorg.banking.investment.model;

public enum TimeInForce {
    DAY("Day", "Order valid for the current trading day"),
    GTC("Good Till Cancelled", "Order valid until cancelled"),
    IOC("Immediate or Cancel", "Execute immediately or cancel"),
    FOK("Fill or Kill", "Execute completely or cancel"),
    GTD("Good Till Date", "Order valid until specified date");
    
    private final String displayName;
    private final String description;
    
    TimeInForce(String displayName, String description) {
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

