package com.yourorg.banking.payments.repository;

import com.yourorg.banking.payments.model.Beneficiary;
import com.yourorg.banking.payments.model.BeneficiaryStatus;
import com.yourorg.banking.payments.model.BeneficiaryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, UUID> {
    
    List<Beneficiary> findByCustomerIdAndStatus(UUID customerId, BeneficiaryStatus status);
    
    List<Beneficiary> findByCustomerIdAndTypeAndStatus(UUID customerId, BeneficiaryType type, BeneficiaryStatus status);
    
    Optional<Beneficiary> findByCustomerIdAndAccountNumberAndBankCode(UUID customerId, String accountNumber, String bankCode);
    
    @Query("SELECT b FROM Beneficiary b WHERE b.customerId = :customerId AND b.status = :status AND (LOWER(b.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(b.accountNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Beneficiary> findByCustomerIdAndStatusAndSearchTerm(@Param("customerId") UUID customerId, 
                                                           @Param("status") BeneficiaryStatus status, 
                                                           @Param("searchTerm") String searchTerm);
    
    @Query("SELECT COUNT(b) FROM Beneficiary b WHERE b.customerId = :customerId AND b.status = :status")
    long countByCustomerIdAndStatus(@Param("customerId") UUID customerId, @Param("status") BeneficiaryStatus status);
}

