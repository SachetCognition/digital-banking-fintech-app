package com.yourorg.banking.payments.service;

import com.yourorg.banking.payments.model.*;
import com.yourorg.banking.payments.repository.TransferRepository;
import com.yourorg.banking.payments.repository.ExternalTransferRepository;
import com.yourorg.banking.payments.repository.InternationalTransferRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class IdempotencyService {
    
    private final TransferRepository transferRepository;
    private final ExternalTransferRepository externalTransferRepository;
    private final InternationalTransferRepository internationalTransferRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String IDEMPOTENCY_PREFIX = "transfer:idempotency:";
    private static final String BILL_PAY_IDEMPOTENCY_PREFIX = "billpay:idempotency:";
    private static final String EXTERNAL_TRANSFER_IDEMPOTENCY_PREFIX = "external_transfer:idempotency:";
    private static final String INTERNATIONAL_TRANSFER_IDEMPOTENCY_PREFIX = "international_transfer:idempotency:";
    private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(24);
    
    public IdempotencyService(TransferRepository transferRepository, 
                            ExternalTransferRepository externalTransferRepository,
                            InternationalTransferRepository internationalTransferRepository,
                            RedisTemplate<String, String> redisTemplate) {
        this.transferRepository = transferRepository;
        this.externalTransferRepository = externalTransferRepository;
        this.internationalTransferRepository = internationalTransferRepository;
        this.redisTemplate = redisTemplate;
    }
    
    public Optional<TransferResponse> getExistingTransfer(String idempotencyKey) {
        // First check Redis cache
        String cachedTransferId = redisTemplate.opsForValue().get(IDEMPOTENCY_PREFIX + idempotencyKey);
        if (cachedTransferId != null) {
            Optional<Transfer> transfer = transferRepository.findById(UUID.fromString(cachedTransferId));
            if (transfer.isPresent()) {
                return Optional.of(createTransferResponse(transfer.get()));
            }
        }
        
        // Fallback to database lookup
        Optional<Transfer> transfer = transferRepository.findByIdempotencyKey(idempotencyKey);
        if (transfer.isPresent()) {
            // Cache the result
            redisTemplate.opsForValue().set(
                IDEMPOTENCY_PREFIX + idempotencyKey,
                transfer.get().getId().toString(),
                IDEMPOTENCY_TTL
            );
            return Optional.of(createTransferResponse(transfer.get()));
        }
        
        return Optional.empty();
    }
    
    public void cacheTransferId(String idempotencyKey, UUID transferId) {
        redisTemplate.opsForValue().set(
            IDEMPOTENCY_PREFIX + idempotencyKey,
            transferId.toString(),
            IDEMPOTENCY_TTL
        );
    }
    
    public Optional<BillPayResponse> getExistingBillPay(String idempotencyKey) {
        // For now, we'll use the same transfer lookup since bill pay uses transfers internally
        // In a more complex system, you might have a separate bill pay repository
        String cachedTransferId = redisTemplate.opsForValue().get(BILL_PAY_IDEMPOTENCY_PREFIX + idempotencyKey);
        if (cachedTransferId != null) {
            Optional<Transfer> transfer = transferRepository.findById(UUID.fromString(cachedTransferId));
            if (transfer.isPresent()) {
                return Optional.of(createBillPayResponse(transfer.get()));
            }
        }
        
        // Fallback to database lookup by idempotency key
        Optional<Transfer> transfer = transferRepository.findByIdempotencyKey(idempotencyKey);
        if (transfer.isPresent()) {
            redisTemplate.opsForValue().set(
                BILL_PAY_IDEMPOTENCY_PREFIX + idempotencyKey,
                transfer.get().getId().toString(),
                IDEMPOTENCY_TTL
            );
            return Optional.of(createBillPayResponse(transfer.get()));
        }
        
        return Optional.empty();
    }
    
    public void cacheBillPayId(String idempotencyKey, UUID billPayId) {
        redisTemplate.opsForValue().set(
            BILL_PAY_IDEMPOTENCY_PREFIX + idempotencyKey,
            billPayId.toString(),
            IDEMPOTENCY_TTL
        );
    }
    
    private TransferResponse createTransferResponse(Transfer transfer) {
        return new TransferResponse(
            transfer.getId(),
            transfer.getPayerAccountId(),
            transfer.getPayeeAccountId(),
            transfer.getAmount(),
            transfer.getCurrency(),
            transfer.getStatus().name(),
            transfer.getDescription(),
            transfer.getCreatedAt(),
            transfer.getCompletedAt(),
            transfer.getIdempotencyKey()
        );
    }
    
    private BillPayResponse createBillPayResponse(Transfer transfer) {
        // This is a simplified conversion - in a real system, you'd have more bill pay specific data
        return new BillPayResponse(
            transfer.getId(),
            transfer.getPayerAccountId(),
            null, // beneficiaryId - would need to be stored separately
            "Beneficiary", // beneficiaryName - would need to be stored separately
            "Account", // beneficiaryAccountNumber - would need to be stored separately
            transfer.getAmount(),
            transfer.getCurrency(),
            transfer.getStatus().name(),
            transfer.getDescription(),
            transfer.getCreatedAt(),
            transfer.getCompletedAt(),
            transfer.getIdempotencyKey()
        );
    }
    
    public ExternalTransfer getExistingExternalTransfer(String idempotencyKey) {
        // First check Redis cache
        String cachedTransferId = redisTemplate.opsForValue().get(EXTERNAL_TRANSFER_IDEMPOTENCY_PREFIX + idempotencyKey);
        if (cachedTransferId != null) {
            return externalTransferRepository.findById(UUID.fromString(cachedTransferId)).orElse(null);
        }
        
        // Fallback to database lookup by reference number
        return externalTransferRepository.findByReferenceNumber(idempotencyKey).orElse(null);
    }
    
    public void cacheExternalTransferId(String idempotencyKey, UUID transferId) {
        redisTemplate.opsForValue().set(
            EXTERNAL_TRANSFER_IDEMPOTENCY_PREFIX + idempotencyKey,
            transferId.toString(),
            IDEMPOTENCY_TTL
        );
    }
    
    public InternationalTransfer getExistingInternationalTransfer(String idempotencyKey) {
        // First check Redis cache
        String cachedTransferId = redisTemplate.opsForValue().get(INTERNATIONAL_TRANSFER_IDEMPOTENCY_PREFIX + idempotencyKey);
        if (cachedTransferId != null) {
            return internationalTransferRepository.findById(UUID.fromString(cachedTransferId)).orElse(null);
        }
        
        // Fallback to database lookup by reference number
        return internationalTransferRepository.findByReferenceNumber(idempotencyKey).orElse(null);
    }
    
    public void cacheInternationalTransferId(String idempotencyKey, UUID transferId) {
        redisTemplate.opsForValue().set(
            INTERNATIONAL_TRANSFER_IDEMPOTENCY_PREFIX + idempotencyKey,
            transferId.toString(),
            IDEMPOTENCY_TTL
        );
    }
}
