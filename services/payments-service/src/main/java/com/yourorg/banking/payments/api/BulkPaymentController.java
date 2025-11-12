package com.yourorg.banking.payments.api;

import com.yourorg.banking.payments.model.*;
import com.yourorg.banking.payments.service.BulkPaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bulk-payments")
public class BulkPaymentController {
    
    @Autowired
    private BulkPaymentService bulkPaymentService;
    
    @PostMapping
    public ResponseEntity<BulkPaymentResponse> createBulkPayment(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @Valid @RequestBody BulkPaymentRequest request) {
        
        BulkPaymentResponse response = bulkPaymentService.createBulkPayment(customerId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BulkPaymentResponse> getBulkPayment(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID id) {
        
        BulkPaymentResponse response = bulkPaymentService.getBulkPayment(customerId, id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<BulkPaymentResponse>> getBulkPayments(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @RequestParam(required = false) BulkPaymentStatus status) {
        
        List<BulkPaymentResponse> responses;
        if (status != null) {
            responses = bulkPaymentService.getBulkPaymentsByStatus(customerId, status);
        } else {
            responses = bulkPaymentService.getBulkPayments(customerId);
        }
        
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/{id}/items")
    public ResponseEntity<List<BulkPaymentItemResponse>> getBulkPaymentItems(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID id) {
        
        List<BulkPaymentItemResponse> responses = bulkPaymentService.getBulkPaymentItems(customerId, id);
        return ResponseEntity.ok(responses);
    }
}

