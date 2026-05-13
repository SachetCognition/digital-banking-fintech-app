package com.yourorg.banking.loan.model;

public enum InterestCalculationMethod {
    SIMPLE("Simple Interest", "Simple interest calculation"),
    COMPOUND_DAILY("Daily Compound", "Daily compound interest"),
    COMPOUND_MONTHLY("Monthly Compound", "Monthly compound interest"),
    COMPOUND_ANNUALLY("Annual Compound", "Annual compound interest"),
    RULE_78("Rule of 78", "Rule of 78 interest calculation"),
    ACTUARIAL("Actuarial", "Actuarial interest calculation"),
    PRIME_RATE("Prime Rate", "Prime rate based calculation"),
    FIXED_RATE("Fixed Rate", "Fixed interest rate calculation"),

    // Islamic Finance Methods
    PROFIT_RATE("Profit Rate", "Islamic profit rate calculation"),
    COST_PLUS("Cost Plus", "Murabaha cost-plus markup"),
    DIMINISHING_EQUITY("Diminishing Equity", "Diminishing Musharaka equity split");
    
    private final String displayName;
    private final String description;
    
    InterestCalculationMethod(String displayName, String description) {
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

