package com.yourorg.banking.card.api;

import com.yourorg.banking.card.model.*;
import com.yourorg.banking.card.service.CardPinService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cards/{cardId}/pin")
public class CardPinController {
    
    @Autowired
    private CardPinService cardPinService;
    
    @PostMapping("/change")
    public ResponseEntity<Void> changePin(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID cardId,
            @Valid @RequestBody ChangePinRequest request) {
        
        cardPinService.changePin(customerId, cardId, request);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/reset")
    public ResponseEntity<Void> resetPin(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID cardId) {
        
        cardPinService.resetPin(customerId, cardId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/unlock")
    public ResponseEntity<Void> unlockPin(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID cardId) {
        
        cardPinService.unlockPin(customerId, cardId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/status")
    public ResponseEntity<PinStatusResponse> getPinStatus(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID cardId) {
        
        PinStatusResponse response = cardPinService.getPinStatus(customerId, cardId);
        return ResponseEntity.ok(response);
    }
}

