package com.yourorg.banking.customer.service;

import com.yourorg.banking.customer.model.*;
import com.yourorg.banking.customer.repo.CustomerRepository;
import com.yourorg.banking.customer.repo.EmailVerificationTokenRepository;
import com.yourorg.banking.customer.repo.AuthAuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final CustomerRepository customerRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final AuthAuditLogRepository auditLogRepository;
    private final PasswordService passwordService;
    private final EmailService emailService;
    private final MfaService mfaService;
    private final SessionService sessionService;
    private final RbacService rbacService;
    private final JwtService jwtService;

    @Value("${app.auth.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${app.auth.lockout-duration-minutes:30}")
    private int lockoutDurationMinutes;

    public AuthService(CustomerRepository customerRepository,
                      EmailVerificationTokenRepository tokenRepository,
                      AuthAuditLogRepository auditLogRepository,
                      PasswordService passwordService,
                      EmailService emailService,
                      MfaService mfaService,
                      SessionService sessionService,
                      RbacService rbacService,
                      JwtService jwtService) {
        this.customerRepository = customerRepository;
        this.tokenRepository = tokenRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordService = passwordService;
        this.emailService = emailService;
        this.mfaService = mfaService;
        this.sessionService = sessionService;
        this.rbacService = rbacService;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request, InetAddress ipAddress, String userAgent) {
        // Check if email already exists
        if (customerRepository.findByEmail(request.email()).isPresent()) {
            logAuthEvent(null, "REGISTRATION_FAILED", "Email already exists", ipAddress, userAgent, false);
            throw new IllegalArgumentException("Email already registered");
        }

        // Check if phone already exists
        if (customerRepository.findByPhone(request.phone()).isPresent()) {
            logAuthEvent(null, "REGISTRATION_FAILED", "Phone already exists", ipAddress, userAgent, false);
            throw new IllegalArgumentException("Phone number already registered");
        }

        // Create customer
        String passwordHash = passwordService.encodePassword(request.password());
        Customer customer = new Customer(
                UUID.randomUUID(),
                generateUserNo(),
                request.email(),
                request.phone(),
                request.fullName(),
                request.dob(),
                request.country(),
                CustomerStatus.ACTIVE,
                passwordHash,
                false, // email_verified
                false, // phone_verified
                false, // mfa_enabled
                null,  // last_login_at
                0,     // failed_login_attempts
                null,  // locked_until
                Instant.now(),
                Instant.now()
        );

        customerRepository.save(customer);

        // Create email verification token
        String verificationToken = passwordService.generateSecureToken();
        EmailVerificationToken token = new EmailVerificationToken(
                UUID.randomUUID(),
                customer.id(),
                verificationToken,
                EmailVerificationToken.TokenType.EMAIL_VERIFICATION,
                Instant.now().plusSeconds(24 * 60 * 60), // 24 hours
                null,
                Instant.now()
        );
        tokenRepository.save(token);

        // Send verification email
        emailService.sendEmailVerification(customer.email(), verificationToken);

        // Assign default customer role
        rbacService.assignRole(customer.id(), UUID.fromString("550e8400-e29b-41d4-a716-446655440001"), null);

        logAuthEvent(customer.id(), "REGISTRATION_SUCCESS", "User registered successfully", ipAddress, userAgent, true);

        return new AuthResponse(
                customer.id(),
                customer.email(),
                customer.fullName(),
                null, // accessToken - will be generated after email verification
                null, // refreshToken
                null, // sessionToken
                null, // expiresAt
                false, // mfaRequired
                List.of("CUSTOMER"),
                List.of("ACCOUNT_READ", "ACCOUNT_CREATE", "ACCOUNT_UPDATE", "TRANSFER_READ", "TRANSFER_CREATE", "CUSTOMER_READ", "CUSTOMER_UPDATE")
        );
    }

    public AuthResponse verifyEmail(String token) {
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        if (verificationToken.isExpired()) {
            throw new IllegalArgumentException("Verification token has expired");
        }

        if (verificationToken.isUsed()) {
            throw new IllegalArgumentException("Verification token already used");
        }

        // Mark token as used
        EmailVerificationToken usedToken = new EmailVerificationToken(
                verificationToken.id(),
                verificationToken.customerId(),
                verificationToken.token(),
                verificationToken.type(),
                verificationToken.expiresAt(),
                Instant.now(),
                verificationToken.createdAt()
        );
        tokenRepository.save(usedToken);

        // Update customer email verification status
        Customer customer = customerRepository.findById(verificationToken.customerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        Customer updatedCustomer = new Customer(
                customer.id(),
                customer.userNo(),
                customer.email(),
                customer.phone(),
                customer.fullName(),
                customer.dob(),
                customer.country(),
                customer.status(),
                customer.passwordHash(),
                true, // email_verified
                customer.phoneVerified(),
                customer.mfaEnabled(),
                customer.lastLoginAt(),
                customer.failedLoginAttempts(),
                customer.lockedUntil(),
                customer.createdAt(),
                Instant.now()
        );
        customerRepository.save(updatedCustomer);

        logAuthEvent(customer.id(), "EMAIL_VERIFIED", "Email verification successful", null, null, true);

        return new AuthResponse(
                customer.id(),
                customer.email(),
                customer.fullName(),
                null, // accessToken - user needs to login
                null, // refreshToken
                null, // sessionToken
                null, // expiresAt
                false, // mfaRequired
                List.of("CUSTOMER"),
                List.of("ACCOUNT_READ", "ACCOUNT_CREATE", "ACCOUNT_UPDATE", "TRANSFER_READ", "TRANSFER_CREATE", "CUSTOMER_READ", "CUSTOMER_UPDATE")
        );
    }

    public AuthResponse login(LoginRequest request, InetAddress ipAddress, String userAgent) {
        Customer customer = customerRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    logAuthEvent(null, "LOGIN_FAILED", "Invalid email", ipAddress, userAgent, false);
                    return new IllegalArgumentException("Invalid credentials");
                });

        // Check if account is locked
        if (customer.lockedUntil() != null && Instant.now().isBefore(customer.lockedUntil())) {
            logAuthEvent(customer.id(), "LOGIN_FAILED", "Account locked", ipAddress, userAgent, false);
            throw new IllegalArgumentException("Account is temporarily locked");
        }

        // Verify password
        if (!passwordService.matchesPassword(request.password(), customer.passwordHash())) {
            handleFailedLogin(customer, ipAddress, userAgent);
            throw new IllegalArgumentException("Invalid credentials");
        }

        // Reset failed attempts on successful login
        if (customer.failedLoginAttempts() > 0) {
            Customer resetCustomer = new Customer(
                    customer.id(),
                    customer.userNo(),
                    customer.email(),
                    customer.phone(),
                    customer.fullName(),
                    customer.dob(),
                    customer.country(),
                    customer.status(),
                    customer.passwordHash(),
                    customer.emailVerified(),
                    customer.phoneVerified(),
                    customer.mfaEnabled(),
                    customer.lastLoginAt(),
                    0, // failed_login_attempts
                    null, // locked_until
                    customer.createdAt(),
                    Instant.now()
            );
            customerRepository.save(resetCustomer);
        }

        // Check if MFA is required
        if (customer.mfaEnabled() && request.mfaCode() == null) {
            // Generate and send MFA code
            String mfaCode = generateMfaCode();
            emailService.sendMfaCode(customer.email(), mfaCode);
            
            logAuthEvent(customer.id(), "MFA_REQUIRED", "MFA code sent", ipAddress, userAgent, true);
            
            return new AuthResponse(
                    customer.id(),
                    customer.email(),
                    customer.fullName(),
                    null, // accessToken
                    null, // refreshToken
                    null, // sessionToken
                    null, // expiresAt
                    true, // mfaRequired
                    List.of("CUSTOMER"),
                    List.of()
            );
        }

        // Verify MFA if provided
        if (customer.mfaEnabled() && request.mfaCode() != null) {
            if (!mfaService.verifyMfaCode(customer.id(), request.mfaCode())) {
                logAuthEvent(customer.id(), "MFA_FAILED", "Invalid MFA code", ipAddress, userAgent, false);
                throw new IllegalArgumentException("Invalid MFA code");
            }
        }

        // Create session
        UserSession session = sessionService.createSession(
                customer.id(),
                request.deviceFingerprint(),
                ipAddress,
                userAgent
        );

        // Generate JWT tokens
        String accessToken = jwtService.generateAccessToken(customer);
        String refreshToken = jwtService.generateRefreshToken(customer);

        // Update last login
        Customer updatedCustomer = new Customer(
                customer.id(),
                customer.userNo(),
                customer.email(),
                customer.phone(),
                customer.fullName(),
                customer.dob(),
                customer.country(),
                customer.status(),
                customer.passwordHash(),
                customer.emailVerified(),
                customer.phoneVerified(),
                customer.mfaEnabled(),
                Instant.now(), // last_login_at
                customer.failedLoginAttempts(),
                customer.lockedUntil(),
                customer.createdAt(),
                Instant.now()
        );
        customerRepository.save(updatedCustomer);

        // Get roles and permissions
        List<String> roles = rbacService.getCustomerRoles(customer.id()).stream()
                .map(Role::name)
                .collect(Collectors.toList());
        
        List<String> permissions = rbacService.getCustomerPermissions(customer.id()).stream()
                .map(Permission::name)
                .collect(Collectors.toList());

        logAuthEvent(customer.id(), "LOGIN_SUCCESS", "Login successful", ipAddress, userAgent, true);

        return new AuthResponse(
                customer.id(),
                customer.email(),
                customer.fullName(),
                accessToken,
                refreshToken,
                session.sessionToken(),
                session.expiresAt(),
                false, // mfaRequired
                roles,
                permissions
        );
    }

    public void requestPasswordReset(PasswordResetRequest request, InetAddress ipAddress, String userAgent) {
        Customer customer = customerRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    logAuthEvent(null, "PASSWORD_RESET_REQUEST_FAILED", "Email not found", ipAddress, userAgent, false);
                    return new IllegalArgumentException("Email not found");
                });

        // Create password reset token
        String resetToken = passwordService.generateSecureToken();
        EmailVerificationToken token = new EmailVerificationToken(
                UUID.randomUUID(),
                customer.id(),
                resetToken,
                EmailVerificationToken.TokenType.PASSWORD_RESET,
                Instant.now().plusSeconds(60 * 60), // 1 hour
                null,
                Instant.now()
        );
        tokenRepository.save(token);

        // Send password reset email
        emailService.sendPasswordResetEmail(customer.email(), resetToken);

        logAuthEvent(customer.id(), "PASSWORD_RESET_REQUESTED", "Password reset requested", ipAddress, userAgent, true);
    }

    public void confirmPasswordReset(PasswordResetConfirmRequest request, InetAddress ipAddress, String userAgent) {
        EmailVerificationToken token = tokenRepository.findByToken(request.token())
                .orElseThrow(() -> new IllegalArgumentException("Invalid reset token"));

        if (token.isExpired()) {
            throw new IllegalArgumentException("Reset token has expired");
        }

        if (token.isUsed()) {
            throw new IllegalArgumentException("Reset token already used");
        }

        // Mark token as used
        EmailVerificationToken usedToken = new EmailVerificationToken(
                token.id(),
                token.customerId(),
                token.token(),
                token.type(),
                token.expiresAt(),
                Instant.now(),
                token.createdAt()
        );
        tokenRepository.save(usedToken);

        // Update customer password
        Customer customer = customerRepository.findById(token.customerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        String newPasswordHash = passwordService.encodePassword(request.newPassword());
        Customer updatedCustomer = new Customer(
                customer.id(),
                customer.userNo(),
                customer.email(),
                customer.phone(),
                customer.fullName(),
                customer.dob(),
                customer.country(),
                customer.status(),
                newPasswordHash,
                customer.emailVerified(),
                customer.phoneVerified(),
                customer.mfaEnabled(),
                customer.lastLoginAt(),
                0, // failed_login_attempts
                null, // locked_until
                customer.createdAt(),
                Instant.now()
        );
        customerRepository.save(updatedCustomer);

        // Invalidate all sessions
        sessionService.invalidateAllSessions(customer.id());

        logAuthEvent(customer.id(), "PASSWORD_RESET_SUCCESS", "Password reset successful", ipAddress, userAgent, true);
    }

    public MfaSecret setupMfa(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        MfaSecret secret = mfaService.generateMfaSecret(customerId);

        // Update customer MFA status
        Customer updatedCustomer = new Customer(
                customer.id(),
                customer.userNo(),
                customer.email(),
                customer.phone(),
                customer.fullName(),
                customer.dob(),
                customer.country(),
                customer.status(),
                customer.passwordHash(),
                customer.emailVerified(),
                customer.phoneVerified(),
                true, // mfa_enabled
                customer.lastLoginAt(),
                customer.failedLoginAttempts(),
                customer.lockedUntil(),
                customer.createdAt(),
                Instant.now()
        );
        customerRepository.save(updatedCustomer);

        return secret;
    }

    public void disableMfa(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        mfaService.deleteMfaSecret(customerId);

        // Update customer MFA status
        Customer updatedCustomer = new Customer(
                customer.id(),
                customer.userNo(),
                customer.email(),
                customer.phone(),
                customer.fullName(),
                customer.dob(),
                customer.country(),
                customer.status(),
                customer.passwordHash(),
                customer.emailVerified(),
                customer.phoneVerified(),
                false, // mfa_enabled
                customer.lastLoginAt(),
                customer.failedLoginAttempts(),
                customer.lockedUntil(),
                customer.createdAt(),
                Instant.now()
        );
        customerRepository.save(updatedCustomer);
    }

    public void logout(String sessionToken) {
        sessionService.invalidateSession(sessionToken);
    }

    public void logoutAllSessions(UUID customerId) {
        sessionService.invalidateAllSessions(customerId);
    }

    private void handleFailedLogin(Customer customer, InetAddress ipAddress, String userAgent) {
        int newFailedAttempts = customer.failedLoginAttempts() + 1;
        Instant lockedUntil = null;

        if (newFailedAttempts >= maxFailedAttempts) {
            lockedUntil = Instant.now().plusSeconds(lockoutDurationMinutes * 60L);
        }

        Customer updatedCustomer = new Customer(
                customer.id(),
                customer.userNo(),
                customer.email(),
                customer.phone(),
                customer.fullName(),
                customer.dob(),
                customer.country(),
                customer.status(),
                customer.passwordHash(),
                customer.emailVerified(),
                customer.phoneVerified(),
                customer.mfaEnabled(),
                customer.lastLoginAt(),
                newFailedAttempts,
                lockedUntil,
                customer.createdAt(),
                Instant.now()
        );
        customerRepository.save(updatedCustomer);

        logAuthEvent(customer.id(), "LOGIN_FAILED", "Invalid password", ipAddress, userAgent, false);
    }

    private String generateUserNo() {
        return "USR" + System.currentTimeMillis();
    }

    private String generateMfaCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }

    private void logAuthEvent(UUID customerId, String eventType, String eventData, 
                            InetAddress ipAddress, String userAgent, boolean success) {
        AuthAuditLog log = new AuthAuditLog(
                UUID.randomUUID(),
                customerId,
                eventType,
                eventData,
                ipAddress,
                userAgent,
                success,
                Instant.now()
        );
        auditLogRepository.save(log);
    }
}

