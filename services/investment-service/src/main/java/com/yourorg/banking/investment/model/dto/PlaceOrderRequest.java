package com.yourorg.banking.investment.model.dto;

import com.yourorg.banking.investment.model.OrderSide;
import com.yourorg.banking.investment.model.OrderType;
import com.yourorg.banking.investment.model.TimeInForce;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceOrderRequest {
    private String symbol;
    private OrderType orderType;
    private OrderSide side;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal stopPrice;
    private BigDecimal limitPrice;
    private TimeInForce timeInForce;
    private LocalDateTime expiresAt;
}

