package com.yourorg.banking.card.api;

import com.yourorg.banking.card.model.*;
import com.yourorg.banking.card.service.CardReplacementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cards/{cardId}/replacements")
public class CardReplacementController {
    
    @Autowired
    private CardReplacementService cardReplacementService;
    
    @PostMapping
    public ResponseEntity<CardReplacementResponse> requestCardReplacement(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID cardId,
            @Valid @RequestBody CardReplacementRequest request) {
        
        CardReplacementResponse response = cardReplacementService.requestCardReplacement(customerId, cardId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{replacementId}")
    public ResponseEntity<CardReplacementResponse> getCardReplacement(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID cardId,
            @PathVariable UUID replacementId) {
        
        CardReplacementResponse response = cardReplacementService.getCardReplacement(customerId, cardId, replacementId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<CardReplacementResponse>> getCardReplacements(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID cardId) {
        
        List<CardReplacementResponse> responses = cardReplacementService.getCardReplacements(customerId, cardId);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/customer")
    public ResponseEntity<List<CardReplacementResponse>> getCustomerReplacements(
            @RequestHeader("X-Customer-ID") UUID customerId) {
        
        List<CardReplacementResponse> responses = cardReplacementService.getCustomerReplacements(customerId);
        return ResponseEntity.ok(responses);
    }
}

