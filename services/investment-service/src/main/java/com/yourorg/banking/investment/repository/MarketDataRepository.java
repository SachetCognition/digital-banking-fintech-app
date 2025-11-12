package com.yourorg.banking.investment.repository;

import com.yourorg.banking.investment.model.MarketData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MarketDataRepository extends JpaRepository<MarketData, UUID> {
    
    Optional<MarketData> findTopBySymbolOrderByLastUpdatedDesc(String symbol);
    
    List<MarketData> findBySymbolOrderByLastUpdatedDesc(String symbol);
    
    @Query("SELECT md FROM MarketData md WHERE md.symbol IN :symbols AND md.lastUpdated >= :since ORDER BY md.lastUpdated DESC")
    List<MarketData> findBySymbolInAndLastUpdatedAfter(@Param("symbols") List<String> symbols, 
                                                      @Param("since") LocalDateTime since);
    
    @Query("SELECT md FROM MarketData md WHERE md.lastUpdated >= :since ORDER BY md.lastUpdated DESC")
    List<MarketData> findByLastUpdatedAfter(@Param("since") LocalDateTime since);
    
    @Query("SELECT md FROM MarketData md WHERE md.symbol = :symbol AND md.lastUpdated = (SELECT MAX(md2.lastUpdated) FROM MarketData md2 WHERE md2.symbol = :symbol)")
    Optional<MarketData> findLatestBySymbol(@Param("symbol") String symbol);
    
    @Query("SELECT COUNT(md) FROM MarketData md WHERE md.lastUpdated >= :since")
    Long countByLastUpdatedAfter(@Param("since") LocalDateTime since);
}

