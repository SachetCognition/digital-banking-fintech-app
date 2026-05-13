package com.yourorg.banking.customer.kyc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EmiratesIdValidationServiceTest {

    private EmiratesIdValidationService service;

    @BeforeEach
    void setUp() {
        service = new EmiratesIdValidationService();
    }

    @Test
    void validate_validEmiratesId_passes() {
        // TC-KY-001: Valid Emirates ID 784-1990-1234567-1 passes validation
        assertDoesNotThrow(() -> service.validate("784-1990-1234567-1"));
    }

    @Test
    void validate_expiredEmiratesId_rejected() {
        // TC-KY-002: Expired Emirates ID rejected with reason
        LocalDate pastDate = LocalDate.now().minusYears(1);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.validate("784-1990-1234567-1", pastDate));
        assertTrue(ex.getMessage().contains("expired"));
    }

    @Test
    void validate_invalidChecksum_rejected() {
        // TC-KY-003: Invalid checksum 784-1990-1234567-9 rejected
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.validate("784-1990-1234567-9"));
        assertTrue(ex.getMessage().contains("checksum"));
    }
}
