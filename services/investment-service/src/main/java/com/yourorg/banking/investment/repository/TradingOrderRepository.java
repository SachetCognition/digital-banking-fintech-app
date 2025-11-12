package com.yourorg.banking.investment.repository;

import com.yourorg.banking.investment.model.OrderStatus;
import com.yourorg.banking.investment.model.TradingOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TradingOrderRepository extends JpaRepository<TradingOrder, UUID> {
    
    List<TradingOrder> findByAccountIdOrderBySubmittedAtDesc(UUID accountId);
    
    List<TradingOrder> findByStatusOrderBySubmittedAtDesc(OrderStatus status);
    
    Optional<TradingOrder> findByOrderNumber(String orderNumber);
    
    @Query("SELECT to FROM TradingOrder to WHERE to.accountId = :accountId AND to.status = :status")
    List<TradingOrder> findByAccountIdAndStatus(@Param("accountId") UUID accountId, 
                                               @Param("status") OrderStatus status);
    
    @Query("SELECT to FROM TradingOrder to WHERE to.symbol = :symbol AND to.status IN ('PENDING', 'SUBMITTED', 'PARTIALLY_FILLED')")
    List<TradingOrder> findActiveOrdersBySymbol(@Param("symbol") String symbol);
    
    @Query("SELECT to FROM TradingOrder to WHERE to.submittedAt BETWEEN :startDate AND :endDate")
    List<TradingOrder> findBySubmittedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(to.filledValue) FROM TradingOrder to WHERE to.accountId = :accountId AND to.status = 'FILLED'")
    Double getTotalTradingValueByAccountId(@Param("accountId") UUID accountId);
}

