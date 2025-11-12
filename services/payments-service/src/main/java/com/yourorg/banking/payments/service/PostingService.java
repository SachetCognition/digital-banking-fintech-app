package com.yourorg.banking.payments.service;

import com.yourorg.banking.payments.client.LedgerServiceClient;
import com.yourorg.banking.payments.model.Transfer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostingService {
    
    private final LedgerServiceClient ledgerServiceClient;
    
    public PostingService(LedgerServiceClient ledgerServiceClient) {
        this.ledgerServiceClient = ledgerServiceClient;
    }
    
    @Transactional
    public void postTransfer(Transfer transfer, String authToken) {
        try {
            // Post the actual transfer in the ledger service
            ledgerServiceClient.postTransfer(
                transfer.getId(),
                transfer.getPayerAccountId(),
                transfer.getPayeeAccountId(),
                transfer.getAmount(),
                transfer.getCurrency(),
                transfer.getDescription(),
                authToken
            );
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to post transfer", e);
        }
    }
}

