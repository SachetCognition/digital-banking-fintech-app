package com.yourorg.banking.payments.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SadadService {

    private final Map<String, BillInfo> billStore = new ConcurrentHashMap<>();
    private final Map<String, PaymentRecord> paymentRecords = new ConcurrentHashMap<>();

    public SadadService() {
        billStore.put("BILL-001", new BillInfo("BILL-001", "DEWA", "CUST-001",
                new BigDecimal("1500.00"), "AED", "2025-12-31", false));
        billStore.put("BILL-002", new BillInfo("BILL-002", "Etisalat", "CUST-001",
                new BigDecimal("350.00"), "AED", "2025-11-30", false));
        billStore.put("BILL-003", new BillInfo("BILL-003", "SEWA", "CUST-002",
                new BigDecimal("800.00"), "AED", "2025-12-15", false));
    }

    public BillPresentment getBillPresentment(String customerId, String billerId) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID must not be null or blank");
        }
        if (billerId == null || billerId.isBlank()) {
            throw new IllegalArgumentException("Biller ID must not be null or blank");
        }

        return billStore.values().stream()
                .filter(b -> b.customerId().equals(customerId) && b.billerName().equals(billerId))
                .filter(b -> !b.paid())
                .findFirst()
                .map(b -> new BillPresentment(true, b.billId(), b.billerName(), b.amount(),
                        b.currency(), b.dueDate(), "Bill found"))
                .orElse(new BillPresentment(false, null, billerId, null, null, null,
                        "No outstanding bills found for customer " + customerId + " with biller " + billerId));
    }

    public PaymentResult payBill(String billId, BigDecimal amount) {
        if (billId == null || billId.isBlank()) {
            return new PaymentResult(false, "Bill ID must not be null or blank", null, null);
        }

        BillInfo bill = billStore.get(billId);
        if (bill == null) {
            return new PaymentResult(false, "Bill not found: " + billId, null, null);
        }

        if (bill.paid()) {
            return new PaymentResult(false, "Bill already paid: " + billId, null, null);
        }

        if (amount == null || amount.compareTo(bill.amount()) < 0) {
            return new PaymentResult(false,
                    "Payment amount must be at least " + bill.amount().toPlainString(), null, null);
        }

        billStore.put(billId, new BillInfo(bill.billId(), bill.billerName(), bill.customerId(),
                bill.amount(), bill.currency(), bill.dueDate(), true));

        UUID paymentId = UUID.randomUUID();
        String reconciliationId = "REC-" + paymentId.toString().substring(0, 8).toUpperCase();

        PaymentRecord record = new PaymentRecord(paymentId, billId, bill.billerName(),
                amount, bill.currency(), Instant.now(), reconciliationId);
        paymentRecords.put(reconciliationId, record);

        return new PaymentResult(true, "Bill paid successfully", paymentId, reconciliationId);
    }

    public PaymentRecord getReconciliationRecord(String reconciliationId) {
        return paymentRecords.get(reconciliationId);
    }

    public record BillInfo(
            String billId,
            String billerName,
            String customerId,
            BigDecimal amount,
            String currency,
            String dueDate,
            boolean paid
    ) {
    }

    public record BillPresentment(
            boolean found,
            String billId,
            String billerName,
            BigDecimal amount,
            String currency,
            String dueDate,
            String message
    ) {
    }

    public record PaymentResult(
            boolean success,
            String message,
            UUID paymentId,
            String reconciliationId
    ) {
    }

    public record PaymentRecord(
            UUID paymentId,
            String billId,
            String billerName,
            BigDecimal amount,
            String currency,
            Instant paidAt,
            String reconciliationId
    ) {
    }
}
