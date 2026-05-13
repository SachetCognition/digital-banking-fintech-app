package com.yourorg.banking.customer.client;

import com.yourorg.banking.account.model.KycStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class KycServiceClient {
    public KycStatus checkKycStatus(UUID customerId, String level) {
        return KycStatus.APPROVED;
    }
}
