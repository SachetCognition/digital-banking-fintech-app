package com.yourorg.banking.customer.service;

import com.yourorg.banking.customer.model.UserSession;
import com.yourorg.banking.customer.repo.UserSessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionService {

    private final UserSessionRepository sessionRepository;
    private final PasswordService passwordService;
    private final int sessionTimeoutMinutes;

    public SessionService(UserSessionRepository sessionRepository, 
                         PasswordService passwordService,
                         @Value("${app.session.timeout-minutes:30}") int sessionTimeoutMinutes) {
        this.sessionRepository = sessionRepository;
        this.passwordService = passwordService;
        this.sessionTimeoutMinutes = sessionTimeoutMinutes;
    }

    public UserSession createSession(UUID customerId, String deviceFingerprint, 
                                   InetAddress ipAddress, String userAgent) {
        String sessionToken = passwordService.generateSecureToken();
        Instant expiresAt = Instant.now().plusSeconds(sessionTimeoutMinutes * 60L);
        
        UserSession session = new UserSession(
                UUID.randomUUID(),
                customerId,
                sessionToken,
                deviceFingerprint,
                ipAddress,
                userAgent,
                expiresAt,
                Instant.now(),
                Instant.now()
        );
        
        sessionRepository.save(session);
        return session;
    }

    public Optional<UserSession> validateSession(String sessionToken) {
        return sessionRepository.findBySessionToken(sessionToken)
                .filter(session -> !session.isExpired());
    }

    public void updateLastActivity(String sessionToken) {
        sessionRepository.updateLastActivity(sessionToken);
    }

    public void invalidateSession(String sessionToken) {
        sessionRepository.deleteBySessionToken(sessionToken);
    }

    public void invalidateAllSessions(UUID customerId) {
        sessionRepository.deleteByCustomerId(customerId);
    }

    public List<UserSession> getActiveSessions(UUID customerId) {
        return sessionRepository.findByCustomerId(customerId);
    }

    public void invalidateOtherSessions(UUID customerId, String currentSessionToken) {
        List<UserSession> sessions = sessionRepository.findByCustomerId(customerId);
        sessions.stream()
                .filter(session -> !session.sessionToken().equals(currentSessionToken))
                .forEach(session -> sessionRepository.deleteBySessionToken(session.sessionToken()));
    }

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void cleanupExpiredSessions() {
        sessionRepository.deleteExpiredSessions();
    }
}

