package com.yourorg.banking.loan.repository;

import com.yourorg.banking.loan.model.LoanPayment;
import com.yourorg.banking.loan.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface LoanPaymentRepository extends JpaRepository<LoanPayment, UUID> {
    
    List<LoanPayment> findByLoanIdOrderByScheduledDateAsc(UUID loanId);
    
    List<LoanPayment> findByStatusOrderByScheduledDateAsc(PaymentStatus status);
    
    @Query("SELECT lp FROM LoanPayment lp WHERE lp.loanId = :loanId AND lp.status = :status")
    List<LoanPayment> findByLoanIdAndStatus(@Param("loanId") UUID loanId, 
                                           @Param("status") PaymentStatus status);
    
    @Query("SELECT lp FROM LoanPayment lp WHERE lp.scheduledDate <= :date AND lp.status IN ('PENDING', 'DUE')")
    List<LoanPayment> findDuePayments(@Param("date") LocalDate date);
    
    @Query("SELECT lp FROM LoanPayment lp WHERE lp.scheduledDate < :date AND lp.status IN ('PENDING', 'DUE')")
    List<LoanPayment> findOverduePayments(@Param("date") LocalDate date);
    
    @Query("SELECT SUM(lp.totalAmount) FROM LoanPayment lp WHERE lp.loanId = :loanId AND lp.status = 'PAID'")
    Double getTotalPaidAmount(@Param("loanId") UUID loanId);
    
    @Query("SELECT SUM(lp.totalAmount) FROM LoanPayment lp WHERE lp.loanId = :loanId AND lp.status IN ('PENDING', 'DUE')")
    Double getTotalOutstandingAmount(@Param("loanId") UUID loanId);
}

