package com.yourorg.banking.compliance.api;

import com.yourorg.banking.compliance.model.AmlCase;
import com.yourorg.banking.compliance.model.CaseStatus;
import com.yourorg.banking.compliance.service.AmlMonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/aml")
@RequiredArgsConstructor
@Slf4j
public class AmlController {
    
    private final AmlMonitoringService amlMonitoringService;
    
    @GetMapping("/cases")
    public ResponseEntity<List<AmlCase>> getOpenCases() {
        log.info("Getting open AML cases");
        
        List<AmlCase> cases = amlMonitoringService.getOpenCases();
        
        return ResponseEntity.ok(cases);
    }
    
    @GetMapping("/cases/customer/{customerId}")
    public ResponseEntity<List<AmlCase>> getCasesByCustomer(@PathVariable UUID customerId) {
        log.info("Getting AML cases for customer: {}", customerId);
        
        List<AmlCase> cases = amlMonitoringService.getCasesByCustomer(customerId);
        
        return ResponseEntity.ok(cases);
    }
    
    @PutMapping("/cases/{caseId}/status")
    public ResponseEntity<String> updateCaseStatus(
            @PathVariable UUID caseId,
            @RequestParam CaseStatus status,
            @RequestParam(required = false) String resolutionNotes) {
        log.info("Updating AML case {} status to {}", caseId, status);
        
        amlMonitoringService.updateCaseStatus(caseId, status, resolutionNotes);
        
        return ResponseEntity.ok("Case status updated successfully");
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("AML Service is healthy");
    }
}

