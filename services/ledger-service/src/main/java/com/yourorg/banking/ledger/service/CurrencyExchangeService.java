package com.yourorg.banking.ledger.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CurrencyExchangeService {

    private final JdbcTemplate jdbcTemplate;
    private final Map<String, BigDecimal> rateCache = new ConcurrentHashMap<>();

    public CurrencyExchangeService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        refreshRates();
    }

    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) return amount;

        String key = fromCurrency + "_" + toCurrency;
        BigDecimal rate = rateCache.get(key);

        if (rate == null) {
            BigDecimal fromToUsd = getRate(fromCurrency, "USD");
            BigDecimal usdToTarget = getRate("USD", toCurrency);
            rate = fromToUsd.multiply(usdToTarget);
        }

        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getRate(String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) return BigDecimal.ONE;
        String key = fromCurrency + "_" + toCurrency;
        return rateCache.getOrDefault(key, BigDecimal.ONE);
    }

    @Scheduled(fixedRate = 3600000)
    public void refreshRates() {
        try {
            jdbcTemplate.query(
                "SELECT from_currency, to_currency, rate FROM exchange_rates",
                (rs) -> {
                    String key = rs.getString("from_currency") + "_" + rs.getString("to_currency");
                    rateCache.put(key, rs.getBigDecimal("rate"));
                }
            );
        } catch (Exception e) {
            // Table may not exist yet during initial startup
        }
    }
}
