package com.yourorg.banking.loan.repository;

import com.yourorg.banking.loan.model.InterestCalculation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface InterestCalculationRepository extends JpaRepository<InterestCalculation, UUID> {
    
    List<InterestCalculation> findByLoanIdOrderByCalculationDateDesc(UUID loanId);
    
    List<InterestCalculation> findByCreditLineIdOrderByCalculationDateDesc(UUID creditLineId);
    
    @Query("SELECT ic FROM InterestCalculation ic WHERE ic.loanId = :loanId AND ic.calculationDate = :date")
    List<InterestCalculation> findByLoanIdAndCalculationDate(@Param("loanId") UUID loanId, 
                                                           @Param("date") LocalDate date);
    
    @Query("SELECT ic FROM InterestCalculation ic WHERE ic.creditLineId = :creditLineId AND ic.calculationDate = :date")
    List<InterestCalculation> findByCreditLineIdAndCalculationDate(@Param("creditLineId") UUID creditLineId, 
                                                                 @Param("date") LocalDate date);
    
    @Query("SELECT SUM(ic.accruedInterest) FROM InterestCalculation ic WHERE ic.loanId = :loanId")
    Double getTotalAccruedInterest(@Param("loanId") UUID loanId);
    
    @Query("SELECT SUM(ic.accruedInterest) FROM InterestCalculation ic WHERE ic.creditLineId = :creditLineId")
    Double getTotalAccruedInterestForCreditLine(@Param("creditLineId") UUID creditLineId);
}

