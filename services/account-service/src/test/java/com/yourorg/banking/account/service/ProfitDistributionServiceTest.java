package com.yourorg.banking.account.service;

import com.yourorg.banking.account.model.AccountType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProfitDistributionServiceTest {

    @InjectMocks
    private ProfitDistributionService service;

    @Test
    void tcIF006_wadiahAccountCreation_verifyProperties() {
        // TC-IF-006: Wadiah account creation — verify type, interest=0, allows transactions
        AccountType wadiah = AccountType.WADIAH_CURRENT;

        assertEquals("Wadiah Current", wadiah.getDisplayName());
        assertEquals(0.00, wadiah.getInterestRate());
        assertTrue(wadiah.allowsTransactions());
        assertTrue(wadiah.isIslamicAccount());
    }

    @Test
    void tcIF008_profitDistribution_mudarabahSavings() {
        // TC-IF-008: 100K balance, 5K profit, 70% customer ratio -> 3500 credited
        UUID accountId = UUID.randomUUID();
        BigDecimal profitPool = new BigDecimal("5000");
        BigDecimal customerRatio = new BigDecimal("0.70");

        var distribution = service.distributeProfits(accountId, profitPool, customerRatio);

        assertNotNull(distribution);
        assertNotNull(distribution.distributionId());
        assertEquals(accountId, distribution.accountId());
        assertEquals(new BigDecimal("3500.00"), distribution.customerAmount());
        assertEquals(new BigDecimal("1500.00"), distribution.bankAmount());
        assertEquals("MUDARABAH_PROFIT_SHARING", distribution.source());
        assertNotNull(distribution.distributionDate());
    }

    @Test
    void distributionHistory_tracksMultipleDistributions() {
        UUID accountId = UUID.randomUUID();

        service.distributeProfits(accountId, new BigDecimal("5000"), new BigDecimal("0.70"));
        service.distributeProfits(accountId, new BigDecimal("3000"), new BigDecimal("0.60"));

        var history = service.getDistributionHistory(accountId);
        assertEquals(2, history.size());
    }

    @Test
    void islamicAccountTypes_identifiedCorrectly() {
        assertTrue(AccountType.WADIAH_CURRENT.isIslamicAccount());
        assertTrue(AccountType.MUDARABAH_SAVINGS.isIslamicAccount());
        assertTrue(AccountType.WAKALA_INVESTMENT.isIslamicAccount());
        assertTrue(AccountType.COMMODITY_MURABAHA_DEPOSIT.isIslamicAccount());
        assertFalse(AccountType.CHECKING.isIslamicAccount());
        assertFalse(AccountType.SAVINGS.isIslamicAccount());
    }
}
