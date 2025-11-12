package com.yourorg.banking.card.service;

import com.yourorg.banking.card.model.*;
import com.yourorg.banking.card.repository.VirtualCardRepository;
import com.yourorg.banking.card.repository.CardPinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class CardIssuanceService {
    
    @Autowired
    private VirtualCardRepository virtualCardRepository;
    
    @Autowired
    private CardPinRepository cardPinRepository;
    
    @Autowired
    private CardEncryptionService cardEncryptionService;
    
    @Autowired
    private CardEventService cardEventService;
    
    @Value("${app.card.pan-length:16}")
    private int panLength;
    
    @Value("${app.card.cvv-length:3}")
    private int cvvLength;
    
    @Value("${app.card.expiry-years:5}")
    private int expiryYears;
    
    @Value("${app.card.max-cards-per-customer:5}")
    private int maxCardsPerCustomer;
    
    public CardResponse createVirtualCard(UUID customerId, CreateCardRequest request) {
        // Check card limit
        long existingCards = virtualCardRepository.countByCustomerIdAndStatus(customerId, CardStatus.ACTIVE);
        if (existingCards >= maxCardsPerCustomer) {
            throw new RuntimeException("Maximum number of cards reached for customer");
        }
        
        // Generate card details
        String cardNumber = generateCardNumber();
        String maskedCardNumber = maskCardNumber(cardNumber);
        String encryptedCardNumber = cardEncryptionService.encryptCardNumber(cardNumber);
        
        String cvv = generateCVV();
        String encryptedCvv = cardEncryptionService.encryptCvv(cvv);
        
        LocalDate expiryDate = LocalDate.now().plusYears(expiryYears);
        
        // Create virtual card
        VirtualCard card = new VirtualCard(
            UUID.randomUUID(),
            customerId,
            request.accountId(),
            encryptedCardNumber,
            maskedCardNumber,
            request.cardHolderName(),
            expiryDate.getMonthValue(),
            expiryDate.getYear(),
            encryptedCvv,
            request.cardType(),
            CardStatus.ACTIVE,
            request.isPrimary(),
            request.spendingLimit(),
            request.dailyLimit(),
            request.monthlyLimit(),
            request.currency(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now(), // activatedAt
            null, // blockedAt
            null, // cancelledAt
            expiryDate,
            null // lastUsedAt
        );
        
        card = virtualCardRepository.save(card);
        
        // Create PIN
        String pin = generatePIN();
        String pinHash = cardEncryptionService.hashPin(pin);
        
        CardPin cardPin = new CardPin(
            UUID.randomUUID(),
            card.id(),
            pinHash,
            0, // pinAttempts
            false, // isLocked
            null, // lockedUntil
            LocalDateTime.now(), // lastChangedAt
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        
        cardPinRepository.save(cardPin);
        
        // Log event
        cardEventService.logCardEvent(card.id(), "CREATED", null);
        
        return CardResponse.from(card);
    }
    
    public CardResponse getCard(UUID customerId, UUID cardId) {
        VirtualCard card = virtualCardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("Card not found"));
        
        if (!card.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        return CardResponse.from(card);
    }
    
    public java.util.List<CardResponse> getCustomerCards(UUID customerId) {
        return virtualCardRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
            .stream()
            .map(CardResponse::from)
            .toList();
    }
    
    public CardResponse updateCardLimits(UUID customerId, UUID cardId, UpdateCardLimitsRequest request) {
        VirtualCard card = virtualCardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("Card not found"));
        
        if (!card.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        VirtualCard updatedCard = card.withLimits(
            request.spendingLimit(),
            request.dailyLimit(),
            request.monthlyLimit()
        );
        
        updatedCard = virtualCardRepository.save(updatedCard);
        
        // Log event
        cardEventService.logCardEvent(cardId, "LIMIT_CHANGED", null);
        
        return CardResponse.from(updatedCard);
    }
    
    public CardResponse blockCard(UUID customerId, UUID cardId) {
        VirtualCard card = virtualCardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("Card not found"));
        
        if (!card.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        VirtualCard blockedCard = card.withBlocked();
        blockedCard = virtualCardRepository.save(blockedCard);
        
        // Log event
        cardEventService.logCardEvent(cardId, "BLOCKED", null);
        
        return CardResponse.from(blockedCard);
    }
    
    public CardResponse unblockCard(UUID customerId, UUID cardId) {
        VirtualCard card = virtualCardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("Card not found"));
        
        if (!card.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        VirtualCard unblockedCard = card.withStatus(CardStatus.ACTIVE);
        unblockedCard = virtualCardRepository.save(unblockedCard);
        
        // Log event
        cardEventService.logCardEvent(cardId, "UNBLOCKED", null);
        
        return CardResponse.from(unblockedCard);
    }
    
    public CardResponse cancelCard(UUID customerId, UUID cardId) {
        VirtualCard card = virtualCardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("Card not found"));
        
        if (!card.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        VirtualCard cancelledCard = card.withCancelled();
        cancelledCard = virtualCardRepository.save(cancelledCard);
        
        // Log event
        cardEventService.logCardEvent(cardId, "CANCELLED", null);
        
        return CardResponse.from(cancelledCard);
    }
    
    private String generateCardNumber() {
        SecureRandom random = new SecureRandom();
        StringBuilder cardNumber = new StringBuilder();
        
        // Start with a valid BIN (Bank Identification Number)
        cardNumber.append("4"); // Visa
        
        // Generate remaining digits
        for (int i = 1; i < panLength - 1; i++) {
            cardNumber.append(random.nextInt(10));
        }
        
        // Calculate and append check digit (Luhn algorithm)
        int checkDigit = calculateLuhnCheckDigit(cardNumber.toString());
        cardNumber.append(checkDigit);
        
        return cardNumber.toString();
    }
    
    private String maskCardNumber(String cardNumber) {
        if (cardNumber.length() < 8) {
            return cardNumber;
        }
        
        String firstFour = cardNumber.substring(0, 4);
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        String middle = "*".repeat(cardNumber.length() - 8);
        
        return firstFour + "-" + middle + "-" + middle + "-" + lastFour;
    }
    
    private String generateCVV() {
        SecureRandom random = new SecureRandom();
        return String.format("%03d", random.nextInt(1000));
    }
    
    private String generatePIN() {
        SecureRandom random = new SecureRandom();
        return String.format("%04d", random.nextInt(10000));
    }
    
    private int calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean alternate = false;
        
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return (10 - (sum % 10)) % 10;
    }
}

