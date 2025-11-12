package com.yourorg.banking.account.service;

import com.yourorg.banking.account.model.*;
import com.yourorg.banking.account.repo.AccountRepository;
import com.yourorg.banking.account.repo.AccountClosureRequestRepository;
import com.yourorg.banking.ledger.client.LedgerServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AccountClosureService {

    private static final Logger logger = LoggerFactory.getLogger(AccountClosureService.class);

    private final AccountRepository accountRepository;
    private final AccountClosureRequestRepository closureRequestRepository;
    private final LedgerServiceClient ledgerServiceClient;
    private final StatementService statementService;
    private final EmailService emailService;

    public AccountClosureService(AccountRepository accountRepository,
                               AccountClosureRequestRepository closureRequestRepository,
                               LedgerServiceClient ledgerServiceClient,
                               StatementService statementService,
                               EmailService emailService) {
        this.accountRepository = accountRepository;
        this.closureRequestRepository = closureRequestRepository;
        this.ledgerServiceClient = ledgerServiceClient;
        this.statementService = statementService;
        this.emailService = emailService;
    }

    public AccountClosureRequest requestAccountClosure(UUID customerId, AccountClosureRequest request) {
        logger.info("Processing account closure request for account: {} by customer: {}", 
                request.accountId(), customerId);

        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        // Validate account belongs to customer
        if (!account.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("Account does not belong to customer");
        }

        // Check if account is already closed or pending closure
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new IllegalStateException("Account is already closed");
        }

        if (account.getClosureRequestedAt() != null) {
            throw new IllegalStateException("Account closure is already pending");
        }

        // Validate transfer account if provided
        if (request.transferAccountId() != null) {
            Account transferAccount = accountRepository.findById(request.transferAccountId())
                    .orElseThrow(() -> new IllegalArgumentException("Transfer account not found"));
            
            if (!transferAccount.getCustomerId().equals(customerId)) {
                throw new IllegalArgumentException("Transfer account does not belong to customer");
            }
        }

        // Create closure request
        AccountClosureRequest closureRequest = new AccountClosureRequest(
                UUID.randomUUID(),
                request.accountId(),
                customerId,
                request.closureReason(),
                request.transferAccountId(),
                request.comments(),
                AccountClosureStatus.PENDING,
                null, // approved_by
                null, // approved_at
                null, // rejection_reason
                null, // final_balance
                null, // transfer_amount
                false, // final_statement_generated
                false, // final_statement_sent
                Instant.now(),
                Instant.now(),
                null // completed_at
        );

        closureRequestRepository.save(closureRequest);

        // Update account status
        Account updatedAccount = new Account(
                account.getId(),
                account.getCustomerId(),
                account.getAccountNumber(),
                account.getAccountName(),
                account.getType(),
                account.getStatus(),
                account.getCurrency(),
                account.getBalance(),
                account.getAvailableBalance(),
                account.getInterestRate(),
                account.getFeeRate(),
                account.getDailyTransferLimit(),
                account.getPerTransactionLimit(),
                account.getLastTransactionAt(),
                account.getDescription(),
                account.isPaperlessStatements(),
                account.isEmailNotifications(),
                account.getPreferredLanguage(),
                account.isKycRequired(),
                account.getKycLevel(),
                account.getKycStatus(),
                account.getOpeningStatus(),
                account.getOpeningReason(),
                request.closureReason(),
                Instant.now(), // closure_requested_at
                null, // closure_approved_at
                null, // closure_approved_by
                account.getMinimumBalance(),
                account.getMaximumBalance(),
                account.getMonthlyFee(),
                account.getOverdraftLimit(),
                account.getLastInterestCalculation(),
                account.getNextInterestCalculation(),
                account.getCreatedAt(),
                Instant.now()
        );

        accountRepository.save(updatedAccount);

        logger.info("Account closure request created: {}", closureRequest.id());
        return closureRequest;
    }

    public void approveAccountClosure(UUID closureRequestId, UUID approvedBy, String approvalNotes) {
        logger.info("Approving account closure request: {}", closureRequestId);

        AccountClosureRequest closureRequest = closureRequestRepository.findById(closureRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Closure request not found"));

        if (closureRequest.status() != AccountClosureStatus.PENDING) {
            throw new IllegalStateException("Closure request is not in pending status");
        }

        Account account = accountRepository.findById(closureRequest.accountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        // Get final balance
        BigDecimal finalBalance = account.getBalance();

        // Process transfer if specified
        BigDecimal transferAmount = BigDecimal.ZERO;
        if (closureRequest.transferAccountId() != null && finalBalance.compareTo(BigDecimal.ZERO) > 0) {
            transferAmount = processBalanceTransfer(account, closureRequest.transferAccountId(), finalBalance);
        }

        // Generate final statement if requested
        if (closureRequest.generateFinalStatement()) {
            try {
                statementService.generateStatement(account.getId(), 
                        account.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                        java.time.LocalDate.now());
            } catch (Exception e) {
                logger.warn("Failed to generate final statement for account: {}", account.getId(), e);
            }
        }

        // Update closure request
        AccountClosureRequest updatedRequest = new AccountClosureRequest(
                closureRequest.id(),
                closureRequest.accountId(),
                closureRequest.requestedBy(),
                closureRequest.closureReason(),
                closureRequest.transferAccountId(),
                closureRequest.comments(),
                AccountClosureStatus.APPROVED,
                approvedBy,
                Instant.now(),
                null, // rejection_reason
                finalBalance,
                transferAmount,
                closureRequest.generateFinalStatement(),
                false, // final_statement_sent
                closureRequest.createdAt(),
                Instant.now(),
                null // completed_at
        );

        closureRequestRepository.save(updatedRequest);

        // Close account
        closeAccount(account, finalBalance, transferAmount);

        logger.info("Account closure approved and completed: {}", closureRequestId);
    }

    public void rejectAccountClosure(UUID closureRequestId, String rejectionReason) {
        logger.info("Rejecting account closure request: {}", closureRequestId);

        AccountClosureRequest closureRequest = closureRequestRepository.findById(closureRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Closure request not found"));

        if (closureRequest.status() != AccountClosureStatus.PENDING) {
            throw new IllegalStateException("Closure request is not in pending status");
        }

        // Update closure request
        AccountClosureRequest updatedRequest = new AccountClosureRequest(
                closureRequest.id(),
                closureRequest.accountId(),
                closureRequest.requestedBy(),
                closureRequest.closureReason(),
                closureRequest.transferAccountId(),
                closureRequest.comments(),
                AccountClosureStatus.REJECTED,
                null, // approved_by
                null, // approved_at
                rejectionReason,
                null, // final_balance
                null, // transfer_amount
                false, // final_statement_generated
                false, // final_statement_sent
                closureRequest.createdAt(),
                Instant.now(),
                null // completed_at
        );

        closureRequestRepository.save(updatedRequest);

        // Reset account closure status
        Account account = accountRepository.findById(closureRequest.accountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        Account updatedAccount = new Account(
                account.getId(),
                account.getCustomerId(),
                account.getAccountNumber(),
                account.getAccountName(),
                account.getType(),
                account.getStatus(),
                account.getCurrency(),
                account.getBalance(),
                account.getAvailableBalance(),
                account.getInterestRate(),
                account.getFeeRate(),
                account.getDailyTransferLimit(),
                account.getPerTransactionLimit(),
                account.getLastTransactionAt(),
                account.getDescription(),
                account.isPaperlessStatements(),
                account.isEmailNotifications(),
                account.getPreferredLanguage(),
                account.isKycRequired(),
                account.getKycLevel(),
                account.getKycStatus(),
                account.getOpeningStatus(),
                account.getOpeningReason(),
                null, // closure_reason
                null, // closure_requested_at
                null, // closure_approved_at
                null, // closure_approved_by
                account.getMinimumBalance(),
                account.getMaximumBalance(),
                account.getMonthlyFee(),
                account.getOverdraftLimit(),
                account.getLastInterestCalculation(),
                account.getNextInterestCalculation(),
                account.getCreatedAt(),
                Instant.now()
        );

        accountRepository.save(updatedAccount);

        logger.info("Account closure request rejected: {}", closureRequestId);
    }

    public List<AccountClosureRequest> getClosureRequests(UUID customerId) {
        return closureRequestRepository.findByRequestedBy(customerId);
    }

    public List<AccountClosureRequest> getPendingClosureRequests() {
        return closureRequestRepository.findByStatus(AccountClosureStatus.PENDING);
    }

    private BigDecimal processBalanceTransfer(Account fromAccount, UUID toAccountId, BigDecimal amount) {
        // This would integrate with the ledger service to process the transfer
        // For now, just return the amount
        logger.info("Processing balance transfer of {} from account {} to account {}", 
                amount, fromAccount.getId(), toAccountId);
        return amount;
    }

    private void closeAccount(Account account, BigDecimal finalBalance, BigDecimal transferAmount) {
        Account closedAccount = new Account(
                account.getId(),
                account.getCustomerId(),
                account.getAccountNumber(),
                account.getAccountName(),
                account.getType(),
                AccountStatus.CLOSED,
                account.getCurrency(),
                finalBalance,
                BigDecimal.ZERO, // available_balance
                account.getInterestRate(),
                account.getFeeRate(),
                account.getDailyTransferLimit(),
                account.getPerTransactionLimit(),
                account.getLastTransactionAt(),
                account.getDescription(),
                account.isPaperlessStatements(),
                account.isEmailNotifications(),
                account.getPreferredLanguage(),
                account.isKycRequired(),
                account.getKycLevel(),
                account.getKycStatus(),
                account.getOpeningStatus(),
                account.getOpeningReason(),
                account.getClosureReason(),
                account.getClosureRequestedAt(),
                Instant.now(), // closure_approved_at
                account.getClosureApprovedBy(),
                account.getMinimumBalance(),
                account.getMaximumBalance(),
                account.getMonthlyFee(),
                account.getOverdraftLimit(),
                account.getLastInterestCalculation(),
                account.getNextInterestCalculation(),
                account.getCreatedAt(),
                Instant.now()
        );

        accountRepository.save(closedAccount);

        // Send closure notification
        try {
            emailService.sendAccountClosureNotification(
                    "customer@example.com", // This would come from customer service
                    account.getAccountName(),
                    account.getAccountNumber()
            );
        } catch (Exception e) {
            logger.warn("Failed to send closure notification for account: {}", account.getId(), e);
        }
    }
}

