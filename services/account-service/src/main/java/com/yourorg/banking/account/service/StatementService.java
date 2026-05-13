package com.yourorg.banking.account.service;

import com.yourorg.banking.account.model.*;
import com.yourorg.banking.account.repo.AccountRepository;
import com.yourorg.banking.account.repo.AccountStatementRepository;
import com.yourorg.banking.ledger.client.LedgerServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class StatementService {

    private static final Logger logger = LoggerFactory.getLogger(StatementService.class);

    private final AccountRepository accountRepository;
    private final AccountStatementRepository statementRepository;
    private final LedgerServiceClient ledgerServiceClient;
    private final PdfGenerationService pdfGenerationService;
    private final EmailService emailService;

    @Value("${app.statement.storage-path:/tmp/statements}")
    private String statementStoragePath;

    public StatementService(AccountRepository accountRepository,
                          AccountStatementRepository statementRepository,
                          LedgerServiceClient ledgerServiceClient,
                          PdfGenerationService pdfGenerationService,
                          EmailService emailService) {
        this.accountRepository = accountRepository;
        this.statementRepository = statementRepository;
        this.ledgerServiceClient = ledgerServiceClient;
        this.pdfGenerationService = pdfGenerationService;
        this.emailService = emailService;
    }

    public AccountStatement generateStatement(UUID accountId, LocalDate fromDate, LocalDate toDate) {
        logger.info("Generating statement for account: {} from {} to {}", accountId, fromDate, toDate);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        // Get transactions from ledger service
        List<StatementTransaction> transactions = getTransactionsFromLedger(accountId, fromDate, toDate);

        // Calculate balances
        BigDecimal openingBalance = calculateOpeningBalance(accountId, fromDate);
        BigDecimal closingBalance = calculateClosingBalance(accountId, toDate);
        BigDecimal totalDebits = calculateTotalDebits(transactions);
        BigDecimal totalCredits = calculateTotalCredits(transactions);

        // Generate statement ID
        String statementId = generateStatementId(account.accountNumber(), toDate);

        // Create statement
        AccountStatement statement = new AccountStatement(
                UUID.randomUUID(),
                accountId,
                account.accountNumber(),
                account.accountName(),
                statementId,
                toDate,
                fromDate,
                toDate,
                openingBalance,
                closingBalance,
                totalDebits,
                totalCredits,
                transactions,
                account.currency(),
                java.time.Instant.now(),
                null, // generatedBy
                null, // filePath
                false, // emailSent
                null, // emailSentAt
                java.time.Instant.now() // createdAt
        );

        // Save statement
        statementRepository.save(statement);

        // Generate PDF
        generateStatementPdf(statement);

        logger.info("Statement generated successfully: {}", statementId);
        return statement;
    }

    public AccountStatement generateMonthlyStatement(UUID accountId, int year, int month) {
        LocalDate fromDate = LocalDate.of(year, month, 1);
        LocalDate toDate = fromDate.withDayOfMonth(fromDate.lengthOfMonth());
        return generateStatement(accountId, fromDate, toDate);
    }

    public void emailStatement(String statementId, String emailAddress) {
        AccountStatement statement = statementRepository.findByStatementId(statementId)
                .orElseThrow(() -> new IllegalArgumentException("Statement not found"));

        Account account = accountRepository.findById(statement.accountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        try {
            // Generate PDF if not exists
            if (statement.filePath() == null) {
                generateStatementPdf(statement);
            }

            // Send email
            emailService.sendStatementEmail(emailAddress, account.accountName(), statement);

            // Update statement
            AccountStatement updatedStatement = new AccountStatement(
                    statement.id(),
                    statement.accountId(),
                    statement.accountNumber(),
                    statement.accountName(),
                    statement.statementId(),
                    statement.statementDate(),
                    statement.periodStart(),
                    statement.periodEnd(),
                    statement.openingBalance(),
                    statement.closingBalance(),
                    statement.totalDebits(),
                    statement.totalCredits(),
                    statement.transactions(),
                    statement.currency(),
                    statement.generatedAt(),
                    statement.generatedBy(),
                    statement.filePath(),
                    true,
                    java.time.Instant.now(),
                    statement.createdAt()
            );

            statementRepository.save(updatedStatement);

        } catch (Exception e) {
            logger.error("Failed to email statement: {}", statementId, e);
            throw new RuntimeException("Failed to email statement", e);
        }
    }

    public List<AccountStatement> getAccountStatements(UUID accountId, int limit) {
        return statementRepository.findByAccountId(accountId, limit);
    }

    public AccountStatement getStatementById(UUID statementId) {
        return statementRepository.findById(statementId)
                .orElseThrow(() -> new IllegalArgumentException("Statement not found"));
    }

    public byte[] downloadStatementPdf(String statementId) {
        AccountStatement statement = statementRepository.findByStatementId(statementId)
                .orElseThrow(() -> new IllegalArgumentException("Statement not found"));

        if (statement.filePath() == null) {
            generateStatementPdf(statement);
        }

        try {
            return java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(statement.filePath()));
        } catch (IOException e) {
            logger.error("Failed to read statement PDF: {}", statementId, e);
            throw new RuntimeException("Failed to read statement PDF", e);
        }
    }

    private List<StatementTransaction> getTransactionsFromLedger(UUID accountId, LocalDate fromDate, LocalDate toDate) {
        // This would integrate with the ledger service to get actual transactions
        // For now, return mock data
        List<StatementTransaction> transactions = new ArrayList<>();
        
        // Mock transactions
        transactions.add(new StatementTransaction(
                UUID.randomUUID(),
                fromDate.plusDays(1),
                "Initial Deposit",
                "INIT001",
                BigDecimal.ZERO,
                new BigDecimal("1000.00"),
                new BigDecimal("1000.00"),
                "DEPOSIT",
                "COMPLETED"
        ));

        transactions.add(new StatementTransaction(
                UUID.randomUUID(),
                fromDate.plusDays(5),
                "Transfer Out",
                "TRF001",
                new BigDecimal("100.00"),
                BigDecimal.ZERO,
                new BigDecimal("900.00"),
                "TRANSFER",
                "COMPLETED"
        ));

        return transactions;
    }

    private BigDecimal calculateOpeningBalance(UUID accountId, LocalDate fromDate) {
        // This would calculate the actual opening balance from the ledger
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateClosingBalance(UUID accountId, LocalDate toDate) {
        // This would calculate the actual closing balance from the ledger
        return new BigDecimal("900.00");
    }

    private BigDecimal calculateTotalDebits(List<StatementTransaction> transactions) {
        return transactions.stream()
                .map(StatementTransaction::debitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotalCredits(List<StatementTransaction> transactions) {
        return transactions.stream()
                .map(StatementTransaction::creditAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generateStatementId(String accountNumber, LocalDate statementDate) {
        return String.format("STMT-%s-%s", accountNumber, statementDate.format(DateTimeFormatter.ofPattern("yyyyMM")));
    }

    private void generateStatementPdf(AccountStatement statement) {
        try {
            byte[] pdfBytes = pdfGenerationService.generateStatementPdf(statement);
            
            // Save PDF to file system
            String fileName = String.format("statement_%s_%s.pdf", 
                    statement.accountId(), 
                    statement.statementDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            
            java.nio.file.Path filePath = java.nio.file.Paths.get(statementStoragePath, fileName);
            java.nio.file.Files.createDirectories(filePath.getParent());
            java.nio.file.Files.write(filePath, pdfBytes);

            // Update statement with file path
            AccountStatement updatedStatement = new AccountStatement(
                    statement.id(),
                    statement.accountId(),
                    statement.accountNumber(),
                    statement.accountName(),
                    statement.statementId(),
                    statement.statementDate(),
                    statement.periodStart(),
                    statement.periodEnd(),
                    statement.openingBalance(),
                    statement.closingBalance(),
                    statement.totalDebits(),
                    statement.totalCredits(),
                    statement.transactions(),
                    statement.currency(),
                    statement.generatedAt(),
                    statement.generatedBy(),
                    filePath.toString(),
                    statement.emailSent(),
                    statement.emailSentAt(),
                    statement.createdAt()
            );

            statementRepository.save(updatedStatement);

        } catch (Exception e) {
            logger.error("Failed to generate statement PDF: {}", statement.statementId(), e);
            throw new RuntimeException("Failed to generate statement PDF", e);
        }
    }
}

