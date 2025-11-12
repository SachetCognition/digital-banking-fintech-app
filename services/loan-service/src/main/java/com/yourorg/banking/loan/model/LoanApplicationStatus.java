package com.yourorg.banking.loan.model;

public enum LoanApplicationStatus {
    PENDING("Pending", "Application submitted and under review"),
    UNDER_REVIEW("Under Review", "Application being reviewed by underwriter"),
    APPROVED("Approved", "Application approved for funding"),
    REJECTED("Rejected", "Application rejected"),
    CONDITIONAL_APPROVAL("Conditional Approval", "Approved with conditions"),
    WITHDRAWN("Withdrawn", "Application withdrawn by customer"),
    EXPIRED("Expired", "Application expired due to inactivity"),
    FUNDED("Funded", "Loan has been disbursed");
    
    private final String displayName;
    private final String description;
    
    LoanApplicationStatus(String displayName, String description) {
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

