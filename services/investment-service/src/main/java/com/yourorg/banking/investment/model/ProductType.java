package com.yourorg.banking.investment.model;

public enum ProductType {
    STOCK("Stock", "Individual company stock"),
    ETF("ETF", "Exchange-Traded Fund"),
    MUTUAL_FUND("Mutual Fund", "Mutual fund investment"),
    BOND("Bond", "Fixed income security"),
    OPTION("Option", "Stock option contract"),
    FUTURE("Future", "Futures contract"),
    COMMODITY("Commodity", "Commodity investment"),
    CRYPTO("Cryptocurrency", "Digital currency"),
    REIT("REIT", "Real Estate Investment Trust"),
    PREFERRED_STOCK("Preferred Stock", "Preferred stock security"),
    WARRANT("Warrant", "Stock warrant"),
    ADR("ADR", "American Depositary Receipt"),
    GDR("GDR", "Global Depositary Receipt");
    
    private final String displayName;
    private final String description;
    
    ProductType(String displayName, String description) {
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

