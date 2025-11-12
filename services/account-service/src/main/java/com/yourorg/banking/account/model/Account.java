package com.yourorg.banking.account.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Account(
    UUID id,
    UUID customerId,
    String accountNumber,
    String accountName,
    AccountType type,
    AccountStatus status,
    String currency,
    BigDecimal balance,
    BigDecimal availableBalance,
    BigDecimal interestRate,
    BigDecimal feeRate,
    BigDecimal dailyTransferLimit,
    BigDecimal perTransactionLimit,
    Instant lastTransactionAt,
    String description,
    boolean paperlessStatements,
    boolean emailNotifications,
    String preferredLanguage,
    boolean kycRequired,
    String kycLevel,
    KycStatus kycStatus,
    AccountOpeningStatus openingStatus,
    String openingReason,
    String closureReason,
    Instant closureRequestedAt,
    Instant closureApprovedAt,
    UUID closureApprovedBy,
    BigDecimal minimumBalance,
    BigDecimal maximumBalance,
    BigDecimal monthlyFee,
    BigDecimal overdraftLimit,
    Instant lastInterestCalculation,
    Instant nextInterestCalculation,
    Instant createdAt,
    Instant updatedAt
) {
    // Business methods
    public Account updateBalance(BigDecimal newBalance) {
        return new Account(
                id, customerId, accountNumber, accountName, type, status, currency,
                newBalance, newBalance, interestRate, feeRate, dailyTransferLimit,
                perTransactionLimit, Instant.now(), description, paperlessStatements,
                emailNotifications, preferredLanguage, kycRequired, kycLevel, kycStatus,
                openingStatus, openingReason, closureReason, closureRequestedAt,
                closureApprovedAt, closureApprovedBy, minimumBalance, maximumBalance,
                monthlyFee, overdraftLimit, lastInterestCalculation, nextInterestCalculation,
                createdAt, Instant.now()
        );
    }

    public Account reserveAmount(BigDecimal amount) {
        return new Account(
                id, customerId, accountNumber, accountName, type, status, currency,
                balance, availableBalance.subtract(amount), interestRate, feeRate,
                dailyTransferLimit, perTransactionLimit, lastTransactionAt, description,
                paperlessStatements, emailNotifications, preferredLanguage, kycRequired,
                kycLevel, kycStatus, openingStatus, openingReason, closureReason,
                closureRequestedAt, closureApprovedAt, closureApprovedBy, minimumBalance,
                maximumBalance, monthlyFee, overdraftLimit, lastInterestCalculation,
                nextInterestCalculation, createdAt, Instant.now()
        );
    }

    public Account releaseReservation(BigDecimal amount) {
        return new Account(
                id, customerId, accountNumber, accountName, type, status, currency,
                balance, availableBalance.add(amount), interestRate, feeRate,
                dailyTransferLimit, perTransactionLimit, lastTransactionAt, description,
                paperlessStatements, emailNotifications, preferredLanguage, kycRequired,
                kycLevel, kycStatus, openingStatus, openingReason, closureReason,
                closureRequestedAt, closureApprovedAt, closureApprovedBy, minimumBalance,
                maximumBalance, monthlyFee, overdraftLimit, lastInterestCalculation,
                nextInterestCalculation, createdAt, Instant.now()
        );
    }

    public boolean hasSufficientBalance(BigDecimal amount) {
        return availableBalance.compareTo(amount) >= 0;
    }

    public Account deactivate() {
        return new Account(
                id, customerId, accountNumber, accountName, AccountStatus.INACTIVE, status,
                currency, balance, availableBalance, interestRate, feeRate, dailyTransferLimit,
                perTransactionLimit, lastTransactionAt, description, paperlessStatements,
                emailNotifications, preferredLanguage, kycRequired, kycLevel, kycStatus,
                openingStatus, openingReason, closureReason, closureRequestedAt,
                closureApprovedAt, closureApprovedBy, minimumBalance, maximumBalance,
                monthlyFee, overdraftLimit, lastInterestCalculation, nextInterestCalculation,
                createdAt, Instant.now()
        );
    }
}
