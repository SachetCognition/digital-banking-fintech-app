package com.yourorg.banking.payments.service;

import com.yourorg.banking.payments.model.WpsSifRecord;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class WpsService {

    public enum BatchStatus {
        VALIDATED, INSUFFICIENT_FUNDS, PROCESSED, FAILED
    }

    public String generateSifFile(String employerId, List<WpsSifRecord> records) {
        if (records == null || records.isEmpty()) {
            throw new IllegalArgumentException("Records list must not be null or empty");
        }

        StringBuilder sif = new StringBuilder();

        String fileDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        BigDecimal totalAmount = records.stream()
                .map(WpsSifRecord::getNetSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        sif.append("EDR|")
                .append(employerId).append("|")
                .append(fileDate).append("|")
                .append(records.size()).append("|")
                .append(totalAmount.toPlainString())
                .append("\n");

        for (WpsSifRecord record : records) {
            sif.append("SDR|").append(record.toString()).append("\n");
        }

        return sif.toString();
    }

    public BatchResult validateBatch(String batchId, List<WpsSifRecord> records, BigDecimal availableBalance) {
        if (records == null || records.isEmpty()) {
            return new BatchResult(batchId, BatchStatus.FAILED, "No records to validate");
        }

        for (WpsSifRecord record : records) {
            if (record.getEmployeeId() == null || record.getEmployeeId().isBlank()) {
                return new BatchResult(batchId, BatchStatus.FAILED,
                        "Invalid employee record: missing employee ID");
            }
            if (record.getNetSalary() == null || record.getNetSalary().compareTo(BigDecimal.ZERO) <= 0) {
                return new BatchResult(batchId, BatchStatus.FAILED,
                        "Invalid employee record: net salary must be positive for employee " + record.getEmployeeId());
            }
        }

        BigDecimal totalAmount = records.stream()
                .map(WpsSifRecord::getNetSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalAmount.compareTo(availableBalance) > 0) {
            return new BatchResult(batchId, BatchStatus.INSUFFICIENT_FUNDS,
                    "Total batch amount " + totalAmount.toPlainString()
                            + " exceeds available balance " + availableBalance.toPlainString());
        }

        return new BatchResult(batchId, BatchStatus.VALIDATED,
                "Batch validated successfully: " + records.size() + " records, total " + totalAmount.toPlainString());
    }

    public BatchResult processBatch(String employerId, String batchId,
                                     List<WpsSifRecord> records, BigDecimal availableBalance) {
        BatchResult validation = validateBatch(batchId, records, availableBalance);
        if (validation.status() != BatchStatus.VALIDATED) {
            return validation;
        }

        String sifContent = generateSifFile(employerId, records);
        if (sifContent == null || sifContent.isEmpty()) {
            return new BatchResult(batchId, BatchStatus.FAILED, "Failed to generate SIF file");
        }

        return new BatchResult(batchId, BatchStatus.PROCESSED,
                "Batch processed successfully: " + records.size() + " salary payments disbursed",
                UUID.randomUUID(), sifContent);
    }

    public record BatchResult(
            String batchId,
            BatchStatus status,
            String message,
            UUID transactionId,
            String sifContent
    ) {
        public BatchResult(String batchId, BatchStatus status, String message) {
            this(batchId, status, message, null, null);
        }
    }
}
