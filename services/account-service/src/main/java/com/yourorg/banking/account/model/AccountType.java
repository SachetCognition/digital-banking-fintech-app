package com.yourorg.banking.account.model;

public enum AccountType {
    // Personal Accounts
    CHECKING("Personal Checking", "Basic checking account for daily transactions", 0.00, 0.00, true),
    SAVINGS("Personal Savings", "Interest-bearing savings account", 0.01, 0.00, true),
    MONEY_MARKET("Money Market", "High-yield savings with limited transactions", 0.02, 0.00, true),
    CD("Certificate of Deposit", "Fixed-term deposit with guaranteed interest", 0.03, 0.00, false),
    
    // Business Accounts
    BUSINESS_CHECKING("Business Checking", "Checking account for business operations", 0.00, 0.00, true),
    BUSINESS_SAVINGS("Business Savings", "Savings account for business funds", 0.01, 0.00, true),
    MERCHANT("Merchant Account", "Account for processing payments", 0.00, 0.00, true),
    
    // Investment Accounts
    INVESTMENT("Investment Account", "Account for securities and investments", 0.00, 0.00, true),
    IRA("Individual Retirement Account", "Tax-advantaged retirement savings", 0.00, 0.00, false),
    ROTH_IRA("Roth IRA", "Tax-free retirement savings", 0.00, 0.00, false),
    
    // Credit Accounts
    CREDIT_CARD("Credit Card", "Revolving credit line", 0.00, 0.20, true),
    LINE_OF_CREDIT("Line of Credit", "Flexible credit facility", 0.00, 0.15, true),
    PERSONAL_LOAN("Personal Loan", "Fixed-term personal loan", 0.00, 0.12, false),
    BUSINESS_LOAN("Business Loan", "Fixed-term business loan", 0.00, 0.10, false),
    MORTGAGE("Mortgage", "Home loan secured by property", 0.00, 0.06, false),
    
    // Specialized Accounts
    STUDENT("Student Account", "Account for students with special benefits", 0.00, 0.00, true),
    SENIOR("Senior Account", "Account for seniors with special benefits", 0.01, 0.00, true),
    JOINT("Joint Account", "Account shared by multiple customers", 0.00, 0.00, true),
    TRUST("Trust Account", "Account held in trust", 0.00, 0.00, false),
    ESCROW("Escrow Account", "Account for holding funds in escrow", 0.00, 0.00, false),

    // Islamic Accounts
    WADIAH_CURRENT("Wadiah Current", "Safekeeping current account", 0.00, 0.00, true),
    MUDARABAH_SAVINGS("Mudarabah Savings", "Profit-sharing savings", 0.00, 0.00, true),
    WAKALA_INVESTMENT("Wakala Investment", "Agency-based investment", 0.00, 0.00, true),
    COMMODITY_MURABAHA_DEPOSIT("Commodity Murabaha Deposit", "Islamic fixed deposit", 0.00, 0.00, false);

    private final String displayName;
    private final String description;
    private final double interestRate;
    private final double feeRate;
    private final boolean allowsTransactions;

    AccountType(String displayName, String description, double interestRate, double feeRate, boolean allowsTransactions) {
        this.displayName = displayName;
        this.description = description;
        this.interestRate = interestRate;
        this.feeRate = feeRate;
        this.allowsTransactions = allowsTransactions;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public double getFeeRate() {
        return feeRate;
    }

    public boolean allowsTransactions() {
        return allowsTransactions;
    }

    public boolean isPersonalAccount() {
        return this == CHECKING || this == SAVINGS || this == MONEY_MARKET || 
               this == CD || this == STUDENT || this == SENIOR;
    }

    public boolean isBusinessAccount() {
        return this == BUSINESS_CHECKING || this == BUSINESS_SAVINGS || 
               this == MERCHANT || this == BUSINESS_LOAN;
    }

    public boolean isInvestmentAccount() {
        return this == INVESTMENT || this == IRA || this == ROTH_IRA;
    }

    public boolean isCreditAccount() {
        return this == CREDIT_CARD || this == LINE_OF_CREDIT || this == PERSONAL_LOAN || 
               this == BUSINESS_LOAN || this == MORTGAGE;
    }

    public boolean isJointAccount() {
        return this == JOINT;
    }

    public boolean isIslamicAccount() {
        return this == WADIAH_CURRENT || this == MUDARABAH_SAVINGS ||
               this == WAKALA_INVESTMENT || this == COMMODITY_MURABAHA_DEPOSIT;
    }

    public boolean requiresKyc() {
        return isBusinessAccount() || isInvestmentAccount() || isCreditAccount();
    }
}
