package com.yourorg.banking.card.repository;

import com.yourorg.banking.card.model.CardReplacement;
import com.yourorg.banking.card.model.CardReplacementStatus;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CardReplacementRepository extends CrudRepository<CardReplacement, UUID> {
    
    List<CardReplacement> findByOriginalCardIdOrderByCreatedAtDesc(UUID originalCardId);
    
    List<CardReplacement> findByReplacementCardIdOrderByCreatedAtDesc(UUID replacementCardId);
    
    List<CardReplacement> findByOriginalCardIdAndReplacementStatusOrderByCreatedAtDesc(
        UUID originalCardId, CardReplacementStatus status);
    
    @Query("SELECT * FROM card_replacements WHERE original_card_id IN (SELECT id FROM virtual_cards WHERE customer_id = :customerId) ORDER BY created_at DESC")
    List<CardReplacement> findByCustomerId(@Param("customerId") UUID customerId);
    
    @Query("SELECT * FROM card_replacements WHERE replacement_status = :status ORDER BY created_at ASC")
    List<CardReplacement> findByStatusOrderByCreatedAtAsc(CardReplacementStatus status);
}

