package com.yourorg.banking.card.service;

import com.yourorg.banking.card.model.*;
import com.yourorg.banking.card.repository.CardTransactionRepository;
import com.yourorg.banking.card.repository.VirtualCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CardTransactionService {
    
    @Autowired
    private CardTransactionRepository cardTransactionRepository;
    
    @Autowired
    private VirtualCardRepository virtualCardRepository;
    
    @Autowired
    private CardControlService cardControlService;
    
    @Autowired
    private CardEventService cardEventService;
    
    public CardTransactionResponse createTransaction(UUID cardId, CardTransactionRequest request) {
        // Verify card exists and is active
        VirtualCard card = virtualCardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("Card not found"));
        
        if (!card.isActive()) {
            throw new RuntimeException("Card is not active");
        }
        
        // Check card controls
        if (!cardControlService.isTransactionAllowed(cardId, request.merchantName(), 
                request.merchantCountry(), request.transactionType().name())) {
            throw new RuntimeException("Transaction blocked by card controls");
        }
        
        // Check spending limits
        if (!checkSpendingLimits(card, request.amount())) {
            throw new RuntimeException("Transaction exceeds spending limits");
        }
        
        // Create transaction
        CardTransaction transaction = new CardTransaction(
            UUID.randomUUID(),
            cardId,
            request.transactionId(),
            request.amount(),
            request.currency(),
            request.transactionType(),
            request.merchantName(),
            request.merchantCategoryCode(),
            request.merchantCountry(),
            request.transactionDate(),
            null, // settlementDate
            CardTransactionStatus.PENDING,
            request.description(),
            request.referenceNumber(),
            null, // authorizationCode
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        
        transaction = cardTransactionRepository.save(transaction);
        
        // Update card last used
        VirtualCard updatedCard = card.withLastUsed();
        virtualCardRepository.save(updatedCard);
        
        // Log event
        cardEventService.logCardEvent(cardId, "TRANSACTION_CREATED", transaction.transactionId());
        
        return CardTransactionResponse.from(transaction);
    }
    
    public CardTransactionResponse getTransaction(UUID customerId, UUID cardId, UUID transactionId) {
        // Verify card ownership
        VirtualCard card = virtualCardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("Card not found"));
        
        if (!card.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        CardTransaction transaction = cardTransactionRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        if (!transaction.cardId().equals(cardId)) {
            throw new RuntimeException("Transaction does not belong to this card");
        }
        
        return CardTransactionResponse.from(transaction);
    }
    
    public List<CardTransactionResponse> getCardTransactions(UUID customerId, UUID cardId) {
        // Verify card ownership
        VirtualCard card = virtualCardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("Card not found"));
        
        if (!card.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        return cardTransactionRepository.findByCardIdOrderByTransactionDateDesc(cardId)
            .stream()
            .map(CardTransactionResponse::from)
            .toList();
    }
    
    public List<CardTransactionResponse> getCardTransactionsByStatus(UUID customerId, UUID cardId, CardTransactionStatus status) {
        // Verify card ownership
        VirtualCard card = virtualCardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("Card not found"));
        
        if (!card.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        return cardTransactionRepository.findByCardIdAndStatusOrderByTransactionDateDesc(cardId, status)
            .stream()
            .map(CardTransactionResponse::from)
            .toList();
    }
    
    public List<CardTransactionResponse> getCardTransactionsByDateRange(UUID customerId, UUID cardId, 
            LocalDateTime fromDate, LocalDateTime toDate) {
        // Verify card ownership
        VirtualCard card = virtualCardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("Card not found"));
        
        if (!card.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        return cardTransactionRepository.findByCardIdAndDateRange(cardId, fromDate, toDate)
            .stream()
            .map(CardTransactionResponse::from)
            .toList();
    }
    
    public CardTransactionResponse approveTransaction(UUID transactionId, String authorizationCode) {
        CardTransaction transaction = cardTransactionRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        CardTransaction approvedTransaction = transaction
            .withStatus(CardTransactionStatus.APPROVED)
            .withSettled(LocalDateTime.now());
        
        approvedTransaction = cardTransactionRepository.save(approvedTransaction);
        
        // Log event
        cardEventService.logCardEvent(transaction.cardId(), "TRANSACTION_APPROVED", transactionId.toString());
        
        return CardTransactionResponse.from(approvedTransaction);
    }
    
    public CardTransactionResponse declineTransaction(UUID transactionId, String reason) {
        CardTransaction transaction = cardTransactionRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        CardTransaction declinedTransaction = transaction
            .withStatus(CardTransactionStatus.DECLINED);
        
        declinedTransaction = cardTransactionRepository.save(declinedTransaction);
        
        // Log event
        cardEventService.logCardEvent(transaction.cardId(), "TRANSACTION_DECLINED", reason);
        
        return CardTransactionResponse.from(declinedTransaction);
    }
    
    private boolean checkSpendingLimits(VirtualCard card, BigDecimal amount) {
        // Check per-transaction limit
        if (card.spendingLimit() != null && amount.compareTo(card.spendingLimit()) > 0) {
            return false;
        }
        
        // Check daily limit
        if (card.dailyLimit() != null) {
            Double todayAmount = cardTransactionRepository
                .sumAmountByCardIdAndStatusToday(card.id(), CardTransactionStatus.APPROVED);
            BigDecimal todayTotal = todayAmount != null ? BigDecimal.valueOf(todayAmount) : BigDecimal.ZERO;
            
            if (todayTotal.add(amount).compareTo(card.dailyLimit()) > 0) {
                return false;
            }
        }
        
        // Check monthly limit
        if (card.monthlyLimit() != null) {
            LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime monthEnd = LocalDateTime.now();
            
            Double monthAmount = cardTransactionRepository
                .sumAmountByCardIdAndStatusAndDateRange(card.id(), CardTransactionStatus.APPROVED, monthStart, monthEnd);
            BigDecimal monthTotal = monthAmount != null ? BigDecimal.valueOf(monthAmount) : BigDecimal.ZERO;
            
            if (monthTotal.add(amount).compareTo(card.monthlyLimit()) > 0) {
                return false;
            }
        }
        
        return true;
    }
}

