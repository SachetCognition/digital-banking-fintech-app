package com.yourorg.banking.investment.model;

public enum AssetClass {
    EQUITY("Equity", "Stock investments"),
    FIXED_INCOME("Fixed Income", "Bond and debt investments"),
    CASH("Cash", "Cash and cash equivalents"),
    ALTERNATIVES("Alternatives", "Alternative investments"),
    COMMODITIES("Commodities", "Commodity investments"),
    REAL_ESTATE("Real Estate", "Real estate investments"),
    CRYPTO("Cryptocurrency", "Digital currency investments"),
    MIXED("Mixed", "Mixed asset class investments");
    
    private final String displayName;
    private final String description;
    
    AssetClass(String displayName, String description) {
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

