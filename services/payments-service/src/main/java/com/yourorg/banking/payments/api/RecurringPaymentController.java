package com.yourorg.banking.payments.api;

import com.yourorg.banking.payments.model.*;
import com.yourorg.banking.payments.service.RecurringPaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recurring-payments")
public class RecurringPaymentController {
    
    @Autowired
    private RecurringPaymentService recurringPaymentService;
    
    @PostMapping
    public ResponseEntity<RecurringPaymentResponse> createRecurringPayment(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @Valid @RequestBody RecurringPaymentRequest request) {
        
        RecurringPaymentResponse response = recurringPaymentService.createRecurringPayment(customerId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RecurringPaymentResponse> getRecurringPayment(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID id) {
        
        RecurringPaymentResponse response = recurringPaymentService.getRecurringPayment(customerId, id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<RecurringPaymentResponse>> getRecurringPayments(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @RequestParam(required = false) RecurringPaymentStatus status) {
        
        List<RecurringPaymentResponse> responses;
        if (status != null) {
            responses = recurringPaymentService.getRecurringPaymentsByStatus(customerId, status);
        } else {
            responses = recurringPaymentService.getRecurringPayments(customerId);
        }
        
        return ResponseEntity.ok(responses);
    }
    
    @PostMapping("/{id}/pause")
    public ResponseEntity<RecurringPaymentResponse> pauseRecurringPayment(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID id) {
        
        RecurringPaymentResponse response = recurringPaymentService.pauseRecurringPayment(customerId, id);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/resume")
    public ResponseEntity<RecurringPaymentResponse> resumeRecurringPayment(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID id) {
        
        RecurringPaymentResponse response = recurringPaymentService.resumeRecurringPayment(customerId, id);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/cancel")
    public ResponseEntity<RecurringPaymentResponse> cancelRecurringPayment(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID id,
            @RequestParam(required = false) String reason) {
        
        RecurringPaymentResponse response = recurringPaymentService.cancelRecurringPayment(customerId, id, reason);
        return ResponseEntity.ok(response);
    }
}

