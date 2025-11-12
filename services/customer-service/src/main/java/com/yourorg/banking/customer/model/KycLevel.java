package com.yourorg.banking.customer.model;

public enum KycLevel {
    BASIC,      // Basic verification (email, phone)
    STANDARD,   // Standard verification (ID document)
    ENHANCED,   // Enhanced verification (multiple documents)
    PREMIUM     // Premium verification (full due diligence)
}

