package com.yourorg.banking.investment.service;

import com.yourorg.banking.investment.model.*;
import com.yourorg.banking.investment.model.dto.OrderResponse;
import com.yourorg.banking.investment.model.dto.PlaceOrderRequest;
import com.yourorg.banking.investment.repository.InvestmentAccountRepository;
import com.yourorg.banking.investment.repository.PortfolioHoldingRepository;
import com.yourorg.banking.investment.repository.TradingOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradingService {
    
    private final TradingOrderRepository tradingOrderRepository;
    private final InvestmentAccountRepository investmentAccountRepository;
    private final PortfolioHoldingRepository portfolioHoldingRepository;
    private final MarketDataService marketDataService;
    private final OrderNumberGeneratorService orderNumberGeneratorService;
    
    @Transactional
    public OrderResponse placeOrder(UUID accountId, PlaceOrderRequest request) {
        log.info("Placing order for account: {} - {} {} {} shares of {}", 
                accountId, request.getSide(), request.getQuantity(), request.getSymbol());
        
        // Validate account
        InvestmentAccount account = investmentAccountRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("Account is not active");
        }
        
        // Validate order
        validateOrder(account, request);
        
        // Create order
        TradingOrder order = TradingOrder.builder()
            .accountId(accountId)
            .orderNumber(orderNumberGeneratorService.generateOrderNumber())
            .symbol(request.getSymbol())
            .orderType(request.getOrderType())
            .side(request.getSide())
            .quantity(request.getQuantity())
            .price(request.getPrice())
            .stopPrice(request.getStopPrice())
            .limitPrice(request.getLimitPrice())
            .timeInForce(request.getTimeInForce() != null ? request.getTimeInForce() : TimeInForce.DAY)
            .status(OrderStatus.PENDING)
            .submittedAt(LocalDateTime.now())
            .expiresAt(request.getExpiresAt())
            .build();
        
        TradingOrder savedOrder = tradingOrderRepository.save(order);
        
        // Process order
        processOrder(savedOrder);
        
        return convertToResponse(savedOrder);
    }
    
    public List<OrderResponse> getAccountOrders(UUID accountId) {
        log.info("Getting orders for account: {}", accountId);
        
        List<TradingOrder> orders = tradingOrderRepository.findByAccountIdOrderBySubmittedAtDesc(accountId);
        
        return orders.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    public OrderResponse getOrder(String orderNumber) {
        log.info("Getting order: {}", orderNumber);
        
        TradingOrder order = tradingOrderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        
        return convertToResponse(order);
    }
    
    @Transactional
    public OrderResponse cancelOrder(String orderNumber) {
        log.info("Cancelling order: {}", orderNumber);
        
        TradingOrder order = tradingOrderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (order.getStatus() == OrderStatus.FILLED) {
            throw new RuntimeException("Cannot cancel filled order");
        }
        
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Order already cancelled");
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        
        TradingOrder savedOrder = tradingOrderRepository.save(order);
        
        return convertToResponse(savedOrder);
    }
    
    @Transactional
    public void processOrder(TradingOrder order) {
        log.info("Processing order: {}", order.getOrderNumber());
        
        // Get current market price
        marketDataService.getMarketData(order.getSymbol())
            .ifPresentOrElse(
                marketData -> executeOrder(order, marketData.getPrice()),
                () -> {
                    log.warn("No market data available for symbol: {}", order.getSymbol());
                    order.setStatus(OrderStatus.REJECTED);
                    tradingOrderRepository.save(order);
                }
            );
    }
    
    private void executeOrder(TradingOrder order, BigDecimal currentPrice) {
        log.info("Executing order: {} at price: {}", order.getOrderNumber(), currentPrice);
        
        // Check if order can be executed
        if (canExecuteOrder(order, currentPrice)) {
            // Execute the order
            BigDecimal executionPrice = determineExecutionPrice(order, currentPrice);
            BigDecimal filledValue = order.getQuantity().multiply(executionPrice);
            BigDecimal commission = calculateCommission(filledValue);
            BigDecimal fees = calculateFees(filledValue);
            BigDecimal totalCost = filledValue.add(commission).add(fees);
            
            order.setStatus(OrderStatus.FILLED);
            order.setFilledQuantity(order.getQuantity());
            order.setFilledPrice(executionPrice);
            order.setFilledValue(filledValue);
            order.setCommission(commission);
            order.setFees(fees);
            order.setTotalCost(totalCost);
            order.setFilledAt(LocalDateTime.now());
            
            tradingOrderRepository.save(order);
            
            // Update portfolio
            updatePortfolio(order);
            
            // Update account balance
            updateAccountBalance(order);
        } else {
            order.setStatus(OrderStatus.REJECTED);
            tradingOrderRepository.save(order);
        }
    }
    
    private boolean canExecuteOrder(TradingOrder order, BigDecimal currentPrice) {
        // Check if account has sufficient funds for buy orders
        if (order.getSide() == OrderSide.BUY) {
            InvestmentAccount account = investmentAccountRepository.findById(order.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));
            
            BigDecimal requiredAmount = order.getQuantity().multiply(currentPrice);
            if (account.getAvailableCash().compareTo(requiredAmount) < 0) {
                log.warn("Insufficient funds for order: {}", order.getOrderNumber());
                return false;
            }
        }
        
        // Check if account has sufficient shares for sell orders
        if (order.getSide() == OrderSide.SELL) {
            PortfolioHolding holding = portfolioHoldingRepository.findByAccountIdAndSymbol(
                order.getAccountId(), order.getSymbol()).orElse(null);
            
            if (holding == null || holding.getQuantity().compareTo(order.getQuantity()) < 0) {
                log.warn("Insufficient shares for order: {}", order.getOrderNumber());
                return false;
            }
        }
        
        return true;
    }
    
    private BigDecimal determineExecutionPrice(TradingOrder order, BigDecimal currentPrice) {
        switch (order.getOrderType()) {
            case MARKET:
                return currentPrice;
            case LIMIT:
                if (order.getSide() == OrderSide.BUY) {
                    return order.getLimitPrice().min(currentPrice);
                } else {
                    return order.getLimitPrice().max(currentPrice);
                }
            default:
                return currentPrice;
        }
    }
    
    private BigDecimal calculateCommission(BigDecimal tradeValue) {
        // Commission-free trading
        return BigDecimal.ZERO;
    }
    
    private BigDecimal calculateFees(BigDecimal tradeValue) {
        // Minimal fees
        return BigDecimal.ZERO;
    }
    
    private void updatePortfolio(TradingOrder order) {
        PortfolioHolding holding = portfolioHoldingRepository.findByAccountIdAndSymbol(
            order.getAccountId(), order.getSymbol()).orElse(null);
        
        if (order.getSide() == OrderSide.BUY) {
            if (holding == null) {
                // Create new holding
                holding = PortfolioHolding.builder()
                    .accountId(order.getAccountId())
                    .symbol(order.getSymbol())
                    .quantity(order.getQuantity())
                    .averageCost(order.getFilledPrice())
                    .currentPrice(order.getFilledPrice())
                    .build();
            } else {
                // Update existing holding
                BigDecimal totalQuantity = holding.getQuantity().add(order.getQuantity());
                BigDecimal totalCost = holding.getCostBasis().add(order.getFilledValue());
                BigDecimal newAverageCost = totalCost.divide(totalQuantity, 4, BigDecimal.ROUND_HALF_UP);
                
                holding.setQuantity(totalQuantity);
                holding.setAverageCost(newAverageCost);
                holding.setCurrentPrice(order.getFilledPrice());
            }
        } else {
            // Sell order
            if (holding != null) {
                BigDecimal newQuantity = holding.getQuantity().subtract(order.getQuantity());
                if (newQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                    // Remove holding if quantity is zero or negative
                    portfolioHoldingRepository.delete(holding);
                    return;
                } else {
                    holding.setQuantity(newQuantity);
                }
            }
        }
        
        if (holding != null) {
            // Update calculated fields
            holding.setMarketValue(holding.getQuantity().multiply(holding.getCurrentPrice()));
            holding.setCostBasis(holding.getQuantity().multiply(holding.getAverageCost()));
            holding.setUnrealizedPnl(holding.getMarketValue().subtract(holding.getCostBasis()));
            
            if (holding.getCostBasis().compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal unrealizedPnlPercentage = holding.getUnrealizedPnl()
                    .divide(holding.getCostBasis(), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
                holding.setUnrealizedPnlPercentage(unrealizedPnlPercentage);
            }
            
            portfolioHoldingRepository.save(holding);
        }
    }
    
    private void updateAccountBalance(TradingOrder order) {
        InvestmentAccount account = investmentAccountRepository.findById(order.getAccountId())
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        if (order.getSide() == OrderSide.BUY) {
            // Deduct cost from available cash
            account.setAvailableCash(account.getAvailableCash().subtract(order.getTotalCost()));
        } else {
            // Add proceeds to available cash
            account.setAvailableCash(account.getAvailableCash().add(order.getFilledValue()));
        }
        
        investmentAccountRepository.save(account);
    }
    
    private void validateOrder(InvestmentAccount account, PlaceOrderRequest request) {
        // Basic validation
        if (request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Quantity must be positive");
        }
        
        if (request.getSide() == OrderSide.BUY && request.getPrice() != null && 
            request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Price must be positive for buy orders");
        }
    }
    
    private OrderResponse convertToResponse(TradingOrder order) {
        return OrderResponse.builder()
            .id(order.getId())
            .orderNumber(order.getOrderNumber())
            .symbol(order.getSymbol())
            .orderType(order.getOrderType().getDisplayName())
            .side(order.getSide().getDisplayName())
            .quantity(order.getQuantity())
            .price(order.getPrice())
            .stopPrice(order.getStopPrice())
            .limitPrice(order.getLimitPrice())
            .timeInForce(order.getTimeInForce().getDisplayName())
            .status(order.getStatus().getDisplayName())
            .filledQuantity(order.getFilledQuantity())
            .filledPrice(order.getFilledPrice())
            .filledValue(order.getFilledValue())
            .commission(order.getCommission())
            .fees(order.getFees())
            .totalCost(order.getTotalCost())
            .submittedAt(order.getSubmittedAt())
            .filledAt(order.getFilledAt())
            .cancelledAt(order.getCancelledAt())
            .expiresAt(order.getExpiresAt())
            .remainingQuantity(order.getRemainingQuantity())
            .isFullyFilled(order.isFullyFilled())
            .isPartiallyFilled(order.isPartiallyFilled())
            .build();
    }
}

