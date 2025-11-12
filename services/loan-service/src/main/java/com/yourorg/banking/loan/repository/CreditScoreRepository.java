package com.yourorg.banking.loan.repository;

import com.yourorg.banking.loan.model.CreditScore;
import com.yourorg.banking.loan.model.CreditScoreType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditScoreRepository extends JpaRepository<CreditScore, UUID> {
    
    List<CreditScore> findByCustomerIdOrderByCalculatedAtDesc(UUID customerId);
    
    Optional<CreditScore> findTopByCustomerIdAndScoreTypeOrderByCalculatedAtDesc(UUID customerId, CreditScoreType scoreType);
    
    @Query("SELECT cs FROM CreditScore cs WHERE cs.customerId = :customerId AND cs.calculatedAt >= :since")
    List<CreditScore> findByCustomerIdAndCalculatedAtAfter(@Param("customerId") UUID customerId, 
                                                          @Param("since") LocalDateTime since);
    
    @Query("SELECT cs FROM CreditScore cs WHERE cs.customerId = :customerId AND cs.expiresAt > :now ORDER BY cs.calculatedAt DESC")
    List<CreditScore> findValidScoresByCustomerId(@Param("customerId") UUID customerId, 
                                                 @Param("now") LocalDateTime now);
    
    @Query("SELECT AVG(cs.score) FROM CreditScore cs WHERE cs.customerId = :customerId AND cs.calculatedAt >= :since")
    Double getAverageScore(@Param("customerId") UUID customerId, 
                          @Param("since") LocalDateTime since);
}

