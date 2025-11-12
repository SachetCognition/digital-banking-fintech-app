package com.yourorg.banking.payments.model;

public enum SagaStep {
    VALIDATION,
    RESERVATION,
    POSTING,
    NOTIFICATION,
    COMPLETED,
    COMPENSATION
}

