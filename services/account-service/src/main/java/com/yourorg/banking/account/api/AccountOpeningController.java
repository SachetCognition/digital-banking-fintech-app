package com.yourorg.banking.account.api;

import com.yourorg.banking.account.model.*;
import com.yourorg.banking.account.service.AccountOpeningService;
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
@RequestMapping("/api/v1/account-opening")
@Tag(name = "Account Opening", description = "Account opening and management operations")
public class AccountOpeningController {

    private final AccountOpeningService accountOpeningService;

    public AccountOpeningController(AccountOpeningService accountOpeningService) {
        this.accountOpeningService = accountOpeningService;
    }

    @PostMapping
    @Operation(summary = "Open a new account")
    public ResponseEntity<AccountOpeningResponse> openAccount(
            @Validated @RequestBody AccountOpeningRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        AccountOpeningResponse response = accountOpeningService.openAccount(customerId, request);
        return ResponseEntity.created(URI.create("/api/v1/account-opening/" + response.accountId())).body(response);
    }

    @GetMapping("/{accountId}/status")
    @Operation(summary = "Get account opening status")
    public ResponseEntity<AccountOpeningResponse> getAccountOpeningStatus(@PathVariable UUID accountId) {
        AccountOpeningResponse response = accountOpeningService.getAccountOpeningStatus(accountId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{accountId}/approve")
    @Operation(summary = "Approve account opening")
    public ResponseEntity<Map<String, String>> approveAccountOpening(
            @PathVariable UUID accountId,
            @RequestParam(required = false) String approvalNotes) {
        accountOpeningService.approveAccountOpening(accountId, approvalNotes);
        return ResponseEntity.ok(Map.of("message", "Account opening approved successfully"));
    }

    @GetMapping("/account-types")
    @Operation(summary = "Get available account types")
    public ResponseEntity<List<Map<String, Object>>> getAccountTypes() {
        List<Map<String, Object>> accountTypes = List.of(
                Map.of("type", "CHECKING", "displayName", "Personal Checking", "description", "Basic checking account for daily transactions", "requiresKyc", false),
                Map.of("type", "SAVINGS", "displayName", "Personal Savings", "description", "Interest-bearing savings account", "requiresKyc", false),
                Map.of("type", "MONEY_MARKET", "displayName", "Money Market", "description", "High-yield savings with limited transactions", "requiresKyc", false),
                Map.of("type", "CD", "displayName", "Certificate of Deposit", "description", "Fixed-term deposit with guaranteed interest", "requiresKyc", false),
                Map.of("type", "BUSINESS_CHECKING", "displayName", "Business Checking", "description", "Checking account for business operations", "requiresKyc", true),
                Map.of("type", "BUSINESS_SAVINGS", "displayName", "Business Savings", "description", "Savings account for business funds", "requiresKyc", true),
                Map.of("type", "INVESTMENT", "displayName", "Investment Account", "description", "Account for securities and investments", "requiresKyc", true),
                Map.of("type", "IRA", "displayName", "Individual Retirement Account", "description", "Tax-advantaged retirement savings", "requiresKyc", true),
                Map.of("type", "ROTH_IRA", "displayName", "Roth IRA", "description", "Tax-free retirement savings", "requiresKyc", true),
                Map.of("type", "CREDIT_CARD", "displayName", "Credit Card", "description", "Revolving credit line", "requiresKyc", true),
                Map.of("type", "LINE_OF_CREDIT", "displayName", "Line of Credit", "description", "Flexible credit facility", "requiresKyc", true),
                Map.of("type", "PERSONAL_LOAN", "displayName", "Personal Loan", "description", "Fixed-term personal loan", "requiresKyc", true),
                Map.of("type", "BUSINESS_LOAN", "displayName", "Business Loan", "description", "Fixed-term business loan", "requiresKyc", true),
                Map.of("type", "MORTGAGE", "displayName", "Mortgage", "description", "Home loan secured by property", "requiresKyc", true),
                Map.of("type", "STUDENT", "displayName", "Student Account", "description", "Account for students with special benefits", "requiresKyc", false),
                Map.of("type", "SENIOR", "displayName", "Senior Account", "description", "Account for seniors with special benefits", "requiresKyc", false),
                Map.of("type", "JOINT", "displayName", "Joint Account", "description", "Account shared by multiple customers", "requiresKyc", false),
                Map.of("type", "TRUST", "displayName", "Trust Account", "description", "Account held in trust", "requiresKyc", true),
                Map.of("type", "ESCROW", "displayName", "Escrow Account", "description", "Account for holding funds in escrow", "requiresKyc", true)
        );
        return ResponseEntity.ok(accountTypes);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check for account opening service")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}

