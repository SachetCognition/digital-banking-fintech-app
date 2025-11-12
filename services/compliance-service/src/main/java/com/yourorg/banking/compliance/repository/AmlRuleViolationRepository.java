package com.yourorg.banking.compliance.repository;

import com.yourorg.banking.compliance.model.AmlRuleViolation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AmlRuleViolationRepository extends JpaRepository<AmlRuleViolation, UUID> {
    List<AmlRuleViolation> findByCaseIdOrderByViolationTimestampDesc(UUID caseId);
    List<AmlRuleViolation> findByRuleIdOrderByViolationTimestampDesc(UUID ruleId);
    List<AmlRuleViolation> findByIsResolvedFalse();
    List<AmlRuleViolation> findByTransactionId(UUID transactionId);
}

