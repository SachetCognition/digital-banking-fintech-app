package com.yourorg.banking.compliance.repository;

import com.yourorg.banking.compliance.model.AmlRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AmlRuleRepository extends JpaRepository<AmlRule, UUID> {
    List<AmlRule> findByIsActiveTrue();
    List<AmlRule> findByRuleTypeAndIsActiveTrue(String ruleType);
    List<AmlRule> findBySeverityAndIsActiveTrue(String severity);
}

