package com.yourorg.banking.loan.model;

public enum LoanType {
    PERSONAL("Personal Loan", "Unsecured personal loan for various purposes"),
    HOME("Home Loan", "Secured loan for home purchase or refinancing"),
    AUTO("Auto Loan", "Secured loan for vehicle purchase"),
    BUSINESS("Business Loan", "Loan for business purposes"),
    EDUCATION("Education Loan", "Loan for educational expenses"),
    CREDIT_LINE("Credit Line", "Revolving credit facility"),
    MORTGAGE("Mortgage", "Long-term loan secured by real estate"),
    CONSOLIDATION("Debt Consolidation", "Loan to consolidate multiple debts");
    
    private final String displayName;
    private final String description;
    
    LoanType(String displayName, String description) {
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

