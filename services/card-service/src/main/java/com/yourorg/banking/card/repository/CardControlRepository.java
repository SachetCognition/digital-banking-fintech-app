package com.yourorg.banking.card.repository;

import com.yourorg.banking.card.model.CardControl;
import com.yourorg.banking.card.model.CardControlType;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CardControlRepository extends CrudRepository<CardControl, UUID> {
    
    List<CardControl> findByCardIdOrderByCreatedAtDesc(UUID cardId);
    
    List<CardControl> findByCardIdAndIsActiveOrderByCreatedAtDesc(UUID cardId, boolean isActive);
    
    List<CardControl> findByCardIdAndControlTypeOrderByCreatedAtDesc(UUID cardId, CardControlType controlType);
    
    @Query("SELECT * FROM card_controls WHERE card_id = :cardId AND control_type = :controlType AND is_active = true AND (expires_at IS NULL OR expires_at > :currentTime)")
    List<CardControl> findActiveByCardIdAndType(
        @Param("cardId") UUID cardId,
        @Param("controlType") CardControlType controlType,
        @Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT COUNT(*) FROM card_controls WHERE card_id = :cardId AND is_active = true")
    long countActiveByCardId(@Param("cardId") UUID cardId);
}

