package com.yourorg.banking.payments.client;

import com.yourorg.banking.payments.model.AccountInfo;
import com.yourorg.banking.payments.model.AccountLimitInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
public class AccountServiceClient {
    
    private final RestTemplate restTemplate;
    private final String baseUrl;
    
    public AccountServiceClient(RestTemplate restTemplate, @Value("${external.account-service.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }
    
    public AccountInfo getAccountInfo(UUID accountId, String authToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(authToken);

            String url = baseUrl + "/api/v1/accounts/" + accountId;
            RequestEntity<Void> req = RequestEntity.get(url)
                    .headers(headers)
                    .build();
            ResponseEntity<AccountInfo> resp = restTemplate.exchange(req, AccountInfo.class);
            return resp.getBody();
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to fetch account info for account: " + accountId, e);
        }
    }
    
    public boolean validateAccount(UUID accountId, String authToken) {
        try {
            AccountInfo accountInfo = getAccountInfo(accountId, authToken);
            return accountInfo.active();
        } catch (Exception e) {
            return false;
        }
    }

    public AccountLimitInfo getAccountLimits(UUID accountId, String authToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(authToken);

            String url = baseUrl + "/api/v1/accounts/" + accountId + "/limits";
            RequestEntity<Void> req = RequestEntity.get(url)
                    .headers(headers)
                    .build();
            ResponseEntity<AccountLimitInfo> resp = restTemplate.exchange(req, AccountLimitInfo.class);
            return resp.getBody();
        } catch (Exception e) {
            // Fallback: no limits found; caller can apply defaults
            return null;
        }
    }
}
