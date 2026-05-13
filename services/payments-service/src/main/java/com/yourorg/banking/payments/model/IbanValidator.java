package com.yourorg.banking.payments.model;

public class IbanValidator {

    private static final int UAE_IBAN_LENGTH = 23;
    private static final int KSA_IBAN_LENGTH = 24;

    public static void validateUaeIban(String iban) {
        if (iban == null || iban.isBlank()) {
            throw new IllegalArgumentException("IBAN must not be null or blank");
        }
        String normalized = iban.replaceAll("\\s", "").toUpperCase();
        if (normalized.length() != UAE_IBAN_LENGTH) {
            throw new IllegalArgumentException(
                    "UAE IBAN must be exactly " + UAE_IBAN_LENGTH + " characters, got " + normalized.length());
        }
        if (!normalized.startsWith("AE")) {
            throw new IllegalArgumentException("UAE IBAN must start with 'AE'");
        }
        if (!normalized.substring(2, 4).matches("\\d{2}")) {
            throw new IllegalArgumentException("UAE IBAN check digits (positions 3-4) must be numeric");
        }
        if (!normalized.substring(4, 7).matches("\\d{3}")) {
            throw new IllegalArgumentException("UAE IBAN bank code (positions 5-7) must be 3 digits");
        }
        if (!normalized.substring(7).matches("\\d{16}")) {
            throw new IllegalArgumentException("UAE IBAN account number (positions 8-23) must be 16 digits");
        }
    }

    public static void validateKsaIban(String iban) {
        if (iban == null || iban.isBlank()) {
            throw new IllegalArgumentException("IBAN must not be null or blank");
        }
        String normalized = iban.replaceAll("\\s", "").toUpperCase();
        if (normalized.length() != KSA_IBAN_LENGTH) {
            throw new IllegalArgumentException(
                    "KSA IBAN must be exactly " + KSA_IBAN_LENGTH + " characters, got " + normalized.length());
        }
        if (!normalized.startsWith("SA")) {
            throw new IllegalArgumentException("KSA IBAN must start with 'SA'");
        }
        if (!normalized.substring(2, 4).matches("\\d{2}")) {
            throw new IllegalArgumentException("KSA IBAN check digits (positions 3-4) must be numeric");
        }
        if (!normalized.substring(4, 6).matches("\\d{2}")) {
            throw new IllegalArgumentException("KSA IBAN bank code (positions 5-6) must be 2 digits");
        }
        if (!normalized.substring(6).matches("\\d{18}")) {
            throw new IllegalArgumentException("KSA IBAN account number (positions 7-24) must be 18 digits");
        }
    }

    public static void validate(String iban) {
        if (iban == null || iban.isBlank()) {
            throw new IllegalArgumentException("IBAN must not be null or blank");
        }
        String normalized = iban.replaceAll("\\s", "").toUpperCase();
        if (normalized.startsWith("AE")) {
            validateUaeIban(normalized);
        } else if (normalized.startsWith("SA")) {
            validateKsaIban(normalized);
        } else {
            throw new IllegalArgumentException("Unsupported IBAN country code: " + normalized.substring(0, Math.min(2, normalized.length())));
        }
    }
}
