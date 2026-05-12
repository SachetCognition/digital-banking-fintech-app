package com.yourorg.banking.investment.service;

import com.yourorg.banking.investment.model.*;
import com.yourorg.banking.investment.repository.OrderRepository;
import com.yourorg.banking.investment.repository.PortfolioHoldingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradingServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PortfolioHoldingRepository portfolioHoldingRepository;

    @Mock
    private MarketDataService marketDataService;

    @InjectMocks
    private TradingService tradingService;

    @Test
    void placeMarketOrder_buy_succeeds() {
        UUID accountId = UUID.randomUUID();
        OrderRequest request = new OrderRequest(
            accountId, "AAPL", OrderType.MARKET, OrderSide.BUY,
            new BigDecimal("10"), null, null
        );

        when(marketDataService.getCurrentPrice("AAPL")).thenReturn(new BigDecimal("150.00"));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order order = tradingService.placeOrder(request);

        assertNotNull(order);
        assertEquals(OrderStatus.FILLED, order.getStatus());
        verify(portfolioHoldingRepository).save(any());
    }

    @Test
    void placeLimitOrder_priceNotMet_pendingStatus() {
        UUID accountId = UUID.randomUUID();
        OrderRequest request = new OrderRequest(
            accountId, "AAPL", OrderType.LIMIT, OrderSide.BUY,
            new BigDecimal("10"), new BigDecimal("140.00"), null
        );

        when(marketDataService.getCurrentPrice("AAPL")).thenReturn(new BigDecimal("150.00"));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order order = tradingService.placeOrder(request);

        assertNotNull(order);
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void placeStopOrder_belowStopPrice_pendingStatus() {
        UUID accountId = UUID.randomUUID();
        OrderRequest request = new OrderRequest(
            accountId, "AAPL", OrderType.STOP, OrderSide.SELL,
            new BigDecimal("5"), null, new BigDecimal("145.00")
        );

        when(marketDataService.getCurrentPrice("AAPL")).thenReturn(new BigDecimal("150.00"));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order order = tradingService.placeOrder(request);

        assertNotNull(order);
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }
}
