package com.yourorg.banking.payments.service;

import com.yourorg.banking.payments.model.*;
import com.yourorg.banking.payments.repository.RecurringPaymentRepository;
import com.yourorg.banking.payments.repository.PaymentLimitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RecurringPaymentService {
    
    @Autowired
    private RecurringPaymentRepository recurringPaymentRepository;
    
    @Autowired
    private PaymentLimitRepository paymentLimitRepository;
    
    @Autowired
    private TransferSagaService transferSagaService;
    
    public RecurringPaymentResponse createRecurringPayment(UUID customerId, RecurringPaymentRequest request) {
        // Validate limits
        validateRecurringPaymentLimits(customerId, request.fromAccountId(), request.amount());
        
        // Calculate next execution date
        LocalDate nextExecutionDate = calculateNextExecutionDate(
            request.startDate(), 
            request.frequency(), 
            request.dayOfMonth(), 
            request.dayOfWeek()
        );
        
        // Create recurring payment
        RecurringPayment recurringPayment = new RecurringPayment(
            UUID.randomUUID(),
            customerId,
            request.fromAccountId(),
            request.toAccountNumber(),
            request.toBankCode(),
            request.toBankName(),
            request.toAccountName(),
            request.amount(),
            request.currency(),
            request.description(),
            request.frequency(),
            request.dayOfMonth(),
            request.dayOfWeek(),
            request.startDate(),
            request.endDate(),
            nextExecutionDate,
            RecurringPaymentStatus.ACTIVE,
            request.maxExecutions(),
            0, // executionCount
            LocalDateTime.now(),
            LocalDateTime.now(),
            null, // lastExecutedAt
            null, // cancelledAt
            null  // cancellationReason
        );
        
        recurringPayment = recurringPaymentRepository.save(recurringPayment);
        
        return RecurringPaymentResponse.from(recurringPayment);
    }
    
    public RecurringPaymentResponse getRecurringPayment(UUID customerId, UUID paymentId) {
        RecurringPayment payment = recurringPaymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Recurring payment not found"));
        
        if (!payment.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        return RecurringPaymentResponse.from(payment);
    }
    
    public List<RecurringPaymentResponse> getRecurringPayments(UUID customerId) {
        return recurringPaymentRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
            .stream()
            .map(RecurringPaymentResponse::from)
            .toList();
    }
    
    public List<RecurringPaymentResponse> getRecurringPaymentsByStatus(UUID customerId, RecurringPaymentStatus status) {
        return recurringPaymentRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, status)
            .stream()
            .map(RecurringPaymentResponse::from)
            .toList();
    }
    
    public RecurringPaymentResponse pauseRecurringPayment(UUID customerId, UUID paymentId) {
        RecurringPayment payment = recurringPaymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Recurring payment not found"));
        
        if (!payment.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        if (payment.status() != RecurringPaymentStatus.ACTIVE) {
            throw new RuntimeException("Only active recurring payments can be paused");
        }
        
        RecurringPayment paused = payment.withStatus(RecurringPaymentStatus.PAUSED);
        paused = recurringPaymentRepository.save(paused);
        
        return RecurringPaymentResponse.from(paused);
    }
    
    public RecurringPaymentResponse resumeRecurringPayment(UUID customerId, UUID paymentId) {
        RecurringPayment payment = recurringPaymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Recurring payment not found"));
        
        if (!payment.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        if (payment.status() != RecurringPaymentStatus.PAUSED) {
            throw new RuntimeException("Only paused recurring payments can be resumed");
        }
        
        RecurringPayment resumed = payment.withStatus(RecurringPaymentStatus.ACTIVE);
        resumed = recurringPaymentRepository.save(resumed);
        
        return RecurringPaymentResponse.from(resumed);
    }
    
    public RecurringPaymentResponse cancelRecurringPayment(UUID customerId, UUID paymentId, String reason) {
        RecurringPayment payment = recurringPaymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Recurring payment not found"));
        
        if (!payment.customerId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }
        
        RecurringPayment cancelled = payment.withCancelled(reason);
        cancelled = recurringPaymentRepository.save(cancelled);
        
        return RecurringPaymentResponse.from(cancelled);
    }
    
    @Scheduled(cron = "0 0 9 * * ?") // Run daily at 9 AM
    public void processDueRecurringPayments() {
        LocalDate today = LocalDate.now();
        List<RecurringPayment> duePayments = recurringPaymentRepository
            .findDueForExecution(RecurringPaymentStatus.ACTIVE, today);
        
        for (RecurringPayment payment : duePayments) {
            try {
                processRecurringPayment(payment);
            } catch (Exception e) {
                // Log error but continue with other payments
                System.err.println("Failed to process recurring payment " + payment.id() + ": " + e.getMessage());
            }
        }
    }
    
    private void processRecurringPayment(RecurringPayment payment) {
        // Check if payment should be executed
        if (payment.endDate() != null && LocalDate.now().isAfter(payment.endDate())) {
            RecurringPayment completed = payment.withStatus(RecurringPaymentStatus.COMPLETED);
            recurringPaymentRepository.save(completed);
            return;
        }
        
        if (payment.maxExecutions() != null && payment.executionCount() >= payment.maxExecutions()) {
            RecurringPayment completed = payment.withStatus(RecurringPaymentStatus.COMPLETED);
            recurringPaymentRepository.save(completed);
            return;
        }
        
        // Create transfer request
        TransferRequest transferRequest = new TransferRequest(
            payment.fromAccountId(),
            null, // payeeAccountId - for external transfers, this would be null
            payment.amount(),
            payment.currency(),
            payment.description() + " (Recurring Payment)",
            null // idempotencyKey
        );
        
        try {
            // Execute the transfer
            TransferResponse transferResponse = transferSagaService.executeTransfer(
                payment.customerId(), transferRequest);
            
            // Update recurring payment
            RecurringPayment updated = payment.withExecuted();
            LocalDate nextExecution = calculateNextExecutionDate(
                payment.startDate(),
                payment.frequency(),
                payment.dayOfMonth(),
                payment.dayOfWeek()
            );
            updated = updated.withNextExecutionDate(nextExecution);
            
            recurringPaymentRepository.save(updated);
            
        } catch (Exception e) {
            // Log error but don't fail the recurring payment
            System.err.println("Failed to execute recurring payment " + payment.id() + ": " + e.getMessage());
        }
    }
    
    private LocalDate calculateNextExecutionDate(LocalDate startDate, PaymentFrequency frequency, 
                                               Integer dayOfMonth, Integer dayOfWeek) {
        LocalDate nextDate = startDate;
        LocalDate today = LocalDate.now();
        
        while (nextDate.isBefore(today) || nextDate.isEqual(today)) {
            switch (frequency) {
                case DAILY:
                    nextDate = nextDate.plusDays(1);
                    break;
                case WEEKLY:
                    nextDate = nextDate.plusWeeks(1);
                    break;
                case MONTHLY:
                    nextDate = nextDate.plusMonths(1);
                    break;
                case QUARTERLY:
                    nextDate = nextDate.plusMonths(3);
                    break;
                case YEARLY:
                    nextDate = nextDate.plusYears(1);
                    break;
            }
        }
        
        return nextDate;
    }
    
    private void validateRecurringPaymentLimits(UUID customerId, UUID accountId, BigDecimal amount) {
        // Check per-transaction limit
        PaymentLimit perTransactionLimit = paymentLimitRepository
            .findActiveByAccountIdAndType(accountId, PaymentLimitType.PER_TRANSACTION, LocalDateTime.now())
            .orElse(null);
        
        if (perTransactionLimit != null && amount.compareTo(perTransactionLimit.limitValue()) > 0) {
            throw new RuntimeException("Amount exceeds per-transaction limit");
        }
    }
}

