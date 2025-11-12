package com.yourorg.banking.card.api;

import com.yourorg.banking.card.model.*;
import com.yourorg.banking.card.service.CardTransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cards/{cardId}/transactions")
public class CardTransactionController {
    
    @Autowired
    private CardTransactionService cardTransactionService;
    
    @PostMapping
    public ResponseEntity<CardTransactionResponse> createTransaction(
            @PathVariable UUID cardId,
            @Valid @RequestBody CardTransactionRequest request) {
        
        CardTransactionResponse response = cardTransactionService.createTransaction(cardId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{transactionId}")
    public ResponseEntity<CardTransactionResponse> getTransaction(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID cardId,
            @PathVariable UUID transactionId) {
        
        CardTransactionResponse response = cardTransactionService.getTransaction(customerId, cardId, transactionId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<CardTransactionResponse>> getCardTransactions(
            @RequestHeader("X-Customer-ID") UUID customerId,
            @PathVariable UUID cardId,
            @RequestParam(required = false) CardTransactionStatus status,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        
        List<CardTransactionResponse> responses;
        
        if (status != null) {
            responses = cardTransactionService.getCardTransactionsByStatus(customerId, cardId, status);
        } else if (fromDate != null && toDate != null) {
            LocalDateTime from = LocalDateTime.parse(fromDate);
            LocalDateTime to = LocalDateTime.parse(toDate);
            responses = cardTransactionService.getCardTransactionsByDateRange(customerId, cardId, from, to);
        } else {
            responses = cardTransactionService.getCardTransactions(customerId, cardId);
        }
        
        return ResponseEntity.ok(responses);
    }
    
    @PostMapping("/{transactionId}/approve")
    public ResponseEntity<CardTransactionResponse> approveTransaction(
            @PathVariable UUID cardId,
            @PathVariable UUID transactionId,
            @RequestParam(required = false) String authorizationCode) {
        
        CardTransactionResponse response = cardTransactionService.approveTransaction(transactionId, authorizationCode);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{transactionId}/decline")
    public ResponseEntity<CardTransactionResponse> declineTransaction(
            @PathVariable UUID cardId,
            @PathVariable UUID transactionId,
            @RequestParam(required = false) String reason) {
        
        CardTransactionResponse response = cardTransactionService.declineTransaction(transactionId, reason);
        return ResponseEntity.ok(response);
    }
}

