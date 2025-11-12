package com.yourorg.banking.payments.repository;

import com.yourorg.banking.payments.model.Reservation;
import com.yourorg.banking.payments.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    
    List<Reservation> findByTransferId(UUID transferId);
    
    List<Reservation> findByAccountIdAndStatus(UUID accountId, ReservationStatus status);
    
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Reservation r WHERE r.accountId = :accountId AND r.status = 'ACTIVE'")
    Double getTotalReservedAmountByAccount(@Param("accountId") UUID accountId);
    
    @Query("SELECT r FROM Reservation r WHERE r.status = 'ACTIVE' AND r.createdAt < :expiryTime")
    List<Reservation> findExpiredReservations(@Param("expiryTime") Instant expiryTime);
}

