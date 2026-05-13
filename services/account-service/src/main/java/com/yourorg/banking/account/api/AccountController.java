package com.yourorg.banking.account.api;

import com.yourorg.banking.account.model.*;
import com.yourorg.banking.account.service.AccountService;
import com.yourorg.banking.account.service.AccountOpeningService;
import com.yourorg.banking.account.service.StatementService;
import com.yourorg.banking.account.service.AccountClosureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Accounts", description = "Account management operations")
public class AccountController {
    
    private final AccountService accountService;
    private final AccountOpeningService accountOpeningService;
    private final StatementService statementService;
    private final AccountClosureService accountClosureService;
    
    public AccountController(AccountService accountService,
                           AccountOpeningService accountOpeningService,
                           StatementService statementService,
                           AccountClosureService accountClosureService) {
        this.accountService = accountService;
        this.accountOpeningService = accountOpeningService;
        this.statementService = statementService;
        this.accountClosureService = accountClosureService;
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check for account-service")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
    
    @PostMapping
    @Operation(summary = "Create a new account")
    public ResponseEntity<?> createAccount(
            @Valid @RequestBody CreateAccountRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        
        try {
            Account response = accountService.createAccount(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create account: " + e.getMessage()));
        }
    }
    
    @GetMapping
    @Operation(summary = "Get accounts by customer ID")
    public ResponseEntity<?> getAccountsByCustomer(
            @Parameter(description = "Customer ID") @RequestParam UUID customerId,
            @AuthenticationPrincipal Jwt jwt) {
        
        try {
            List<Account> accounts = accountService.getAccountsByCustomer(customerId);
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch accounts: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID")
    public ResponseEntity<?> getAccountById(
            @Parameter(description = "Account ID") @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        
        try {
            return accountService.getAccountById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch account: " + e.getMessage()));
        }
    }
    
    @GetMapping("/number/{accountNumber}")
    @Operation(summary = "Get account by account number")
    public ResponseEntity<?> getAccountByNumber(
            @Parameter(description = "Account Number") @PathVariable String accountNumber,
            @AuthenticationPrincipal Jwt jwt) {
        
        try {
            return accountService.getAccountByNumber(accountNumber)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch account: " + e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate account")
    public ResponseEntity<?> deactivateAccount(
            @Parameter(description = "Account ID") @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        
        try {
            accountService.deactivateAccount(id);
            return ResponseEntity.ok(Map.of("message", "Account deactivated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to deactivate account: " + e.getMessage()));
        }
    }

    // Account Opening Endpoints
    @PostMapping("/open")
    @Operation(summary = "Open a new account")
    public ResponseEntity<?> openAccount(
            @Valid @RequestBody AccountOpeningRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            UUID customerId = UUID.fromString(jwt.getSubject());
            AccountOpeningResponse response = accountOpeningService.openAccount(customerId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to open account: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/opening-status")
    @Operation(summary = "Get account opening status")
    public ResponseEntity<?> getAccountOpeningStatus(@PathVariable UUID id) {
        try {
            AccountOpeningResponse response = accountOpeningService.getAccountOpeningStatus(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get opening status: " + e.getMessage()));
        }
    }

    // Statement Endpoints
    @PostMapping("/{id}/statements/generate")
    @Operation(summary = "Generate account statement")
    public ResponseEntity<?> generateStatement(
            @PathVariable UUID id,
            @RequestParam LocalDate fromDate,
            @RequestParam LocalDate toDate,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            AccountStatement statement = statementService.generateStatement(id, fromDate, toDate);
            return ResponseEntity.ok(statement);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate statement: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/statements")
    @Operation(summary = "Get account statements")
    public ResponseEntity<?> getAccountStatements(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            List<AccountStatement> statements = statementService.getAccountStatements(id, limit);
            return ResponseEntity.ok(statements);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get statements: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/statements/{statementId}/email")
    @Operation(summary = "Email statement to customer")
    public ResponseEntity<?> emailStatement(
            @PathVariable UUID id,
            @PathVariable String statementId,
            @RequestParam String emailAddress,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            statementService.emailStatement(statementId, emailAddress);
            return ResponseEntity.ok(Map.of("message", "Statement emailed successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to email statement: " + e.getMessage()));
        }
    }

    // Account Closure Endpoints
    @PostMapping("/{id}/close")
    @Operation(summary = "Request account closure")
    public ResponseEntity<?> requestAccountClosure(
            @PathVariable UUID id,
            @Valid @RequestBody AccountClosureRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            UUID customerId = UUID.fromString(jwt.getSubject());
            AccountClosureRequest closureRequest = accountClosureService.requestAccountClosure(customerId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(closureRequest);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to request closure: " + e.getMessage()));
        }
    }

    @GetMapping("/closure-requests")
    @Operation(summary = "Get my closure requests")
    public ResponseEntity<?> getMyClosureRequests(@AuthenticationPrincipal Jwt jwt) {
        try {
            UUID customerId = UUID.fromString(jwt.getSubject());
            List<AccountClosureRequest> requests = accountClosureService.getClosureRequests(customerId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get closure requests: " + e.getMessage()));
        }
    }
}