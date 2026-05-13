package com.yourorg.banking.account.model;

public enum Currency {
    AED("United Arab Emirates Dirham", "د.إ", 2),
    SAR("Saudi Riyal", "﷼", 2),
    QAR("Qatari Riyal", "﷼", 2),
    BHD("Bahraini Dinar", ".د.ب", 3),
    OMR("Omani Rial", "﷼", 3),
    KWD("Kuwaiti Dinar", "د.ك", 3),
    USD("US Dollar", "$", 2),
    EUR("Euro", "€", 2),
    GBP("British Pound", "£", 2);

    private final String displayName;
    private final String symbol;
    private final int decimalPlaces;

    Currency(String displayName, String symbol, int decimalPlaces) {
        this.displayName = displayName;
        this.symbol = symbol;
        this.decimalPlaces = decimalPlaces;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getDecimalPlaces() {
        return decimalPlaces;
    }
}
