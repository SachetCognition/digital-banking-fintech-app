package com.yourorg.banking.loan.repository;

import com.yourorg.banking.loan.model.CreditTransaction;
import com.yourorg.banking.loan.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, UUID> {
    
    List<CreditTransaction> findByCreditLineIdOrderByTransactionDateDesc(UUID creditLineId);
    
    List<CreditTransaction> findByStatusOrderByTransactionDateDesc(TransactionStatus status);
    
    @Query("SELECT ct FROM CreditTransaction ct WHERE ct.creditLineId = :creditLineId AND ct.status = :status")
    List<CreditTransaction> findByCreditLineIdAndStatus(@Param("creditLineId") UUID creditLineId, 
                                                       @Param("status") TransactionStatus status);
    
    @Query("SELECT ct FROM CreditTransaction ct WHERE ct.creditLineId = :creditLineId AND ct.transactionDate BETWEEN :startDate AND :endDate")
    List<CreditTransaction> findByCreditLineIdAndTransactionDateBetween(@Param("creditLineId") UUID creditLineId, 
                                                                       @Param("startDate") LocalDateTime startDate, 
                                                                       @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(ct.amount) FROM CreditTransaction ct WHERE ct.creditLineId = :creditLineId AND ct.status = 'COMPLETED'")
    Double getTotalTransactionAmount(@Param("creditLineId") UUID creditLineId);
}

