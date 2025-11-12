package com.yourorg.banking.loan.model;

public enum PaymentMethod {
    BANK_TRANSFER("Bank Transfer", "Direct bank transfer"),
    DEBIT_CARD("Debit Card", "Payment using debit card"),
    CREDIT_CARD("Credit Card", "Payment using credit card"),
    ACH("ACH", "Automated Clearing House transfer"),
    WIRE("Wire Transfer", "Wire transfer"),
    CHECK("Check", "Payment by check"),
    CASH("Cash", "Cash payment"),
    AUTO_PAY("Auto Pay", "Automatic payment setup"),
    ONLINE("Online", "Online payment portal"),
    MOBILE("Mobile", "Mobile app payment");
    
    private final String displayName;
    private final String description;
    
    PaymentMethod(String displayName, String description) {
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

