package com.yourorg.banking.card.service;

import com.yourorg.banking.card.model.*;
import com.yourorg.banking.card.repository.CardReplacementRepository;
import com.yourorg.banking.card.repository.VirtualCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CardReplacementService {
    
    @Autowired
    private CardReplacementRepository cardReplacementRepository;
    
    @Autowired
    private VirtualCardRepository virtualCardRepository;
    
    @Autowired
    private CardIssuanceService cardIssuanceService;
    
    @Autowired
    private CardEventService cardEventService;
    
    public CardReplacementResponse requestCardReplacement(UUID customerId, UUID cardId, CardReplacementRequest request) {
        // Verify card ownership
        VirtualCard card = virtualCardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("Card not found"));
        
        if (!card.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        // Check if there's already a pending replacement
        List<CardReplacement> pendingReplacements = cardReplacementRepository
            .findByOriginalCardIdAndReplacementStatusOrderByCreatedAtDesc(cardId, CardReplacementStatus.PENDING);
        
        if (!pendingReplacements.isEmpty()) {
            throw new RuntimeException("Card replacement already in progress");
        }
        
        // Create replacement request
        CardReplacement replacement = new CardReplacement(
            UUID.randomUUID(),
            cardId,
            null, // replacementCardId - will be set when processed
            request.replacementReason(),
            CardReplacementStatus.PENDING,
            LocalDateTime.now(),
            null, // processedAt
            null, // completedAt
            request.deliveryMethod(),
            request.deliveryAddress(),
            null, // trackingNumber
            request.notes(),
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        
        replacement = cardReplacementRepository.save(replacement);
        
        // Log event
        cardEventService.logCardEvent(cardId, "REPLACEMENT_REQUESTED", request.replacementReason().name());
        
        // Process replacement asynchronously
        processCardReplacementAsync(replacement);
        
        return CardReplacementResponse.from(replacement);
    }
    
    public CardReplacementResponse getCardReplacement(UUID customerId, UUID cardId, UUID replacementId) {
        // Verify card ownership
        VirtualCard card = virtualCardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("Card not found"));
        
        if (!card.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        CardReplacement replacement = cardReplacementRepository.findById(replacementId)
            .orElseThrow(() -> new RuntimeException("Card replacement not found"));
        
        if (!replacement.originalCardId().equals(cardId)) {
            throw new RuntimeException("Card replacement does not belong to this card");
        }
        
        return CardReplacementResponse.from(replacement);
    }
    
    public List<CardReplacementResponse> getCardReplacements(UUID customerId, UUID cardId) {
        // Verify card ownership
        VirtualCard card = virtualCardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("Card not found"));
        
        if (!card.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        return cardReplacementRepository.findByOriginalCardIdOrderByCreatedAtDesc(cardId)
            .stream()
            .map(CardReplacementResponse::from)
            .toList();
    }
    
    public List<CardReplacementResponse> getCustomerReplacements(UUID customerId) {
        return cardReplacementRepository.findByCustomerId(customerId)
            .stream()
            .map(CardReplacementResponse::from)
            .toList();
    }
    
    private void processCardReplacementAsync(CardReplacement replacement) {
        // This would typically be handled by a message queue or async processor
        // For now, we'll simulate the processing
        try {
            // Update status to processing
            CardReplacement processing = replacement.withProcessed();
            cardReplacementRepository.save(processing);
            
            // Get original card details
            VirtualCard originalCard = virtualCardRepository.findById(replacement.originalCardId())
                .orElseThrow(() -> new RuntimeException("Original card not found"));
            
            // Create replacement card
            CreateCardRequest replacementCardRequest = new CreateCardRequest(
                originalCard.accountId(),
                originalCard.cardHolderName(),
                originalCard.cardType(),
                originalCard.spendingLimit(),
                originalCard.dailyLimit(),
                originalCard.monthlyLimit(),
                originalCard.currency(),
                false // isPrimary - don't make replacement primary by default
            );
            
            CardResponse replacementCardResponse = cardIssuanceService.createVirtualCard(
                originalCard.customerId(), replacementCardRequest);
            
            // Update replacement with new card ID
            CardReplacement completed = processing.withCompleted(replacementCardResponse.id());
            cardReplacementRepository.save(completed);
            
            // Block original card
            cardIssuanceService.blockCard(originalCard.customerId(), originalCard.id());
            
            // Log event
            cardEventService.logCardEvent(originalCard.id(), "REPLACED", replacement.id().toString());
            cardEventService.logCardEvent(replacementCardResponse.id(), "REPLACEMENT_CARD", replacement.id().toString());
            
        } catch (Exception e) {
            CardReplacement failed = replacement.withStatus(CardReplacementStatus.FAILED);
            cardReplacementRepository.save(failed);
            
            // Log event
            cardEventService.logCardEvent(replacement.originalCardId(), "REPLACEMENT_FAILED", e.getMessage());
        }
    }
}

