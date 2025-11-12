package com.yourorg.banking.investment.api;

import com.yourorg.banking.investment.model.dto.MarketDataResponse;
import com.yourorg.banking.investment.service.MarketDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/market-data")
@RequiredArgsConstructor
@Slf4j
public class MarketDataController {
    
    private final MarketDataService marketDataService;
    
    @GetMapping("/{symbol}")
    public ResponseEntity<MarketDataResponse> getMarketData(@PathVariable String symbol) {
        log.info("Getting market data for symbol: {}", symbol);
        
        Optional<MarketDataResponse> marketData = marketDataService.getMarketData(symbol);
        
        if (marketData.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(marketData.get());
    }
    
    @PostMapping("/batch")
    public ResponseEntity<List<MarketDataResponse>> getMarketDataForSymbols(@RequestBody List<String> symbols) {
        log.info("Getting market data for {} symbols", symbols.size());
        
        List<MarketDataResponse> marketDataList = marketDataService.getMarketDataForSymbols(symbols);
        
        return ResponseEntity.ok(marketDataList);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Market Data Service is healthy");
    }
}

