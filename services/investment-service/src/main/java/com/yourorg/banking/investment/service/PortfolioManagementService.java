package com.yourorg.banking.investment.service;

import com.yourorg.banking.investment.model.InvestmentAccount;
import com.yourorg.banking.investment.model.PortfolioHolding;
import com.yourorg.banking.investment.model.dto.PortfolioHoldingResponse;
import com.yourorg.banking.investment.model.dto.PortfolioPerformanceResponse;
import com.yourorg.banking.investment.repository.InvestmentAccountRepository;
import com.yourorg.banking.investment.repository.PortfolioHoldingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioManagementService {
    
    private final PortfolioHoldingRepository portfolioHoldingRepository;
    private final InvestmentAccountRepository investmentAccountRepository;
    private final MarketDataService marketDataService;
    
    public List<PortfolioHoldingResponse> getPortfolioHoldings(UUID accountId) {
        log.info("Getting portfolio holdings for account: {}", accountId);
        
        List<PortfolioHolding> holdings = portfolioHoldingRepository.findByAccountIdOrderByMarketValueDesc(accountId);
        
        return holdings.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public void updatePortfolioHoldings(UUID accountId) {
        log.info("Updating portfolio holdings for account: {}", accountId);
        
        List<PortfolioHolding> holdings = portfolioHoldingRepository.findActiveHoldingsByAccountId(accountId);
        BigDecimal totalMarketValue = BigDecimal.ZERO;
        
        for (PortfolioHolding holding : holdings) {
            // Get current market price
            marketDataService.getMarketData(holding.getSymbol())
                .ifPresent(marketData -> {
                    BigDecimal currentPrice = marketData.getPrice();
                    BigDecimal marketValue = holding.getQuantity().multiply(currentPrice);
                    BigDecimal unrealizedPnl = marketValue.subtract(holding.getCostBasis());
                    BigDecimal unrealizedPnlPercentage = holding.getCostBasis().compareTo(BigDecimal.ZERO) != 0 ?
                        unrealizedPnl.divide(holding.getCostBasis(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
                    
                    holding.setCurrentPrice(currentPrice);
                    holding.setMarketValue(marketValue);
                    holding.setUnrealizedPnl(unrealizedPnl);
                    holding.setUnrealizedPnlPercentage(unrealizedPnlPercentage);
                    
                    portfolioHoldingRepository.save(holding);
                });
        }
        
        // Calculate total market value
        totalMarketValue = holdings.stream()
            .map(PortfolioHolding::getMarketValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Update account with new values
        updateAccountValues(accountId, totalMarketValue);
        
        // Update weight percentages
        updateWeightPercentages(accountId, totalMarketValue);
    }
    
    public PortfolioPerformanceResponse getPortfolioPerformance(UUID accountId, LocalDate date) {
        log.info("Getting portfolio performance for account: {} on date: {}", accountId, date);
        
        InvestmentAccount account = investmentAccountRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        List<PortfolioHolding> holdings = portfolioHoldingRepository.findActiveHoldingsByAccountId(accountId);
        
        BigDecimal totalValue = holdings.stream()
            .map(PortfolioHolding::getMarketValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCost = holdings.stream()
            .map(PortfolioHolding::getCostBasis)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalReturn = totalValue.subtract(totalCost);
        BigDecimal totalReturnPercentage = totalCost.compareTo(BigDecimal.ZERO) != 0 ?
            totalReturn.divide(totalCost, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
        
        // Calculate additional metrics
        BigDecimal dailyReturn = calculateDailyReturn(accountId, date);
        BigDecimal dailyReturnPercentage = totalValue.compareTo(BigDecimal.ZERO) != 0 ?
            dailyReturn.divide(totalValue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
        
        BigDecimal benchmarkReturn = calculateBenchmarkReturn(date);
        BigDecimal alpha = totalReturnPercentage.subtract(benchmarkReturn);
        BigDecimal beta = calculateBeta(accountId);
        BigDecimal sharpeRatio = calculateSharpeRatio(accountId);
        BigDecimal maxDrawdown = calculateMaxDrawdown(accountId);
        BigDecimal volatility = calculateVolatility(accountId);
        
        return PortfolioPerformanceResponse.builder()
            .calculationDate(date)
            .totalValue(totalValue)
            .totalCost(totalCost)
            .totalReturn(totalReturn)
            .totalReturnPercentage(totalReturnPercentage)
            .dailyReturn(dailyReturn)
            .dailyReturnPercentage(dailyReturnPercentage)
            .benchmarkReturn(benchmarkReturn)
            .alpha(alpha)
            .beta(beta)
            .sharpeRatio(sharpeRatio)
            .maxDrawdown(maxDrawdown)
            .volatility(volatility)
            .build();
    }
    
    public BigDecimal getTotalPortfolioValue(UUID accountId) {
        List<PortfolioHolding> holdings = portfolioHoldingRepository.findActiveHoldingsByAccountId(accountId);
        return holdings.stream()
            .map(PortfolioHolding::getMarketValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal getTotalUnrealizedPnl(UUID accountId) {
        List<PortfolioHolding> holdings = portfolioHoldingRepository.findActiveHoldingsByAccountId(accountId);
        return holdings.stream()
            .map(PortfolioHolding::getUnrealizedPnl)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private void updateAccountValues(UUID accountId, BigDecimal totalMarketValue) {
        InvestmentAccount account = investmentAccountRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        account.setInvestedAmount(totalMarketValue);
        account.setUnrealizedPnl(getTotalUnrealizedPnl(accountId));
        account.setCurrentBalance(account.getAvailableCash().add(totalMarketValue));
        
        // Calculate total return
        BigDecimal totalReturn = account.getUnrealizedPnl().add(account.getRealizedPnl());
        account.setTotalReturn(totalReturn);
        
        // Calculate total return percentage
        BigDecimal totalCost = account.getInitialDeposit().add(account.getRealizedPnl());
        if (totalCost.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal totalReturnPercentage = totalReturn.divide(totalCost, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
            account.setTotalReturnPercentage(totalReturnPercentage);
        }
        
        investmentAccountRepository.save(account);
    }
    
    private void updateWeightPercentages(UUID accountId, BigDecimal totalMarketValue) {
        List<PortfolioHolding> holdings = portfolioHoldingRepository.findActiveHoldingsByAccountId(accountId);
        
        for (PortfolioHolding holding : holdings) {
            BigDecimal weightPercentage = totalMarketValue.compareTo(BigDecimal.ZERO) != 0 ?
                holding.getMarketValue().divide(totalMarketValue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
            holding.setWeightPercentage(weightPercentage);
            portfolioHoldingRepository.save(holding);
        }
    }
    
    private BigDecimal calculateDailyReturn(UUID accountId, LocalDate date) {
        // Simplified daily return calculation
        // In a real implementation, this would compare with previous day's value
        return BigDecimal.ZERO;
    }
    
    private BigDecimal calculateBenchmarkReturn(LocalDate date) {
        // Simplified benchmark return (S&P 500)
        // In a real implementation, this would fetch actual benchmark data
        return BigDecimal.valueOf(0.1); // 0.1% daily return
    }
    
    private BigDecimal calculateBeta(UUID accountId) {
        // Simplified beta calculation
        // In a real implementation, this would calculate against a benchmark
        return BigDecimal.valueOf(1.0);
    }
    
    private BigDecimal calculateSharpeRatio(UUID accountId) {
        // Simplified Sharpe ratio calculation
        // In a real implementation, this would use risk-free rate and volatility
        return BigDecimal.valueOf(1.5);
    }
    
    private BigDecimal calculateMaxDrawdown(UUID accountId) {
        // Simplified max drawdown calculation
        // In a real implementation, this would analyze historical performance
        return BigDecimal.valueOf(5.0); // 5% max drawdown
    }
    
    private BigDecimal calculateVolatility(UUID accountId) {
        // Simplified volatility calculation
        // In a real implementation, this would calculate standard deviation of returns
        return BigDecimal.valueOf(15.0); // 15% volatility
    }
    
    private PortfolioHoldingResponse convertToResponse(PortfolioHolding holding) {
        return PortfolioHoldingResponse.builder()
            .id(holding.getId())
            .symbol(holding.getSymbol())
            .quantity(holding.getQuantity())
            .averageCost(holding.getAverageCost())
            .currentPrice(holding.getCurrentPrice())
            .marketValue(holding.getMarketValue())
            .costBasis(holding.getCostBasis())
            .unrealizedPnl(holding.getUnrealizedPnl())
            .unrealizedPnlPercentage(holding.getUnrealizedPnlPercentage())
            .weightPercentage(holding.getWeightPercentage())
            .lastUpdated(holding.getLastUpdated())
            .build();
    }
}

