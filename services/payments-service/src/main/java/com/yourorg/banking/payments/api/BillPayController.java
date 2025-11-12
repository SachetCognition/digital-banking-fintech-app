package com.yourorg.banking.payments.api;

import com.yourorg.banking.payments.model.BillPayRequest;
import com.yourorg.banking.payments.model.BillPayResponse;
import com.yourorg.banking.payments.service.BillPayService;
import com.yourorg.banking.payments.service.IdempotencyService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/v1/bill-pay")
@Tag(name = "Bill Pay", description = "Bill payment operations")
public class BillPayController {
    
    private final BillPayService billPayService;
    private final IdempotencyService idempotencyService;
    
    public BillPayController(BillPayService billPayService, IdempotencyService idempotencyService) {
        this.billPayService = billPayService;
        this.idempotencyService = idempotencyService;
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check for bill pay")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
    
    @PostMapping
    @Operation(summary = "Pay bill to saved beneficiary")
    public ResponseEntity<?> payBill(
            @Valid @RequestBody BillPayRequest request,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest) {
        
        try {
            UUID customerId = extractCustomerId(jwt);
            
            // Check for idempotency
            Optional<BillPayResponse> existingPayment = idempotencyService.getExistingBillPay(request.idempotencyKey());
            if (existingPayment.isPresent()) {
                return ResponseEntity.ok(existingPayment.get());
            }
            
            // Extract auth token
            String authToken = extractAuthToken(httpRequest);
            
            // Execute bill payment
            BillPayResponse response = billPayService.payBill(customerId, request, authToken);
            
            // Cache the response for idempotency
            idempotencyService.cacheBillPayId(request.idempotencyKey(), response.billPayId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Bill payment failed: " + e.getMessage()));
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
    
    private String extractAuthToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new IllegalArgumentException("Missing or invalid Authorization header");
    }
}

