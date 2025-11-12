package com.yourorg.banking.ledger.service;

import com.yourorg.banking.ledger.model.*;
import com.yourorg.banking.ledger.repo.AccountBalanceRepository;
import com.yourorg.banking.ledger.repo.JournalEntryRepository;
import com.yourorg.banking.ledger.repo.JournalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LedgerService {
    
    private final JournalRepository journalRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final AccountBalanceRepository accountBalanceRepository;
    
    public LedgerService(JournalRepository journalRepository,
                        JournalEntryRepository journalEntryRepository,
                        AccountBalanceRepository accountBalanceRepository) {
        this.journalRepository = journalRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.accountBalanceRepository = accountBalanceRepository;
    }
    
    @Transactional
    public JournalResponse postJournal(PostJournalRequest request) {
        // Validate journal entries (debits must equal credits)
        validateJournalEntries(request.entries());
        
        // Create journal
        Journal journal = new Journal(UUID.randomUUID(), request.reference(), request.description());
        journalRepository.save(journal);
        
        // Create journal entries
        List<JournalEntry> entries = request.entries().stream()
            .map(entryRequest -> new JournalEntry(
                UUID.randomUUID(),
                journal.getId(),
                entryRequest.accountId(),
                entryRequest.type(),
                entryRequest.amount(),
                entryRequest.currency(),
                entryRequest.description(),
                entryRequest.reference()
            ))
            .collect(Collectors.toList());
        
        journalEntryRepository.saveAll(entries);
        
        // Post journal and update balances
        journal.post();
        journalRepository.save(journal);
        
        updateAccountBalances(entries);
        
        return createJournalResponse(journal, entries);
    }
    
    public BalanceResponse getAccountBalance(UUID accountId) {
        AccountBalance balance = accountBalanceRepository.findByAccountId(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Account balance not found"));
        
        return new BalanceResponse(
            balance.getAccountId(),
            balance.getCurrency(),
            balance.getBalance(),
            balance.getLastUpdatedAt()
        );
    }
    
    public StatementResponse getAccountStatement(UUID accountId, Instant fromDate, Instant toDate) {
        // Get opening balance (balance before fromDate)
        BigDecimal openingBalance = getBalanceBeforeDate(accountId, fromDate);
        
        // Get entries in date range
        List<JournalEntry> entries = journalEntryRepository.findByAccountIdAndDateRange(accountId, fromDate, toDate);
        
        // Calculate running balance
        BigDecimal runningBalance = openingBalance;
        List<StatementResponse.StatementEntry> statementEntries = entries.stream()
            .map(entry -> {
                if (entry.getType() == EntryType.DEBIT) {
                    runningBalance = runningBalance.add(entry.getAmount());
                } else {
                    runningBalance = runningBalance.subtract(entry.getAmount());
                }
                
                return new StatementResponse.StatementEntry(
                    entry.getId(),
                    entry.getCreatedAt(),
                    entry.getDescription(),
                    entry.getReference(),
                    entry.getType() == EntryType.DEBIT ? entry.getAmount() : BigDecimal.ZERO,
                    entry.getType() == EntryType.CREDIT ? entry.getAmount() : BigDecimal.ZERO,
                    runningBalance
                );
            })
            .collect(Collectors.toList());
        
        // Get closing balance
        BigDecimal closingBalance = getAccountBalance(accountId).balance();
        
        return new StatementResponse(
            accountId,
            entries.isEmpty() ? "USD" : entries.get(0).getCurrency(),
            openingBalance,
            closingBalance,
            fromDate,
            toDate,
            statementEntries
        );
    }
    
    @Transactional
    public void updateAccountBalance(UUID accountId, BigDecimal newBalance) {
        AccountBalance balance = accountBalanceRepository.findByAccountId(accountId)
            .orElse(new AccountBalance(UUID.randomUUID(), accountId, "USD", BigDecimal.ZERO));
        
        balance.updateBalance(newBalance);
        accountBalanceRepository.save(balance);
    }
    
    private void validateJournalEntries(List<PostJournalRequest.JournalEntryRequest> entries) {
        BigDecimal totalDebits = entries.stream()
            .filter(entry -> entry.type() == EntryType.DEBIT)
            .map(PostJournalRequest.JournalEntryRequest::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCredits = entries.stream()
            .filter(entry -> entry.type() == EntryType.CREDIT)
            .map(PostJournalRequest.JournalEntryRequest::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalDebits.compareTo(totalCredits) != 0) {
            throw new IllegalArgumentException("Total debits must equal total credits");
        }
    }
    
    private void updateAccountBalances(List<JournalEntry> entries) {
        for (JournalEntry entry : entries) {
            AccountBalance balance = accountBalanceRepository.findByAccountId(entry.getAccountId())
                .orElse(new AccountBalance(UUID.randomUUID(), entry.getAccountId(), entry.getCurrency(), BigDecimal.ZERO));
            
            if (entry.getType() == EntryType.DEBIT) {
                balance.addAmount(entry.getAmount());
            } else {
                balance.subtractAmount(entry.getAmount());
            }
            
            accountBalanceRepository.save(balance);
        }
    }
    
    private BigDecimal getBalanceBeforeDate(UUID accountId, Instant fromDate) {
        BigDecimal debitTotal = journalEntryRepository.getTotalAmountByAccountIdAndType(accountId, EntryType.DEBIT);
        BigDecimal creditTotal = journalEntryRepository.getTotalAmountByAccountIdAndType(accountId, EntryType.CREDIT);
        
        if (debitTotal == null) debitTotal = BigDecimal.ZERO;
        if (creditTotal == null) creditTotal = BigDecimal.ZERO;
        
        return debitTotal.subtract(creditTotal);
    }
    
    private JournalResponse createJournalResponse(Journal journal, List<JournalEntry> entries) {
        List<JournalResponse.JournalEntryResponse> entryResponses = entries.stream()
            .map(entry -> new JournalResponse.JournalEntryResponse(
                entry.getId(),
                entry.getAccountId(),
                entry.getType(),
                entry.getAmount(),
                entry.getCurrency(),
                entry.getDescription(),
                entry.getReference(),
                entry.getCreatedAt()
            ))
            .collect(Collectors.toList());
        
        return new JournalResponse(
            journal.getId(),
            journal.getReference(),
            journal.getDescription(),
            journal.getStatus(),
            journal.getCreatedAt(),
            journal.getPostedAt(),
            entryResponses
        );
    }
}

