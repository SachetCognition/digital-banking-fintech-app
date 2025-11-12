package com.yourorg.banking.payments.api;

import com.yourorg.banking.payments.model.TransferRequest;
import com.yourorg.banking.payments.model.TransferResponse;
import com.yourorg.banking.payments.service.IdempotencyService;
import com.yourorg.banking.payments.service.TransferValidationService;
import com.yourorg.banking.payments.service.TransferSagaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transfers")
@Tag(name = "Transfers", description = "P2P transfer operations")
public class TransferController {
    
    private final TransferSagaService transferSagaService;
    private final IdempotencyService idempotencyService;
    private final TransferValidationService validationService;
    
    public TransferController(TransferSagaService transferSagaService, 
                           IdempotencyService idempotencyService,
                           TransferValidationService validationService) {
        this.transferSagaService = transferSagaService;
        this.idempotencyService = idempotencyService;
        this.validationService = validationService;
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check for payments-service")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
    
    @PostMapping
    @Operation(summary = "Create a new P2P transfer", 
               description = "Creates a new P2P transfer with validation, reservation, posting, and notification")
    public ResponseEntity<?> createTransfer(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest) {
        
        try {
            // Check for idempotency
            Optional<TransferResponse> existingTransfer = idempotencyService.getExistingTransfer(request.idempotencyKey());
            if (existingTransfer.isPresent()) {
                return ResponseEntity.ok(existingTransfer.get());
            }
            
            // Extract auth token
            String authToken = extractAuthToken(httpRequest);
            
            // Execute transfer saga
            TransferResponse response = transferSagaService.executeTransfer(request, authToken);
            
            // Cache the transfer ID for idempotency
            idempotencyService.cacheTransferId(request.idempotencyKey(), response.transferId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Transfer failed: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get transfer by ID")
    public ResponseEntity<?> getTransfer(
            @Parameter(description = "Transfer ID") @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        
        // This would typically fetch from database
        // For now, return a placeholder response
        return ResponseEntity.ok(Map.of(
            "transferId", id.toString(),
            "status", "COMPLETED",
            "message", "Transfer details would be returned here"
        ));
    }
    
    @GetMapping("/{id}/status")
    @Operation(summary = "Get transfer status")
    public ResponseEntity<?> getTransferStatus(
            @Parameter(description = "Transfer ID") @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        
        // This would typically fetch status from database
        return ResponseEntity.ok(Map.of(
            "transferId", id.toString(),
            "status", "COMPLETED",
            "lastUpdated", java.time.Instant.now().toString()
        ));
    }
    
    @GetMapping("/daily-usage")
    @Operation(summary = "Get today's usage and remaining daily quota for a payer account")
    public ResponseEntity<?> getDailyUsage(
            @RequestParam("payerAccountId") UUID payerAccountId,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest
    ) {
        try {
            String authToken = extractAuthToken(httpRequest);
            var usage = validationService.getDailyUsage(payerAccountId, authToken);
            return ResponseEntity.ok(Map.of(
                    "payerAccountId", payerAccountId.toString(),
                    "dailyLimit", usage.dailyLimit(),
                    "used", usage.used(),
                    "remaining", usage.remaining()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch daily usage: " + e.getMessage()));
        }
    }
    
    private String extractAuthToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new IllegalArgumentException("Missing or invalid Authorization header");
    }
}

