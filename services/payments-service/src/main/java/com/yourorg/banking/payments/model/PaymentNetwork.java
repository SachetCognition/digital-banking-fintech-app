package com.yourorg.banking.payments.model;

public enum PaymentNetwork {
    UAEFTS("UAE Funds Transfer System", "RTGS", "AE"),
    UAEIPS("UAE Instant Payment System", "INSTANT", "AE"),
    WPS("Wage Protection System", "BATCH", "AE"),
    SADAD("SADAD Bill Payment", "BILL_PAY", "SA"),
    SARIE("Saudi Instant Payment", "INSTANT", "SA"),
    GCC_RTGS("GCC Real-Time Gross Settlement", "RTGS", "GCC"),
    AFAQ("AFAQ Cross-Border", "CROSS_BORDER", "GCC");

    private final String displayName;
    private final String type;
    private final String region;

    PaymentNetwork(String displayName, String type, String region) {
        this.displayName = displayName;
        this.type = type;
        this.region = region;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getType() {
        return type;
    }

    public String getRegion() {
        return region;
    }
}
