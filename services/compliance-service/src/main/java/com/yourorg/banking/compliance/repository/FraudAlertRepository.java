package com.yourorg.banking.compliance.repository;

import com.yourorg.banking.compliance.model.FraudDetection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FraudAlertRepository extends JpaRepository<FraudDetection, UUID> {
}
