package com.yourorg.banking.ledger.client;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class LedgerServiceClient {
    public void transferBalance(UUID fromAccountId, UUID toAccountId, BigDecimal amount) {
        // Stub - no-op for now
    }
}
