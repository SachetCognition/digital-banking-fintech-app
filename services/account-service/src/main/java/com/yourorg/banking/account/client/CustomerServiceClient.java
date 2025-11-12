package com.yourorg.banking.account.client;

import com.yourorg.banking.account.model.CustomerInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class CustomerServiceClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public CustomerServiceClient(RestTemplate restTemplate, @Value("${external.customer-service.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public CustomerInfo getCustomerInfo(UUID customerId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String url = baseUrl + "/api/v1/customers/" + customerId;
            RequestEntity<Void> req = RequestEntity.get(url)
                    .headers(headers)
                    .build();
            ResponseEntity<CustomerInfo> resp = restTemplate.exchange(req, CustomerInfo.class);
            return resp.getBody();
        } catch (Exception e) {
            // Log error but return null for now
            return null;
        }
    }
}

