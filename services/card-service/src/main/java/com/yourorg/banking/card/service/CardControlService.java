package com.yourorg.banking.card.service;

import com.yourorg.banking.card.model.*;
import com.yourorg.banking.card.repository.CardControlRepository;
import com.yourorg.banking.card.repository.VirtualCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CardControlService {
    
    @Autowired
    private CardControlRepository cardControlRepository;
    
    @Autowired
    private VirtualCardRepository virtualCardRepository;
    
    @Autowired
    private CardEventService cardEventService;
    
    public CardControlResponse createCardControl(UUID customerId, UUID cardId, CardControlRequest request) {
        // Verify card ownership
        VirtualCard card = virtualCardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("Card not found"));
        
        if (!card.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        // Create card control
        CardControl control = new CardControl(
            UUID.randomUUID(),
            cardId,
            request.controlType(),
            request.controlValue(),
            true, // isActive
            LocalDateTime.now(),
            LocalDateTime.now(),
            request.expiresAt()
        );
        
        control = cardControlRepository.save(control);
        
        // Log event
        cardEventService.logCardEvent(cardId, "CONTROL_ADDED", control.controlType().name());
        
        return CardControlResponse.from(control);
    }
    
    public List<CardControlResponse> getCardControls(UUID customerId, UUID cardId) {
        // Verify card ownership
        VirtualCard card = virtualCardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("Card not found"));
        
        if (!card.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        return cardControlRepository.findByCardIdOrderByCreatedAtDesc(cardId)
            .stream()
            .map(CardControlResponse::from)
            .toList();
    }
    
    public CardControlResponse updateCardControl(UUID customerId, UUID cardId, UUID controlId, CardControlRequest request) {
        // Verify card ownership
        VirtualCard card = virtualCardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("Card not found"));
        
        if (!card.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        CardControl control = cardControlRepository.findById(controlId)
            .orElseThrow(() -> new RuntimeException("Card control not found"));
        
        if (!control.cardId().equals(cardId)) {
            throw new RuntimeException("Card control does not belong to this card");
        }
        
        // Update control
        CardControl updatedControl = new CardControl(
            control.id(),
            control.cardId(),
            request.controlType(),
            request.controlValue(),
            control.isActive(),
            control.createdAt(),
            LocalDateTime.now(),
            request.expiresAt()
        );
        
        updatedControl = cardControlRepository.save(updatedControl);
        
        // Log event
        cardEventService.logCardEvent(cardId, "CONTROL_UPDATED", control.controlType().name());
        
        return CardControlResponse.from(updatedControl);
    }
    
    public CardControlResponse toggleCardControl(UUID customerId, UUID cardId, UUID controlId) {
        // Verify card ownership
        VirtualCard card = virtualCardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("Card not found"));
        
        if (!card.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        CardControl control = cardControlRepository.findById(controlId)
            .orElseThrow(() -> new RuntimeException("Card control not found"));
        
        if (!control.cardId().equals(cardId)) {
            throw new RuntimeException("Card control does not belong to this card");
        }
        
        // Toggle control
        CardControl updatedControl = control.withActive(!control.isActive());
        updatedControl = cardControlRepository.save(updatedControl);
        
        // Log event
        cardEventService.logCardEvent(cardId, "CONTROL_TOGGLED", control.controlType().name());
        
        return CardControlResponse.from(updatedControl);
    }
    
    public void deleteCardControl(UUID customerId, UUID cardId, UUID controlId) {
        // Verify card ownership
        VirtualCard card = virtualCardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("Card not found"));
        
        if (!card.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        CardControl control = cardControlRepository.findById(controlId)
            .orElseThrow(() -> new RuntimeException("Card control not found"));
        
        if (!control.cardId().equals(cardId)) {
            throw new RuntimeException("Card control does not belong to this card");
        }
        
        cardControlRepository.deleteById(controlId);
        
        // Log event
        cardEventService.logCardEvent(cardId, "CONTROL_REMOVED", control.controlType().name());
    }
    
    public boolean isTransactionAllowed(UUID cardId, String merchantName, String merchantCountry, String transactionType) {
        List<CardControl> activeControls = cardControlRepository
            .findByCardIdAndIsActiveOrderByCreatedAtDesc(cardId, true);
        
        LocalDateTime now = LocalDateTime.now();
        
        for (CardControl control : activeControls) {
            if (control.isExpired()) {
                continue;
            }
            
            switch (control.controlType()) {
                case MERCHANT_BLOCK:
                    if (merchantName != null && merchantName.toLowerCase().contains(control.controlValue().toLowerCase())) {
                        return false;
                    }
                    break;
                case COUNTRY_BLOCK:
                    if (merchantCountry != null && merchantCountry.equalsIgnoreCase(control.controlValue())) {
                        return false;
                    }
                    break;
                case ONLINE_BLOCK:
                    if ("ONLINE".equalsIgnoreCase(transactionType)) {
                        return false;
                    }
                    break;
                case ATM_BLOCK:
                    if ("ATM".equalsIgnoreCase(transactionType)) {
                        return false;
                    }
                    break;
                case CASH_ADVANCE_BLOCK:
                    if ("CASH_ADVANCE".equalsIgnoreCase(transactionType)) {
                        return false;
                    }
                    break;
                case INTERNATIONAL_BLOCK:
                    if (merchantCountry != null && !"US".equalsIgnoreCase(merchantCountry)) {
                        return false;
                    }
                    break;
            }
        }
        
        return true;
    }
}

