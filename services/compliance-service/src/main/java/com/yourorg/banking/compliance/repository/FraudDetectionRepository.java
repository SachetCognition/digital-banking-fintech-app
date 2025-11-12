package com.yourorg.banking.compliance.repository;

import com.yourorg.banking.compliance.model.FraudDetection;
import com.yourorg.banking.compliance.model.FraudType;
import com.yourorg.banking.compliance.model.InvestigationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FraudDetectionRepository extends JpaRepository<FraudDetection, UUID> {
    List<FraudDetection> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    List<FraudDetection> findByFraudTypeOrderByCreatedAtDesc(FraudType fraudType);
    List<FraudDetection> findByInvestigationStatusOrderByCreatedAtDesc(InvestigationStatus investigationStatus);
    List<FraudDetection> findByInvestigationStatusInOrderByCreatedAtDesc(List<InvestigationStatus> statuses);
    List<FraudDetection> findByTransactionId(UUID transactionId);
    List<FraudDetection> findByIsConfirmedTrue();
    List<FraudDetection> findByIsFalsePositiveTrue();
}

