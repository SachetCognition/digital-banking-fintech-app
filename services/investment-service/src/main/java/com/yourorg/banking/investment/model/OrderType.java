package com.yourorg.banking.investment.model;

public enum OrderType {
    MARKET("Market", "Execute at current market price"),
    LIMIT("Limit", "Execute at specified price or better"),
    STOP("Stop", "Execute when price reaches stop price"),
    STOP_LIMIT("Stop Limit", "Execute limit order when stop price is reached"),
    TRAILING_STOP("Trailing Stop", "Stop order that follows price movement"),
    BRACKET("Bracket", "Combination of limit and stop orders");
    
    private final String displayName;
    private final String description;
    
    OrderType(String displayName, String description) {
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

