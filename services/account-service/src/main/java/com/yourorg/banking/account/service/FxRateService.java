package com.yourorg.banking.account.service;

import com.yourorg.banking.account.model.Currency;
import com.yourorg.banking.account.model.FxRate;

public interface FxRateService {
    FxRate getRate(Currency from, Currency to);
}
