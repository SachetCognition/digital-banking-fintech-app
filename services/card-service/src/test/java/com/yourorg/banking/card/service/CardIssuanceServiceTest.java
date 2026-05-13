package com.yourorg.banking.card.service;

import com.yourorg.banking.card.model.*;
import com.yourorg.banking.card.repo.VirtualCardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardIssuanceServiceTest {

    @Mock
    private VirtualCardRepository virtualCardRepository;

    @Mock
    private CardEncryptionService cardEncryptionService;

    @InjectMocks
    private CardIssuanceService cardIssuanceService;

    @Test
    void createVirtualCard_succeeds() {
        UUID customerId = UUID.randomUUID();
        when(virtualCardRepository.countByCustomerIdAndStatus(customerId, "ACTIVE")).thenReturn(2);
        when(cardEncryptionService.encryptCardNumber(any())).thenReturn("encrypted_number");
        when(virtualCardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        VirtualCard card = cardIssuanceService.createVirtualCard(customerId, "DEBIT");

        assertNotNull(card);
        assertEquals(customerId, card.getCustomerId());
        verify(virtualCardRepository).save(any(VirtualCard.class));
    }

    @Test
    void createVirtualCard_maxLimitReached_throwsException() {
        UUID customerId = UUID.randomUUID();
        when(virtualCardRepository.countByCustomerIdAndStatus(customerId, "ACTIVE")).thenReturn(10);

        assertThrows(IllegalStateException.class,
            () -> cardIssuanceService.createVirtualCard(customerId, "DEBIT"));
    }
}
