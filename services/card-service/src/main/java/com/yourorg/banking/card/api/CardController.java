package com.yourorg.banking.card.api;

import com.yourorg.banking.card.model.*;
import com.yourorg.banking.card.service.CardIssuanceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cards")
public class CardController {
    
    @Autowired
    private CardIssuanceService cardIssuanceService;
    
    @PostMapping
    public ResponseEntity<CardResponse> createCard(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @Valid @RequestBody CreateCardRequest request) {
        
        CardResponse response = cardIssuanceService.createVirtualCard(customerId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CardResponse> getCard(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID id) {
        
        CardResponse response = cardIssuanceService.getCard(customerId, id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<CardResponse>> getCustomerCards(
            @RequestHeader("X-Customer-ID") UUID customerId) {
        
        List<CardResponse> responses = cardIssuanceService.getCustomerCards(customerId);
        return ResponseEntity.ok(responses);
    }
    
    @PutMapping("/{id}/limits")
    public ResponseEntity<CardResponse> updateCardLimits(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCardLimitsRequest request) {
        
        CardResponse response = cardIssuanceService.updateCardLimits(customerId, id, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/block")
    public ResponseEntity<CardResponse> blockCard(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID id) {
        
        CardResponse response = cardIssuanceService.blockCard(customerId, id);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/unblock")
    public ResponseEntity<CardResponse> unblockCard(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID id) {
        
        CardResponse response = cardIssuanceService.unblockCard(customerId, id);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/cancel")
    public ResponseEntity<CardResponse> cancelCard(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID id) {
        
        CardResponse response = cardIssuanceService.cancelCard(customerId, id);
        return ResponseEntity.ok(response);
    }
}

