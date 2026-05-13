package com.yourorg.banking.account.service;

import com.yourorg.banking.account.model.Currency;
import com.yourorg.banking.account.model.FxRate;
import com.yourorg.banking.account.model.Wallet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MultiCurrencyWalletServiceTest {

    @Mock
    private FxRateService fxRateService;

    @InjectMocks
    private MultiCurrencyWalletService walletService;

    @Test
    void createWalletsForAllGccCurrencies_verifyZeroBalanceAndParentLink() {
        // TC-MC-001
        UUID customerId = UUID.randomUUID();
        Currency[] gccCurrencies = {
            Currency.AED, Currency.SAR, Currency.QAR,
            Currency.BHD, Currency.OMR, Currency.KWD
        };

        for (Currency currency : gccCurrencies) {
            Wallet wallet = walletService.createWallet(customerId, currency);

            assertNotNull(wallet);
            assertNotNull(wallet.id());
            assertEquals(customerId, wallet.customerId());
            assertEquals(currency, wallet.currency());
            assertEquals(BigDecimal.ZERO, wallet.balance());
            assertNotNull(wallet.createdAt());
        }

        List<Wallet> wallets = walletService.getWallets(customerId);
        assertEquals(6, wallets.size());
    }

    @Test
    void convertCurrency_aedToSar_withSpread() {
        // TC-MC-002
        BigDecimal midRate = new BigDecimal("1.02109");
        BigDecimal spread = new BigDecimal("0.005");
        BigDecimal spreadAmount = midRate.multiply(spread);
        BigDecimal sellRate = midRate.add(spreadAmount);

        FxRate fxRate = new FxRate(
            Currency.AED, Currency.SAR,
            midRate, spread,
            midRate.subtract(spreadAmount),
            sellRate,
            Instant.now(), "CBUAE"
        );

        when(fxRateService.getRate(Currency.AED, Currency.SAR)).thenReturn(fxRate);

        BigDecimal amount = new BigDecimal("1000.00");
        BigDecimal result = walletService.convertCurrency(
            UUID.randomUUID(), Currency.AED, Currency.SAR, amount
        );

        BigDecimal expected = amount.multiply(sellRate).setScale(2, RoundingMode.HALF_UP);
        assertEquals(expected, result);
        verify(fxRateService).getRate(Currency.AED, Currency.SAR);
    }

    @Test
    void fxRateLookup_returnsCachedRate_withCbuaeSource() {
        // TC-MC-003
        BigDecimal midRate = new BigDecimal("1.02109");
        BigDecimal spread = new BigDecimal("0.005");
        BigDecimal spreadAmount = midRate.multiply(spread);

        FxRate fxRate = new FxRate(
            Currency.AED, Currency.SAR,
            midRate, spread,
            midRate.subtract(spreadAmount),
            midRate.add(spreadAmount),
            Instant.now(), "CBUAE"
        );

        when(fxRateService.getRate(Currency.AED, Currency.SAR)).thenReturn(fxRate);

        FxRate rate = fxRateService.getRate(Currency.AED, Currency.SAR);

        assertNotNull(rate);
        assertEquals("CBUAE", rate.source());
        assertEquals(Currency.AED, rate.from());
        assertEquals(Currency.SAR, rate.to());
        assertNotNull(rate.timestamp());
        assertTrue(rate.buyRate().compareTo(rate.midRate()) < 0);
        assertTrue(rate.sellRate().compareTo(rate.midRate()) > 0);
    }
}
