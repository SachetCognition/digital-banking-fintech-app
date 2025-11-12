package com.yourorg.banking.account.client;

import com.yourorg.banking.account.model.KycStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class KycServiceClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public KycServiceClient(RestTemplate restTemplate, @Value("${external.customer-service.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public KycStatus getKycStatus(UUID customerId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String url = baseUrl + "/api/v1/kyc/status/" + customerId;
            RequestEntity<Void> req = RequestEntity.get(url)
                    .headers(headers)
                    .build();
            ResponseEntity<KycStatus> resp = restTemplate.exchange(req, KycStatus.class);
            return resp.getBody();
        } catch (Exception e) {
            // Log error but return NOT_REQUIRED for now
            return KycStatus.NOT_REQUIRED;
        }
    }
}

