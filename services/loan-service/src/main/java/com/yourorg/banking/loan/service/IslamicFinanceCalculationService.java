package com.yourorg.banking.loan.service;

import com.yourorg.banking.loan.model.InterestCalculationMethod;
import com.yourorg.banking.loan.model.LoanType;
import com.yourorg.banking.loan.model.ShariahViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class IslamicFinanceCalculationService {

    private static final Set<LoanType> ISLAMIC_LOAN_TYPES = Set.of(
            LoanType.MURABAHA, LoanType.IJARA, LoanType.MUSHARAKA, LoanType.MUDARABAH
    );

    private static final Set<InterestCalculationMethod> ISLAMIC_METHODS = Set.of(
            InterestCalculationMethod.PROFIT_RATE,
            InterestCalculationMethod.COST_PLUS,
            InterestCalculationMethod.DIMINISHING_EQUITY
    );

    public record MurabahaResult(BigDecimal totalPrice, BigDecimal profitAmount, BigDecimal monthlyInstallment) {}

    public record MusharakaScheduleEntry(int period, BigDecimal bankEquity, BigDecimal customerEquity) {}

    public record IjaraScheduleEntry(int period, BigDecimal monthlyRental) {}

    public record MudarabahProfitSplit(BigDecimal customerShare, BigDecimal bankShare) {}

    public MurabahaResult calculateMurabaha(BigDecimal assetCost, BigDecimal markupRate, int termMonths) {
        BigDecimal profitAmount = assetCost.multiply(markupRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPrice = assetCost.add(profitAmount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal monthlyInstallment = totalPrice.divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);
        return new MurabahaResult(totalPrice, profitAmount, monthlyInstallment);
    }

    public List<MusharakaScheduleEntry> calculateMusharaka(BigDecimal propertyValue, BigDecimal bankEquityPercent, int termMonths) {
        List<MusharakaScheduleEntry> schedule = new ArrayList<>();
        BigDecimal bankEquity = propertyValue.multiply(bankEquityPercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal equityReductionPerPeriod = bankEquity.divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);

        for (int period = 1; period <= termMonths; period++) {
            BigDecimal currentBankEquity;
            if (period == termMonths) {
                currentBankEquity = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            } else {
                currentBankEquity = bankEquity.subtract(equityReductionPerPeriod.multiply(BigDecimal.valueOf(period))).setScale(2, RoundingMode.HALF_UP);
                if (currentBankEquity.compareTo(BigDecimal.ZERO) < 0) {
                    currentBankEquity = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                }
            }
            BigDecimal currentCustomerEquity = propertyValue.subtract(currentBankEquity).setScale(2, RoundingMode.HALF_UP);
            schedule.add(new MusharakaScheduleEntry(period, currentBankEquity, currentCustomerEquity));
        }
        return schedule;
    }

    public List<IjaraScheduleEntry> calculateIjara(BigDecimal assetValue, BigDecimal annualRentalRate, int termMonths) {
        BigDecimal annualRental = assetValue.multiply(annualRentalRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal monthlyRental = annualRental.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        List<IjaraScheduleEntry> schedule = new ArrayList<>();
        for (int period = 1; period <= termMonths; period++) {
            schedule.add(new IjaraScheduleEntry(period, monthlyRental));
        }
        return schedule;
    }

    public MudarabahProfitSplit calculateMudarabahProfit(BigDecimal totalProfit, BigDecimal customerRatioPercent) {
        BigDecimal customerShare = totalProfit.multiply(customerRatioPercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal bankShare = totalProfit.subtract(customerShare).setScale(2, RoundingMode.HALF_UP);
        return new MudarabahProfitSplit(customerShare, bankShare);
    }

    public void validateNotInterestBased(LoanType loanType, InterestCalculationMethod method) {
        if (ISLAMIC_LOAN_TYPES.contains(loanType) && !ISLAMIC_METHODS.contains(method)) {
            throw new ShariahViolationException(
                    "Islamic loan type " + loanType.getDisplayName() +
                    " cannot use conventional interest method " + method.getDisplayName() +
                    ". This violates Shariah principles (riba prohibition)."
            );
        }
    }
}
