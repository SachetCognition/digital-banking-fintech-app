package com.yourorg.banking.loan.repository;

import com.yourorg.banking.loan.model.LoanApplication;
import com.yourorg.banking.loan.model.LoanApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, UUID> {
    
    List<LoanApplication> findByCustomerIdOrderBySubmittedAtDesc(UUID customerId);
    
    List<LoanApplication> findByStatusOrderBySubmittedAtDesc(LoanApplicationStatus status);
    
    Optional<LoanApplication> findByApplicationNumber(String applicationNumber);
    
    @Query("SELECT la FROM LoanApplication la WHERE la.customerId = :customerId AND la.status = :status")
    List<LoanApplication> findByCustomerIdAndStatus(@Param("customerId") UUID customerId, 
                                                   @Param("status") LoanApplicationStatus status);
    
    @Query("SELECT la FROM LoanApplication la WHERE la.submittedAt BETWEEN :startDate AND :endDate")
    List<LoanApplication> findBySubmittedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(la) FROM LoanApplication la WHERE la.customerId = :customerId AND la.status = :status")
    Long countByCustomerIdAndStatus(@Param("customerId") UUID customerId, 
                                   @Param("status") LoanApplicationStatus status);
}

