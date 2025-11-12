package com.yourorg.banking.ledger.api;

import com.yourorg.banking.ledger.model.*;
import com.yourorg.banking.ledger.service.LedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ledger")
@Tag(name = "Ledger", description = "Ledger operations")
public class LedgerController {
    
    private final LedgerService ledgerService;
    
    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check for ledger-service")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
    
    @PostMapping("/journals")
    @Operation(summary = "Post a batch of debit/credit entries")
    public ResponseEntity<?> postJournal(
            @Valid @RequestBody PostJournalRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        
        try {
            JournalResponse response = ledgerService.postJournal(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to post journal: " + e.getMessage()));
        }
    }
    
    @GetMapping("/accounts/{accountId}/balance")
    @Operation(summary = "Get materialized balance for account")
    public ResponseEntity<?> getAccountBalance(
            @Parameter(description = "Account ID") @PathVariable UUID accountId,
            @AuthenticationPrincipal Jwt jwt) {
        
        try {
            BalanceResponse response = ledgerService.getAccountBalance(accountId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to get balance: " + e.getMessage()));
        }
    }
    
    @GetMapping("/accounts/{accountId}/statement")
    @Operation(summary = "Get account statement")
    public ResponseEntity<?> getAccountStatement(
            @Parameter(description = "Account ID") @PathVariable UUID accountId,
            @Parameter(description = "From date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @Parameter(description = "To date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @AuthenticationPrincipal Jwt jwt) {
        
        try {
            StatementResponse response = ledgerService.getAccountStatement(accountId, from, to);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to get statement: " + e.getMessage()));
        }
    }
}