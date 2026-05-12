package com.yourorg.banking.card.service;

import com.yourorg.banking.card.model.VirtualCard;
import com.yourorg.banking.card.repo.VirtualCardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardPinServiceTest {

    @Mock
    private VirtualCardRepository virtualCardRepository;

    @Mock
    private CardEncryptionService cardEncryptionService;

    @InjectMocks
    private CardPinService cardPinService;

    @Test
    void changePin_validOldPin_succeeds() {
        UUID cardId = UUID.randomUUID();
        VirtualCard card = new VirtualCard();
        card.setId(cardId);
        card.setPinHash("hashed_old_pin");
        card.setPinAttempts(0);

        when(virtualCardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardEncryptionService.verifyPin("1234", "hashed_old_pin")).thenReturn(true);
        when(cardEncryptionService.hashPin("5678")).thenReturn("hashed_new_pin");

        assertDoesNotThrow(() -> cardPinService.changePin(cardId, "1234", "5678"));
        verify(virtualCardRepository).save(any(VirtualCard.class));
    }

    @Test
    void changePin_invalidOldPin_incrementsAttempts() {
        UUID cardId = UUID.randomUUID();
        VirtualCard card = new VirtualCard();
        card.setId(cardId);
        card.setPinHash("hashed_old_pin");
        card.setPinAttempts(0);

        when(virtualCardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardEncryptionService.verifyPin("wrong", "hashed_old_pin")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
            () -> cardPinService.changePin(cardId, "wrong", "5678"));
    }

    @Test
    void changePin_maxAttemptsReached_locksCard() {
        UUID cardId = UUID.randomUUID();
        VirtualCard card = new VirtualCard();
        card.setId(cardId);
        card.setPinHash("hashed_old_pin");
        card.setPinAttempts(4);

        when(virtualCardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardEncryptionService.verifyPin("wrong", "hashed_old_pin")).thenReturn(false);

        assertThrows(IllegalStateException.class,
            () -> cardPinService.changePin(cardId, "wrong", "5678"));
    }
}
