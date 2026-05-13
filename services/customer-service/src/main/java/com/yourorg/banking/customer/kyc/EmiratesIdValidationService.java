package com.yourorg.banking.customer.kyc;

import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class EmiratesIdValidationService {

    private static final String EMIRATES_ID_PATTERN = "784-\\d{4}-\\d{7}-\\d";

    public void validate(String emiratesId) {
        validateFormat(emiratesId);
        validateChecksum(emiratesId);
    }

    public void validate(String emiratesId, LocalDate expiryDate) {
        validate(emiratesId);
        validateExpiry(expiryDate);
    }

    private void validateFormat(String emiratesId) {
        if (emiratesId == null || !emiratesId.matches(EMIRATES_ID_PATTERN)) {
            throw new IllegalArgumentException(
                    "Invalid Emirates ID format. Expected: 784-YYYY-NNNNNNN-C");
        }
    }

    private void validateChecksum(String emiratesId) {
        String[] parts = emiratesId.split("-");
        String payload = parts[0] + parts[1] + parts[2];
        int actualCheckDigit = Integer.parseInt(parts[3]);

        int expectedCheckDigit = computeCheckDigit(payload);
        if (actualCheckDigit != expectedCheckDigit) {
            throw new IllegalArgumentException("Invalid Emirates ID checksum");
        }
    }

    private void validateExpiry(LocalDate expiryDate) {
        if (expiryDate != null && expiryDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Emirates ID has expired on " + expiryDate);
        }
    }

    int computeCheckDigit(String payload) {
        int sum = 0;
        boolean doubleDigit = false;
        for (int i = payload.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(payload.charAt(i));
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return (9 - (sum % 9)) % 9;
    }
}
