package com.yourorg.banking.account.service;

import com.yourorg.banking.account.model.Currency;
import com.yourorg.banking.account.model.FxRate;
import com.yourorg.banking.account.model.Wallet;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MultiCurrencyWalletService {

    private final FxRateService fxRateService;
    private final Map<UUID, List<Wallet>> walletStore = new ConcurrentHashMap<>();

    public MultiCurrencyWalletService(FxRateService fxRateService) {
        this.fxRateService = fxRateService;
    }

    public Wallet createWallet(UUID customerId, Currency currency) {
        List<Wallet> customerWallets = walletStore.computeIfAbsent(customerId, k -> new ArrayList<>());

        boolean exists = customerWallets.stream()
                .anyMatch(w -> w.currency() == currency);
        if (exists) {
            throw new IllegalArgumentException("Wallet already exists for currency: " + currency);
        }

        Wallet wallet = new Wallet(
                UUID.randomUUID(),
                customerId,
                currency,
                BigDecimal.ZERO,
                Instant.now(),
                Instant.now()
        );

        customerWallets.add(wallet);
        return wallet;
    }

    public BigDecimal convertCurrency(UUID walletId, Currency from, Currency to, BigDecimal amount) {
        if (from == to) {
            return amount;
        }

        FxRate rate = fxRateService.getRate(from, to);
        return amount.multiply(rate.sellRate()).setScale(to.getDecimalPlaces(), RoundingMode.HALF_UP);
    }

    public List<Wallet> getWallets(UUID customerId) {
        return walletStore.getOrDefault(customerId, List.of());
    }
}
