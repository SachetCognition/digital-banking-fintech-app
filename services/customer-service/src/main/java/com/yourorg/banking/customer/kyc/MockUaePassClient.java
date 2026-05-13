package com.yourorg.banking.customer.kyc;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class MockUaePassClient implements UaePassClient {

    @Override
    public String exchangeToken(String authorizationCode) {
        return "mock-access-token-" + authorizationCode;
    }

    @Override
    public UaePassIdentity getIdentityAssertions(String accessToken) {
        if (accessToken == null || accessToken.contains("expired")) {
            throw new UaePassTokenExpiredException("Token expired");
        }
        return new UaePassIdentity(
                "Ahmed Al-Rashid",
                "784-1990-1234567-1",
                "ARE",
                LocalDate.of(1990, 1, 15)
        );
    }
}
