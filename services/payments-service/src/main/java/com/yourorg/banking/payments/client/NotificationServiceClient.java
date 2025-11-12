package com.yourorg.banking.payments.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
public class NotificationServiceClient {
    
    private final RestTemplate restTemplate;
    private final String baseUrl;
    
    public NotificationServiceClient(RestTemplate restTemplate, @Value("${external.notification-service.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }
    
    public void sendTransferNotification(UUID transferId, UUID customerId, String type, 
                                       BigDecimal amount, String currency, String description, String authToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(authToken);
            
            Map<String, Object> request = Map.of(
                "transferId", transferId.toString(),
                "customerId", customerId.toString(),
                "type", type, // "TRANSFER_SENT" or "TRANSFER_RECEIVED"
                "amount", amount,
                "currency", currency,
                "description", description != null ? description : "",
                "template", "transfer_notification"
            );
            
            // This would be a real API call to notification-service
            // For now, we'll just log it
            System.out.println("Sending transfer notification: " + request);
            
        } catch (Exception e) {
            // Don't fail the transfer if notification fails
            System.err.println("Failed to send notification: " + e.getMessage());
        }
    }
}

