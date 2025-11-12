package com.yourorg.banking.compliance.model;

public enum InvestigationStatus {
    PENDING("Pending", "Investigation pending assignment"),
    ASSIGNED("Assigned", "Investigation assigned to investigator"),
    IN_PROGRESS("In Progress", "Investigation in progress"),
    UNDER_REVIEW("Under Review", "Investigation under review"),
    RESOLVED("Resolved", "Investigation resolved"),
    CLOSED("Closed", "Investigation closed"),
    ESCALATED("Escalated", "Investigation escalated"),
    SUSPENDED("Suspended", "Investigation suspended");
    
    private final String displayName;
    private final String description;
    
    InvestigationStatus(String displayName, String description) {
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

