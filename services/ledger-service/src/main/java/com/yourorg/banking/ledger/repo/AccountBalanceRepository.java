package com.yourorg.banking.ledger.repo;

import com.yourorg.banking.ledger.model.AccountBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountBalanceRepository extends JpaRepository<AccountBalance, UUID> {
    
    Optional<AccountBalance> findByAccountId(UUID accountId);
    
    List<AccountBalance> findByCurrency(String currency);
    
    @Query("SELECT ab FROM AccountBalance ab WHERE ab.accountId = :accountId AND ab.currency = :currency")
    Optional<AccountBalance> findByAccountIdAndCurrency(@Param("accountId") UUID accountId, @Param("currency") String currency);
}

