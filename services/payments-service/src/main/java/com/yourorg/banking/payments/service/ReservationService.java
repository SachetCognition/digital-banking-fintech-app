package com.yourorg.banking.payments.service;

import com.yourorg.banking.payments.client.LedgerServiceClient;
import com.yourorg.banking.payments.model.Reservation;
import com.yourorg.banking.payments.model.ReservationStatus;
import com.yourorg.banking.payments.model.Transfer;
import com.yourorg.banking.payments.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationService {
    
    private final ReservationRepository reservationRepository;
    private final LedgerServiceClient ledgerServiceClient;
    
    public ReservationService(ReservationRepository reservationRepository, 
                            LedgerServiceClient ledgerServiceClient) {
        this.reservationRepository = reservationRepository;
        this.ledgerServiceClient = ledgerServiceClient;
    }
    
    @Transactional
    public void createReservation(Transfer transfer, String authToken) {
        try {
            // Create reservation for payer account
            Reservation reservation = new Reservation(
                UUID.randomUUID(),
                transfer.getId(),
                transfer.getPayerAccountId(),
                transfer.getAmount(),
                transfer.getCurrency()
            );
            
            reservationRepository.save(reservation);
            
            // Create reservation in ledger service
            ledgerServiceClient.createReservation(
                transfer.getId(),
                transfer.getPayerAccountId(),
                transfer.getAmount(),
                transfer.getCurrency(),
                authToken
            );
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create reservation", e);
        }
    }
    
    @Transactional
    public void releaseReservation(UUID transferId, String authToken) {
        List<Reservation> reservations = reservationRepository.findByTransferId(transferId);
        
        for (Reservation reservation : reservations) {
            if (reservation.getStatus() == ReservationStatus.ACTIVE) {
                reservation.release();
                reservationRepository.save(reservation);
                
                // Release reservation in ledger service
                ledgerServiceClient.releaseReservation(
                    transferId,
                    reservation.getAccountId(),
                    authToken
                );
            }
        }
    }
    
    @Transactional
    public void releaseReservation(UUID transferId, UUID accountId, String authToken) {
        List<Reservation> reservations = reservationRepository.findByTransferId(transferId);
        
        for (Reservation reservation : reservations) {
            if (reservation.getAccountId().equals(accountId) && 
                reservation.getStatus() == ReservationStatus.ACTIVE) {
                reservation.release();
                reservationRepository.save(reservation);
                
                // Release reservation in ledger service
                ledgerServiceClient.releaseReservation(
                    transferId,
                    accountId,
                    authToken
                );
            }
        }
    }
    
    public BigDecimal getTotalReservedAmount(UUID accountId) {
        Double total = reservationRepository.getTotalReservedAmountByAccount(accountId);
        return BigDecimal.valueOf(total != null ? total : 0.0);
    }
    
    @Transactional
    public void releaseExpiredReservations() {
        // Release reservations older than 1 hour
        java.time.Instant expiryTime = java.time.Instant.now().minus(1, java.time.temporal.ChronoUnit.HOURS);
        List<Reservation> expiredReservations = reservationRepository.findExpiredReservations(expiryTime);
        
        for (Reservation reservation : expiredReservations) {
            reservation.release();
            reservationRepository.save(reservation);
        }
    }
}

