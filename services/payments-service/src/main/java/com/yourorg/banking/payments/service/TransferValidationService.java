package com.yourorg.banking.payments.service;

import com.yourorg.banking.payments.client.AccountServiceClient;
import com.yourorg.banking.payments.model.AccountInfo;
import com.yourorg.banking.payments.model.AccountLimitInfo;
import com.yourorg.banking.payments.model.TransferRequest;
import com.yourorg.banking.payments.repository.TransferRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class TransferValidationService {
    
    private final AccountServiceClient accountServiceClient;
    private final TransferRepository transferRepository;
    private final BigDecimal dailyLimit;
    private final BigDecimal perTransactionLimit;
    private final BigDecimal minAmount;
    
    public TransferValidationService(AccountServiceClient accountServiceClient,
                                  TransferRepository transferRepository,
                                  @Value("${transfer.limits.daily-limit}") BigDecimal dailyLimit,
                                  @Value("${transfer.limits.per-transaction-limit}") BigDecimal perTransactionLimit,
                                  @Value("${transfer.limits.min-amount}") BigDecimal minAmount) {
        this.accountServiceClient = accountServiceClient;
        this.transferRepository = transferRepository;
        this.dailyLimit = dailyLimit;
        this.perTransactionLimit = perTransactionLimit;
        this.minAmount = minAmount;
    }
    
    public void validateTransfer(TransferRequest request, String authToken) {
        // 1. Validate accounts exist and are active
        validateAccounts(request, authToken);
        
        // Resolve effective per-account limits (fallback to config if none set)
        Limits effective = getEffectiveLimits(request.payerAccountId(), authToken);

        // 2. Validate amount limits (min + per-transaction)
        validateAmountLimits(request, effective);
        
        // 3. Validate daily limits
        validateDailyLimits(request, effective);
        
        // 4. Validate payer has sufficient balance
        validateSufficientBalance(request, authToken);
        
        // 5. Validate payer and payee are different
        validateDifferentAccounts(request);
    }

    private void validateAccounts(TransferRequest request, String authToken) {
        AccountInfo payerAccount = accountServiceClient.getAccountInfo(request.payerAccountId(), authToken);
        AccountInfo payeeAccount = accountServiceClient.getAccountInfo(request.payeeAccountId(), authToken);
        
        if (!payerAccount.active()) {
            throw new IllegalArgumentException("Payer account is not active");
        }
        
        if (!payeeAccount.active()) {
            throw new IllegalArgumentException("Payee account is not active");
        }
        
        if (!payerAccount.currency().equals(request.currency())) {
            throw new IllegalArgumentException("Payer account currency mismatch");
        }
        
        if (!payeeAccount.currency().equals(request.currency())) {
            throw new IllegalArgumentException("Payee account currency mismatch");
        }
    }
    
    private void validateAmountLimits(TransferRequest request, Limits limits) {
        if (request.amount().compareTo(minAmount) < 0) {
            throw new IllegalArgumentException("Amount must be at least " + minAmount);
        }
        
        if (request.amount().compareTo(limits.perTxnLimit()) > 0) {
            throw new IllegalArgumentException("Amount exceeds per-transaction limit of " + limits.perTxnLimit());
        }
    }
    
    private void validateDailyLimits(TransferRequest request, Limits limits) {
        Instant startOfDay = Instant.now().truncatedTo(ChronoUnit.DAYS);
        Double totalTransferred = transferRepository.getTotalTransferredAmountByPayerSince(
            request.payerAccountId(), startOfDay);
        
        BigDecimal totalTransferredAmount = BigDecimal.valueOf(totalTransferred != null ? totalTransferred : 0.0);
        BigDecimal projectedTotal = totalTransferredAmount.add(request.amount());
        
        if (projectedTotal.compareTo(limits.dailyLimit()) > 0) {
            throw new IllegalArgumentException("Transfer would exceed daily limit of " + limits.dailyLimit());
        }
    }
    
    private void validateSufficientBalance(TransferRequest request, String authToken) {
        AccountInfo payerAccount = accountServiceClient.getAccountInfo(request.payerAccountId(), authToken);
        
        if (payerAccount.balance().compareTo(request.amount()) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }
    }
    
    private void validateDifferentAccounts(TransferRequest request) {
        if (request.payerAccountId().equals(request.payeeAccountId())) {
            throw new IllegalArgumentException("Payer and payee accounts must be different");
        }
    }

    private Limits getEffectiveLimits(UUID accountId, String authToken) {
        AccountLimitInfo ali = accountServiceClient.getAccountLimits(accountId, authToken);
        BigDecimal effDaily = (ali != null && ali.dailyAmount() != null) ? ali.dailyAmount() : dailyLimit;
        BigDecimal effPerTxn = (ali != null && ali.perTxnAmount() != null) ? ali.perTxnAmount() : perTransactionLimit;
        return new Limits(effDaily, effPerTxn);
    }

    private record Limits(BigDecimal dailyLimit, BigDecimal perTxnLimit) {}

    public DailyUsage getDailyUsage(UUID payerAccountId, String authToken) {
        Limits limits = getEffectiveLimits(payerAccountId, authToken);
        Instant startOfDay = Instant.now().truncatedTo(ChronoUnit.DAYS);
        Double totalTransferred = transferRepository.getTotalTransferredAmountByPayerSince(payerAccountId, startOfDay);
        BigDecimal used = BigDecimal.valueOf(totalTransferred != null ? totalTransferred : 0.0);
        BigDecimal remaining = limits.dailyLimit().subtract(used);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) remaining = BigDecimal.ZERO;
        return new DailyUsage(limits.dailyLimit(), used, remaining);
    }

    public record DailyUsage(BigDecimal dailyLimit, BigDecimal used, BigDecimal remaining) {}
}

