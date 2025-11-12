package com.yourorg.banking.account.service;

import com.yourorg.banking.account.model.AccountStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${app.email.from:noreply@digitalbank.com}")
    private String fromEmail;

    public void sendStatementEmail(String toEmail, String accountName, AccountStatement statement) {
        if (!emailEnabled) {
            logger.info("Email service disabled - would send statement to: {} for account: {}", toEmail, accountName);
            return;
        }

        String subject = String.format("Account Statement - %s (%s)", 
                accountName, 
                statement.statementDate().format(DateTimeFormatter.ofPattern("MMM yyyy")));
        
        String body = String.format("""
            <html>
            <body>
                <h2>Account Statement</h2>
                <p>Dear Customer,</p>
                <p>Please find attached your account statement for %s.</p>
                <p><strong>Account:</strong> %s</p>
                <p><strong>Statement Period:</strong> %s to %s</p>
                <p><strong>Closing Balance:</strong> $%s</p>
                <p>If you have any questions about this statement, please contact our customer service.</p>
                <p>Best regards,<br>Digital Bank Team</p>
            </body>
            </html>
            """, 
            accountName,
            statement.accountNumber(),
            statement.periodStart().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
            statement.periodEnd().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
            statement.closingBalance());

        sendEmail(toEmail, subject, body);
    }

    public void sendAccountOpeningNotification(String toEmail, String accountName, String accountNumber) {
        if (!emailEnabled) {
            logger.info("Email service disabled - would send account opening notification to: {}", toEmail);
            return;
        }

        String subject = "Account Opening Confirmation - " + accountName;
        String body = String.format("""
            <html>
            <body>
                <h2>Account Opening Confirmation</h2>
                <p>Dear Customer,</p>
                <p>Your account has been successfully opened!</p>
                <p><strong>Account Name:</strong> %s</p>
                <p><strong>Account Number:</strong> %s</p>
                <p>You can now start using your account for transactions.</p>
                <p>If you have any questions, please contact our customer service.</p>
                <p>Best regards,<br>Digital Bank Team</p>
            </body>
            </html>
            """, accountName, accountNumber);

        sendEmail(toEmail, subject, body);
    }

    public void sendAccountClosureNotification(String toEmail, String accountName, String accountNumber) {
        if (!emailEnabled) {
            logger.info("Email service disabled - would send account closure notification to: {}", toEmail);
            return;
        }

        String subject = "Account Closure Confirmation - " + accountName;
        String body = String.format("""
            <html>
            <body>
                <h2>Account Closure Confirmation</h2>
                <p>Dear Customer,</p>
                <p>Your account has been successfully closed.</p>
                <p><strong>Account Name:</strong> %s</p>
                <p><strong>Account Number:</strong> %s</p>
                <p>Any remaining balance has been transferred as requested.</p>
                <p>If you have any questions, please contact our customer service.</p>
                <p>Best regards,<br>Digital Bank Team</p>
            </body>
            </html>
            """, accountName, accountNumber);

        sendEmail(toEmail, subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        // In a real implementation, this would integrate with an email service like SendGrid, AWS SES, etc.
        logger.info("Sending email to: {}, subject: {}", to, subject);
        logger.debug("Email body: {}", body);
        
        // Simulate email sending delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

