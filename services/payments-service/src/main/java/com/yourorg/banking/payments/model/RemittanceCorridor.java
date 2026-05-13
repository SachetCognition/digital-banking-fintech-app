package com.yourorg.banking.payments.model;

import java.math.BigDecimal;

public enum RemittanceCorridor {
    INDIA("India", "INR", new BigDecimal("15.00"), new BigDecimal("0.25"), new BigDecimal("15.00"), new BigDecimal("100000")),
    PAKISTAN("Pakistan", "PKR", new BigDecimal("10.00"), new BigDecimal("0.30"), new BigDecimal("10.00"), new BigDecimal("80000")),
    PHILIPPINES("Philippines", "PHP", new BigDecimal("12.00"), new BigDecimal("0.25"), new BigDecimal("12.00"), new BigDecimal("90000")),
    BANGLADESH("Bangladesh", "BDT", new BigDecimal("10.00"), new BigDecimal("0.35"), new BigDecimal("10.00"), new BigDecimal("70000"));

    private final String countryName;
    private final String targetCurrency;
    private final BigDecimal flatFee;
    private final BigDecimal percentageFee;
    private final BigDecimal minFee;
    private final BigDecimal monthlyAmlLimit;

    RemittanceCorridor(String countryName, String targetCurrency, BigDecimal flatFee,
                       BigDecimal percentageFee, BigDecimal minFee, BigDecimal monthlyAmlLimit) {
        this.countryName = countryName;
        this.targetCurrency = targetCurrency;
        this.flatFee = flatFee;
        this.percentageFee = percentageFee;
        this.minFee = minFee;
        this.monthlyAmlLimit = monthlyAmlLimit;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public BigDecimal getFlatFee() {
        return flatFee;
    }

    public BigDecimal getPercentageFee() {
        return percentageFee;
    }

    public BigDecimal getMinFee() {
        return minFee;
    }

    public BigDecimal getMonthlyAmlLimit() {
        return monthlyAmlLimit;
    }
}
