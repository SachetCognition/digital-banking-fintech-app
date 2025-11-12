package com.yourorg.banking.card.api;

import com.yourorg.banking.card.model.*;
import com.yourorg.banking.card.service.CardControlService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cards/{cardId}/controls")
public class CardControlController {
    
    @Autowired
    private CardControlService cardControlService;
    
    @PostMapping
    public ResponseEntity<CardControlResponse> createCardControl(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID cardId,
            @Valid @RequestBody CardControlRequest request) {
        
        CardControlResponse response = cardControlService.createCardControl(customerId, cardId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<CardControlResponse>> getCardControls(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID cardId) {
        
        List<CardControlResponse> responses = cardControlService.getCardControls(customerId, cardId);
        return ResponseEntity.ok(responses);
    }
    
    @PutMapping("/{controlId}")
    public ResponseEntity<CardControlResponse> updateCardControl(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID cardId,
            @PathVariable UUID controlId,
            @Valid @RequestBody CardControlRequest request) {
        
        CardControlResponse response = cardControlService.updateCardControl(customerId, cardId, controlId, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{controlId}/toggle")
    public ResponseEntity<CardControlResponse> toggleCardControl(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID cardId,
            @PathVariable UUID controlId) {
        
        CardControlResponse response = cardControlService.toggleCardControl(customerId, cardId, controlId);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{controlId}")
    public ResponseEntity<Void> deleteCardControl(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID cardId,
            @PathVariable UUID controlId) {
        
        cardControlService.deleteCardControl(customerId, cardId, controlId);
        return ResponseEntity.noContent().build();
    }
}

