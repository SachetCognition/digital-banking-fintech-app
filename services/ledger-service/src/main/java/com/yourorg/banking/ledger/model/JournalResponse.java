package com.yourorg.banking.ledger.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record JournalResponse(
        UUID journalId,
        String reference,
        String description,
        JournalStatus status,
        Instant createdAt,
        Instant postedAt,
        List<JournalEntryResponse> entries
) {
    public record JournalEntryResponse(
            UUID entryId,
            UUID accountId,
            EntryType type,
            BigDecimal amount,
            String currency,
            String description,
            String reference,
            Instant createdAt
    ) {}
}

