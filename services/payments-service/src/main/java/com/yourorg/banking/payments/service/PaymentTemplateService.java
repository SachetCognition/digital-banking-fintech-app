package com.yourorg.banking.payments.service;

import com.yourorg.banking.payments.model.*;
import com.yourorg.banking.payments.repository.PaymentTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PaymentTemplateService {
    
    @Autowired
    private PaymentTemplateRepository paymentTemplateRepository;
    
    public PaymentTemplateResponse createPaymentTemplate(UUID customerId, PaymentTemplateRequest request) {
        // Check if template name already exists for this customer
        List<PaymentTemplate> existingTemplates = paymentTemplateRepository
            .findByCustomerIdOrderByCreatedAtDesc(customerId);
        
        boolean nameExists = existingTemplates.stream()
            .anyMatch(template -> template.templateName().equals(request.templateName()));
        
        if (nameExists) {
            throw new RuntimeException("Template name already exists");
        }
        
        // Create payment template
        PaymentTemplate template = new PaymentTemplate(
            UUID.randomUUID(),
            customerId,
            request.templateName(),
            request.fromAccountId(),
            request.toAccountNumber(),
            request.toBankCode(),
            request.toBankName(),
            request.toAccountName(),
            request.amount(),
            request.currency(),
            request.description(),
            false, // isFavorite
            java.time.LocalDateTime.now(),
            java.time.LocalDateTime.now()
        );
        
        template = paymentTemplateRepository.save(template);
        
        return PaymentTemplateResponse.from(template);
    }
    
    public PaymentTemplateResponse getPaymentTemplate(UUID customerId, UUID templateId) {
        PaymentTemplate template = paymentTemplateRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Payment template not found"));
        
        if (!template.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        return PaymentTemplateResponse.from(template);
    }
    
    public List<PaymentTemplateResponse> getPaymentTemplates(UUID customerId) {
        return paymentTemplateRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
            .stream()
            .map(PaymentTemplateResponse::from)
            .toList();
    }
    
    public List<PaymentTemplateResponse> getFavoritePaymentTemplates(UUID customerId) {
        return paymentTemplateRepository.findByCustomerIdAndIsFavoriteOrderByCreatedAtDesc(customerId, true)
            .stream()
            .map(PaymentTemplateResponse::from)
            .toList();
    }
    
    public List<PaymentTemplateResponse> searchPaymentTemplates(UUID customerId, String searchTerm) {
        return paymentTemplateRepository.findByCustomerIdAndSearchTerm(customerId, "%" + searchTerm + "%")
            .stream()
            .map(PaymentTemplateResponse::from)
            .toList();
    }
    
    public PaymentTemplateResponse updatePaymentTemplate(UUID customerId, UUID templateId, PaymentTemplateRequest request) {
        PaymentTemplate template = paymentTemplateRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Payment template not found"));
        
        if (!template.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        // Update template
        PaymentTemplate updated = new PaymentTemplate(
            template.id(),
            template.customerId(),
            request.templateName(),
            request.fromAccountId(),
            request.toAccountNumber(),
            request.toBankCode(),
            request.toBankName(),
            request.toAccountName(),
            request.amount(),
            request.currency(),
            request.description(),
            template.isFavorite(),
            template.createdAt(),
            java.time.LocalDateTime.now()
        );
        
        updated = paymentTemplateRepository.save(updated);
        
        return PaymentTemplateResponse.from(updated);
    }
    
    public PaymentTemplateResponse toggleFavorite(UUID customerId, UUID templateId) {
        PaymentTemplate template = paymentTemplateRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Payment template not found"));
        
        if (!template.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        PaymentTemplate updated = template.withFavorite(!template.isFavorite());
        updated = paymentTemplateRepository.save(updated);
        
        return PaymentTemplateResponse.from(updated);
    }
    
    public void deletePaymentTemplate(UUID customerId, UUID templateId) {
        PaymentTemplate template = paymentTemplateRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Payment template not found"));
        
        if (!template.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        paymentTemplateRepository.deleteById(templateId);
    }
}

