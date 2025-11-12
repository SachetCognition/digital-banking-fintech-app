package com.yourorg.banking.customer.api;

import com.yourorg.banking.customer.model.*;
import com.yourorg.banking.customer.service.AuthService;
import com.yourorg.banking.customer.service.MfaService;
import com.yourorg.banking.customer.service.RbacService;
import com.yourorg.banking.customer.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication and authorization operations")
public class AuthController {

    private final AuthService authService;
    private final MfaService mfaService;
    private final RbacService rbacService;
    private final SessionService sessionService;

    public AuthController(AuthService authService, MfaService mfaService, 
                         RbacService rbacService, SessionService sessionService) {
        this.authService = authService;
        this.mfaService = mfaService;
        this.rbacService = rbacService;
        this.sessionService = sessionService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new customer")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request, 
                                               HttpServletRequest httpRequest) {
        InetAddress ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        AuthResponse response = authService.register(request, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email address with token")
    public ResponseEntity<AuthResponse> verifyEmail(@RequestParam String token) {
        AuthResponse response = authService.verifyEmail(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                            HttpServletRequest httpRequest) {
        InetAddress ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        AuthResponse response = authService.login(request, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/password-reset")
    @Operation(summary = "Request password reset")
    public ResponseEntity<Map<String, String>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request,
                                                                   HttpServletRequest httpRequest) {
        InetAddress ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        authService.requestPasswordReset(request, ipAddress, userAgent);
        return ResponseEntity.ok(Map.of("message", "Password reset email sent"));
    }

    @PostMapping("/password-reset/confirm")
    @Operation(summary = "Confirm password reset with token")
    public ResponseEntity<Map<String, String>> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request,
                                                                   HttpServletRequest httpRequest) {
        InetAddress ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        authService.confirmPasswordReset(request, ipAddress, userAgent);
        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }

    @PostMapping("/mfa/setup")
    @Operation(summary = "Setup MFA for customer")
    public ResponseEntity<Map<String, Object>> setupMfa(@AuthenticationPrincipal Jwt jwt) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        MfaSecret secret = authService.setupMfa(customerId);
        String qrCodeUrl = mfaService.generateQrCodeUrl(customerId, jwt.getClaimAsString("email"));
        
        return ResponseEntity.ok(Map.of(
                "secret", secret.secretKey(),
                "backupCodes", secret.backupCodes(),
                "qrCodeUrl", qrCodeUrl
        ));
    }

    @PostMapping("/mfa/disable")
    @Operation(summary = "Disable MFA for customer")
    public ResponseEntity<Map<String, String>> disableMfa(@AuthenticationPrincipal Jwt jwt) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        authService.disableMfa(customerId);
        return ResponseEntity.ok(Map.of("message", "MFA disabled successfully"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout current session")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("X-Session-Token") String sessionToken) {
        authService.logout(sessionToken);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Logout all sessions")
    public ResponseEntity<Map<String, String>> logoutAll(@AuthenticationPrincipal Jwt jwt) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        authService.logoutAllSessions(customerId);
        return ResponseEntity.ok(Map.of("message", "All sessions logged out successfully"));
    }

    @GetMapping("/sessions")
    @Operation(summary = "Get active sessions for customer")
    public ResponseEntity<List<UserSession>> getActiveSessions(@AuthenticationPrincipal Jwt jwt) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        List<UserSession> sessions = sessionService.getActiveSessions(customerId);
        return ResponseEntity.ok(sessions);
    }

    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "Invalidate specific session")
    public ResponseEntity<Map<String, String>> invalidateSession(@PathVariable String sessionId,
                                                               @AuthenticationPrincipal Jwt jwt) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        // Verify the session belongs to the customer
        List<UserSession> sessions = sessionService.getActiveSessions(customerId);
        boolean sessionExists = sessions.stream()
                .anyMatch(session -> session.id().toString().equals(sessionId));
        
        if (!sessionExists) {
            return ResponseEntity.notFound().build();
        }
        
        sessionService.invalidateSession(sessionId);
        return ResponseEntity.ok(Map.of("message", "Session invalidated successfully"));
    }

    @GetMapping("/permissions")
    @Operation(summary = "Get customer permissions")
    public ResponseEntity<Map<String, Object>> getPermissions(@AuthenticationPrincipal Jwt jwt) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        
        List<Role> roles = rbacService.getCustomerRoles(customerId);
        List<Permission> permissions = rbacService.getCustomerPermissions(customerId);
        
        return ResponseEntity.ok(Map.of(
                "roles", roles.stream().map(Role::name).toList(),
                "permissions", permissions.stream().map(Permission::name).toList()
        ));
    }

    @GetMapping("/health")
    @Operation(summary = "Health check for auth service")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    private InetAddress getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return InetAddress.getLoopbackAddress(); // Simplified for demo
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return InetAddress.getLoopbackAddress(); // Simplified for demo
        }
        
        return InetAddress.getLoopbackAddress(); // Simplified for demo
    }
}

