package com.yourorg.banking.compliance.model;

public enum CaseStatus {
    OPEN("Open", "Case is open and under investigation"),
    UNDER_REVIEW("Under Review", "Case is being reviewed by compliance team"),
    PENDING_APPROVAL("Pending Approval", "Case is pending management approval"),
    APPROVED("Approved", "Case has been approved"),
    REJECTED("Rejected", "Case has been rejected"),
    CLOSED("Closed", "Case has been closed"),
    ESCALATED("Escalated", "Case has been escalated to higher authority"),
    SUSPENDED("Suspended", "Case investigation has been suspended");
    
    private final String displayName;
    private final String description;
    
    CaseStatus(String displayName, String description) {
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

