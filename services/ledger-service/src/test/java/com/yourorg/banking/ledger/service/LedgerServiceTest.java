package com.yourorg.banking.ledger.service;

import com.yourorg.banking.ledger.model.*;
import com.yourorg.banking.ledger.repo.AccountBalanceRepository;
import com.yourorg.banking.ledger.repo.JournalEntryRepository;
import com.yourorg.banking.ledger.repo.JournalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    @Mock
    private JournalRepository journalRepository;

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private AccountBalanceRepository accountBalanceRepository;

    @InjectMocks
    private LedgerService ledgerService;

    @Test
    void postJournal_balancedEntries_succeeds() {
        UUID accountId1 = UUID.randomUUID();
        UUID accountId2 = UUID.randomUUID();

        PostJournalRequest request = new PostJournalRequest(
            "Test Transfer",
            List.of(
                new PostJournalRequest.Entry(accountId1, EntryType.DEBIT, new BigDecimal("100.00")),
                new PostJournalRequest.Entry(accountId2, EntryType.CREDIT, new BigDecimal("100.00"))
            )
        );

        when(journalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(accountBalanceRepository.findByAccountId(any())).thenReturn(
            Optional.of(new AccountBalance(accountId1, new BigDecimal("500.00"), "USD"))
        );

        JournalResponse response = ledgerService.postJournal(request);

        assertNotNull(response);
        assertEquals(JournalStatus.POSTED, response.status());
    }

    @Test
    void postJournal_unbalancedEntries_throwsException() {
        UUID accountId1 = UUID.randomUUID();
        UUID accountId2 = UUID.randomUUID();

        PostJournalRequest request = new PostJournalRequest(
            "Unbalanced",
            List.of(
                new PostJournalRequest.Entry(accountId1, EntryType.DEBIT, new BigDecimal("100.00")),
                new PostJournalRequest.Entry(accountId2, EntryType.CREDIT, new BigDecimal("50.00"))
            )
        );

        assertThrows(IllegalArgumentException.class, () -> ledgerService.postJournal(request));
    }

    @Test
    void getBalance_returnsBalanceForExistingAccount() {
        UUID accountId = UUID.randomUUID();
        AccountBalance balance = new AccountBalance(accountId, new BigDecimal("1000.00"), "USD");
        when(accountBalanceRepository.findByAccountId(accountId)).thenReturn(Optional.of(balance));

        BalanceResponse response = ledgerService.getBalance(accountId);

        assertNotNull(response);
        assertEquals(new BigDecimal("1000.00"), response.balance());
    }
}
