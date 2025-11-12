package com.yourorg.banking.payments;

import com.yourorg.banking.payments.model.TransferRequest;
import com.yourorg.banking.payments.model.TransferResponse;
import com.yourorg.banking.payments.service.TransferSagaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class P2PTransferIntegrationTest {
    
    @Autowired
    private TransferSagaService transferSagaService;
    
    @Test
    public void testSuccessfulP2PTransfer() {
        // Given
        UUID payerAccountId = UUID.randomUUID();
        UUID payeeAccountId = UUID.randomUUID();
        String idempotencyKey = "test-transfer-" + System.currentTimeMillis();
        
        TransferRequest request = new TransferRequest(
            payerAccountId,
            payeeAccountId,
            new BigDecimal("100.00"),
            "USD",
            "Test P2P transfer",
            idempotencyKey
        );
        
        // When
        TransferResponse response = transferSagaService.executeTransfer(request, "mock-token");
        
        // Then
        assertNotNull(response);
        assertEquals(payerAccountId, response.payerAccountId());
        assertEquals(payeeAccountId, response.payeeAccountId());
        assertEquals(new BigDecimal("100.00"), response.amount());
        assertEquals("USD", response.currency());
        assertEquals("COMPLETED", response.status());
        assertEquals(idempotencyKey, response.idempotencyKey());
        assertNotNull(response.transferId());
        assertNotNull(response.createdAt());
        assertNotNull(response.completedAt());
    }
    
    @Test
    public void testIdempotency() {
        // Given
        UUID payerAccountId = UUID.randomUUID();
        UUID payeeAccountId = UUID.randomUUID();
        String idempotencyKey = "idempotency-test-" + System.currentTimeMillis();
        
        TransferRequest request = new TransferRequest(
            payerAccountId,
            payeeAccountId,
            new BigDecimal("50.00"),
            "USD",
            "Idempotency test",
            idempotencyKey
        );
        
        // When - Execute same transfer twice
        TransferResponse response1 = transferSagaService.executeTransfer(request, "mock-token");
        TransferResponse response2 = transferSagaService.executeTransfer(request, "mock-token");
        
        // Then - Should get same response
        assertEquals(response1.transferId(), response2.transferId());
        assertEquals(response1.amount(), response2.amount());
        assertEquals(response1.status(), response2.status());
    }
    
    @Test
    public void testTransferValidation() {
        // Given - Invalid transfer (same payer and payee)
        UUID accountId = UUID.randomUUID();
        String idempotencyKey = "validation-test-" + System.currentTimeMillis();
        
        TransferRequest request = new TransferRequest(
            accountId,
            accountId, // Same as payer - should fail
            new BigDecimal("100.00"),
            "USD",
            "Invalid transfer",
            idempotencyKey
        );
        
        // When & Then - Should throw exception
        assertThrows(RuntimeException.class, () -> {
            transferSagaService.executeTransfer(request, "mock-token");
        });
    }
}

