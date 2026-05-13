package com.yourorg.banking.payments.controller;

import com.yourorg.banking.payments.model.Dispute;
import com.yourorg.banking.payments.service.DisputeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/disputes")
public class DisputeController {

    private final DisputeService disputeService;

    public DisputeController(DisputeService disputeService) {
        this.disputeService = disputeService;
    }

    @PostMapping
    public ResponseEntity<Dispute> openDispute(@RequestBody Map<String, Object> request) {
        UUID transactionId = UUID.fromString((String) request.get("transactionId"));
        UUID customerId = UUID.fromString((String) request.get("customerId"));
        String reason = (String) request.get("reason");
        BigDecimal amount = new BigDecimal(request.get("amount").toString());

        Dispute dispute = disputeService.openDispute(transactionId, customerId, reason, amount);
        return ResponseEntity.created(URI.create("/api/v1/disputes/" + dispute.getId())).body(dispute);
    }

    @GetMapping
    public ResponseEntity<List<Dispute>> getDisputes(@RequestParam UUID customerId) {
        return ResponseEntity.ok(disputeService.getCustomerDisputes(customerId));
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<Dispute> resolveDispute(@PathVariable UUID id, @RequestBody Map<String, String> request) {
        Dispute.DisputeStatus resolution = Dispute.DisputeStatus.valueOf(request.get("resolution"));
        Dispute dispute = disputeService.resolveDispute(id, resolution);
        return ResponseEntity.ok(dispute);
    }
}
