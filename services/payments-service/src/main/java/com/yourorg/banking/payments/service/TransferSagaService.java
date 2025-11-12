package com.yourorg.banking.payments.service;

import com.yourorg.banking.payments.model.*;
import com.yourorg.banking.payments.repository.TransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TransferSagaService {
    
    private final TransferRepository transferRepository;
    private final TransferValidationService validationService;
    private final ReservationService reservationService;
    private final PostingService postingService;
    private final NotificationService notificationService;
    private final TransferEventPublisher eventPublisher;
    
    public TransferSagaService(TransferRepository transferRepository,
                             TransferValidationService validationService,
                             ReservationService reservationService,
                             PostingService postingService,
                             NotificationService notificationService,
                             TransferEventPublisher eventPublisher) {
        this.transferRepository = transferRepository;
        this.validationService = validationService;
        this.reservationService = reservationService;
        this.postingService = postingService;
        this.notificationService = notificationService;
        this.eventPublisher = eventPublisher;
    }
    
    @Transactional
    public TransferResponse executeTransfer(TransferRequest request, String authToken) {
        Transfer transfer = new Transfer(
            UUID.randomUUID(),
            request.payerAccountId(),
            request.payeeAccountId(),
            request.amount(),
            request.currency(),
            request.description(),
            request.idempotencyKey()
        );
        
        transferRepository.save(transfer);
        eventPublisher.publishTransferCreated(transfer);
        
        try {
            // Step 1: Validation
            transfer.updateStep(SagaStep.VALIDATION);
            transfer.setStatus(TransferStatus.VALIDATING);
            transferRepository.save(transfer);
            eventPublisher.publishTransferStatusChanged(transfer);
            
            validationService.validateTransfer(request, authToken);
            
            // Step 2: Reservation
            transfer.updateStep(SagaStep.RESERVATION);
            transfer.setStatus(TransferStatus.RESERVING);
            transferRepository.save(transfer);
            eventPublisher.publishTransferStatusChanged(transfer);
            
            reservationService.createReservation(transfer, authToken);
            
            // Step 3: Posting
            transfer.updateStep(SagaStep.POSTING);
            transfer.setStatus(TransferStatus.POSTING);
            transferRepository.save(transfer);
            eventPublisher.publishTransferStatusChanged(transfer);
            
            postingService.postTransfer(transfer, authToken);
            
            // Step 4: Notification
            transfer.updateStep(SagaStep.NOTIFICATION);
            transferRepository.save(transfer);
            eventPublisher.publishTransferStatusChanged(transfer);
            
            notificationService.sendTransferNotifications(transfer, authToken);
            
            // Step 5: Completion
            transfer.updateStep(SagaStep.COMPLETED);
            transfer.markAsCompleted();
            transferRepository.save(transfer);
            eventPublisher.publishTransferCompleted(transfer);
            
            return createTransferResponse(transfer);
            
        } catch (Exception e) {
            // Compensation: Release reservation and mark as failed
            compensateTransfer(transfer, authToken, e.getMessage());
            eventPublisher.publishTransferFailed(transfer);
            throw new RuntimeException("Transfer failed: " + e.getMessage(), e);
        }
    }
    
    @Transactional
    public void compensateTransfer(Transfer transfer, String authToken, String reason) {
        try {
            transfer.updateStep(SagaStep.COMPENSATION);
            transfer.markAsFailed(reason);
            transferRepository.save(transfer);
            
            // Release any active reservations
            reservationService.releaseReservation(transfer.getId(), authToken);
            
        } catch (Exception e) {
            System.err.println("Failed to compensate transfer " + transfer.getId() + ": " + e.getMessage());
        }
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
}
