package com.yourorg.banking.business.service;

import com.yourorg.banking.business.model.InterestAccrual;
import com.yourorg.banking.business.repository.InterestAccrualRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class InterestPostingService {

    private static final Logger log = LoggerFactory.getLogger(InterestPostingService.class);
    private static final Map<String, BigDecimal> ANNUAL_RATES = Map.of(
        "SAVINGS", new BigDecimal("0.045"),
        "CHECKING", new BigDecimal("0.001"),
        "MONEY_MARKET", new BigDecimal("0.05"),
        "CD", new BigDecimal("0.055")
    );

    private final InterestAccrualRepository interestAccrualRepository;
    private final RestTemplate restTemplate;

    public InterestPostingService(InterestAccrualRepository interestAccrualRepository, RestTemplate restTemplate) {
        this.interestAccrualRepository = interestAccrualRepository;
        this.restTemplate = restTemplate;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void postDailyInterest() {
        log.info("Starting daily interest posting for {}", LocalDate.now());
        List<Map<String, Object>> accounts = fetchActiveAccounts();

        int posted = 0;
        for (Map<String, Object> account : accounts) {
            try {
                postInterestForAccount(account);
                posted++;
            } catch (Exception e) {
                log.error("Failed to post interest for account {}: {}", account.get("id"), e.getMessage());
            }
        }
        log.info("Daily interest posting complete. Processed {} accounts", posted);
    }

    private void postInterestForAccount(Map<String, Object> account) {
        String accountId = (String) account.get("id");
        String accountType = (String) account.get("type");
        BigDecimal annualRate = ANNUAL_RATES.getOrDefault(accountType, BigDecimal.ZERO);

        if (annualRate.compareTo(BigDecimal.ZERO) == 0) return;

        BigDecimal balance = fetchBalance(accountId);
        if (balance.compareTo(BigDecimal.ZERO) <= 0) return;

        BigDecimal dailyInterest = balance.multiply(annualRate)
            .divide(new BigDecimal("365"), 8, RoundingMode.HALF_UP);

        if (dailyInterest.compareTo(new BigDecimal("0.01")) < 0) return;

        postJournalEntry(accountId, dailyInterest);

        InterestAccrual accrual = new InterestAccrual();
        accrual.setId(UUID.randomUUID());
        accrual.setAccountId(UUID.fromString(accountId));
        accrual.setAmount(dailyInterest);
        accrual.setRate(annualRate);
        accrual.setPostingDate(LocalDate.now());
        interestAccrualRepository.save(accrual);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchActiveAccounts() {
        try {
            return restTemplate.getForObject("http://account-service:8082/api/v1/accounts?status=ACTIVE", List.class);
        } catch (Exception e) {
            log.error("Failed to fetch active accounts: {}", e.getMessage());
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private BigDecimal fetchBalance(String accountId) {
        try {
            Map<String, Object> response = restTemplate.getForObject(
                "http://ledger-service:8083/api/v1/ledger/balances/" + accountId, Map.class);
            if (response != null && response.get("balance") != null) {
                return new BigDecimal(response.get("balance").toString());
            }
        } catch (Exception e) {
            log.error("Failed to fetch balance for account {}: {}", accountId, e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    private void postJournalEntry(String accountId, BigDecimal amount) {
        Map<String, Object> journalRequest = Map.of(
            "description", "Daily interest posting",
            "entries", List.of(
                Map.of("accountId", "INTEREST_EXPENSE", "type", "DEBIT", "amount", amount),
                Map.of("accountId", accountId, "type", "CREDIT", "amount", amount)
            )
        );
        restTemplate.postForObject("http://ledger-service:8083/api/v1/ledger/journals", journalRequest, Map.class);
    }
}
