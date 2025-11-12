package com.yourorg.banking.compliance.model;

public enum ReportStatus {
    DRAFT("Draft", "Report is in draft status"),
    GENERATING("Generating", "Report is being generated"),
    READY("Ready", "Report is ready for review"),
    UNDER_REVIEW("Under Review", "Report is under review"),
    APPROVED("Approved", "Report has been approved"),
    SUBMITTED("Submitted", "Report has been submitted"),
    REJECTED("Rejected", "Report has been rejected"),
    OVERDUE("Overdue", "Report is overdue");
    
    private final String displayName;
    private final String description;
    
    ReportStatus(String displayName, String description) {
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

