package com.yourorg.banking.investment.repository;

import com.yourorg.banking.investment.model.AssetClass;
import com.yourorg.banking.investment.model.InvestmentProduct;
import com.yourorg.banking.investment.model.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvestmentProductRepository extends JpaRepository<InvestmentProduct, UUID> {
    
    Optional<InvestmentProduct> findBySymbol(String symbol);
    
    List<InvestmentProduct> findByProductTypeOrderByNameAsc(ProductType productType);
    
    List<InvestmentProduct> findByAssetClassOrderByNameAsc(AssetClass assetClass);
    
    List<InvestmentProduct> findByIsActiveTrueOrderByNameAsc();
    
    @Query("SELECT ip FROM InvestmentProduct ip WHERE ip.symbol LIKE %:symbol% OR ip.name LIKE %:name%")
    List<InvestmentProduct> findBySymbolOrNameContaining(@Param("symbol") String symbol, 
                                                        @Param("name") String name);
    
    @Query("SELECT ip FROM InvestmentProduct ip WHERE ip.sector = :sector AND ip.isActive = true")
    List<InvestmentProduct> findBySectorAndActive(@Param("sector") String sector);
    
    @Query("SELECT DISTINCT ip.sector FROM InvestmentProduct ip WHERE ip.sector IS NOT NULL ORDER BY ip.sector")
    List<String> findDistinctSectors();
}

