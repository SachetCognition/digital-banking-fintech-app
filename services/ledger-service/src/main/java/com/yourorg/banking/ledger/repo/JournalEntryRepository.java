package com.yourorg.banking.ledger.repo;

import com.yourorg.banking.ledger.model.EntryType;
import com.yourorg.banking.ledger.model.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {
    
    List<JournalEntry> findByJournalId(UUID journalId);
    
    List<JournalEntry> findByAccountId(UUID accountId);
    
    List<JournalEntry> findByAccountIdAndType(UUID accountId, EntryType type);
    
    @Query("SELECT je FROM JournalEntry je WHERE je.accountId = :accountId AND je.createdAt >= :fromDate AND je.createdAt <= :toDate ORDER BY je.createdAt ASC")
    List<JournalEntry> findByAccountIdAndDateRange(@Param("accountId") UUID accountId, 
                                                  @Param("fromDate") Instant fromDate, 
                                                  @Param("toDate") Instant toDate);
    
    @Query("SELECT SUM(je.amount) FROM JournalEntry je WHERE je.accountId = :accountId AND je.type = :type")
    BigDecimal getTotalAmountByAccountIdAndType(@Param("accountId") UUID accountId, @Param("type") EntryType type);
}

