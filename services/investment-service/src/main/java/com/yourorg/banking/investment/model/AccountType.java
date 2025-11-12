package com.yourorg.banking.investment.model;

public enum AccountType {
    INDIVIDUAL("Individual", "Individual investment account"),
    JOINT("Joint", "Joint investment account"),
    IRA("IRA", "Individual Retirement Account"),
    ROTH_IRA("Roth IRA", "Roth Individual Retirement Account"),
    SEP_IRA("SEP IRA", "Simplified Employee Pension IRA"),
    SIMPLE_IRA("SIMPLE IRA", "Savings Incentive Match Plan for Employees IRA"),
    TRADITIONAL_401K("Traditional 401(k)", "Traditional 401(k) retirement account"),
    ROTH_401K("Roth 401(k)", "Roth 401(k) retirement account"),
    TRUST("Trust", "Trust investment account"),
    CUSTODIAL("Custodial", "Custodial investment account"),
    CORPORATE("Corporate", "Corporate investment account");
    
    private final String displayName;
    private final String description;
    
    AccountType(String displayName, String description) {
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

