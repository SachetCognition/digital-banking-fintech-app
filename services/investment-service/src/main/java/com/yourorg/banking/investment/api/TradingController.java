package com.yourorg.banking.investment.api;

import com.yourorg.banking.investment.model.dto.OrderResponse;
import com.yourorg.banking.investment.model.dto.PlaceOrderRequest;
import com.yourorg.banking.investment.service.TradingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trading")
@RequiredArgsConstructor
@Slf4j
public class TradingController {
    
    private final TradingService tradingService;
    
    @PostMapping("/accounts/{accountId}/orders")
    public ResponseEntity<OrderResponse> placeOrder(
            @PathVariable UUID accountId,
            @RequestBody PlaceOrderRequest request) {
        log.info("Placing order for account: {}", accountId);
        
        OrderResponse response = tradingService.placeOrder(accountId, request);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/accounts/{accountId}/orders")
    public ResponseEntity<List<OrderResponse>> getAccountOrders(@PathVariable UUID accountId) {
        log.info("Getting orders for account: {}", accountId);
        
        List<OrderResponse> orders = tradingService.getAccountOrders(accountId);
        
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/orders/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderNumber) {
        log.info("Getting order: {}", orderNumber);
        
        OrderResponse order = tradingService.getOrder(orderNumber);
        
        return ResponseEntity.ok(order);
    }
    
    @PostMapping("/orders/{orderNumber}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable String orderNumber) {
        log.info("Cancelling order: {}", orderNumber);
        
        OrderResponse response = tradingService.cancelOrder(orderNumber);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Trading Service is healthy");
    }
}

