package com.yourorg.banking.payments.api;

import com.yourorg.banking.payments.model.*;
import com.yourorg.banking.payments.service.PaymentLimitService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payment-limits")
public class PaymentLimitController {
    
    @Autowired
    private PaymentLimitService paymentLimitService;
    
    @PostMapping
    public ResponseEntity<PaymentLimitResponse> createPaymentLimit(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @Valid @RequestBody PaymentLimitRequest request) {
        
        PaymentLimitResponse response = paymentLimitService.createPaymentLimit(customerId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PaymentLimitResponse> getPaymentLimit(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID id) {
        
        PaymentLimitResponse response = paymentLimitService.getPaymentLimit(customerId, id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<PaymentLimitResponse>> getPaymentLimits(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) Boolean active) {
        
        List<PaymentLimitResponse> responses;
        if (accountId != null) {
            responses = paymentLimitService.getAccountPaymentLimits(customerId, accountId);
        } else if (active != null && active) {
            responses = paymentLimitService.getActivePaymentLimits(customerId);
        } else {
            responses = paymentLimitService.getPaymentLimits(customerId);
        }
        
        return ResponseEntity.ok(responses);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<PaymentLimitResponse> updatePaymentLimit(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID id,
            @Valid @RequestBody PaymentLimitRequest request) {
        
        PaymentLimitResponse response = paymentLimitService.updatePaymentLimit(customerId, id, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/toggle")
    public ResponseEntity<PaymentLimitResponse> togglePaymentLimit(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID id) {
        
        PaymentLimitResponse response = paymentLimitService.togglePaymentLimit(customerId, id);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/reset-usage")
    public ResponseEntity<PaymentLimitResponse> resetUsage(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID id) {
        
        PaymentLimitResponse response = paymentLimitService.resetUsage(customerId, id);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaymentLimit(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID id) {
        
        paymentLimitService.deletePaymentLimit(customerId, id);
        return ResponseEntity.noContent().build();
    }
}

