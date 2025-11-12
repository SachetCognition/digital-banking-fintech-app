package com.yourorg.banking.customer.model;

public enum KycStatus {
    PENDING,        // Initial state
    SUBMITTED,      // Submitted to provider
    UNDER_REVIEW,   // Under review by provider
    APPROVED,       // Approved by provider
    REJECTED,       // Rejected by provider
    EXPIRED,        // Expired (timeout)
    CANCELLED       // Cancelled by customer
}

