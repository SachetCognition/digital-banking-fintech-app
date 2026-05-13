package com.yourorg.banking.customer.kyc;

import org.springframework.stereotype.Service;

@Service
public class UaePassIntegrationService {

    private final UaePassClient uaePassClient;

    public UaePassIntegrationService(UaePassClient uaePassClient) {
        this.uaePassClient = uaePassClient;
    }

    public String exchangeToken(String authorizationCode) {
        if (authorizationCode == null || authorizationCode.isBlank()) {
            throw new IllegalArgumentException("Authorization code is required");
        }
        return uaePassClient.exchangeToken(authorizationCode);
    }

    public UaePassIdentity getIdentityAssertions(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("Access token is required");
        }
        try {
            return uaePassClient.getIdentityAssertions(accessToken);
        } catch (UaePassTokenExpiredException e) {
            throw e;
        } catch (Exception e) {
            throw new UaePassTokenExpiredException("Failed to get identity assertions", e);
        }
    }
}
