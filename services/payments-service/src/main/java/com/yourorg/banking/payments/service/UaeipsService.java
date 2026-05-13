package com.yourorg.banking.payments.service;

import com.yourorg.banking.payments.model.IbanValidator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class UaeipsService {

    private static final BigDecimal MAX_INSTANT_AMOUNT = new BigDecimal("50000");

    public TransferResult initiateInstantTransfer(String senderIban, String receiverIban,
                                                   BigDecimal amount, String currency) {
        try {
            IbanValidator.validateUaeIban(senderIban);
        } catch (IllegalArgumentException e) {
            return new TransferResult(false, "Invalid sender IBAN: " + e.getMessage(),
                    null, null, null);
        }

        try {
            IbanValidator.validateUaeIban(receiverIban);
        } catch (IllegalArgumentException e) {
            return new TransferResult(false, "Invalid receiver IBAN: " + e.getMessage(),
                    null, null, null);
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return new TransferResult(false, "Amount must be positive",
                    null, null, null);
        }

        if (amount.compareTo(MAX_INSTANT_AMOUNT) > 0) {
            return new TransferResult(false,
                    "Amount exceeds instant payment limit of " + MAX_INSTANT_AMOUNT.toPlainString(),
                    null, null, null);
        }

        if (currency == null || currency.isBlank()) {
            return new TransferResult(false, "Currency must be specified",
                    null, null, null);
        }

        UUID transactionId = UUID.randomUUID();
        Instant settlementTime = Instant.now();

        return new TransferResult(true, "Instant transfer completed successfully",
                transactionId, "SETTLED", settlementTime);
    }

    public record TransferResult(
            boolean success,
            String message,
            UUID transactionId,
            String status,
            Instant settlementTime
    ) {
    }
}
