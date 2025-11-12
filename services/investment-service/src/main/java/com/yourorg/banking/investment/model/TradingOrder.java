package com.yourorg.banking.investment.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "trading_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradingOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "account_id", nullable = false)
    private UUID accountId;
    
    @Column(name = "order_number", unique = true, nullable = false)
    private String orderNumber;
    
    @Column(name = "symbol", nullable = false)
    private String symbol;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false)
    private OrderSide side;
    
    @Column(name = "quantity", nullable = false, precision = 15, scale = 6)
    private BigDecimal quantity;
    
    @Column(name = "price", precision = 15, scale = 4)
    private BigDecimal price;
    
    @Column(name = "stop_price", precision = 15, scale = 4)
    private BigDecimal stopPrice;
    
    @Column(name = "limit_price", precision = 15, scale = 4)
    private BigDecimal limitPrice;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "time_in_force")
    private TimeInForce timeInForce;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;
    
    @Column(name = "filled_quantity", precision = 15, scale = 6)
    private BigDecimal filledQuantity;
    
    @Column(name = "filled_price", precision = 15, scale = 4)
    private BigDecimal filledPrice;
    
    @Column(name = "filled_value", precision = 15, scale = 2)
    private BigDecimal filledValue;
    
    @Column(name = "commission", precision = 15, scale = 2)
    private BigDecimal commission;
    
    @Column(name = "fees", precision = 15, scale = 2)
    private BigDecimal fees;
    
    @Column(name = "total_cost", precision = 15, scale = 2)
    private BigDecimal totalCost;
    
    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;
    
    @Column(name = "filled_at")
    private LocalDateTime filledAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
        if (filledQuantity == null) {
            filledQuantity = BigDecimal.ZERO;
        }
        if (commission == null) {
            commission = BigDecimal.ZERO;
        }
        if (fees == null) {
            fees = BigDecimal.ZERO;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public boolean isFullyFilled() {
        return filledQuantity != null && filledQuantity.compareTo(quantity) >= 0;
    }
    
    public boolean isPartiallyFilled() {
        return filledQuantity != null && filledQuantity.compareTo(BigDecimal.ZERO) > 0 && !isFullyFilled();
    }
    
    public BigDecimal getRemainingQuantity() {
        return quantity.subtract(filledQuantity != null ? filledQuantity : BigDecimal.ZERO);
    }
}

