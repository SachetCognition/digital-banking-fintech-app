package com.yourorg.banking.payments.service;

import com.yourorg.banking.payments.client.AccountServiceClient;
import com.yourorg.banking.payments.client.NotificationServiceClient;
import com.yourorg.banking.payments.model.AccountInfo;
import com.yourorg.banking.payments.model.Transfer;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NotificationService {
    
    private final NotificationServiceClient notificationServiceClient;
    private final AccountServiceClient accountServiceClient;
    
    public NotificationService(NotificationServiceClient notificationServiceClient,
                             AccountServiceClient accountServiceClient) {
        this.notificationServiceClient = notificationServiceClient;
        this.accountServiceClient = accountServiceClient;
    }
    
    public void sendTransferNotifications(Transfer transfer, String authToken) {
        try {
            // Get customer IDs for both accounts
            AccountInfo payerAccount = accountServiceClient.getAccountInfo(transfer.getPayerAccountId(), authToken);
            AccountInfo payeeAccount = accountServiceClient.getAccountInfo(transfer.getPayeeAccountId(), authToken);
            
            // Send notification to payer
            notificationServiceClient.sendTransferNotification(
                transfer.getId(),
                payerAccount.customerId(),
                "TRANSFER_SENT",
                transfer.getAmount(),
                transfer.getCurrency(),
                transfer.getDescription(),
                authToken
            );
            
            // Send notification to payee
            notificationServiceClient.sendTransferNotification(
                transfer.getId(),
                payeeAccount.customerId(),
                "TRANSFER_RECEIVED",
                transfer.getAmount(),
                transfer.getCurrency(),
                transfer.getDescription(),
                authToken
            );
            
        } catch (Exception e) {
            // Don't fail the transfer if notification fails
            System.err.println("Failed to send transfer notifications: " + e.getMessage());
        }
    }
}

