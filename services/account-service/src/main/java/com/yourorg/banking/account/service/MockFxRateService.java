package com.yourorg.banking.account.service;

import com.yourorg.banking.account.model.Currency;
import com.yourorg.banking.account.model.FxRate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MockFxRateService implements FxRateService {

    private static final String SOURCE = "CBUAE";

    @Value("${fx.spread:0.005}")
    private BigDecimal defaultSpread;

    @Value("${fx.cache.ttl-minutes:15}")
    private long cacheTtlMinutes;

    private final Map<String, CachedRate> rateCache = new ConcurrentHashMap<>();

    private static final Map<String, BigDecimal> BASE_RATES_TO_USD = Map.ofEntries(
        Map.entry("AED", new BigDecimal("3.6725")),
        Map.entry("SAR", new BigDecimal("3.7500")),
        Map.entry("QAR", new BigDecimal("3.6400")),
        Map.entry("BHD", new BigDecimal("0.3760")),
        Map.entry("OMR", new BigDecimal("0.3845")),
        Map.entry("KWD", new BigDecimal("0.3070")),
        Map.entry("USD", BigDecimal.ONE),
        Map.entry("EUR", new BigDecimal("0.9200")),
        Map.entry("GBP", new BigDecimal("0.7900"))
    );

    @Override
    public FxRate getRate(Currency from, Currency to) {
        String cacheKey = from.name() + "_" + to.name();

        CachedRate cached = rateCache.get(cacheKey);
        if (cached != null && !cached.isExpired(cacheTtlMinutes)) {
            return cached.rate();
        }

        FxRate rate = calculateRate(from, to);
        rateCache.put(cacheKey, new CachedRate(rate, Instant.now()));
        return rate;
    }

    private FxRate calculateRate(Currency from, Currency to) {
        BigDecimal fromToUsd = BASE_RATES_TO_USD.get(from.name());
        BigDecimal toToUsd = BASE_RATES_TO_USD.get(to.name());

        BigDecimal midRate = toToUsd.divide(fromToUsd, 8, RoundingMode.HALF_UP);
        BigDecimal spreadAmount = midRate.multiply(defaultSpread);
        BigDecimal buyRate = midRate.subtract(spreadAmount);
        BigDecimal sellRate = midRate.add(spreadAmount);

        return new FxRate(from, to, midRate, defaultSpread, buyRate, sellRate, Instant.now(), SOURCE);
    }

    private record CachedRate(FxRate rate, Instant cachedAt) {
        boolean isExpired(long ttlMinutes) {
            return Instant.now().isAfter(cachedAt.plusSeconds(ttlMinutes * 60));
        }
    }
}
