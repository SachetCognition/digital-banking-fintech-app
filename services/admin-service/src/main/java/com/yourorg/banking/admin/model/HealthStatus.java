package com.yourorg.banking.admin.model;

public enum HealthStatus {
    HEALTHY("Healthy", "Service is operating normally"),
    WARNING("Warning", "Service has minor issues but is functional"),
    CRITICAL("Critical", "Service has serious issues affecting functionality"),
    UNKNOWN("Unknown", "Service status cannot be determined");
    
    private final String displayName;
    private final String description;
    
    HealthStatus(String displayName, String description) {
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

