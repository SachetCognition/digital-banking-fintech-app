package com.yourorg.banking.compliance.model;

public enum ReportType {
    SAR("SAR", "Suspicious Activity Report"),
    CTR("CTR", "Currency Transaction Report"),
    BSA("BSA", "Bank Secrecy Act Report"),
    AML("AML", "Anti-Money Laundering Report"),
    KYC("KYC", "Know Your Customer Report"),
    RISK_ASSESSMENT("Risk Assessment", "Risk Assessment Report"),
    COMPLIANCE_AUDIT("Compliance Audit", "Compliance Audit Report"),
    REGULATORY_FILING("Regulatory Filing", "General Regulatory Filing");
    
    private final String code;
    private final String displayName;
    
    ReportType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}

