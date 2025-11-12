package com.yourorg.banking.loan.repository;

import com.yourorg.banking.loan.model.CreditLine;
import com.yourorg.banking.loan.model.CreditLineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditLineRepository extends JpaRepository<CreditLine, UUID> {
    
    List<CreditLine> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    
    List<CreditLine> findByStatusOrderByCreatedAtDesc(CreditLineStatus status);
    
    Optional<CreditLine> findByCreditLineNumber(String creditLineNumber);
    
    @Query("SELECT cl FROM CreditLine cl WHERE cl.customerId = :customerId AND cl.status = :status")
    List<CreditLine> findByCustomerIdAndStatus(@Param("customerId") UUID customerId, 
                                              @Param("status") CreditLineStatus status);
    
    @Query("SELECT cl FROM CreditLine cl WHERE cl.usedCredit > cl.creditLimit")
    List<CreditLine> findOverLimitCreditLines();
    
    @Query("SELECT SUM(cl.creditLimit) FROM CreditLine cl WHERE cl.customerId = :customerId AND cl.status = 'ACTIVE'")
    Double getTotalCreditLimit(@Param("customerId") UUID customerId);
    
    @Query("SELECT SUM(cl.usedCredit) FROM CreditLine cl WHERE cl.customerId = :customerId AND cl.status = 'ACTIVE'")
    Double getTotalUsedCredit(@Param("customerId") UUID customerId);
}

