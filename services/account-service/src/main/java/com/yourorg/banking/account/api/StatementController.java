package com.yourorg.banking.account.api;

import com.yourorg.banking.account.model.AccountStatement;
import com.yourorg.banking.account.service.StatementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/statements")
@Tag(name = "Account Statements", description = "Account statement operations")
public class StatementController {

    private final StatementService statementService;

    public StatementController(StatementService statementService) {
        this.statementService = statementService;
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate account statement")
    public ResponseEntity<AccountStatement> generateStatement(
            @RequestParam UUID accountId,
            @RequestParam LocalDate fromDate,
            @RequestParam LocalDate toDate,
            @AuthenticationPrincipal Jwt jwt) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        AccountStatement statement = statementService.generateStatement(accountId, fromDate, toDate);
        return ResponseEntity.ok(statement);
    }

    @PostMapping("/generate-monthly")
    @Operation(summary = "Generate monthly statement")
    public ResponseEntity<AccountStatement> generateMonthlyStatement(
            @RequestParam UUID accountId,
            @RequestParam int year,
            @RequestParam int month,
            @AuthenticationPrincipal Jwt jwt) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        AccountStatement statement = statementService.generateMonthlyStatement(accountId, year, month);
        return ResponseEntity.ok(statement);
    }

    @PostMapping("/{statementId}/email")
    @Operation(summary = "Email statement to customer")
    public ResponseEntity<Map<String, String>> emailStatement(
            @PathVariable String statementId,
            @RequestParam String emailAddress,
            @AuthenticationPrincipal Jwt jwt) {
        statementService.emailStatement(statementId, emailAddress);
        return ResponseEntity.ok(Map.of("message", "Statement emailed successfully"));
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get account statements")
    public ResponseEntity<List<AccountStatement>> getAccountStatements(
            @PathVariable UUID accountId,
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal Jwt jwt) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        List<AccountStatement> statements = statementService.getAccountStatements(accountId, limit);
        return ResponseEntity.ok(statements);
    }

    @GetMapping("/{statementId}")
    @Operation(summary = "Get statement by ID")
    public ResponseEntity<AccountStatement> getStatementById(
            @PathVariable UUID statementId,
            @AuthenticationPrincipal Jwt jwt) {
        AccountStatement statement = statementService.getStatementById(statementId);
        return ResponseEntity.ok(statement);
    }

    @GetMapping("/{statementId}/download")
    @Operation(summary = "Download statement PDF")
    public ResponseEntity<byte[]> downloadStatementPdf(
            @PathVariable String statementId,
            @AuthenticationPrincipal Jwt jwt) {
        byte[] pdfBytes = statementService.downloadStatementPdf(statementId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "statement_" + statementId + ".pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check for statement service")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}

