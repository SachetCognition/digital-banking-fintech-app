package com.yourorg.banking.customer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${app.email.from:noreply@digitalbank.com}")
    private String fromEmail;

    public void sendEmailVerification(String toEmail, String verificationToken) {
        if (!emailEnabled) {
            logger.info("Email service disabled - would send verification email to: {} with token: {}", toEmail, verificationToken);
            return;
        }

        String subject = "Verify Your Email Address";
        String verificationUrl = "https://app.digitalbank.com/verify-email?token=" + verificationToken;
        
        String body = String.format("""
            <html>
            <body>
                <h2>Welcome to Digital Bank!</h2>
                <p>Please click the link below to verify your email address:</p>
                <p><a href="%s">Verify Email Address</a></p>
                <p>This link will expire in 24 hours.</p>
                <p>If you didn't create an account, please ignore this email.</p>
            </body>
            </html>
            """, verificationUrl);

        sendEmail(toEmail, subject, body);
    }

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        if (!emailEnabled) {
            logger.info("Email service disabled - would send password reset email to: {} with token: {}", toEmail, resetToken);
            return;
        }

        String subject = "Reset Your Password";
        String resetUrl = "https://app.digitalbank.com/reset-password?token=" + resetToken;
        
        String body = String.format("""
            <html>
            <body>
                <h2>Password Reset Request</h2>
                <p>You requested to reset your password. Click the link below to set a new password:</p>
                <p><a href="%s">Reset Password</a></p>
                <p>This link will expire in 1 hour.</p>
                <p>If you didn't request this, please ignore this email.</p>
            </body>
            </html>
            """, resetUrl);

        sendEmail(toEmail, subject, body);
    }

    public void sendMfaCode(String toEmail, String mfaCode) {
        if (!emailEnabled) {
            logger.info("Email service disabled - would send MFA code to: {} with code: {}", toEmail, mfaCode);
            return;
        }

        String subject = "Your MFA Code";
        String body = String.format("""
            <html>
            <body>
                <h2>Multi-Factor Authentication</h2>
                <p>Your MFA code is: <strong>%s</strong></p>
                <p>This code will expire in 5 minutes.</p>
                <p>If you didn't request this, please contact support immediately.</p>
            </body>
            </html>
            """, mfaCode);

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

