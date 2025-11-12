package com.yourorg.banking.card.repository;

import com.yourorg.banking.card.model.CardPin;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardPinRepository extends CrudRepository<CardPin, UUID> {
    
    Optional<CardPin> findByCardId(UUID cardId);
    
    @Query("SELECT * FROM card_pins WHERE card_id = :cardId AND is_locked = false")
    Optional<CardPin> findUnlockedByCardId(@Param("cardId") UUID cardId);
    
    @Query("SELECT * FROM card_pins WHERE card_id = :cardId AND (is_locked = false OR locked_until < :currentTime)")
    Optional<CardPin> findActiveByCardId(@Param("cardId") UUID cardId, @Param("currentTime") LocalDateTime currentTime);
}

