package com.yourorg.banking.investment.model;

public enum OrderStatus {
    PENDING("Pending", "Order is pending submission"),
    SUBMITTED("Submitted", "Order has been submitted to market"),
    PARTIALLY_FILLED("Partially Filled", "Order has been partially executed"),
    FILLED("Filled", "Order has been completely executed"),
    CANCELLED("Cancelled", "Order has been cancelled"),
    REJECTED("Rejected", "Order has been rejected"),
    EXPIRED("Expired", "Order has expired"),
    PENDING_CANCEL("Pending Cancel", "Order cancellation is pending");
    
    private final String displayName;
    private final String description;
    
    OrderStatus(String displayName, String description) {
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

