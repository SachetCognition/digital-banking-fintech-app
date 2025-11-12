package com.yourorg.banking.payments.model;

public enum TransferStatus {
    PENDING,
    VALIDATING,
    RESERVING,
    POSTING,
    COMPLETED,
    FAILED,
    CANCELLED
}

