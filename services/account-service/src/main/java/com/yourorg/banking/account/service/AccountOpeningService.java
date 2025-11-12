package com.yourorg.banking.account.service;

import com.yourorg.banking.account.model.*;
import com.yourorg.banking.account.repo.AccountRepository;
import com.yourorg.banking.account.repo.AccountOpeningCaseRepository;
import com.yourorg.banking.account.repo.JointAccountHolderRepository;
import com.yourorg.banking.customer.client.CustomerServiceClient;
import com.yourorg.banking.customer.client.KycServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AccountOpeningService {

    private static final Logger logger = LoggerFactory.getLogger(AccountOpeningService.class);

    private final AccountRepository accountRepository;
    private final AccountOpeningCaseRepository openingCaseRepository;
    private final JointAccountHolderRepository jointAccountHolderRepository;
    private final CustomerServiceClient customerServiceClient;
    private final KycServiceClient kycServiceClient;
    private final AccountNumberGenerator accountNumberGenerator;

    public AccountOpeningService(AccountRepository accountRepository,
                               AccountOpeningCaseRepository openingCaseRepository,
                               JointAccountHolderRepository jointAccountHolderRepository,
                               CustomerServiceClient customerServiceClient,
                               KycServiceClient kycServiceClient,
                               AccountNumberGenerator accountNumberGenerator) {
        this.accountRepository = accountRepository;
        this.openingCaseRepository = openingCaseRepository;
        this.jointAccountHolderRepository = jointAccountHolderRepository;
        this.customerServiceClient = customerServiceClient;
        this.kycServiceClient = kycServiceClient;
        this.accountNumberGenerator = accountNumberGenerator;
    }

    public AccountOpeningResponse openAccount(UUID customerId, AccountOpeningRequest request) {
        logger.info("Opening account for customer: {} with type: {}", customerId, request.accountType());

        // Validate customer exists and is active
        CustomerInfo customerInfo = customerServiceClient.getCustomerInfo(customerId);
        if (customerInfo == null) {
            throw new IllegalArgumentException("Customer not found or inactive");
        }

        // Check KYC requirements
        boolean kycRequired = request.kycRequired() || request.accountType().requiresKyc();
        KycStatus kycStatus = KycStatus.NOT_REQUIRED;
        
        if (kycRequired) {
            kycStatus = checkKycStatus(customerId, request.kycLevel());
            if (kycStatus == KycStatus.REJECTED) {
                throw new IllegalStateException("KYC verification required and not completed");
            }
        }

        // Generate account number
        String accountNumber = accountNumberGenerator.generateAccountNumber(request.accountType());

        // Create account
        Account account = new Account(
                UUID.randomUUID(),
                customerId,
                accountNumber,
                request.accountName(),
                request.accountType(),
                AccountStatus.PENDING,
                request.currency(),
                request.initialDeposit(),
                request.initialDeposit(),
                request.accountType().getInterestRate(),
                request.accountType().getFeeRate(),
                BigDecimal.ZERO, // daily_transfer_limit
                BigDecimal.ZERO, // per_transaction_limit
                null, // last_transaction_at
                request.description(),
                request.paperlessStatements(),
                request.emailNotifications(),
                request.preferredLanguage(),
                kycRequired,
                request.kycLevel(),
                kycStatus,
                AccountOpeningStatus.PENDING,
                "Account opening request",
                null, // closure_reason
                null, // closure_requested_at
                null, // closure_approved_at
                null, // closure_approved_by
                BigDecimal.ZERO, // minimum_balance
                null, // maximum_balance
                BigDecimal.ZERO, // monthly_fee
                BigDecimal.ZERO, // overdraft_limit
                null, // last_interest_calculation
                null, // next_interest_calculation
                Instant.now(),
                Instant.now()
        );

        accountRepository.save(account);

        // Create opening case
        AccountOpeningCase openingCase = new AccountOpeningCase(
                UUID.randomUUID(),
                account.getId(),
                customerId,
                determineCaseType(request),
                AccountOpeningStatus.PENDING,
                determinePriority(request),
                null, // assigned_to
                null, // kyc_case_id
                determineRequiredDocuments(request),
                new ArrayList<>(),
                null, // review_notes
                null, // approval_notes
                null, // rejection_reason
                Instant.now(),
                Instant.now(),
                null // completed_at
        );

        openingCaseRepository.save(openingCase);

        // Handle joint account holders
        if (request.jointAccountHolders() != null && !request.jointAccountHolders().isEmpty()) {
            createJointAccountHolders(account.getId(), customerId, request.jointAccountHolders());
        }

        // Determine next steps
        List<String> nextSteps = determineNextSteps(request, kycStatus);
        List<String> requiredDocuments = determineRequiredDocuments(request);

        return new AccountOpeningResponse(
                account.getId(),
                accountNumber,
                request.accountName(),
                request.accountType(),
                request.currency(),
                request.initialDeposit(),
                account.getStatus(),
                account.getOpeningStatus().name(),
                "Account opening request submitted successfully",
                nextSteps,
                requiredDocuments,
                Instant.now(),
                calculateEstimatedCompletion(request)
        );
    }

    public AccountOpeningResponse getAccountOpeningStatus(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        AccountOpeningCase openingCase = openingCaseRepository.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Opening case not found"));

        List<String> nextSteps = determineNextStepsFromStatus(account.getOpeningStatus());
        List<String> requiredDocuments = determineRequiredDocumentsFromType(account.getType());

        return new AccountOpeningResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountName(),
                account.getType(),
                account.getCurrency(),
                account.getBalance(),
                account.getStatus(),
                account.getOpeningStatus().name(),
                getStatusMessage(account.getOpeningStatus()),
                nextSteps,
                requiredDocuments,
                account.getCreatedAt(),
                calculateEstimatedCompletionFromStatus(account.getOpeningStatus())
        );
    }

    public void approveAccountOpening(UUID accountId, String approvalNotes) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (account.getOpeningStatus() != AccountOpeningStatus.PENDING) {
            throw new IllegalStateException("Account is not in pending status");
        }

        // Update account status
        Account updatedAccount = new Account(
                account.getId(),
                account.getCustomerId(),
                account.getAccountNumber(),
                account.getAccountName(),
                account.getType(),
                AccountStatus.ACTIVE,
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
                AccountOpeningStatus.COMPLETED,
                account.getOpeningReason(),
                account.getClosureReason(),
                account.getClosureRequestedAt(),
                account.getClosureApprovedAt(),
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

        accountRepository.save(updatedAccount);

        // Update opening case
        AccountOpeningCase openingCase = openingCaseRepository.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Opening case not found"));

        AccountOpeningCase updatedCase = new AccountOpeningCase(
                openingCase.getId(),
                openingCase.getAccountId(),
                openingCase.getCustomerId(),
                openingCase.getCaseType(),
                AccountOpeningStatus.COMPLETED,
                openingCase.getPriority(),
                openingCase.getAssignedTo(),
                openingCase.getKycCaseId(),
                openingCase.getRequiredDocuments(),
                openingCase.getSubmittedDocuments(),
                openingCase.getReviewNotes(),
                approvalNotes,
                openingCase.getRejectionReason(),
                openingCase.getCreatedAt(),
                Instant.now(),
                Instant.now()
        );

        openingCaseRepository.save(updatedCase);

        logger.info("Account opening approved for account: {}", accountId);
    }

    private KycStatus checkKycStatus(UUID customerId, String kycLevel) {
        // This would integrate with the KYC service
        // For now, return a mock status
        return KycStatus.APPROVED;
    }

    private AccountOpeningCaseType determineCaseType(AccountOpeningRequest request) {
        if (request.jointAccountHolders() != null && !request.jointAccountHolders().isEmpty()) {
            return AccountOpeningCaseType.JOINT_ACCOUNT;
        }
        return AccountOpeningCaseType.NEW_ACCOUNT;
    }

    private AccountOpeningPriority determinePriority(AccountOpeningRequest request) {
        if (request.accountType().isBusinessAccount()) {
            return AccountOpeningPriority.HIGH;
        }
        return AccountOpeningPriority.NORMAL;
    }

    private List<String> determineRequiredDocuments(AccountOpeningRequest request) {
        List<String> documents = new ArrayList<>();
        
        if (request.accountType().isBusinessAccount()) {
            documents.add("Business Registration Certificate");
            documents.add("Tax Identification Number");
            documents.add("Articles of Incorporation");
        }
        
        if (request.accountType().isInvestmentAccount()) {
            documents.add("Investment Risk Assessment");
            documents.add("Financial Advisor Agreement");
        }
        
        if (request.accountType().isCreditAccount()) {
            documents.add("Income Verification");
            documents.add("Credit History Report");
        }
        
        return documents;
    }

    private List<String> determineNextSteps(AccountOpeningRequest request, KycStatus kycStatus) {
        List<String> steps = new ArrayList<>();
        
        if (kycStatus == KycStatus.PENDING) {
            steps.add("Complete KYC verification");
        }
        
        if (request.jointAccountHolders() != null && !request.jointAccountHolders().isEmpty()) {
            steps.add("Joint account holders must approve the account");
        }
        
        steps.add("Account will be reviewed by our team");
        steps.add("You will receive notification once approved");
        
        return steps;
    }

    private List<String> determineNextStepsFromStatus(AccountOpeningStatus status) {
        List<String> steps = new ArrayList<>();
        
        switch (status) {
            case PENDING:
                steps.add("Account is under review");
                steps.add("You will receive notification once approved");
                break;
            case UNDER_REVIEW:
                steps.add("Account is being reviewed by our team");
                steps.add("Additional documents may be required");
                break;
            case APPROVED:
                steps.add("Account has been approved");
                steps.add("You can now start using your account");
                break;
            case REJECTED:
                steps.add("Account opening was rejected");
                steps.add("Please contact support for more information");
                break;
            case COMPLETED:
                steps.add("Account is fully active");
                break;
        }
        
        return steps;
    }

    private List<String> determineRequiredDocumentsFromType(AccountType accountType) {
        return determineRequiredDocuments(new AccountOpeningRequest(
                "Account", accountType, "USD", BigDecimal.ZERO, null, null, false, null, true, true, "EN"
        ));
    }

    private String getStatusMessage(AccountOpeningStatus status) {
        return switch (status) {
            case PENDING -> "Your account opening request is pending review";
            case UNDER_REVIEW -> "Your account is being reviewed by our team";
            case APPROVED -> "Your account has been approved and is ready to use";
            case REJECTED -> "Your account opening request was rejected";
            case COMPLETED -> "Your account is fully active";
        };
    }

    private Instant calculateEstimatedCompletion(AccountOpeningRequest request) {
        // Calculate based on account type and requirements
        int days = 1; // Base processing time
        
        if (request.accountType().isBusinessAccount()) {
            days += 2;
        }
        
        if (request.jointAccountHolders() != null && !request.jointAccountHolders().isEmpty()) {
            days += 1;
        }
        
        if (request.kycRequired() || request.accountType().requiresKyc()) {
            days += 1;
        }
        
        return Instant.now().plusSeconds(days * 24 * 60 * 60);
    }

    private Instant calculateEstimatedCompletionFromStatus(AccountOpeningStatus status) {
        return switch (status) {
            case PENDING -> Instant.now().plusSeconds(24 * 60 * 60); // 1 day
            case UNDER_REVIEW -> Instant.now().plusSeconds(12 * 60 * 60); // 12 hours
            case APPROVED, COMPLETED -> Instant.now();
            case REJECTED -> null;
        };
    }

    private void createJointAccountHolders(UUID accountId, UUID primaryCustomerId, List<UUID> jointHolders) {
        for (UUID holderId : jointHolders) {
            JointAccountHolder holder = new JointAccountHolder(
                    UUID.randomUUID(),
                    accountId,
                    holderId,
                    JointAccountHolder.JointAccountRole.JOINT_OWNER,
                    false,
                    Instant.now(),
                    null,
                    "PENDING"
            );
            jointAccountHolderRepository.save(holder);
        }
    }
}

