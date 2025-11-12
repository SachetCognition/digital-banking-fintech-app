package com.yourorg.banking.payments.service;

import com.yourorg.banking.payments.model.*;
import com.yourorg.banking.payments.repository.ExternalTransferRepository;
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
public class ExternalTransferService {
    
    @Autowired
    private ExternalTransferRepository externalTransferRepository;
    
    @Autowired
    private PaymentLimitRepository paymentLimitRepository;
    
    @Autowired
    private TransferValidationService transferValidationService;
    
    @Autowired
    private IdempotencyService idempotencyService;
    
    @Autowired
    private TransferSagaService transferSagaService;
    
    public ExternalTransferResponse createExternalTransfer(UUID customerId, ExternalTransferRequest request) {
        // Check idempotency
        String idempotencyKey = "external_transfer_" + customerId + "_" + request.hashCode();
        ExternalTransfer existing = idempotencyService.getExistingExternalTransfer(idempotencyKey);
        if (existing != null) {
            return ExternalTransferResponse.from(existing);
        }
        
        // Validate limits
        validateExternalTransferLimits(customerId, request.fromAccountId(), request.amount());
        
        // Create external transfer
        ExternalTransfer transfer = new ExternalTransfer(
            UUID.randomUUID(),
            customerId,
            request.fromAccountId(),
            request.toBankCode(),
            request.toBankName(),
            request.toAccountNumber(),
            request.toAccountName(),
            request.toRoutingNumber(),
            request.amount(),
            request.currency(),
            request.description(),
            request.referenceNumber(),
            ExternalTransferStatus.PENDING,
            null, // externalReference
            calculateProcessingFee(request.amount()),
            null, // exchangeRate
            LocalDateTime.now(),
            LocalDateTime.now(),
            null, // processedAt
            null, // failedAt
            null  // failureReason
        );
        
        transfer = externalTransferRepository.save(transfer);
        
        // Cache for idempotency
        idempotencyService.cacheExternalTransferId(idempotencyKey, transfer.id());
        
        // Start processing asynchronously
        processExternalTransferAsync(transfer);
        
        return ExternalTransferResponse.from(transfer);
    }
    
    public ExternalTransferResponse getExternalTransfer(UUID customerId, UUID transferId) {
        ExternalTransfer transfer = externalTransferRepository.findById(transferId)
            .orElseThrow(() -> new RuntimeException("External transfer not found"));
        
        if (!transfer.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        return ExternalTransferResponse.from(transfer);
    }
    
    public List<ExternalTransferResponse> getExternalTransfers(UUID customerId) {
        return externalTransferRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
            .stream()
            .map(ExternalTransferResponse::from)
            .toList();
    }
    
    public List<ExternalTransferResponse> getExternalTransfersByStatus(UUID customerId, ExternalTransferStatus status) {
        return externalTransferRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, status)
            .stream()
            .map(ExternalTransferResponse::from)
            .toList();
    }
    
    private void validateExternalTransferLimits(UUID customerId, UUID accountId, BigDecimal amount) {
        // Check per-transaction limit
        PaymentLimit perTransactionLimit = paymentLimitRepository
            .findActiveByAccountIdAndType(accountId, PaymentLimitType.PER_TRANSACTION, LocalDateTime.now())
            .orElse(null);
        
        if (perTransactionLimit != null && amount.compareTo(perTransactionLimit.limitValue()) > 0) {
            throw new RuntimeException("Amount exceeds per-transaction limit");
        }
        
        // Check daily amount limit
        PaymentLimit dailyLimit = paymentLimitRepository
            .findActiveByAccountIdAndType(accountId, PaymentLimitType.DAILY_AMOUNT, LocalDateTime.now())
            .orElse(null);
        
        if (dailyLimit != null) {
            Double todayAmount = externalTransferRepository
                .sumAmountByCustomerIdAndStatusToday(customerId, ExternalTransferStatus.COMPLETED);
            BigDecimal todayTotal = todayAmount != null ? BigDecimal.valueOf(todayAmount) : BigDecimal.ZERO;
            
            if (todayTotal.add(amount).compareTo(dailyLimit.limitValue()) > 0) {
                throw new RuntimeException("Amount would exceed daily limit");
            }
        }
    }
    
    private BigDecimal calculateProcessingFee(BigDecimal amount) {
        // Simple fee calculation: $2.50 + 0.1% of amount
        return BigDecimal.valueOf(2.50).add(amount.multiply(BigDecimal.valueOf(0.001)));
    }
    
    private void processExternalTransferAsync(ExternalTransfer transfer) {
        // This would typically be handled by a message queue or async processor
        // For now, we'll simulate the processing
        try {
            // Simulate external bank processing
            Thread.sleep(1000);
            
            ExternalTransfer processed = transfer
                .withStatus(ExternalTransferStatus.PROCESSING)
                .withProcessedAt(LocalDateTime.now());
            
            externalTransferRepository.save(processed);
            
            // Simulate completion
            Thread.sleep(2000);
            
            ExternalTransfer completed = processed
                .withStatus(ExternalTransferStatus.COMPLETED)
                .withProcessedAt(LocalDateTime.now());
            
            externalTransferRepository.save(completed);
            
        } catch (Exception e) {
            ExternalTransfer failed = transfer
                .withFailed("External processing failed: " + e.getMessage());
            
            externalTransferRepository.save(failed);
        }
    }
}

