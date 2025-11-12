package com.yourorg.banking.card.repository;

import com.yourorg.banking.card.model.VirtualCard;
import com.yourorg.banking.card.model.CardStatus;
import com.yourorg.banking.card.model.CardType;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VirtualCardRepository extends CrudRepository<VirtualCard, UUID> {
    
    List<VirtualCard> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    
    List<VirtualCard> findByAccountIdOrderByCreatedAtDesc(UUID accountId);
    
    List<VirtualCard> findByCustomerIdAndCardStatusOrderByCreatedAtDesc(UUID customerId, CardStatus status);
    
    List<VirtualCard> findByCustomerIdAndCardTypeOrderByCreatedAtDesc(UUID customerId, CardType cardType);
    
    @Query("SELECT * FROM virtual_cards WHERE customer_id = :customerId AND is_primary = true")
    Optional<VirtualCard> findPrimaryCardByCustomerId(@Param("customerId") UUID customerId);
    
    @Query("SELECT * FROM virtual_cards WHERE customer_id = :customerId AND card_status = :status AND expiry_date > :currentDate ORDER BY created_at DESC")
    List<VirtualCard> findActiveCardsByCustomerId(
        @Param("customerId") UUID customerId,
        @Param("status") CardStatus status,
        @Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT * FROM virtual_cards WHERE expiry_date <= :expiryDate AND card_status = 'ACTIVE'")
    List<VirtualCard> findExpiredCards(@Param("expiryDate") LocalDate expiryDate);
    
    @Query("SELECT COUNT(*) FROM virtual_cards WHERE customer_id = :customerId AND card_status = :status")
    long countByCustomerIdAndStatus(@Param("customerId") UUID customerId, @Param("status") CardStatus status);
    
    @Query("SELECT * FROM virtual_cards WHERE card_number_encrypted = :encryptedCardNumber")
    Optional<VirtualCard> findByEncryptedCardNumber(@Param("encryptedCardNumber") String encryptedCardNumber);
}

