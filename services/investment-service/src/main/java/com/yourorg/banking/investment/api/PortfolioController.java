package com.yourorg.banking.investment.api;

import com.yourorg.banking.investment.model.dto.PortfolioHoldingResponse;
import com.yourorg.banking.investment.model.dto.PortfolioPerformanceResponse;
import com.yourorg.banking.investment.service.PortfolioManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/portfolio")
@RequiredArgsConstructor
@Slf4j
public class PortfolioController {
    
    private final PortfolioManagementService portfolioManagementService;
    
    @GetMapping("/accounts/{accountId}/holdings")
    public ResponseEntity<List<PortfolioHoldingResponse>> getPortfolioHoldings(@PathVariable UUID accountId) {
        log.info("Getting portfolio holdings for account: {}", accountId);
        
        List<PortfolioHoldingResponse> holdings = portfolioManagementService.getPortfolioHoldings(accountId);
        
        return ResponseEntity.ok(holdings);
    }
    
    @PostMapping("/accounts/{accountId}/update")
    public ResponseEntity<String> updatePortfolio(@PathVariable UUID accountId) {
        log.info("Updating portfolio for account: {}", accountId);
        
        portfolioManagementService.updatePortfolioHoldings(accountId);
        
        return ResponseEntity.ok("Portfolio updated successfully");
    }
    
    @GetMapping("/accounts/{accountId}/performance")
    public ResponseEntity<PortfolioPerformanceResponse> getPortfolioPerformance(
            @PathVariable UUID accountId,
            @RequestParam(required = false) LocalDate date) {
        log.info("Getting portfolio performance for account: {} on date: {}", accountId, date);
        
        LocalDate performanceDate = date != null ? date : LocalDate.now();
        PortfolioPerformanceResponse performance = portfolioManagementService.getPortfolioPerformance(accountId, performanceDate);
        
        return ResponseEntity.ok(performance);
    }
    
    @GetMapping("/accounts/{accountId}/total-value")
    public ResponseEntity<Double> getTotalPortfolioValue(@PathVariable UUID accountId) {
        log.info("Getting total portfolio value for account: {}", accountId);
        
        Double totalValue = portfolioManagementService.getTotalPortfolioValue(accountId).doubleValue();
        
        return ResponseEntity.ok(totalValue);
    }
    
    @GetMapping("/accounts/{accountId}/unrealized-pnl")
    public ResponseEntity<Double> getTotalUnrealizedPnl(@PathVariable UUID accountId) {
        log.info("Getting total unrealized P&L for account: {}", accountId);
        
        Double unrealizedPnl = portfolioManagementService.getTotalUnrealizedPnl(accountId).doubleValue();
        
        return ResponseEntity.ok(unrealizedPnl);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Portfolio Service is healthy");
    }
}

