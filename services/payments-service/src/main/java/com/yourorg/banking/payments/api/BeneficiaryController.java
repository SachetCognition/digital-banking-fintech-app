package com.yourorg.banking.payments.api;

import com.yourorg.banking.payments.model.*;
import com.yourorg.banking.payments.service.BeneficiaryService;
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
@RequestMapping("/api/v1/beneficiaries")
@Tag(name = "Beneficiaries", description = "Beneficiary management operations")
public class BeneficiaryController {
    
    private final BeneficiaryService beneficiaryService;
    
    public BeneficiaryController(BeneficiaryService beneficiaryService) {
        this.beneficiaryService = beneficiaryService;
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check for beneficiaries")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
    
    @PostMapping
    @Operation(summary = "Create a new beneficiary")
    public ResponseEntity<?> createBeneficiary(
            @Valid @RequestBody CreateBeneficiaryRequest request,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest) {
        
        try {
            UUID customerId = extractCustomerId(jwt);
            BeneficiaryResponse response = beneficiaryService.createBeneficiary(customerId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create beneficiary: " + e.getMessage()));
        }
    }
    
    @GetMapping
    @Operation(summary = "Get beneficiaries for customer")
    public ResponseEntity<?> getBeneficiaries(
            @Parameter(description = "Filter by beneficiary type") @RequestParam(required = false) BeneficiaryType type,
            @Parameter(description = "Search term for name or account number") @RequestParam(required = false) String search,
            @AuthenticationPrincipal Jwt jwt) {
        
        try {
            UUID customerId = extractCustomerId(jwt);
            List<BeneficiaryResponse> beneficiaries = beneficiaryService.getBeneficiaries(customerId, type, search);
            return ResponseEntity.ok(beneficiaries);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch beneficiaries: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get beneficiary by ID")
    public ResponseEntity<?> getBeneficiary(
            @Parameter(description = "Beneficiary ID") @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        
        try {
            UUID customerId = extractCustomerId(jwt);
            return beneficiaryService.getBeneficiary(customerId, id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch beneficiary: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update beneficiary")
    public ResponseEntity<?> updateBeneficiary(
            @Parameter(description = "Beneficiary ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateBeneficiaryRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        
        try {
            UUID customerId = extractCustomerId(jwt);
            return beneficiaryService.updateBeneficiary(customerId, id, request)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update beneficiary: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete beneficiary (deactivate)")
    public ResponseEntity<?> deleteBeneficiary(
            @Parameter(description = "Beneficiary ID") @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        
        try {
            UUID customerId = extractCustomerId(jwt);
            boolean deleted = beneficiaryService.deleteBeneficiary(customerId, id);
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "Beneficiary deleted successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete beneficiary: " + e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate beneficiary")
    public ResponseEntity<?> activateBeneficiary(
            @Parameter(description = "Beneficiary ID") @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        
        try {
            UUID customerId = extractCustomerId(jwt);
            boolean activated = beneficiaryService.activateBeneficiary(customerId, id);
            if (activated) {
                return ResponseEntity.ok(Map.of("message", "Beneficiary activated successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to activate beneficiary: " + e.getMessage()));
        }
    }
    
    private UUID extractCustomerId(Jwt jwt) {
        // Extract customer ID from JWT token
        // This would typically come from a custom claim in the token
        String customerIdStr = jwt.getClaimAsString("customer_id");
        if (customerIdStr == null) {
            // Fallback: use subject as customer ID
            customerIdStr = jwt.getSubject();
        }
        return UUID.fromString(customerIdStr);
    }
}

