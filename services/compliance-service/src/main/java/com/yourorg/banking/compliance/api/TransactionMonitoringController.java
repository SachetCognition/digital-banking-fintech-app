package com.yourorg.banking.compliance.api;

import com.yourorg.banking.compliance.model.TransactionMonitoring;
import com.yourorg.banking.compliance.model.ReviewStatus;
import com.yourorg.banking.compliance.service.TransactionMonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transaction-monitoring")
@RequiredArgsConstructor
@Slf4j
public class TransactionMonitoringController {
    
    private final TransactionMonitoringService transactionMonitoringService;
    
    @GetMapping("/flagged")
    public ResponseEntity<List<TransactionMonitoring>> getFlaggedTransactions() {
        log.info("Getting flagged transactions");
        
        List<TransactionMonitoring> transactions = transactionMonitoringService.getFlaggedTransactions();
        
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<TransactionMonitoring>> getTransactionsByCustomer(@PathVariable UUID customerId) {
        log.info("Getting monitored transactions for customer: {}", customerId);
        
        List<TransactionMonitoring> transactions = transactionMonitoringService.getTransactionsByCustomer(customerId);
        
        return ResponseEntity.ok(transactions);
    }
    
    @PutMapping("/{monitoringId}/review")
    public ResponseEntity<String> updateReviewStatus(
            @PathVariable UUID monitoringId,
            @RequestParam ReviewStatus status,
            @RequestParam(required = false) String reviewedBy,
            @RequestParam(required = false) String reviewNotes) {
        log.info("Updating transaction monitoring {} review status to {}", monitoringId, status);
        
        transactionMonitoringService.updateReviewStatus(monitoringId, status, reviewedBy, reviewNotes);
        
        return ResponseEntity.ok("Review status updated successfully");
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Transaction Monitoring Service is healthy");
    }
}

