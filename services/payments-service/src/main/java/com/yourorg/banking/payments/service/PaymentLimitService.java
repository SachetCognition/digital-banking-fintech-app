package com.yourorg.banking.payments.service;

import com.yourorg.banking.payments.model.*;
import com.yourorg.banking.payments.repository.PaymentLimitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PaymentLimitService {
    
    @Autowired
    private PaymentLimitRepository paymentLimitRepository;
    
    public PaymentLimitResponse createPaymentLimit(UUID customerId, PaymentLimitRequest request) {
        // Check if limit already exists for this customer/account and type
        Optional<PaymentLimit> existingLimit = paymentLimitRepository
            .findActiveByCustomerIdAndType(customerId, request.limitType(), LocalDateTime.now());
        
        if (existingLimit.isPresent()) {
            throw new RuntimeException("Active limit already exists for this type");
        }
        
        // Create payment limit
        PaymentLimit limit = new PaymentLimit(
            UUID.randomUUID(),
            customerId,
            request.accountId(),
            request.limitType(),
            request.limitValue(),
            request.currency(),
            BigDecimal.ZERO, // currentUsage
            LocalDateTime.now(), // usagePeriodStart
            true, // isActive
            LocalDateTime.now(),
            LocalDateTime.now(),
            request.expiresAt()
        );
        
        limit = paymentLimitRepository.save(limit);
        
        return PaymentLimitResponse.from(limit);
    }
    
    public PaymentLimitResponse getPaymentLimit(UUID customerId, UUID limitId) {
        PaymentLimit limit = paymentLimitRepository.findById(limitId)
            .orElseThrow(() -> new RuntimeException("Payment limit not found"));
        
        if (!limit.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        return PaymentLimitResponse.from(limit);
    }
    
    public List<PaymentLimitResponse> getPaymentLimits(UUID customerId) {
        return paymentLimitRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
            .stream()
            .map(PaymentLimitResponse::from)
            .toList();
    }
    
    public List<PaymentLimitResponse> getActivePaymentLimits(UUID customerId) {
        return paymentLimitRepository.findByCustomerIdAndIsActiveOrderByCreatedAtDesc(customerId, true)
            .stream()
            .map(PaymentLimitResponse::from)
            .toList();
    }
    
    public List<PaymentLimitResponse> getAccountPaymentLimits(UUID customerId, UUID accountId) {
        // Verify customer has access to this account
        List<PaymentLimit> customerLimits = paymentLimitRepository
            .findByCustomerIdOrderByCreatedAtDesc(customerId);
        
        return paymentLimitRepository.findByAccountIdOrderByCreatedAtDesc(accountId)
            .stream()
            .filter(limit -> limit.customerId().equals(customerId))
            .map(PaymentLimitResponse::from)
            .toList();
    }
    
    public PaymentLimitResponse updatePaymentLimit(UUID customerId, UUID limitId, PaymentLimitRequest request) {
        PaymentLimit limit = paymentLimitRepository.findById(limitId)
            .orElseThrow(() -> new RuntimeException("Payment limit not found"));
        
        if (!limit.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        // Update limit
        PaymentLimit updated = new PaymentLimit(
            limit.id(),
            limit.customerId(),
            request.accountId(),
            request.limitType(),
            request.limitValue(),
            request.currency(),
            limit.currentUsage(),
            limit.usagePeriodStart(),
            limit.isActive(),
            limit.createdAt(),
            LocalDateTime.now(),
            request.expiresAt()
        );
        
        updated = paymentLimitRepository.save(updated);
        
        return PaymentLimitResponse.from(updated);
    }
    
    public PaymentLimitResponse togglePaymentLimit(UUID customerId, UUID limitId) {
        PaymentLimit limit = paymentLimitRepository.findById(limitId)
            .orElseThrow(() -> new RuntimeException("Payment limit not found"));
        
        if (!limit.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        PaymentLimit updated = limit.withActive(!limit.isActive());
        updated = paymentLimitRepository.save(updated);
        
        return PaymentLimitResponse.from(updated);
    }
    
    public void deletePaymentLimit(UUID customerId, UUID limitId) {
        PaymentLimit limit = paymentLimitRepository.findById(limitId)
            .orElseThrow(() -> new RuntimeException("Payment limit not found"));
        
        if (!limit.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        paymentLimitRepository.deleteById(limitId);
    }
    
    public PaymentLimitResponse resetUsage(UUID customerId, UUID limitId) {
        PaymentLimit limit = paymentLimitRepository.findById(limitId)
            .orElseThrow(() -> new RuntimeException("Payment limit not found"));
        
        if (!limit.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        PaymentLimit updated = limit.withResetUsage();
        updated = paymentLimitRepository.save(updated);
        
        return PaymentLimitResponse.from(updated);
    }
    
    public boolean checkLimit(UUID customerId, UUID accountId, PaymentLimitType limitType, BigDecimal amount) {
        // Check customer-level limits first
        Optional<PaymentLimit> customerLimit = paymentLimitRepository
            .findActiveByCustomerIdAndType(customerId, limitType, LocalDateTime.now());
        
        if (customerLimit.isPresent()) {
            if (customerLimit.get().isExceeded()) {
                return false;
            }
        }
        
        // Check account-level limits
        Optional<PaymentLimit> accountLimit = paymentLimitRepository
            .findActiveByAccountIdAndType(accountId, limitType, LocalDateTime.now());
        
        if (accountLimit.isPresent()) {
            if (accountLimit.get().isExceeded()) {
                return false;
            }
        }
        
        return true;
    }
    
    public void updateUsage(UUID customerId, UUID accountId, PaymentLimitType limitType, BigDecimal amount) {
        // Update customer-level usage
        Optional<PaymentLimit> customerLimit = paymentLimitRepository
            .findActiveByCustomerIdAndType(customerId, limitType, LocalDateTime.now());
        
        if (customerLimit.isPresent()) {
            PaymentLimit updated = customerLimit.get()
                .withUpdatedUsage(customerLimit.get().currentUsage().add(amount));
            paymentLimitRepository.save(updated);
        }
        
        // Update account-level usage
        Optional<PaymentLimit> accountLimit = paymentLimitRepository
            .findActiveByAccountIdAndType(accountId, limitType, LocalDateTime.now());
        
        if (accountLimit.isPresent()) {
            PaymentLimit updated = accountLimit.get()
                .withUpdatedUsage(accountLimit.get().currentUsage().add(amount));
            paymentLimitRepository.save(updated);
        }
    }
}

