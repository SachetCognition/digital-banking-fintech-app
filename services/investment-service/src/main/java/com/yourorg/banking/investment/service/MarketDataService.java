package com.yourorg.banking.investment.service;

import com.yourorg.banking.investment.model.MarketData;
import com.yourorg.banking.investment.model.dto.MarketDataResponse;
import com.yourorg.banking.investment.repository.MarketDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketDataService {
    
    private final MarketDataRepository marketDataRepository;
    private final Random random = new Random();
    
    @Cacheable(value = "marketData", key = "#symbol")
    public Optional<MarketDataResponse> getMarketData(String symbol) {
        log.info("Fetching market data for symbol: {}", symbol);
        
        Optional<MarketData> marketData = marketDataRepository.findLatestBySymbol(symbol);
        
        if (marketData.isEmpty()) {
            // Generate mock data if not available
            marketData = Optional.of(generateMockMarketData(symbol));
        }
        
        return marketData.map(this::convertToResponse);
    }
    
    public List<MarketDataResponse> getMarketDataForSymbols(List<String> symbols) {
        log.info("Fetching market data for {} symbols", symbols.size());
        
        List<MarketData> marketDataList = marketDataRepository.findBySymbolInAndLastUpdatedAfter(
            symbols, LocalDateTime.now().minusMinutes(5));
        
        return marketDataList.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void updateMarketData() {
        log.info("Updating market data");
        
        // Get all active symbols from portfolio holdings
        List<String> activeSymbols = getActiveSymbols();
        
        for (String symbol : activeSymbols) {
            updateSymbolData(symbol);
        }
    }
    
    public void updateSymbolData(String symbol) {
        log.debug("Updating market data for symbol: {}", symbol);
        
        Optional<MarketData> existingData = marketDataRepository.findLatestBySymbol(symbol);
        BigDecimal previousPrice = existingData.map(MarketData::getPrice).orElse(BigDecimal.valueOf(100));
        
        // Generate new price with some volatility
        BigDecimal newPrice = generatePriceWithVolatility(previousPrice);
        
        MarketData marketData = MarketData.builder()
            .symbol(symbol)
            .price(newPrice)
            .previousClose(existingData.map(MarketData::getPrice).orElse(newPrice))
            .openPrice(existingData.map(MarketData::getOpenPrice).orElse(newPrice))
            .highPrice(newPrice.max(existingData.map(MarketData::getHighPrice).orElse(newPrice)))
            .lowPrice(newPrice.min(existingData.map(MarketData::getLowPrice).orElse(newPrice)))
            .volume(generateVolume())
            .marketCap(generateMarketCap())
            .peRatio(generatePERatio())
            .dividendYield(generateDividendYield())
            .lastUpdated(LocalDateTime.now())
            .build();
        
        marketDataRepository.save(marketData);
    }
    
    private List<String> getActiveSymbols() {
        // In a real implementation, this would query active portfolio holdings
        // For now, return a list of common symbols
        return List.of("AAPL", "GOOGL", "MSFT", "AMZN", "TSLA", "META", "NVDA", "SPY", "QQQ", "VTI");
    }
    
    private MarketData generateMockMarketData(String symbol) {
        BigDecimal basePrice = BigDecimal.valueOf(100 + random.nextInt(900)); // $100-$1000
        
        return MarketData.builder()
            .symbol(symbol)
            .price(basePrice)
            .previousClose(basePrice.multiply(BigDecimal.valueOf(0.95 + random.nextDouble() * 0.1)))
            .openPrice(basePrice.multiply(BigDecimal.valueOf(0.98 + random.nextDouble() * 0.04)))
            .highPrice(basePrice.multiply(BigDecimal.valueOf(1.01 + random.nextDouble() * 0.05)))
            .lowPrice(basePrice.multiply(BigDecimal.valueOf(0.95 + random.nextDouble() * 0.05)))
            .volume(generateVolume())
            .marketCap(generateMarketCap())
            .peRatio(generatePERatio())
            .dividendYield(generateDividendYield())
            .lastUpdated(LocalDateTime.now())
            .build();
    }
    
    private BigDecimal generatePriceWithVolatility(BigDecimal previousPrice) {
        // Generate price change between -2% and +2%
        double changePercent = (random.nextDouble() - 0.5) * 0.04; // -2% to +2%
        return previousPrice.multiply(BigDecimal.ONE.add(BigDecimal.valueOf(changePercent)))
            .setScale(2, RoundingMode.HALF_UP);
    }
    
    private Long generateVolume() {
        return 1000000L + random.nextInt(10000000); // 1M to 11M
    }
    
    private Long generateMarketCap() {
        return 1000000000L + random.nextInt(9000000000); // 1B to 10B
    }
    
    private BigDecimal generatePERatio() {
        return BigDecimal.valueOf(10 + random.nextDouble() * 40).setScale(2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal generateDividendYield() {
        return BigDecimal.valueOf(random.nextDouble() * 0.05).setScale(4, RoundingMode.HALF_UP);
    }
    
    private MarketDataResponse convertToResponse(MarketData marketData) {
        return MarketDataResponse.builder()
            .symbol(marketData.getSymbol())
            .price(marketData.getPrice())
            .previousClose(marketData.getPreviousClose())
            .openPrice(marketData.getOpenPrice())
            .highPrice(marketData.getHighPrice())
            .lowPrice(marketData.getLowPrice())
            .volume(marketData.getVolume())
            .marketCap(marketData.getMarketCap())
            .peRatio(marketData.getPeRatio())
            .dividendYield(marketData.getDividendYield())
            .priceChange(marketData.getPriceChange())
            .priceChangePercentage(marketData.getPriceChangePercentage())
            .lastUpdated(marketData.getLastUpdated())
            .build();
    }
}

