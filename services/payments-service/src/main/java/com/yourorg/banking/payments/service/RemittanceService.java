package com.yourorg.banking.payments.service;

import com.yourorg.banking.payments.model.RemittanceCorridor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RemittanceService {

    private final Map<String, BigDecimal> monthlySentAmounts = new ConcurrentHashMap<>();

    public BigDecimal calculateFee(RemittanceCorridor corridor, BigDecimal amount) {
        BigDecimal percentageFeeAmount = amount
                .multiply(corridor.getPercentageFee())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        return percentageFeeAmount.max(corridor.getMinFee());
    }

    public boolean validateBeneficiary(String swiftCode, String bankName) {
        if (swiftCode == null || swiftCode.isBlank()) {
            return false;
        }
        if (bankName == null || bankName.isBlank()) {
            return false;
        }
        return swiftCode.matches("^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$");
    }

    public boolean checkAmlLimit(UUID customerId, RemittanceCorridor corridor, BigDecimal amount) {
        String key = customerId.toString() + "_" + corridor.name();
        BigDecimal alreadySent = monthlySentAmounts.getOrDefault(key, BigDecimal.ZERO);
        return alreadySent.add(amount).compareTo(corridor.getMonthlyAmlLimit()) <= 0;
    }

    public RemittanceResult processRemittance(UUID customerId, RemittanceCorridor corridor,
                                               BigDecimal amount, String swiftCode, String bankName) {
        if (!validateBeneficiary(swiftCode, bankName)) {
            return new RemittanceResult(false, "Invalid beneficiary details", null);
        }

        if (!checkAmlLimit(customerId, corridor, amount)) {
            return new RemittanceResult(false, "Monthly AML limit exceeded for " + corridor.getCountryName(), null);
        }

        BigDecimal fee = calculateFee(corridor, amount);

        String key = customerId.toString() + "_" + corridor.name();
        monthlySentAmounts.merge(key, amount, BigDecimal::add);

        UUID transactionId = UUID.randomUUID();
        return new RemittanceResult(true, "Remittance processed successfully", transactionId, fee, amount);
    }

    public void recordMonthlySent(UUID customerId, RemittanceCorridor corridor, BigDecimal amount) {
        String key = customerId.toString() + "_" + corridor.name();
        monthlySentAmounts.merge(key, amount, BigDecimal::add);
    }

    public record RemittanceResult(
        boolean success,
        String message,
        UUID transactionId,
        BigDecimal fee,
        BigDecimal amount
    ) {
        public RemittanceResult(boolean success, String message, UUID transactionId) {
            this(success, message, transactionId, null, null);
        }
    }
}
