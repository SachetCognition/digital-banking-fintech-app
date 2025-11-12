package com.yourorg.banking.investment.repository;

import com.yourorg.banking.investment.model.AccountStatus;
import com.yourorg.banking.investment.model.InvestmentAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvestmentAccountRepository extends JpaRepository<InvestmentAccount, UUID> {
    
    List<InvestmentAccount> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    
    List<InvestmentAccount> findByStatusOrderByCreatedAtDesc(AccountStatus status);
    
    Optional<InvestmentAccount> findByAccountNumber(String accountNumber);
    
    @Query("SELECT ia FROM InvestmentAccount ia WHERE ia.customerId = :customerId AND ia.status = :status")
    List<InvestmentAccount> findByCustomerIdAndStatus(@Param("customerId") UUID customerId, 
                                                     @Param("status") AccountStatus status);
    
    @Query("SELECT SUM(ia.currentBalance) FROM InvestmentAccount ia WHERE ia.customerId = :customerId AND ia.status = 'ACTIVE'")
    Double getTotalBalanceByCustomerId(@Param("customerId") UUID customerId);
    
    @Query("SELECT SUM(ia.investedAmount) FROM InvestmentAccount ia WHERE ia.customerId = :customerId AND ia.status = 'ACTIVE'")
    Double getTotalInvestedAmountByCustomerId(@Param("customerId") UUID customerId);
}

