package com.yourorg.banking.customer.client;

import com.yourorg.banking.account.model.CustomerInfo;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class CustomerServiceClient {
    public CustomerInfo getCustomerInfo(UUID customerId) {
        return new CustomerInfo(customerId, "USR001", "customer@example.com", "+1234567890",
                "Default Customer", "US", "ACTIVE", true, true, false, Instant.now(), Instant.now());
    }
}
