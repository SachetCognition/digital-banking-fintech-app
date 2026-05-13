package com.yourorg.banking.business.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class VatService {

    private static final BigDecimal GCC_VAT_RATE = new BigDecimal("0.05");

    public VatResult calculateVat(BigDecimal feeAmount, boolean isExempt) {
        if (isExempt) {
            return new VatResult(feeAmount, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    feeAmount, true, GCC_VAT_RATE);
        }

        BigDecimal vatAmount = feeAmount.multiply(GCC_VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = feeAmount.add(vatAmount);

        return new VatResult(feeAmount, vatAmount, totalAmount, false, GCC_VAT_RATE);
    }

    public record VatResult(BigDecimal feeAmount, BigDecimal vatAmount, BigDecimal totalAmount,
                            boolean isExempt, BigDecimal vatRate) {
    }
}
