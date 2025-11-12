package com.yourorg.banking.payments.service;

import com.yourorg.banking.payments.model.*;
import com.yourorg.banking.payments.repository.TransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BillPayService {
    
    private final TransferSagaService transferSagaService;
    private final BeneficiaryService beneficiaryService;
    private final TransferRepository transferRepository;
    
    public BillPayService(TransferSagaService transferSagaService,
                         BeneficiaryService beneficiaryService,
                         TransferRepository transferRepository) {
        this.transferSagaService = transferSagaService;
        this.beneficiaryService = beneficiaryService;
        this.transferRepository = transferRepository;
    }
    
    @Transactional
    public BillPayResponse payBill(UUID customerId, BillPayRequest request, String authToken) {
        // Validate beneficiary exists and belongs to customer
        Beneficiary beneficiary = beneficiaryService.getBeneficiaryForPayment(customerId, request.beneficiaryId())
                .orElseThrow(() -> new IllegalArgumentException("Beneficiary not found or not active"));
        
        // Validate currency match
        if (!beneficiary.getCurrency().equals(request.currency())) {
            throw new IllegalArgumentException("Currency mismatch between request and beneficiary");
        }
        
        // Create transfer request for internal transfer
        TransferRequest transferRequest = new TransferRequest(
            request.payerAccountId(),
            UUID.randomUUID(), // For now, we'll use a placeholder payee account ID
            request.amount(),
            request.currency(),
            createTransferDescription(beneficiary, request.description()),
            request.idempotencyKey()
        );
        
        // Execute transfer using existing saga
        TransferResponse transferResponse = transferSagaService.executeTransfer(transferRequest, authToken);
        
        // Create bill pay response
        return new BillPayResponse(
            transferResponse.transferId(),
            transferResponse.payerAccountId(),
            request.beneficiaryId(),
            beneficiary.getName(),
            beneficiary.getAccountNumber(),
            transferResponse.amount(),
            transferResponse.currency(),
            transferResponse.status(),
            transferResponse.description(),
            transferResponse.createdAt(),
            transferResponse.completedAt(),
            transferResponse.idempotencyKey()
        );
    }
    
    private String createTransferDescription(Beneficiary beneficiary, String description) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bill Pay to ").append(beneficiary.getName());
        sb.append(" (").append(beneficiary.getAccountNumber()).append(")");
        
        if (description != null && !description.trim().isEmpty()) {
            sb.append(" - ").append(description);
        }
        
        return sb.toString();
    }
}

