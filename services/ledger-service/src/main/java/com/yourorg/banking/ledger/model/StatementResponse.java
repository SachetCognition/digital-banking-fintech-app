package com.yourorg.banking.ledger.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record StatementResponse(
        UUID accountId,
        String currency,
        BigDecimal openingBalance,
        BigDecimal closingBalance,
        Instant fromDate,
        Instant toDate,
        List<StatementEntry> entries
) {
    public record StatementEntry(
            UUID entryId,
            Instant date,
            String description,
            String reference,
            BigDecimal debitAmount,
            BigDecimal creditAmount,
            BigDecimal runningBalance
    ) {}
}

