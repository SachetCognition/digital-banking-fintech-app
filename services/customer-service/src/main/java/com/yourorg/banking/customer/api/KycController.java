package com.yourorg.banking.customer.api;

import com.yourorg.banking.customer.model.*;
import com.yourorg.banking.customer.service.KycService;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/kyc")
@Tag(name = "KYC", description = "Know Your Customer operations")
public class KycController {
    
    private final KycService kycService;
    
    public KycController(KycService kycService) {
        this.kycService = kycService;
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check for KYC service")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
    
    @PostMapping("/submit")
    @Operation(summary = "Submit KYC documents for verification")
    public ResponseEntity<?> submitKyc(
            @Valid @RequestBody SubmitKycRequest request,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest) {
        
        try {
            KycResponse response = kycService.submitKyc(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "KYC submission failed: " + e.getMessage()));
        }
    }
    
    @GetMapping("/status/{kycCaseId}")
    @Operation(summary = "Get KYC case status")
    public ResponseEntity<?> getKycStatus(
            @Parameter(description = "KYC Case ID") @PathVariable UUID kycCaseId,
            @AuthenticationPrincipal Jwt jwt) {
        
        try {
            UUID customerId = extractCustomerId(jwt);
            KycResponse response = kycService.getKycStatus(customerId, kycCaseId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get KYC status: " + e.getMessage()));
        }
    }
    
    @GetMapping("/history")
    @Operation(summary = "Get customer KYC history")
    public ResponseEntity<?> getKycHistory(@AuthenticationPrincipal Jwt jwt) {
        try {
            UUID customerId = extractCustomerId(jwt);
            List<KycResponse> history = kycService.getCustomerKycHistory(customerId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get KYC history: " + e.getMessage()));
        }
    }
    
    @PostMapping("/callback/{kycCaseId}")
    @Operation(summary = "Process KYC provider callback (internal)")
    public ResponseEntity<?> processCallback(
            @Parameter(description = "KYC Case ID") @PathVariable UUID kycCaseId,
            @Valid @RequestBody KycCallbackRequest callback) {
        
        try {
            kycService.processCallback(kycCaseId, callback);
            return ResponseEntity.ok(Map.of("message", "Callback processed successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process callback: " + e.getMessage()));
        }
    }
    
    @PostMapping("/poll/{kycCaseId}")
    @Operation(summary = "Poll KYC status (for testing)")
    public ResponseEntity<?> pollKycStatus(
            @Parameter(description = "KYC Case ID") @PathVariable UUID kycCaseId,
            @AuthenticationPrincipal Jwt jwt) {
        
        try {
            UUID customerId = extractCustomerId(jwt);
            KycResponse response = kycService.getKycStatus(customerId, kycCaseId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to poll KYC status: " + e.getMessage()));
        }
    }
    
    private UUID extractCustomerId(Jwt jwt) {
        // Extract customer ID from JWT token
        String customerIdStr = jwt.getClaimAsString("customer_id");
        if (customerIdStr == null) {
            customerIdStr = jwt.getSubject();
        }
        return UUID.fromString(customerIdStr);
    }
}