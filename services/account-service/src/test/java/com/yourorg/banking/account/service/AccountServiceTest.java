package com.yourorg.banking.account.service;

import com.yourorg.banking.account.model.*;
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
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccount_savingsType_succeeds() {
        UUID customerId = UUID.randomUUID();
        CreateAccountRequest request = new CreateAccountRequest(
            customerId, AccountType.SAVINGS, "USD", "My Savings"
        );

        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Account account = accountService.createAccount(request);

        assertNotNull(account);
        assertEquals(AccountType.SAVINGS, account.getType());
        assertEquals(customerId, account.getCustomerId());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_checkingType_succeeds() {
        UUID customerId = UUID.randomUUID();
        CreateAccountRequest request = new CreateAccountRequest(
            customerId, AccountType.CHECKING, "USD", "My Checking"
        );

        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Account account = accountService.createAccount(request);

        assertNotNull(account);
        assertEquals(AccountType.CHECKING, account.getType());
    }

    @Test
    void getAccountById_returnsAccountWhenExists() {
        UUID accountId = UUID.randomUUID();
        Account account = new Account();
        account.setId(accountId);
        account.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        Optional<Account> result = accountService.getAccountById(accountId);

        assertTrue(result.isPresent());
        assertEquals(accountId, result.get().getId());
    }
}
