package com.yourorg.banking.account.service;

import com.yourorg.banking.account.model.*;
import com.yourorg.banking.account.repo.AccountClosureRequestRepository;
import com.yourorg.banking.account.repo.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private AccountClosureService accountClosureService;

    @Test
    void requestClosure_validAccount_succeeds() {
        UUID accountId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Account account = new Account();
        account.setId(accountId);
        account.setCustomerId(customerId);
        account.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(closureRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AccountClosureRequest request = accountClosureService.requestClosure(accountId, customerId, "No longer needed");

        assertNotNull(request);
        verify(closureRequestRepository).save(any(AccountClosureRequest.class));
    }

    @Test
    void requestClosure_wrongCustomer_throwsException() {
        UUID accountId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID wrongCustomerId = UUID.randomUUID();
        Account account = new Account();
        account.setId(accountId);
        account.setCustomerId(customerId);
        account.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThrows(IllegalArgumentException.class,
            () -> accountClosureService.requestClosure(accountId, wrongCustomerId, "Close it"));
    }

    @Test
    void requestClosure_alreadyClosed_throwsException() {
        UUID accountId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Account account = new Account();
        account.setId(accountId);
        account.setCustomerId(customerId);
        account.setStatus(AccountStatus.CLOSED);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThrows(IllegalStateException.class,
            () -> accountClosureService.requestClosure(accountId, customerId, "Already closed"));
    }
}
