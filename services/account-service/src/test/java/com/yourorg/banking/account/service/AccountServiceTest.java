package com.yourorg.banking.account.service;

import com.yourorg.banking.account.model.*;
import com.yourorg.banking.account.repo.AccountRepository;
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
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccount_savingsType_succeeds() {
        UUID customerId = UUID.randomUUID();
        CreateAccountRequest request = new CreateAccountRequest(
            customerId, "My Savings", AccountType.SAVINGS, "USD", BigDecimal.ZERO, BigDecimal.ZERO
        );

        when(accountRepository.countByCustomerIdAndStatus(customerId, AccountStatus.ACTIVE)).thenReturn(0L);
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Account account = accountService.createAccount(request);

        assertNotNull(account);
        assertEquals(AccountType.SAVINGS, account.type());
        assertEquals(customerId, account.customerId());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_checkingType_succeeds() {
        UUID customerId = UUID.randomUUID();
        CreateAccountRequest request = new CreateAccountRequest(
            customerId, "My Checking", AccountType.CHECKING, "USD", BigDecimal.ZERO, BigDecimal.ZERO
        );

        when(accountRepository.countByCustomerIdAndStatus(customerId, AccountStatus.ACTIVE)).thenReturn(0L);
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Account account = accountService.createAccount(request);

        assertNotNull(account);
        assertEquals(AccountType.CHECKING, account.type());
    }

    @Test
    void getAccountById_returnsAccountWhenExists() {
        UUID accountId = UUID.randomUUID();
        Account account = new Account(
                accountId, UUID.randomUUID(), "ACC001", "Test", AccountType.CHECKING,
                AccountStatus.ACTIVE, "USD", BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                null, null, false, false, "en", false, null, KycStatus.NOT_REQUIRED,
                AccountOpeningStatus.COMPLETED, null, null, null, null, null,
                BigDecimal.ZERO, null, BigDecimal.ZERO, BigDecimal.ZERO,
                null, null, Instant.now(), Instant.now()
        );

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        Optional<Account> result = accountService.getAccountById(accountId);

        assertTrue(result.isPresent());
        assertEquals(accountId, result.get().id());
    }
}
