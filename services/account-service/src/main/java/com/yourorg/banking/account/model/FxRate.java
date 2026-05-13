package com.yourorg.banking.account.model;

import java.math.BigDecimal;
import java.time.Instant;

public record FxRate(
    Currency from,
    Currency to,
    BigDecimal midRate,
    BigDecimal spread,
    BigDecimal buyRate,
    BigDecimal sellRate,
    Instant timestamp,
    String source
) {}
