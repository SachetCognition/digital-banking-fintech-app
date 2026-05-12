package com.yourorg.banking.payments.service;

import com.yourorg.banking.payments.model.Dispute;
import com.yourorg.banking.payments.repository.DisputeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class DisputeService {

    private final DisputeRepository disputeRepository;

    public DisputeService(DisputeRepository disputeRepository) {
        this.disputeRepository = disputeRepository;
    }

    public Dispute openDispute(UUID transactionId, UUID customerId, String reason, BigDecimal amount) {
        Dispute dispute = new Dispute();
        dispute.setTransactionId(transactionId);
        dispute.setCustomerId(customerId);
        dispute.setReason(reason);
        dispute.setAmount(amount);
        return disputeRepository.save(dispute);
    }

    public List<Dispute> getCustomerDisputes(UUID customerId) {
        return disputeRepository.findByCustomerId(customerId);
    }

    public Dispute resolveDispute(UUID disputeId, Dispute.DisputeStatus resolution) {
        Dispute dispute = disputeRepository.findById(disputeId)
            .orElseThrow(() -> new IllegalArgumentException("Dispute not found: " + disputeId));

        if (dispute.getStatus() == Dispute.DisputeStatus.CLOSED) {
            throw new IllegalStateException("Dispute is already closed");
        }

        dispute.setStatus(resolution);
        return disputeRepository.save(dispute);
    }
}
