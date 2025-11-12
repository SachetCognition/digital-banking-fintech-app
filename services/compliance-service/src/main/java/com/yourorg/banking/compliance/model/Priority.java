package com.yourorg.banking.compliance.model;

public enum Priority {
    LOW("Low", "Low priority case"),
    MEDIUM("Medium", "Medium priority case"),
    HIGH("High", "High priority case"),
    CRITICAL("Critical", "Critical priority case requiring immediate attention");
    
    private final String displayName;
    private final String description;
    
    Priority(String displayName, String description) {
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

