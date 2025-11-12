package com.yourorg.banking.ledger.repo;

import com.yourorg.banking.ledger.model.Journal;
import com.yourorg.banking.ledger.model.JournalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JournalRepository extends JpaRepository<Journal, UUID> {
    
    Optional<Journal> findByReference(String reference);
    
    List<Journal> findByStatus(JournalStatus status);
    
    @Query("SELECT j FROM Journal j WHERE j.createdAt >= :fromDate AND j.createdAt <= :toDate")
    List<Journal> findByDateRange(@Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);
    
    @Query("SELECT j FROM Journal j WHERE j.status = :status AND j.createdAt < :beforeDate")
    List<Journal> findPendingJournalsOlderThan(@Param("status") JournalStatus status, @Param("beforeDate") Instant beforeDate);
}

