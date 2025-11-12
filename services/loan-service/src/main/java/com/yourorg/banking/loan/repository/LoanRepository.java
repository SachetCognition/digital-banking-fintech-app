package com.yourorg.banking.loan.repository;

import com.yourorg.banking.loan.model.Loan;
import com.yourorg.banking.loan.model.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID> {
    
    List<Loan> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    
    List<Loan> findByStatusOrderByCreatedAtDesc(LoanStatus status);
    
    Optional<Loan> findByLoanNumber(String loanNumber);
    
    @Query("SELECT l FROM Loan l WHERE l.customerId = :customerId AND l.status = :status")
    List<Loan> findByCustomerIdAndStatus(@Param("customerId") UUID customerId, 
                                        @Param("status") LoanStatus status);
    
    @Query("SELECT l FROM Loan l WHERE l.nextPaymentDate <= :date AND l.status = 'ACTIVE'")
    List<Loan> findLoansWithDuePayments(@Param("date") LocalDate date);
    
    @Query("SELECT l FROM Loan l WHERE l.nextPaymentDate < :date AND l.status = 'ACTIVE'")
    List<Loan> findOverdueLoans(@Param("date") LocalDate date);
    
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.customerId = :customerId AND l.status = :status")
    Long countByCustomerIdAndStatus(@Param("customerId") UUID customerId, 
                                   @Param("status") LoanStatus status);
}

