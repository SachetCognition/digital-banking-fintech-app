package com.yourorg.banking.admin.model;

public enum ConfigType {
    STRING("String", "Text configuration value"),
    NUMBER("Number", "Numeric configuration value"),
    BOOLEAN("Boolean", "True/false configuration value"),
    JSON("JSON", "JSON object configuration value"),
    URL("URL", "URL configuration value"),
    EMAIL("Email", "Email address configuration value"),
    PASSWORD("Password", "Password configuration value"),
    ENCRYPTED("Encrypted", "Encrypted configuration value");
    
    private final String displayName;
    private final String description;
    
    ConfigType(String displayName, String description) {
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

