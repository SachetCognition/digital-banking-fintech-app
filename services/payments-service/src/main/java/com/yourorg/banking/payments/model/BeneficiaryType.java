package com.yourorg.banking.payments.model;

public enum BeneficiaryType {
    INTERNAL,    // Same bank transfers
    EXTERNAL,    // External bank transfers
    BILL_PAY,    // Bill payment beneficiaries
    MOBILE_MONEY // Mobile money transfers
}

