package com.yourorg.banking.card.service;

import com.yourorg.banking.card.model.*;
import com.yourorg.banking.card.repository.CardPinRepository;
import com.yourorg.banking.card.repository.VirtualCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class CardPinService {
    
    @Autowired
    private CardPinRepository cardPinRepository;
    
    @Autowired
    private VirtualCardRepository virtualCardRepository;
    
    @Autowired
    private CardEncryptionService cardEncryptionService;
    
    @Autowired
    private CardEventService cardEventService;
    
    public void changePin(UUID customerId, UUID cardId, ChangePinRequest request) {
        // Verify card ownership
        VirtualCard card = virtualCardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("Card not found"));
        
        if (!card.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        // Validate PINs match
        if (!request.isPinMatch()) {
            throw new RuntimeException("New PIN and confirm PIN do not match");
        }
        
        // Get current PIN
        CardPin currentPin = cardPinRepository.findByCardId(cardId)
            .orElseThrow(() -> new RuntimeException("PIN not found for this card"));
        
        // Check if PIN is locked
        if (currentPin.isCurrentlyLocked()) {
            throw new RuntimeException("PIN is locked. Please try again later.");
        }
        
        // Verify current PIN
        if (!cardEncryptionService.verifyPin(request.currentPin(), currentPin.pinHash())) {
            // Increment failed attempts
            CardPin updatedPin = currentPin.withFailedAttempt();
            cardPinRepository.save(updatedPin);
            
            throw new RuntimeException("Current PIN is incorrect");
        }
        
        // Hash new PIN
        String newPinHash = cardEncryptionService.hashPin(request.newPin());
        
        // Update PIN
        CardPin updatedPin = currentPin.withPinChanged(newPinHash);
        updatedPin = cardPinRepository.save(updatedPin);
        
        // Log event
        cardEventService.logCardEvent(cardId, "PIN_CHANGED", null);
    }
    
    public void resetPin(UUID customerId, UUID cardId) {
        // Verify card ownership
        VirtualCard card = virtualCardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("Card not found"));
        
        if (!card.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        // Get current PIN
        CardPin currentPin = cardPinRepository.findByCardId(cardId)
            .orElseThrow(() -> new RuntimeException("PIN not found for this card"));
        
        // Generate new PIN
        String newPin = generateNewPin();
        String newPinHash = cardEncryptionService.hashPin(newPin);
        
        // Update PIN
        CardPin updatedPin = currentPin.withPinChanged(newPinHash);
        updatedPin = cardPinRepository.save(updatedPin);
        
        // Log event
        cardEventService.logCardEvent(cardId, "PIN_RESET", null);
        
        // In a real system, you would send the new PIN to the customer via secure channel
        // For now, we'll just log it (in production, this should never be logged)
        System.out.println("New PIN for card " + cardId + ": " + newPin);
    }
    
    public boolean verifyPin(UUID cardId, String pin) {
        CardPin cardPin = cardPinRepository.findByCardId(cardId)
            .orElseThrow(() -> new RuntimeException("PIN not found for this card"));
        
        // Check if PIN is locked
        if (cardPin.isCurrentlyLocked()) {
            return false;
        }
        
        // Verify PIN
        boolean isValid = cardEncryptionService.verifyPin(pin, cardPin.pinHash());
        
        if (isValid) {
            // Reset failed attempts on successful verification
            CardPin resetPin = cardPin.withResetAttempts();
            cardPinRepository.save(resetPin);
        } else {
            // Increment failed attempts
            CardPin failedPin = cardPin.withFailedAttempt();
            cardPinRepository.save(failedPin);
        }
        
        return isValid;
    }
    
    public void unlockPin(UUID customerId, UUID cardId) {
        // Verify card ownership
        VirtualCard card = virtualCardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("Card not found"));
        
        if (!card.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        // Get current PIN
        CardPin currentPin = cardPinRepository.findByCardId(cardId)
            .orElseThrow(() -> new RuntimeException("PIN not found for this card"));
        
        // Unlock PIN
        CardPin unlockedPin = currentPin.withResetAttempts();
        unlockedPin = cardPinRepository.save(unlockedPin);
        
        // Log event
        cardEventService.logCardEvent(cardId, "PIN_UNLOCKED", null);
    }
    
    public PinStatusResponse getPinStatus(UUID customerId, UUID cardId) {
        // Verify card ownership
        VirtualCard card = virtualCardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("Card not found"));
        
        if (!card.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        CardPin cardPin = cardPinRepository.findByCardId(cardId)
            .orElseThrow(() -> new RuntimeException("PIN not found for this card"));
        
        return new PinStatusResponse(
            cardPin.isLocked(),
            cardPin.lockedUntil(),
            cardPin.pinAttempts(),
            cardPin.lastChangedAt()
        );
    }
    
    private String generateNewPin() {
        java.security.SecureRandom random = new java.security.SecureRandom();
        return String.format("%04d", random.nextInt(10000));
    }
}

