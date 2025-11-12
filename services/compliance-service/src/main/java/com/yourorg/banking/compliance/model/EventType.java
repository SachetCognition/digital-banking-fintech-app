package com.yourorg.banking.compliance.model;

public enum EventType {
    // Authentication Events
    LOGIN_SUCCESS("Login Success", "User successfully logged in"),
    LOGIN_FAILURE("Login Failure", "User login attempt failed"),
    LOGOUT("Logout", "User logged out"),
    PASSWORD_CHANGE("Password Change", "User changed password"),
    MFA_ENABLED("MFA Enabled", "Multi-factor authentication enabled"),
    MFA_DISABLED("MFA Disabled", "Multi-factor authentication disabled"),
    
    // Account Events
    ACCOUNT_CREATED("Account Created", "New account created"),
    ACCOUNT_UPDATED("Account Updated", "Account information updated"),
    ACCOUNT_CLOSED("Account Closed", "Account closed"),
    ACCOUNT_SUSPENDED("Account Suspended", "Account suspended"),
    ACCOUNT_REACTIVATED("Account Reactivated", "Account reactivated"),
    
    // Transaction Events
    TRANSACTION_CREATED("Transaction Created", "New transaction created"),
    TRANSACTION_UPDATED("Transaction Updated", "Transaction updated"),
    TRANSACTION_CANCELLED("Transaction Cancelled", "Transaction cancelled"),
    TRANSACTION_APPROVED("Transaction Approved", "Transaction approved"),
    TRANSACTION_REJECTED("Transaction Rejected", "Transaction rejected"),
    
    // Compliance Events
    AML_CASE_CREATED("AML Case Created", "New AML case created"),
    AML_CASE_UPDATED("AML Case Updated", "AML case updated"),
    AML_CASE_CLOSED("AML Case Closed", "AML case closed"),
    SAR_FILED("SAR Filed", "Suspicious Activity Report filed"),
    CTR_FILED("CTR Filed", "Currency Transaction Report filed"),
    
    // Data Events
    DATA_ACCESSED("Data Accessed", "Sensitive data accessed"),
    DATA_EXPORTED("Data Exported", "Data exported"),
    DATA_DELETED("Data Deleted", "Data deleted"),
    DATA_RETENTION_APPLIED("Data Retention Applied", "Data retention policy applied"),
    
    // System Events
    SYSTEM_ERROR("System Error", "System error occurred"),
    SECURITY_VIOLATION("Security Violation", "Security violation detected"),
    CONFIGURATION_CHANGE("Configuration Change", "System configuration changed");
    
    private final String displayName;
    private final String description;
    
    EventType(String displayName, String description) {
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

