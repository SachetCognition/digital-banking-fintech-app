package com.yourorg.banking.account.service;

import com.yourorg.banking.account.model.*;
import com.yourorg.banking.account.repo.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
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
    public Account createAccount(CreateAccountRequest request) {
        long currentCount = accountRepository.countByCustomerIdAndStatus(request.customerId(), AccountStatus.ACTIVE);
        if (currentCount >= MAX_ACCOUNTS_PER_CUSTOMER) {
            throw new IllegalArgumentException("Maximum number of accounts reached (" + MAX_ACCOUNTS_PER_CUSTOMER + ")");
        }
        
        String accountNumber = generateAccountNumber();
        
        Account account = new Account(
            UUID.randomUUID(),
            request.customerId(),
            accountNumber,
            request.accountName(),
            request.type(),
            AccountStatus.ACTIVE,
            request.currency(),
            BigDecimal.ZERO, // balance
            BigDecimal.ZERO, // availableBalance
            BigDecimal.valueOf(request.type().getInterestRate()),
            BigDecimal.valueOf(request.type().getFeeRate()),
            request.dailyTransferLimit() != null ? request.dailyTransferLimit() : BigDecimal.ZERO,
            request.perTransactionLimit() != null ? request.perTransactionLimit() : BigDecimal.ZERO,
            null, // lastTransactionAt
            null, // description
            false, // paperlessStatements
            false, // emailNotifications
            "en", // preferredLanguage
            false, // kycRequired
            null, // kycLevel
            KycStatus.NOT_REQUIRED,
            AccountOpeningStatus.COMPLETED,
            null, // openingReason
            null, // closureReason
            null, // closureRequestedAt
            null, // closureApprovedAt
            null, // closureApprovedBy
            BigDecimal.ZERO, // minimumBalance
            null, // maximumBalance
            BigDecimal.ZERO, // monthlyFee
            BigDecimal.ZERO, // overdraftLimit
            null, // lastInterestCalculation
            null, // nextInterestCalculation
            Instant.now(),
            Instant.now()
        );
        
        return accountRepository.save(account);
    }
    
    public List<Account> getAccountsByCustomer(UUID customerId) {
        return accountRepository.findByCustomerIdAndStatus(customerId, AccountStatus.ACTIVE);
    }
    
    public Optional<Account> getAccountById(UUID accountId) {
        return accountRepository.findById(accountId);
    }
    
    public Optional<Account> getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }
    
    @Transactional
    public void updateBalance(UUID accountId, BigDecimal newBalance) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        
        Account updated = account.updateBalance(newBalance);
        accountRepository.save(updated);
    }
    
    @Transactional
    public void reserveAmount(UUID accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        
        if (!account.hasSufficientBalance(amount)) {
            throw new IllegalArgumentException("Insufficient available balance");
        }
        
        Account updated = account.reserveAmount(amount);
        accountRepository.save(updated);
    }
    
    @Transactional
    public void releaseReservation(UUID accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        
        Account updated = account.releaseReservation(amount);
        accountRepository.save(updated);
    }
    
    @Transactional
    public void deactivateAccount(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        
        Account updated = account.deactivate();
        accountRepository.save(updated);
    }
    
    private String generateAccountNumber() {
        String prefix = "ACC";
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
        String random = String.format("%03d", (int) (Math.random() * 1000));
        return prefix + timestamp + random;
    }
}
