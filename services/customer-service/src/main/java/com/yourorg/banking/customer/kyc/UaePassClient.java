package com.yourorg.banking.customer.kyc;

public interface UaePassClient {

    String exchangeToken(String authorizationCode);

    UaePassIdentity getIdentityAssertions(String accessToken);
}
