package com.yourorg.banking.investment.service;

import com.yourorg.banking.investment.model.*;
import com.yourorg.banking.investment.model.dto.AccountResponse;
import com.yourorg.banking.investment.model.dto.CreateAccountRequest;
import com.yourorg.banking.investment.repository.InvestmentAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvestmentAccountService {
    
    private final InvestmentAccountRepository investmentAccountRepository;
    private final AccountNumberGeneratorService accountNumberGeneratorService;
    
    @Transactional
    public AccountResponse createAccount(UUID customerId, CreateAccountRequest request) {
        log.info("Creating investment account for customer: {}", customerId);
        
        // Validate minimum deposit
        if (request.getInitialDeposit().compareTo(BigDecimal.valueOf(1000)) < 0) {
            throw new RuntimeException("Minimum initial deposit is $1,000");
        }
        
        // Generate account number
        String accountNumber = accountNumberGeneratorService.generateAccountNumber();
        
        // Create account
        InvestmentAccount account = InvestmentAccount.builder()
            .customerId(customerId)
            .accountNumber(accountNumber)
            .accountType(request.getAccountType())
            .accountName(request.getAccountName())
            .status(AccountStatus.ACTIVE)
            .initialDeposit(request.getInitialDeposit())
            .currentBalance(request.getInitialDeposit())
            .availableCash(request.getInitialDeposit())
            .investedAmount(BigDecimal.ZERO)
            .unrealizedPnl(BigDecimal.ZERO)
            .realizedPnl(BigDecimal.ZERO)
            .totalReturn(BigDecimal.ZERO)
            .totalReturnPercentage(BigDecimal.ZERO)
            .riskTolerance(request.getRiskTolerance())
            .investmentObjective(request.getInvestmentObjective())
            .build();
        
        InvestmentAccount savedAccount = investmentAccountRepository.save(account);
        
        return convertToResponse(savedAccount);
    }
    
    public List<AccountResponse> getCustomerAccounts(UUID customerId) {
        log.info("Getting investment accounts for customer: {}", customerId);
        
        List<InvestmentAccount> accounts = investmentAccountRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        
        return accounts.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    public AccountResponse getAccount(String accountNumber) {
        log.info("Getting investment account: {}", accountNumber);
        
        InvestmentAccount account = investmentAccountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        return convertToResponse(account);
    }
    
    @Transactional
    public AccountResponse updateAccount(String accountNumber, String accountName, 
                                       RiskTolerance riskTolerance, String investmentObjective) {
        log.info("Updating investment account: {}", accountNumber);
        
        InvestmentAccount account = investmentAccountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        if (accountName != null) {
            account.setAccountName(accountName);
        }
        if (riskTolerance != null) {
            account.setRiskTolerance(riskTolerance);
        }
        if (investmentObjective != null) {
            account.setInvestmentObjective(investmentObjective);
        }
        
        InvestmentAccount savedAccount = investmentAccountRepository.save(account);
        
        return convertToResponse(savedAccount);
    }
    
    @Transactional
    public AccountResponse deposit(String accountNumber, BigDecimal amount) {
        log.info("Depositing {} to account: {}", amount, accountNumber);
        
        InvestmentAccount account = investmentAccountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("Account is not active");
        }
        
        account.setAvailableCash(account.getAvailableCash().add(amount));
        account.setCurrentBalance(account.getCurrentBalance().add(amount));
        
        InvestmentAccount savedAccount = investmentAccountRepository.save(account);
        
        return convertToResponse(savedAccount);
    }
    
    @Transactional
    public AccountResponse withdraw(String accountNumber, BigDecimal amount) {
        log.info("Withdrawing {} from account: {}", amount, accountNumber);
        
        InvestmentAccount account = investmentAccountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("Account is not active");
        }
        
        if (account.getAvailableCash().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient available cash");
        }
        
        account.setAvailableCash(account.getAvailableCash().subtract(amount));
        account.setCurrentBalance(account.getCurrentBalance().subtract(amount));
        
        InvestmentAccount savedAccount = investmentAccountRepository.save(account);
        
        return convertToResponse(savedAccount);
    }
    
    @Transactional
    public AccountResponse closeAccount(String accountNumber) {
        log.info("Closing investment account: {}", accountNumber);
        
        InvestmentAccount account = investmentAccountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new RuntimeException("Account is already closed");
        }
        
        // Check if account has any holdings
        if (account.getInvestedAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("Cannot close account with active investments. Please sell all holdings first.");
        }
        
        account.setStatus(AccountStatus.CLOSED);
        
        InvestmentAccount savedAccount = investmentAccountRepository.save(account);
        
        return convertToResponse(savedAccount);
    }
    
    public BigDecimal getTotalAccountValue(UUID customerId) {
        List<InvestmentAccount> accounts = investmentAccountRepository.findByCustomerIdAndStatus(
            customerId, AccountStatus.ACTIVE);
        
        return accounts.stream()
            .map(InvestmentAccount::getTotalValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal getTotalInvestedAmount(UUID customerId) {
        List<InvestmentAccount> accounts = investmentAccountRepository.findByCustomerIdAndStatus(
            customerId, AccountStatus.ACTIVE);
        
        return accounts.stream()
            .map(account -> account.getInvestedAmount() != null ? account.getInvestedAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private AccountResponse convertToResponse(InvestmentAccount account) {
        return AccountResponse.builder()
            .id(account.getId())
            .accountNumber(account.getAccountNumber())
            .accountType(account.getAccountType().getDisplayName())
            .accountName(account.getAccountName())
            .status(account.getStatus().getDisplayName())
            .initialDeposit(account.getInitialDeposit())
            .currentBalance(account.getCurrentBalance())
            .availableCash(account.getAvailableCash())
            .investedAmount(account.getInvestedAmount())
            .unrealizedPnl(account.getUnrealizedPnl())
            .realizedPnl(account.getRealizedPnl())
            .totalReturn(account.getTotalReturn())
            .totalReturnPercentage(account.getTotalReturnPercentage())
            .riskTolerance(account.getRiskTolerance() != null ? 
                account.getRiskTolerance().getDisplayName() : null)
            .investmentObjective(account.getInvestmentObjective())
            .totalValue(account.getTotalValue())
            .totalPnl(account.getTotalPnl())
            .createdAt(account.getCreatedAt())
            .build();
    }
}

