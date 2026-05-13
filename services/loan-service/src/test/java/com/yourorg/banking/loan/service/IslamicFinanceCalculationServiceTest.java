package com.yourorg.banking.loan.service;

import com.yourorg.banking.loan.model.InterestCalculationMethod;
import com.yourorg.banking.loan.model.LoanType;
import com.yourorg.banking.loan.model.ShariahViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class IslamicFinanceCalculationServiceTest {

    @InjectMocks
    private IslamicFinanceCalculationService service;

    @Test
    void tcIF001_murabahaMarkup_calculatesCorrectly() {
        // TC-IF-001: cost 100K, rate 15%, 36mo -> total 115K, monthly 3194.44
        var result = service.calculateMurabaha(
                new BigDecimal("100000"),
                new BigDecimal("0.15"),
                36
        );

        assertEquals(new BigDecimal("115000.00"), result.totalPrice());
        assertEquals(new BigDecimal("15000.00"), result.profitAmount());
        assertEquals(new BigDecimal("3194.44"), result.monthlyInstallment());
    }

    @Test
    void tcIF002_musharakaEquity_reducesLinearly() {
        // TC-IF-002: 1M, 80/20, 240mo -> equity reduces linearly
        List<IslamicFinanceCalculationService.MusharakaScheduleEntry> schedule =
                service.calculateMusharaka(
                        new BigDecimal("1000000"),
                        new BigDecimal("80"),
                        240
                );

        assertEquals(240, schedule.size());

        // First period: bank equity should have decreased by one period
        var first = schedule.get(0);
        assertEquals(1, first.period());
        // Bank starts at 800K, reduces by 800K/240 = 3333.33 per period
        // After period 1: 800000 - 3333.33 = 796666.67
        assertEquals(new BigDecimal("796666.67"), first.bankEquity());

        // Last period: bank equity should be 0
        var last = schedule.get(239);
        assertEquals(240, last.period());
        assertEquals(new BigDecimal("0.00"), last.bankEquity());
        assertEquals(new BigDecimal("1000000.00"), last.customerEquity());
    }

    @Test
    void tcIF003_ijaraRental_calculatesMonthly() {
        // TC-IF-003: 500K, 6%, 60mo -> monthly 2500
        List<IslamicFinanceCalculationService.IjaraScheduleEntry> schedule =
                service.calculateIjara(
                        new BigDecimal("500000"),
                        new BigDecimal("0.06"),
                        60
                );

        assertEquals(60, schedule.size());
        assertEquals(new BigDecimal("2500.00"), schedule.get(0).monthlyRental());
        assertEquals(new BigDecimal("2500.00"), schedule.get(59).monthlyRental());
    }

    @Test
    void tcIF004_mudarabahProfit_splitsCorrectly() {
        // TC-IF-004: 30K profit, 60/40 -> customer 18K, bank 12K
        var result = service.calculateMudarabahProfit(
                new BigDecimal("30000"),
                new BigDecimal("60")
        );

        assertEquals(new BigDecimal("18000.00"), result.customerShare());
        assertEquals(new BigDecimal("12000.00"), result.bankShare());
    }

    @Test
    void tcIF005_ribaRejection_throwsShariahViolation() {
        // TC-IF-005: MURABAHA + COMPOUND_MONTHLY -> ShariahViolationException
        assertThrows(ShariahViolationException.class, () ->
                service.validateNotInterestBased(LoanType.MURABAHA, InterestCalculationMethod.COMPOUND_MONTHLY)
        );
    }

    @Test
    void validateNotInterestBased_islamicMethodAllowed() {
        assertDoesNotThrow(() ->
                service.validateNotInterestBased(LoanType.MURABAHA, InterestCalculationMethod.COST_PLUS)
        );
    }

    @Test
    void validateNotInterestBased_conventionalLoanAllowed() {
        assertDoesNotThrow(() ->
                service.validateNotInterestBased(LoanType.PERSONAL, InterestCalculationMethod.COMPOUND_MONTHLY)
        );
    }
}
