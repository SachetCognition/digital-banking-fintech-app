package com.yourorg.banking.account.repo;

import com.yourorg.banking.account.model.Account;
import com.yourorg.banking.account.model.AccountStatus;
import com.yourorg.banking.account.model.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    
    List<Account> findByCustomerId(UUID customerId);
    
    List<Account> findByCustomerIdAndStatus(UUID customerId, AccountStatus status);
    
    Optional<Account> findByAccountNumber(String accountNumber);
    
    List<Account> findByCustomerIdAndType(UUID customerId, AccountType type);
    
    @Query("SELECT a FROM Account a WHERE a.customerId = :customerId AND a.status = :status AND a.currency = :currency")
    List<Account> findByCustomerIdAndStatusAndCurrency(@Param("customerId") UUID customerId, 
                                                      @Param("status") AccountStatus status, 
                                                      @Param("currency") String currency);
    
    @Query("SELECT COUNT(a) FROM Account a WHERE a.customerId = :customerId AND a.status = :status")
    long countByCustomerIdAndStatus(@Param("customerId") UUID customerId, @Param("status") AccountStatus status);
    
    @Query("SELECT a FROM Account a WHERE a.accountNumber LIKE :pattern")
    List<Account> findByAccountNumberPattern(@Param("pattern") String pattern);
}

