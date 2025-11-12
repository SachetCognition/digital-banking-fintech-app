package com.yourorg.banking.payments.repository;

import com.yourorg.banking.payments.model.PaymentTemplate;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentTemplateRepository extends CrudRepository<PaymentTemplate, UUID> {
    
    List<PaymentTemplate> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    
    List<PaymentTemplate> findByCustomerIdAndIsFavoriteOrderByCreatedAtDesc(
        UUID customerId, boolean isFavorite);
    
    @Query("SELECT * FROM payment_templates WHERE customer_id = :customerId " +
           "AND (template_name ILIKE :searchTerm OR to_account_name ILIKE :searchTerm) " +
           "ORDER BY is_favorite DESC, created_at DESC")
    List<PaymentTemplate> findByCustomerIdAndSearchTerm(
        @Param("customerId") UUID customerId,
        @Param("searchTerm") String searchTerm);
    
    @Query("SELECT COUNT(*) FROM payment_templates WHERE customer_id = :customerId")
    long countByCustomerId(@Param("customerId") UUID customerId);
}

