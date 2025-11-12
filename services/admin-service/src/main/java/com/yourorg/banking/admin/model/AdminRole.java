package com.yourorg.banking.admin.model;

public enum AdminRole {
    SUPER_ADMIN("Super Admin", "Full system access and control"),
    SYSTEM_ADMIN("System Admin", "System configuration and monitoring"),
    USER_ADMIN("User Admin", "User management and permissions"),
    COMPLIANCE_ADMIN("Compliance Admin", "Compliance and audit management"),
    SUPPORT_ADMIN("Support Admin", "Customer support and help desk"),
    READONLY_ADMIN("Readonly Admin", "Read-only access to system data");
    
    private final String displayName;
    private final String description;
    
    AdminRole(String displayName, String description) {
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

