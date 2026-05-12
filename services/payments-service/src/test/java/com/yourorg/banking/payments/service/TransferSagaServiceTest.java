package com.yourorg.banking.payments.service;

import com.yourorg.banking.payments.model.*;
import com.yourorg.banking.payments.repository.TransferRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferSagaServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private TransferSagaOrchestrator sagaOrchestrator;

    @InjectMocks
    private TransferService transferService;

    @Test
    void initiateTransfer_validRequest_succeeds() {
        UUID payerAccountId = UUID.randomUUID();
        UUID payeeAccountId = UUID.randomUUID();

        TransferRequest request = new TransferRequest(
            payerAccountId, payeeAccountId, new BigDecimal("250.00"),
            "USD", "Test transfer", UUID.randomUUID().toString()
        );

        when(transferRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transferRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());

        Transfer result = transferService.initiateTransfer(request);

        assertNotNull(result);
        assertEquals(TransferStatus.PENDING, result.getStatus());
        verify(sagaOrchestrator).executeSaga(any());
    }

    @Test
    void initiateTransfer_duplicateIdempotencyKey_returnsExisting() {
        String idempotencyKey = UUID.randomUUID().toString();
        Transfer existing = new Transfer();
        existing.setId(UUID.randomUUID());
        existing.setStatus(TransferStatus.COMPLETED);
        existing.setIdempotencyKey(idempotencyKey);

        when(transferRepository.findByIdempotencyKey(idempotencyKey))
            .thenReturn(Optional.of(existing));

        TransferRequest request = new TransferRequest(
            UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("100.00"),
            "USD", "Duplicate", idempotencyKey
        );

        Transfer result = transferService.initiateTransfer(request);

        assertEquals(existing.getId(), result.getId());
        verify(sagaOrchestrator, never()).executeSaga(any());
    }
}
