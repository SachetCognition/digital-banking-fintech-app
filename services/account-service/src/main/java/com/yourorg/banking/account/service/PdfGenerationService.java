package com.yourorg.banking.account.service;

import com.yourorg.banking.account.model.AccountStatement;
import com.yourorg.banking.account.model.StatementTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(PdfGenerationService.class);

    public byte[] generateStatementPdf(AccountStatement statement) throws IOException {
        logger.info("Generating PDF for statement: {}", statement.statementId());

        // For now, generate a simple text-based PDF
        // In production, you would use a library like iText or Apache PDFBox
        StringBuilder content = new StringBuilder();
        
        // Header
        content.append("ACCOUNT STATEMENT\n");
        content.append("==================\n\n");
        
        // Account Information
        content.append("Account Number: ").append(statement.accountNumber()).append("\n");
        content.append("Account Name: ").append(statement.accountName()).append("\n");
        content.append("Statement Date: ").append(statement.statementDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))).append("\n");
        content.append("Period: ").append(statement.periodStart().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")))
                .append(" to ").append(statement.periodEnd().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))).append("\n\n");
        
        // Balance Information
        content.append("Opening Balance: $").append(statement.openingBalance()).append("\n");
        content.append("Closing Balance: $").append(statement.closingBalance()).append("\n");
        content.append("Total Debits: $").append(statement.totalDebits()).append("\n");
        content.append("Total Credits: $").append(statement.totalCredits()).append("\n\n");
        
        // Transactions
        content.append("TRANSACTIONS\n");
        content.append("============\n");
        content.append(String.format("%-12s %-20s %-15s %-15s %-15s %-10s\n", 
                "Date", "Description", "Debit", "Credit", "Balance", "Status"));
        content.append("-".repeat(100)).append("\n");
        
        for (StatementTransaction transaction : statement.transactions()) {
            content.append(String.format("%-12s %-20s %-15s %-15s %-15s %-10s\n",
                    transaction.transactionDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    transaction.description().length() > 20 ? transaction.description().substring(0, 17) + "..." : transaction.description(),
                    transaction.debitAmount().compareTo(BigDecimal.ZERO) > 0 ? "$" + transaction.debitAmount() : "",
                    transaction.creditAmount().compareTo(BigDecimal.ZERO) > 0 ? "$" + transaction.creditAmount() : "",
                    "$" + transaction.balance(),
                    transaction.status()
            ));
        }
        
        content.append("\n");
        content.append("End of Statement\n");
        
        // Convert to byte array (simplified - in production use proper PDF library)
        return content.toString().getBytes();
    }
}

