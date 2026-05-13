package com.yourorg.banking.business.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ZakatCalculationService {

    private static final BigDecimal ZAKAT_RATE = new BigDecimal("0.025");

    public ZakatResult calculateZakat(BigDecimal totalAssets, BigDecimal nonZakatableAssets, BigDecimal nisabThreshold) {
        BigDecimal eligibleAssets = totalAssets.subtract(nonZakatableAssets);
        boolean isAboveNisab = eligibleAssets.compareTo(nisabThreshold) >= 0;
        BigDecimal zakatAmount;

        if (isAboveNisab) {
            zakatAmount = eligibleAssets.multiply(ZAKAT_RATE).setScale(2, RoundingMode.HALF_UP);
        } else {
            zakatAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return new ZakatResult(eligibleAssets, zakatAmount, isAboveNisab);
    }

    public record ZakatResult(BigDecimal eligibleAssets, BigDecimal zakatAmount, boolean isAboveNisab) {
    }
}
