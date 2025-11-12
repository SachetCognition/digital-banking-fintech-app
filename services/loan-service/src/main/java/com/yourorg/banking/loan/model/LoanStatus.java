package com.yourorg.banking.loan.model;

public enum LoanStatus {
    ACTIVE("Active", "Loan is active and payments are due"),
    PAID_OFF("Paid Off", "Loan has been fully paid off"),
    DEFAULTED("Defaulted", "Loan is in default due to missed payments"),
    FORECLOSED("Foreclosed", "Collateral has been foreclosed"),
    CANCELLED("Cancelled", "Loan has been cancelled"),
    SUSPENDED("Suspended", "Loan payments have been suspended"),
    REFINANCED("Refinanced", "Loan has been refinanced"),
    BANKRUPTCY("Bankruptcy", "Loan is affected by bankruptcy proceedings");
    
    private final String displayName;
    private final String description;
    
    LoanStatus(String displayName, String description) {
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

