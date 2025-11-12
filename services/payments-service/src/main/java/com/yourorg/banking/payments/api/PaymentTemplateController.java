package com.yourorg.banking.payments.api;

import com.yourorg.banking.payments.model.*;
import com.yourorg.banking.payments.service.PaymentTemplateService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payment-templates")
public class PaymentTemplateController {
    
    @Autowired
    private PaymentTemplateService paymentTemplateService;
    
    @PostMapping
    public ResponseEntity<PaymentTemplateResponse> createPaymentTemplate(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @Valid @RequestBody PaymentTemplateRequest request) {
        
        PaymentTemplateResponse response = paymentTemplateService.createPaymentTemplate(customerId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PaymentTemplateResponse> getPaymentTemplate(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID id) {
        
        PaymentTemplateResponse response = paymentTemplateService.getPaymentTemplate(customerId, id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<PaymentTemplateResponse>> getPaymentTemplates(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @RequestParam(required = false) Boolean favorites,
            @RequestParam(required = false) String search) {
        
        List<PaymentTemplateResponse> responses;
        if (favorites != null && favorites) {
            responses = paymentTemplateService.getFavoritePaymentTemplates(customerId);
        } else if (search != null && !search.trim().isEmpty()) {
            responses = paymentTemplateService.searchPaymentTemplates(customerId, search);
        } else {
            responses = paymentTemplateService.getPaymentTemplates(customerId);
        }
        
        return ResponseEntity.ok(responses);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<PaymentTemplateResponse> updatePaymentTemplate(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID id,
            @Valid @RequestBody PaymentTemplateRequest request) {
        
        PaymentTemplateResponse response = paymentTemplateService.updatePaymentTemplate(customerId, id, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/toggle-favorite")
    public ResponseEntity<PaymentTemplateResponse> toggleFavorite(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID id) {
        
        PaymentTemplateResponse response = paymentTemplateService.toggleFavorite(customerId, id);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaymentTemplate(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID id) {
        
        paymentTemplateService.deletePaymentTemplate(customerId, id);
        return ResponseEntity.noContent().build();
    }
}

