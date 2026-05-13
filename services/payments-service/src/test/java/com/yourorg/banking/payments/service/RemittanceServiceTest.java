package com.yourorg.banking.payments.service;

import com.yourorg.banking.payments.model.RemittanceCorridor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RemittanceServiceTest {

    @InjectMocks
    private RemittanceService remittanceService;

    @Test
    void calculateFee_indiaCorridor_5000Aed_minFeeApplies() {
        // TC-MC-004: India corridor fee for 5,000 AED = 15 AED
        // 5000 * 0.25% = 12.50, min fee = 15.00, so fee = max(12.50, 15.00) = 15.00
        BigDecimal amount = new BigDecimal("5000");
        BigDecimal fee = remittanceService.calculateFee(RemittanceCorridor.INDIA, amount);

        assertEquals(new BigDecimal("15.00"), fee);
    }

    @Test
    void checkAmlLimit_95kAlreadySent_attempt10k_rejected() {
        // TC-MC-005: AML limit enforcement
        UUID customerId = UUID.randomUUID();

        remittanceService.recordMonthlySent(customerId, RemittanceCorridor.INDIA, new BigDecimal("95000"));

        boolean allowed = remittanceService.checkAmlLimit(
            customerId, RemittanceCorridor.INDIA, new BigDecimal("10000")
        );

        assertFalse(allowed);
    }

    @Test
    void validateBeneficiary_validSwiftCode_succeeds() {
        // TC-MC-006: Valid SWIFT code
        assertTrue(remittanceService.validateBeneficiary("SBININBB", "State Bank of India"));
    }

    @Test
    void validateBeneficiary_invalidSwiftCode_fails() {
        // TC-MC-006: Invalid SWIFT code
        assertFalse(remittanceService.validateBeneficiary("INVALID", "Some Bank"));
    }

    @Test
    void validateBeneficiary_11CharSwiftCode_succeeds() {
        assertTrue(remittanceService.validateBeneficiary("SBININBB123", "State Bank of India"));
    }

    @Test
    void validateBeneficiary_nullSwiftCode_fails() {
        assertFalse(remittanceService.validateBeneficiary(null, "Some Bank"));
    }

    @Test
    void validateBeneficiary_emptyBankName_fails() {
        assertFalse(remittanceService.validateBeneficiary("SBININBB", ""));
    }

    @Test
    void processRemittance_indiaCorridor_endToEnd_succeeds() {
        // TC-MC-007: End-to-end remittance to India corridor
        UUID customerId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("5000");

        RemittanceService.RemittanceResult result = remittanceService.processRemittance(
            customerId, RemittanceCorridor.INDIA, amount, "SBININBB", "State Bank of India"
        );

        assertTrue(result.success());
        assertNotNull(result.transactionId());
        assertEquals(new BigDecimal("15.00"), result.fee());
        assertEquals(amount, result.amount());
    }

    @Test
    void processRemittance_invalidBeneficiary_fails() {
        UUID customerId = UUID.randomUUID();

        RemittanceService.RemittanceResult result = remittanceService.processRemittance(
            customerId, RemittanceCorridor.INDIA, new BigDecimal("1000"), "INVALID", "Bank"
        );

        assertFalse(result.success());
        assertEquals("Invalid beneficiary details", result.message());
    }

    @Test
    void processRemittance_exceedsAmlLimit_fails() {
        UUID customerId = UUID.randomUUID();

        remittanceService.recordMonthlySent(customerId, RemittanceCorridor.INDIA, new BigDecimal("95000"));

        RemittanceService.RemittanceResult result = remittanceService.processRemittance(
            customerId, RemittanceCorridor.INDIA, new BigDecimal("10000"), "SBININBB", "State Bank of India"
        );

        assertFalse(result.success());
        assertTrue(result.message().contains("AML limit"));
    }

    @Test
    void calculateFee_largeAmount_percentageFeeApplies() {
        // For large amounts, percentage fee > min fee
        BigDecimal amount = new BigDecimal("50000");
        BigDecimal fee = remittanceService.calculateFee(RemittanceCorridor.INDIA, amount);

        // 50000 * 0.25% = 125.00, min fee = 15.00, so fee = 125.00
        assertEquals(new BigDecimal("125.00"), fee);
    }

    @Test
    void checkAmlLimit_withinLimit_allowed() {
        UUID customerId = UUID.randomUUID();

        boolean allowed = remittanceService.checkAmlLimit(
            customerId, RemittanceCorridor.INDIA, new BigDecimal("50000")
        );

        assertTrue(allowed);
    }

    @Test
    void checkAmlLimit_exactlyAtLimit_allowed() {
        UUID customerId = UUID.randomUUID();

        boolean allowed = remittanceService.checkAmlLimit(
            customerId, RemittanceCorridor.INDIA, new BigDecimal("100000")
        );

        assertTrue(allowed);
    }
}
