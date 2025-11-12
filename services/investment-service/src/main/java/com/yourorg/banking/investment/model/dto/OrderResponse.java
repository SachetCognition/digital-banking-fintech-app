package com.yourorg.banking.investment.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private UUID id;
    private String orderNumber;
    private String symbol;
    private String orderType;
    private String side;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal stopPrice;
    private BigDecimal limitPrice;
    private String timeInForce;
    private String status;
    private BigDecimal filledQuantity;
    private BigDecimal filledPrice;
    private BigDecimal filledValue;
    private BigDecimal commission;
    private BigDecimal fees;
    private BigDecimal totalCost;
    private LocalDateTime submittedAt;
    private LocalDateTime filledAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime expiresAt;
    private BigDecimal remainingQuantity;
    private boolean isFullyFilled;
    private boolean isPartiallyFilled;
}

