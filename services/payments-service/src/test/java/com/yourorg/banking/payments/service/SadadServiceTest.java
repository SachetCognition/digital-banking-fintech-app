package com.yourorg.banking.payments.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SadadServiceTest {

    @InjectMocks
    private SadadService sadadService;

    @Test
    void billPresentment_payment_reconciliation_endToEnd() {
        // TC-PN-007: Bill presentment + payment + reconciliation
        SadadService.BillPresentment presentment = sadadService.getBillPresentment("CUST-001", "DEWA");
        assertTrue(presentment.found());
        assertEquals("BILL-001", presentment.billId());
        assertEquals("DEWA", presentment.billerName());
        assertEquals(new BigDecimal("1500.00"), presentment.amount());
        assertEquals("AED", presentment.currency());
        assertNotNull(presentment.dueDate());

        SadadService.PaymentResult paymentResult = sadadService.payBill("BILL-001", new BigDecimal("1500.00"));
        assertTrue(paymentResult.success());
        assertNotNull(paymentResult.paymentId());
        assertNotNull(paymentResult.reconciliationId());
        assertTrue(paymentResult.reconciliationId().startsWith("REC-"));

        SadadService.PaymentRecord reconciliation = sadadService.getReconciliationRecord(
                paymentResult.reconciliationId());
        assertNotNull(reconciliation);
        assertEquals("BILL-001", reconciliation.billId());
        assertEquals("DEWA", reconciliation.billerName());
        assertEquals(new BigDecimal("1500.00"), reconciliation.amount());
        assertEquals("AED", reconciliation.currency());
        assertNotNull(reconciliation.paidAt());
        assertEquals(paymentResult.reconciliationId(), reconciliation.reconciliationId());

        SadadService.PaymentResult duplicatePayment = sadadService.payBill("BILL-001", new BigDecimal("1500.00"));
        assertFalse(duplicatePayment.success());
        assertTrue(duplicatePayment.message().contains("already paid"));
    }
}
