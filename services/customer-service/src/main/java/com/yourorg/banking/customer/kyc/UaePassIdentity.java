package com.yourorg.banking.customer.kyc;

import java.time.LocalDate;

public record UaePassIdentity(
        String fullName,
        String emiratesId,
        String nationality,
        LocalDate dateOfBirth
) {}
