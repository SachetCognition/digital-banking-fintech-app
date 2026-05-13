package com.yourorg.banking.account.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProfitDistributionService {

    public record ProfitDistribution(
            UUID distributionId,
            UUID accountId,
            String source,
            BigDecimal profitPool,
            BigDecimal customerRatio,
            BigDecimal customerAmount,
            BigDecimal bankAmount,
            LocalDateTime distributionDate
    ) {}

    private final Map<UUID, List<ProfitDistribution>> distributionHistory = new ConcurrentHashMap<>();

    public ProfitDistribution distributeProfits(UUID accountId, BigDecimal profitPool, BigDecimal customerRatio) {
        BigDecimal customerAmount = profitPool.multiply(customerRatio).setScale(2, RoundingMode.HALF_UP);
        BigDecimal bankAmount = profitPool.subtract(customerAmount).setScale(2, RoundingMode.HALF_UP);

        ProfitDistribution distribution = new ProfitDistribution(
                UUID.randomUUID(),
                accountId,
                "MUDARABAH_PROFIT_SHARING",
                profitPool,
                customerRatio,
                customerAmount,
                bankAmount,
                LocalDateTime.now()
        );

        distributionHistory.computeIfAbsent(accountId, k -> new ArrayList<>()).add(distribution);
        return distribution;
    }

    public List<ProfitDistribution> getDistributionHistory(UUID accountId) {
        return distributionHistory.getOrDefault(accountId, List.of());
    }
}
