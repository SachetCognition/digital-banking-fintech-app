package com.yourorg.banking.card.service;

import com.yourorg.banking.card.model.CardEvent;
import com.yourorg.banking.card.repository.CardEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class CardEventService {
    
    @Autowired
    private CardEventRepository cardEventRepository;
    
    public void logCardEvent(UUID cardId, String eventType, Object eventData) {
        CardEvent event = new CardEvent(
            UUID.randomUUID(),
            cardId,
            eventType,
            eventData != null ? eventData.toString() : null,
            LocalDateTime.now()
        );
        
        cardEventRepository.save(event);
    }
}

