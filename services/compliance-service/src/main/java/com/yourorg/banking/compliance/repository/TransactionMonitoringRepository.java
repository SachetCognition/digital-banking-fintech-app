package com.yourorg.banking.compliance.repository;

import com.yourorg.banking.compliance.model.TransactionMonitoring;
import com.yourorg.banking.compliance.model.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionMonitoringRepository extends JpaRepository<TransactionMonitoring, UUID> {
    List<TransactionMonitoring> findByIsFlaggedTrueOrderByCreatedAtDesc();
    List<TransactionMonitoring> findByCustomerIdOrderByTransactionDateDesc(UUID customerId);
    List<TransactionMonitoring> findByReviewStatusOrderByCreatedAtDesc(ReviewStatus reviewStatus);
    List<TransactionMonitoring> findByTransactionId(UUID transactionId);
    List<TransactionMonitoring> findByTransactionTypeOrderByTransactionDateDesc(String transactionType);
}

