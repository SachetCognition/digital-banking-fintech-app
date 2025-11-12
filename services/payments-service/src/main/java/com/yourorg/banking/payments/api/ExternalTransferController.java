package com.yourorg.banking.payments.api;

import com.yourorg.banking.payments.model.*;
import com.yourorg.banking.payments.service.ExternalTransferService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/external-transfers")
public class ExternalTransferController {
    
    @Autowired
    private ExternalTransferService externalTransferService;
    
    @PostMapping
    public ResponseEntity<ExternalTransferResponse> createExternalTransfer(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @Valid @RequestBody ExternalTransferRequest request) {
        
        ExternalTransferResponse response = externalTransferService.createExternalTransfer(customerId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ExternalTransferResponse> getExternalTransfer(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID id) {
        
        ExternalTransferResponse response = externalTransferService.getExternalTransfer(customerId, id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<ExternalTransferResponse>> getExternalTransfers(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @RequestParam(required = false) ExternalTransferStatus status) {
        
        List<ExternalTransferResponse> responses;
        if (status != null) {
            responses = externalTransferService.getExternalTransfersByStatus(customerId, status);
        } else {
            responses = externalTransferService.getExternalTransfers(customerId);
        }
        
        return ResponseEntity.ok(responses);
    }
}

