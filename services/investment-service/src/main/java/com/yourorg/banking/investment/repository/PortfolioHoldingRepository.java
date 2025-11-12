package com.yourorg.banking.investment.repository;

import com.yourorg.banking.investment.model.PortfolioHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PortfolioHoldingRepository extends JpaRepository<PortfolioHolding, UUID> {
    
    List<PortfolioHolding> findByAccountIdOrderByMarketValueDesc(UUID accountId);
    
    Optional<PortfolioHolding> findByAccountIdAndSymbol(UUID accountId, String symbol);
    
    @Query("SELECT ph FROM PortfolioHolding ph WHERE ph.accountId = :accountId AND ph.quantity > 0")
    List<PortfolioHolding> findActiveHoldingsByAccountId(@Param("accountId") UUID accountId);
    
    @Query("SELECT SUM(ph.marketValue) FROM PortfolioHolding ph WHERE ph.accountId = :accountId")
    Double getTotalMarketValueByAccountId(@Param("accountId") UUID accountId);
    
    @Query("SELECT SUM(ph.costBasis) FROM PortfolioHolding ph WHERE ph.accountId = :accountId")
    Double getTotalCostBasisByAccountId(@Param("accountId") UUID accountId);
    
    @Query("SELECT SUM(ph.unrealizedPnl) FROM PortfolioHolding ph WHERE ph.accountId = :accountId")
    Double getTotalUnrealizedPnlByAccountId(@Param("accountId") UUID accountId);
    
    @Query("SELECT ph.symbol, SUM(ph.quantity) FROM PortfolioHolding ph WHERE ph.accountId = :accountId GROUP BY ph.symbol")
    List<Object[]> getHoldingsSummaryByAccountId(@Param("accountId") UUID accountId);
}

