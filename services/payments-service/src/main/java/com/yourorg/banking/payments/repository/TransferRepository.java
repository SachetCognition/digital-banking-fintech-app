package com.yourorg.banking.payments.repository;

import com.yourorg.banking.payments.model.Transfer;
import com.yourorg.banking.payments.model.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, UUID> {
    
    Optional<Transfer> findByIdempotencyKey(String idempotencyKey);
    
    List<Transfer> findByPayerAccountIdAndStatus(UUID payerAccountId, TransferStatus status);
    
    List<Transfer> findByPayeeAccountIdAndStatus(UUID payeeAccountId, TransferStatus status);
    
    @Query("SELECT t FROM Transfer t WHERE t.payerAccountId = :accountId AND t.status = 'COMPLETED' AND t.createdAt >= :fromDate")
    List<Transfer> findCompletedTransfersByPayerSince(@Param("accountId") UUID accountId, @Param("fromDate") Instant fromDate);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transfer t WHERE t.payerAccountId = :accountId AND t.status = 'COMPLETED' AND t.createdAt >= :fromDate")
    Double getTotalTransferredAmountByPayerSince(@Param("accountId") UUID accountId, @Param("fromDate") Instant fromDate);
}

