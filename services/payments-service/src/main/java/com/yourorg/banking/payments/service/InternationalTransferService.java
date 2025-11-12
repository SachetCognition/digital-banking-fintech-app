package com.yourorg.banking.payments.service;

import com.yourorg.banking.payments.model.*;
import com.yourorg.banking.payments.repository.InternationalTransferRepository;
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
public class InternationalTransferService {
    
    @Autowired
    private InternationalTransferRepository internationalTransferRepository;
    
    @Autowired
    private PaymentLimitRepository paymentLimitRepository;
    
    @Autowired
    private TransferValidationService transferValidationService;
    
    @Autowired
    private IdempotencyService idempotencyService;
    
    public InternationalTransferResponse createInternationalTransfer(UUID customerId, InternationalTransferRequest request) {
        // Check idempotency
        String idempotencyKey = "international_transfer_" + customerId + "_" + request.hashCode();
        InternationalTransfer existing = idempotencyService.getExistingInternationalTransfer(idempotencyKey);
        if (existing != null) {
            return InternationalTransferResponse.from(existing);
        }
        
        // Validate limits
        validateInternationalTransferLimits(customerId, request.fromAccountId(), request.amount());
        
        // Create international transfer
        InternationalTransfer transfer = new InternationalTransfer(
            UUID.randomUUID(),
            customerId,
            request.fromAccountId(),
            request.transferType(),
            request.toSwiftCode(),
            request.toIban(),
            request.toBankName(),
            request.toBankAddress(),
            request.toAccountNumber(),
            request.toAccountName(),
            request.toAddress(),
            request.toCountryCode(),
            request.amount(),
            request.currency(),
            request.description(),
            request.referenceNumber(),
            InternationalTransferStatus.PENDING,
            null, // swiftMessageId
            request.correspondentBankSwift(),
            request.correspondentBankName(),
            calculateInternationalProcessingFee(request.amount(), request.transferType()),
            null, // exchangeRate
            LocalDateTime.now(),
            LocalDateTime.now(),
            null, // processedAt
            null, // failedAt
            null  // failureReason
        );
        
        transfer = internationalTransferRepository.save(transfer);
        
        // Cache for idempotency
        idempotencyService.cacheInternationalTransferId(idempotencyKey, transfer.id());
        
        // Start processing asynchronously
        processInternationalTransferAsync(transfer);
        
        return InternationalTransferResponse.from(transfer);
    }
    
    public InternationalTransferResponse getInternationalTransfer(UUID customerId, UUID transferId) {
        InternationalTransfer transfer = internationalTransferRepository.findById(transferId)
            .orElseThrow(() -> new RuntimeException("International transfer not found"));
        
        if (!transfer.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        return InternationalTransferResponse.from(transfer);
    }
    
    public List<InternationalTransferResponse> getInternationalTransfers(UUID customerId) {
        return internationalTransferRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
            .stream()
            .map(InternationalTransferResponse::from)
            .toList();
    }
    
    public List<InternationalTransferResponse> getInternationalTransfersByStatus(UUID customerId, InternationalTransferStatus status) {
        return internationalTransferRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, status)
            .stream()
            .map(InternationalTransferResponse::from)
            .toList();
    }
    
    private void validateInternationalTransferLimits(UUID customerId, UUID accountId, BigDecimal amount) {
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
            Double todayAmount = internationalTransferRepository
                .sumAmountByCustomerIdAndStatusToday(customerId, InternationalTransferStatus.COMPLETED);
            BigDecimal todayTotal = todayAmount != null ? BigDecimal.valueOf(todayAmount) : BigDecimal.ZERO;
            
            if (todayTotal.add(amount).compareTo(dailyLimit.limitValue()) > 0) {
                throw new RuntimeException("Amount would exceed daily limit");
            }
        }
    }
    
    private BigDecimal calculateInternationalProcessingFee(BigDecimal amount, InternationalTransferType transferType) {
        // Different fees based on transfer type
        switch (transferType) {
            case SWIFT:
                return BigDecimal.valueOf(25.00).add(amount.multiply(BigDecimal.valueOf(0.002)));
            case SEPA:
                return BigDecimal.valueOf(5.00).add(amount.multiply(BigDecimal.valueOf(0.0005)));
            case WIRE:
                return BigDecimal.valueOf(15.00).add(amount.multiply(BigDecimal.valueOf(0.001)));
            default:
                return BigDecimal.valueOf(20.00).add(amount.multiply(BigDecimal.valueOf(0.0015)));
        }
    }
    
    private void processInternationalTransferAsync(InternationalTransfer transfer) {
        // This would typically be handled by a message queue or async processor
        // For now, we'll simulate the processing
        try {
            // Simulate SWIFT/SEPA processing
            Thread.sleep(2000);
            
            InternationalTransfer processing = transfer
                .withStatus(InternationalTransferStatus.PROCESSING)
                .withProcessedAt(LocalDateTime.now());
            
            internationalTransferRepository.save(processing);
            
            // Simulate SWIFT message generation
            String swiftMessageId = "SWIFT" + System.currentTimeMillis();
            InternationalTransfer sent = processing
                .withStatus(InternationalTransferStatus.SENT)
                .withProcessedAt(LocalDateTime.now());
            
            internationalTransferRepository.save(sent);
            
            // Simulate completion
            Thread.sleep(3000);
            
            InternationalTransfer completed = sent
                .withStatus(InternationalTransferStatus.COMPLETED)
                .withProcessedAt(LocalDateTime.now());
            
            internationalTransferRepository.save(completed);
            
        } catch (Exception e) {
            InternationalTransfer failed = transfer
                .withFailed("International processing failed: " + e.getMessage());
            
            internationalTransferRepository.save(failed);
        }
    }
}

