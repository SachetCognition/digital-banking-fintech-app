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
public class LedgerServiceClient {
    
    private final RestTemplate restTemplate;
    private final String baseUrl;
    
    public LedgerServiceClient(RestTemplate restTemplate, @Value("${external.ledger-service.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }
    
    public void createReservation(UUID transferId, UUID accountId, BigDecimal amount, String currency, String authToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(authToken);
            
            Map<String, Object> request = Map.of(
                "reference", "RESERVATION_" + transferId.toString().substring(0, 8),
                "description", "Fund reservation for transfer " + transferId,
                "entries", List.of(
                    Map.of(
                        "accountId", accountId.toString(),
                        "type", "DEBIT",
                        "amount", amount,
                        "currency", currency,
                        "description", "Reserved amount for transfer",
                        "reference", transferId.toString()
                    )
                )
            );
            
            String url = baseUrl + "/api/v1/ledger/journals";
            RequestEntity<Map<String, Object>> req = RequestEntity.post(url)
                    .headers(headers)
                    .body(request);
            restTemplate.exchange(req, Void.class);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create reservation in ledger", e);
        }
    }
    
    public void postTransfer(UUID transferId, UUID payerAccountId, UUID payeeAccountId, 
                           BigDecimal amount, String currency, String description, String authToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(authToken);
            
            Map<String, Object> request = Map.of(
                "reference", "TRANSFER_" + transferId.toString().substring(0, 8),
                "description", description != null ? description : "P2P Transfer",
                "entries", List.of(
                    Map.of(
                        "accountId", payerAccountId.toString(),
                        "type", "CREDIT",
                        "amount", amount,
                        "currency", currency,
                        "description", "Transfer to " + payeeAccountId,
                        "reference", transferId.toString()
                    ),
                    Map.of(
                        "accountId", payeeAccountId.toString(),
                        "type", "DEBIT",
                        "amount", amount,
                        "currency", currency,
                        "description", "Transfer from " + payerAccountId,
                        "reference", transferId.toString()
                    )
                )
            );
            
            String url = baseUrl + "/api/v1/ledger/journals";
            RequestEntity<Map<String, Object>> req = RequestEntity.post(url)
                    .headers(headers)
                    .body(request);
            restTemplate.exchange(req, Void.class);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to post transfer in ledger", e);
        }
    }
    
    public void releaseReservation(UUID transferId, UUID accountId, String authToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(authToken);
            
            Map<String, Object> request = Map.of(
                "reference", "RELEASE_" + transferId.toString().substring(0, 8),
                "description", "Release reservation for transfer " + transferId,
                "entries", List.of(
                    Map.of(
                        "accountId", accountId.toString(),
                        "type", "CREDIT",
                        "amount", BigDecimal.ZERO, // This would need the actual reserved amount
                        "currency", "USD",
                        "description", "Released reservation for transfer",
                        "reference", transferId.toString()
                    )
                )
            );
            
            String url = baseUrl + "/api/v1/ledger/journals";
            RequestEntity<Map<String, Object>> req = RequestEntity.post(url)
                    .headers(headers)
                    .body(request);
            restTemplate.exchange(req, Void.class);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to release reservation in ledger", e);
        }
    }
}
