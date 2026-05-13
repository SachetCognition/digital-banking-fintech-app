package com.yourorg.banking.account.service;

import com.yourorg.banking.account.model.*;
import com.yourorg.banking.account.repo.AccountClosureRequestRepository;
import com.yourorg.banking.account.repo.AccountRepository;
import com.yourorg.banking.ledger.client.LedgerServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountClosureServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountClosureRequestRepository closureRequestRepository;

    @Mock
    private LedgerServiceClient ledgerServiceClient;

    @Mock
    private StatementService statementService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AccountClosureService accountClosureService;

    private Account createTestAccount(UUID accountId, UUID customerId, AccountStatus status) {
        return new Account(
                accountId, customerId, "ACC001", "Test", AccountType.CHECKING,
                status, "USD", BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                null, null, false, false, "en", false, null, KycStatus.NOT_REQUIRED,
                AccountOpeningStatus.COMPLETED, null, null, null, null, null,
                BigDecimal.ZERO, null, BigDecimal.ZERO, BigDecimal.ZERO,
                null, null, Instant.now(), Instant.now()
        );
    }

    @Test
    void requestAccountClosure_validAccount_succeeds() {
        UUID accountId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Account account = createTestAccount(accountId, customerId, AccountStatus.ACTIVE);

        AccountClosureRequest closureReq = new AccountClosureRequest(
                UUID.randomUUID(), accountId, customerId, "No longer needed",
                null, null, AccountClosureStatus.PENDING, null, null, null,
                null, null, false, false, Instant.now(), Instant.now(), null
        );

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AccountClosureRequest result = accountClosureService.requestAccountClosure(customerId, closureReq);

        assertNotNull(result);
    }

    @Test
    void requestAccountClosure_wrongCustomer_throwsException() {
        UUID accountId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID wrongCustomerId = UUID.randomUUID();
        Account account = createTestAccount(accountId, customerId, AccountStatus.ACTIVE);

        AccountClosureRequest closureReq = new AccountClosureRequest(
                UUID.randomUUID(), accountId, wrongCustomerId, "Close it",
                null, null, AccountClosureStatus.PENDING, null, null, null,
                null, null, false, false, Instant.now(), Instant.now(), null
        );

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThrows(IllegalArgumentException.class,
            () -> accountClosureService.requestAccountClosure(wrongCustomerId, closureReq));
    }

    @Test
    void requestAccountClosure_alreadyClosed_throwsException() {
        UUID accountId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Account account = createTestAccount(accountId, customerId, AccountStatus.CLOSED);

        AccountClosureRequest closureReq = new AccountClosureRequest(
                UUID.randomUUID(), accountId, customerId, "Already closed",
                null, null, AccountClosureStatus.PENDING, null, null, null,
                null, null, false, false, Instant.now(), Instant.now(), null
        );

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThrows(IllegalStateException.class,
            () -> accountClosureService.requestAccountClosure(customerId, closureReq));
    }
}
