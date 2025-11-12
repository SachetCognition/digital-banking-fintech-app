package com.yourorg.banking.payments.service;

import com.yourorg.banking.payments.model.Transfer;
import com.yourorg.banking.payments.model.TransferStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class TransferEventPublisher {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    @Autowired
    public TransferEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    public void publishTransferCreated(Transfer transfer) {
        Map<String, Object> event = Map.of(
            "eventType", "TransferCreated",
            "transferId", transfer.getId().toString(),
            "payerAccountId", transfer.getPayerAccountId().toString(),
            "payeeAccountId", transfer.getPayeeAccountId().toString(),
            "amount", transfer.getAmount(),
            "currency", transfer.getCurrency(),
            "status", transfer.getStatus().name(),
            "createdAt", transfer.getCreatedAt().toString()
        );
        
        kafkaTemplate.send("transfer.events", transfer.getId().toString(), event.toString());
    }
    
    public void publishTransferStatusChanged(Transfer transfer) {
        Map<String, Object> event = Map.of(
            "eventType", "TransferStatusChanged",
            "transferId", transfer.getId().toString(),
            "status", transfer.getStatus().name(),
            "currentStep", transfer.getCurrentStep().name(),
            "updatedAt", transfer.getLastUpdatedAt().toString()
        );
        
        kafkaTemplate.send("transfer.events", transfer.getId().toString(), event.toString());
    }
    
    public void publishTransferCompleted(Transfer transfer) {
        Map<String, Object> event = Map.of(
            "eventType", "TransferCompleted",
            "transferId", transfer.getId().toString(),
            "payerAccountId", transfer.getPayerAccountId().toString(),
            "payeeAccountId", transfer.getPayeeAccountId().toString(),
            "amount", transfer.getAmount(),
            "currency", transfer.getCurrency(),
            "completedAt", transfer.getCompletedAt().toString()
        );
        
        kafkaTemplate.send("transfer.events", transfer.getId().toString(), event.toString());
    }
    
    public void publishTransferFailed(Transfer transfer) {
        Map<String, Object> event = Map.of(
            "eventType", "TransferFailed",
            "transferId", transfer.getId().toString(),
            "payerAccountId", transfer.getPayerAccountId().toString(),
            "payeeAccountId", transfer.getPayeeAccountId().toString(),
            "amount", transfer.getAmount(),
            "currency", transfer.getCurrency(),
            "failureReason", transfer.getFailureReason() != null ? transfer.getFailureReason() : "Unknown error",
            "failedAt", transfer.getCompletedAt().toString()
        );
        
        kafkaTemplate.send("transfer.events", transfer.getId().toString(), event.toString());
    }
}

