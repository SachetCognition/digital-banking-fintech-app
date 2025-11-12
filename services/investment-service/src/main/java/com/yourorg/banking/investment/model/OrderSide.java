package com.yourorg.banking.investment.model;

public enum OrderSide {
    BUY("Buy", "Purchase securities"),
    SELL("Sell", "Sell securities"),
    SHORT("Short", "Short sell securities"),
    COVER("Cover", "Cover short position");
    
    private final String displayName;
    private final String description;
    
    OrderSide(String displayName, String description) {
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

