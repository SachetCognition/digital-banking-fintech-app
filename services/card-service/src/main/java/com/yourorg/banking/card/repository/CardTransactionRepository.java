package com.yourorg.banking.card.repository;

import com.yourorg.banking.card.model.CardTransaction;
import com.yourorg.banking.card.model.CardTransactionStatus;
import com.yourorg.banking.card.model.CardTransactionType;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardTransactionRepository extends CrudRepository<CardTransaction, UUID> {
    
    List<CardTransaction> findByCardIdOrderByTransactionDateDesc(UUID cardId);
    
    List<CardTransaction> findByCardIdAndStatusOrderByTransactionDateDesc(UUID cardId, CardTransactionStatus status);
    
    List<CardTransaction> findByCardIdAndTransactionTypeOrderByTransactionDateDesc(UUID cardId, CardTransactionType transactionType);
    
    @Query("SELECT * FROM card_transactions WHERE card_id = :cardId AND transaction_date >= :fromDate AND transaction_date <= :toDate ORDER BY transaction_date DESC")
    List<CardTransaction> findByCardIdAndDateRange(
        @Param("cardId") UUID cardId,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate);
    
    @Query("SELECT * FROM card_transactions WHERE card_id = :cardId AND merchant_name ILIKE :merchantName ORDER BY transaction_date DESC")
    List<CardTransaction> findByCardIdAndMerchantName(
        @Param("cardId") UUID cardId,
        @Param("merchantName") String merchantName);
    
    @Query("SELECT * FROM card_transactions WHERE transaction_id = :transactionId")
    Optional<CardTransaction> findByTransactionId(@Param("transactionId") String transactionId);
    
    @Query("SELECT SUM(amount) FROM card_transactions WHERE card_id = :cardId AND status = :status AND DATE(transaction_date) = CURRENT_DATE")
    Double sumAmountByCardIdAndStatusToday(
        @Param("cardId") UUID cardId,
        @Param("status") CardTransactionStatus status);
    
    @Query("SELECT SUM(amount) FROM card_transactions WHERE card_id = :cardId AND status = :status AND transaction_date >= :fromDate AND transaction_date <= :toDate")
    Double sumAmountByCardIdAndStatusAndDateRange(
        @Param("cardId") UUID cardId,
        @Param("status") CardTransactionStatus status,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate);
}

