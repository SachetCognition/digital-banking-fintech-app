package com.yourorg.banking.account.api;

import com.yourorg.banking.account.model.AccountClosureRequest;
import com.yourorg.banking.account.service.AccountClosureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/account-closure")
@Tag(name = "Account Closure", description = "Account closure operations")
public class AccountClosureController {

    private final AccountClosureService accountClosureService;

    public AccountClosureController(AccountClosureService accountClosureService) {
        this.accountClosureService = accountClosureService;
    }

    @PostMapping("/request")
    @Operation(summary = "Request account closure")
    public ResponseEntity<AccountClosureRequest> requestAccountClosure(
            @Validated @RequestBody AccountClosureRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        AccountClosureRequest closureRequest = accountClosureService.requestAccountClosure(customerId, request);
        return ResponseEntity.created(URI.create("/api/v1/account-closure/" + closureRequest.id())).body(closureRequest);
    }

    @PostMapping("/{requestId}/approve")
    @Operation(summary = "Approve account closure")
    public ResponseEntity<Map<String, String>> approveAccountClosure(
            @PathVariable UUID requestId,
            @RequestParam(required = false) String approvalNotes,
            @AuthenticationPrincipal Jwt jwt) {
        UUID approvedBy = UUID.fromString(jwt.getSubject());
        accountClosureService.approveAccountClosure(requestId, approvedBy, approvalNotes);
        return ResponseEntity.ok(Map.of("message", "Account closure approved successfully"));
    }

    @PostMapping("/{requestId}/reject")
    @Operation(summary = "Reject account closure")
    public ResponseEntity<Map<String, String>> rejectAccountClosure(
            @PathVariable UUID requestId,
            @RequestParam String rejectionReason,
            @AuthenticationPrincipal Jwt jwt) {
        accountClosureService.rejectAccountClosure(requestId, rejectionReason);
        return ResponseEntity.ok(Map.of("message", "Account closure rejected"));
    }

    @GetMapping("/my-requests")
    @Operation(summary = "Get my closure requests")
    public ResponseEntity<List<AccountClosureRequest>> getMyClosureRequests(
            @AuthenticationPrincipal Jwt jwt) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        List<AccountClosureRequest> requests = accountClosureService.getClosureRequests(customerId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending closure requests (admin only)")
    public ResponseEntity<List<AccountClosureRequest>> getPendingClosureRequests(
            @AuthenticationPrincipal Jwt jwt) {
        List<AccountClosureRequest> requests = accountClosureService.getPendingClosureRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check for account closure service")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}

