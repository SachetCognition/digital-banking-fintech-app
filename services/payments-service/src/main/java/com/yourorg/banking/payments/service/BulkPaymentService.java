package com.yourorg.banking.payments.service;

import com.yourorg.banking.payments.model.*;
import com.yourorg.banking.payments.repository.BulkPaymentRepository;
import com.yourorg.banking.payments.repository.BulkPaymentItemRepository;
import com.yourorg.banking.payments.repository.PaymentLimitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class BulkPaymentService {
    
    @Autowired
    private BulkPaymentRepository bulkPaymentRepository;
    
    @Autowired
    private BulkPaymentItemRepository bulkPaymentItemRepository;
    
    @Autowired
    private PaymentLimitRepository paymentLimitRepository;
    
    @Autowired
    private TransferSagaService transferSagaService;
    
    public BulkPaymentResponse createBulkPayment(UUID customerId, BulkPaymentRequest request) {
        // Validate limits
        validateBulkPaymentLimits(customerId, request.fromAccountId(), request.totalAmount());
        
        // Create bulk payment
        BulkPayment bulkPayment = new BulkPayment(
            UUID.randomUUID(),
            customerId,
            request.batchName(),
            request.fromAccountId(),
            request.totalAmount(),
            request.currency(),
            request.items().size(),
            BulkPaymentStatus.PENDING,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null, // processedAt
            null, // completedAt
            null, // failedAt
            null  // failureReason
        );
        
        bulkPayment = bulkPaymentRepository.save(bulkPayment);
        
        // Create bulk payment items
        for (BulkPaymentItemRequest itemRequest : request.items()) {
            BulkPaymentItem item = new BulkPaymentItem(
                UUID.randomUUID(),
                bulkPayment.id(),
                itemRequest.recipientName(),
                itemRequest.recipientAccountNumber(),
                itemRequest.recipientBankCode(),
                itemRequest.recipientBankName(),
                itemRequest.amount(),
                itemRequest.description(),
                BulkPaymentItemStatus.PENDING,
                null, // transferId
                LocalDateTime.now(),
                LocalDateTime.now(),
                null, // processedAt
                null, // failedAt
                null  // failureReason
            );
            
            bulkPaymentItemRepository.save(item);
        }
        
        // Start processing asynchronously
        processBulkPaymentAsync(bulkPayment);
        
        return BulkPaymentResponse.from(bulkPayment);
    }
    
    public BulkPaymentResponse getBulkPayment(UUID customerId, UUID bulkPaymentId) {
        BulkPayment bulkPayment = bulkPaymentRepository.findById(bulkPaymentId)
            .orElseThrow(() -> new RuntimeException("Bulk payment not found"));
        
        if (!bulkPayment.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        return BulkPaymentResponse.from(bulkPayment);
    }
    
    public List<BulkPaymentResponse> getBulkPayments(UUID customerId) {
        return bulkPaymentRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
            .stream()
            .map(BulkPaymentResponse::from)
            .toList();
    }
    
    public List<BulkPaymentResponse> getBulkPaymentsByStatus(UUID customerId, BulkPaymentStatus status) {
        return bulkPaymentRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, status)
            .stream()
            .map(BulkPaymentResponse::from)
            .toList();
    }
    
    public List<BulkPaymentItemResponse> getBulkPaymentItems(UUID customerId, UUID bulkPaymentId) {
        // Verify access
        BulkPayment bulkPayment = bulkPaymentRepository.findById(bulkPaymentId)
            .orElseThrow(() -> new RuntimeException("Bulk payment not found"));
        
        if (!bulkPayment.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        return bulkPaymentItemRepository.findByBulkPaymentIdOrderByCreatedAtAsc(bulkPaymentId)
            .stream()
            .map(BulkPaymentItemResponse::from)
            .toList();
    }
    
    private void validateBulkPaymentLimits(UUID customerId, UUID accountId, BigDecimal totalAmount) {
        // Check per-transaction limit
        PaymentLimit perTransactionLimit = paymentLimitRepository
            .findActiveByAccountIdAndType(accountId, PaymentLimitType.PER_TRANSACTION, LocalDateTime.now())
            .orElse(null);
        
        if (perTransactionLimit != null && totalAmount.compareTo(perTransactionLimit.limitValue()) > 0) {
            throw new RuntimeException("Total amount exceeds per-transaction limit");
        }
        
        // Check daily amount limit
        PaymentLimit dailyLimit = paymentLimitRepository
            .findActiveByAccountIdAndType(accountId, PaymentLimitType.DAILY_AMOUNT, LocalDateTime.now())
            .orElse(null);
        
        if (dailyLimit != null) {
            Double todayAmount = bulkPaymentRepository
                .sumAmountByCustomerIdAndStatusToday(customerId, BulkPaymentStatus.COMPLETED);
            BigDecimal todayTotal = todayAmount != null ? BigDecimal.valueOf(todayAmount) : BigDecimal.ZERO;
            
            if (todayTotal.add(totalAmount).compareTo(dailyLimit.limitValue()) > 0) {
                throw new RuntimeException("Total amount would exceed daily limit");
            }
        }
    }
    
    private void processBulkPaymentAsync(BulkPayment bulkPayment) {
        // This would typically be handled by a message queue or async processor
        // For now, we'll simulate the processing
        try {
            // Update status to processing
            BulkPayment processing = bulkPayment.withStatus(BulkPaymentStatus.PROCESSING)
                .withProcessedAt(LocalDateTime.now());
            bulkPaymentRepository.save(processing);
            
            // Get all items for this bulk payment
            List<BulkPaymentItem> items = bulkPaymentItemRepository
                .findByBulkPaymentIdOrderByCreatedAtAsc(bulkPayment.id());
            
            int successCount = 0;
            int failureCount = 0;
            
            // Process each item
            for (BulkPaymentItem item : items) {
                try {
                    // Create transfer request for each item
                    TransferRequest transferRequest = new TransferRequest(
                        bulkPayment.fromAccountId(),
                        null, // payeeAccountId - for external transfers, this would be null
                        item.amount(),
                        bulkPayment.currency(),
                        item.description(),
                        null // idempotencyKey
                    );
                    
                    // Execute the transfer
                    TransferResponse transferResponse = transferSagaService.executeTransfer(
                        bulkPayment.customerId(), transferRequest);
                    
                    // Update item as completed
                    BulkPaymentItem completed = item
                        .withStatus(BulkPaymentItemStatus.COMPLETED)
                        .withProcessedAt(LocalDateTime.now());
                    bulkPaymentItemRepository.save(completed);
                    
                    successCount++;
                    
                } catch (Exception e) {
                    // Update item as failed
                    BulkPaymentItem failed = item
                        .withFailed("Transfer failed: " + e.getMessage());
                    bulkPaymentItemRepository.save(failed);
                    
                    failureCount++;
                }
            }
            
            // Update bulk payment status
            BulkPaymentStatus finalStatus;
            if (failureCount == 0) {
                finalStatus = BulkPaymentStatus.COMPLETED;
            } else if (successCount == 0) {
                finalStatus = BulkPaymentStatus.FAILED;
            } else {
                finalStatus = BulkPaymentStatus.PARTIAL;
            }
            
            BulkPayment finalBulkPayment = processing
                .withStatus(finalStatus)
                .withCompletedAt(LocalDateTime.now());
            bulkPaymentRepository.save(finalBulkPayment);
            
        } catch (Exception e) {
            BulkPayment failed = bulkPayment
                .withFailed("Bulk processing failed: " + e.getMessage());
            bulkPaymentRepository.save(failed);
        }
    }
}

