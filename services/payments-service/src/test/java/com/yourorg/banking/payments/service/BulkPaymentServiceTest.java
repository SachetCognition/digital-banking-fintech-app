package com.yourorg.banking.payments.service;

import com.yourorg.banking.payments.model.*;
import com.yourorg.banking.payments.repository.BulkPaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulkPaymentServiceTest {

    @Mock
    private BulkPaymentRepository bulkPaymentRepository;

    @Mock
    private TransferService transferService;

    @InjectMocks
    private BulkPaymentService bulkPaymentService;

    @Test
    void processBulkPayment_allSuccess() {
        UUID sourceAccountId = UUID.randomUUID();
        List<BulkPaymentItem> items = List.of(
            new BulkPaymentItem(UUID.randomUUID(), new BigDecimal("100.00"), "USD", "Payment 1"),
            new BulkPaymentItem(UUID.randomUUID(), new BigDecimal("200.00"), "USD", "Payment 2")
        );

        Transfer successfulTransfer = new Transfer();
        successfulTransfer.setStatus(TransferStatus.COMPLETED);
        when(transferService.initiateTransfer(any())).thenReturn(successfulTransfer);
        when(bulkPaymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BulkPaymentResult result = bulkPaymentService.processBulkPayment(sourceAccountId, items);

        assertNotNull(result);
        assertEquals(2, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
    }

    @Test
    void processBulkPayment_partialFailures() {
        UUID sourceAccountId = UUID.randomUUID();
        List<BulkPaymentItem> items = List.of(
            new BulkPaymentItem(UUID.randomUUID(), new BigDecimal("100.00"), "USD", "Payment 1"),
            new BulkPaymentItem(UUID.randomUUID(), new BigDecimal("200.00"), "USD", "Payment 2")
        );

        Transfer successfulTransfer = new Transfer();
        successfulTransfer.setStatus(TransferStatus.COMPLETED);
        when(transferService.initiateTransfer(any()))
            .thenReturn(successfulTransfer)
            .thenThrow(new IllegalStateException("Insufficient funds"));
        when(bulkPaymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BulkPaymentResult result = bulkPaymentService.processBulkPayment(sourceAccountId, items);

        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
    }
}
