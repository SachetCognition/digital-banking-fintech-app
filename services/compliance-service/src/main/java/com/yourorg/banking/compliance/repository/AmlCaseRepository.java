package com.yourorg.banking.compliance.repository;

import com.yourorg.banking.compliance.model.AmlCase;
import com.yourorg.banking.compliance.model.CaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AmlCaseRepository extends JpaRepository<AmlCase, UUID> {
    List<AmlCase> findByStatusOrderByCreatedAtDesc(CaseStatus status);
    List<AmlCase> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    List<AmlCase> findByAssignedToOrderByCreatedAtDesc(String assignedTo);
    boolean existsByCustomerIdAndDescriptionContaining(UUID customerId, String description);
    List<AmlCase> findByStatusAndPriorityOrderByCreatedAtDesc(CaseStatus status, String priority);
}

