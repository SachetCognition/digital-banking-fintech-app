package com.yourorg.banking.account.service;

import com.yourorg.banking.account.model.*;
import com.yourorg.banking.account.repo.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountService {
    
    private final AccountRepository accountRepository;
    private static final int MAX_ACCOUNTS_PER_CUSTOMER = 10;
    
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
    
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        // Check if customer has reached maximum accounts limit
        long currentCount = accountRepository.countByCustomerIdAndStatus(request.customerId(), AccountStatus.ACTIVE);
        if (currentCount >= MAX_ACCOUNTS_PER_CUSTOMER) {
            throw new IllegalArgumentException("Maximum number of accounts reached (" + MAX_ACCOUNTS_PER_CUSTOMER + ")");
        }
        
        // Generate unique account number
        String accountNumber = generateAccountNumber();
        
        // Create account
        Account account = new Account(
            UUID.randomUUID(),
            request.customerId(),
            accountNumber,
            request.accountName(),
            request.type(),
            request.currency(),
            request.dailyTransferLimit(),
            request.perTransactionLimit()
        );
        
        accountRepository.save(account);
        return createAccountResponse(account);
    }
    
    public List<AccountResponse> getAccountsByCustomer(UUID customerId) {
        List<Account> accounts = accountRepository.findByCustomerIdAndStatus(customerId, AccountStatus.ACTIVE);
        return accounts.stream()
                .map(this::createAccountResponse)
                .collect(Collectors.toList());
    }
    
    public Optional<AccountResponse> getAccountById(UUID accountId) {
        return accountRepository.findById(accountId)
                .map(this::createAccountResponse);
    }
    
    public Optional<AccountResponse> getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .map(this::createAccountResponse);
    }
    
    @Transactional
    public void updateBalance(UUID accountId, BigDecimal newBalance) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        
        account.updateBalance(newBalance);
        accountRepository.save(account);
    }
    
    @Transactional
    public void reserveAmount(UUID accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        
        if (!account.hasSufficientBalance(amount)) {
            throw new IllegalArgumentException("Insufficient available balance");
        }
        
        account.reserveAmount(amount);
        accountRepository.save(account);
    }
    
    @Transactional
    public void releaseReservation(UUID accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        
        account.releaseReservation(amount);
        accountRepository.save(account);
    }
    
    @Transactional
    public void deactivateAccount(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        
        account.deactivate();
        accountRepository.save(account);
    }
    
    private String generateAccountNumber() {
        String prefix = "ACC";
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
        String random = String.valueOf((int) (Math.random() * 1000)).padStart(3, '0');
        return prefix + timestamp + random;
    }
    
    private AccountResponse createAccountResponse(Account account) {
        return new AccountResponse(
            account.getId(),
            account.getCustomerId(),
            account.getAccountNumber(),
            account.getAccountName(),
            account.getType(),
            account.getStatus(),
            account.getCurrency(),
            account.getBalance(),
            account.getAvailableBalance(),
            account.getDailyTransferLimit(),
            account.getPerTransactionLimit(),
            account.getLastTransactionAt(),
            account.getCreatedAt(),
            account.getUpdatedAt()
        );
    }
}

