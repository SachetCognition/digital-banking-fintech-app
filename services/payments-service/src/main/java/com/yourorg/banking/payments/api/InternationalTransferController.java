package com.yourorg.banking.payments.api;

import com.yourorg.banking.payments.model.*;
import com.yourorg.banking.payments.service.InternationalTransferService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/international-transfers")
public class InternationalTransferController {
    
    @Autowired
    private InternationalTransferService internationalTransferService;
    
    @PostMapping
    public ResponseEntity<InternationalTransferResponse> createInternationalTransfer(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @Valid @RequestBody InternationalTransferRequest request) {
        
        InternationalTransferResponse response = internationalTransferService.createInternationalTransfer(customerId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<InternationalTransferResponse> getInternationalTransfer(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID id) {
        
        InternationalTransferResponse response = internationalTransferService.getInternationalTransfer(customerId, id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<InternationalTransferResponse>> getInternationalTransfers(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @RequestParam(required = false) InternationalTransferStatus status) {
        
        List<InternationalTransferResponse> responses;
        if (status != null) {
            responses = internationalTransferService.getInternationalTransfersByStatus(customerId, status);
        } else {
            responses = internationalTransferService.getInternationalTransfers(customerId);
        }
        
        return ResponseEntity.ok(responses);
    }
}

