package com.yourorg.banking.compliance.api;

import com.yourorg.banking.compliance.model.FraudDetection;
import com.yourorg.banking.compliance.model.InvestigationStatus;
import com.yourorg.banking.compliance.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fraud-detection")
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionController {
    
    private final FraudDetectionService fraudDetectionService;
    
    @GetMapping("/active")
    public ResponseEntity<List<FraudDetection>> getActiveFraudDetections() {
        log.info("Getting active fraud detections");
        
        List<FraudDetection> detections = fraudDetectionService.getActiveFraudDetections();
        
        return ResponseEntity.ok(detections);
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<FraudDetection>> getFraudDetectionsByCustomer(@PathVariable UUID customerId) {
        log.info("Getting fraud detections for customer: {}", customerId);
        
        List<FraudDetection> detections = fraudDetectionService.getFraudDetectionsByCustomer(customerId);
        
        return ResponseEntity.ok(detections);
    }
    
    @PutMapping("/{detectionId}/investigation")
    public ResponseEntity<String> updateInvestigationStatus(
            @PathVariable UUID detectionId,
            @RequestParam InvestigationStatus status,
            @RequestParam(required = false) String investigator,
            @RequestParam(required = false) String notes) {
        log.info("Updating fraud detection {} investigation status to {}", detectionId, status);
        
        fraudDetectionService.updateInvestigationStatus(detectionId, status, investigator, notes);
        
        return ResponseEntity.ok("Investigation status updated successfully");
    }
    
    @PostMapping("/{detectionId}/confirm")
    public ResponseEntity<String> confirmFraud(
            @PathVariable UUID detectionId,
            @RequestParam String investigator,
            @RequestParam(required = false) String notes) {
        log.info("Confirming fraud for detection: {}", detectionId);
        
        fraudDetectionService.confirmFraud(detectionId, investigator, notes);
        
        return ResponseEntity.ok("Fraud confirmed successfully");
    }
    
    @PostMapping("/{detectionId}/false-positive")
    public ResponseEntity<String> markAsFalsePositive(
            @PathVariable UUID detectionId,
            @RequestParam String investigator,
            @RequestParam(required = false) String notes) {
        log.info("Marking fraud detection {} as false positive", detectionId);
        
        fraudDetectionService.markAsFalsePositive(detectionId, investigator, notes);
        
        return ResponseEntity.ok("Marked as false positive successfully");
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Fraud Detection Service is healthy");
    }
}

