package com.yourorg.banking.payments.service;

import com.yourorg.banking.payments.model.*;
import com.yourorg.banking.payments.repository.BeneficiaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BeneficiaryService {
    
    private final BeneficiaryRepository beneficiaryRepository;
    private static final int MAX_BENEFICIARIES_PER_CUSTOMER = 50;
    
    public BeneficiaryService(BeneficiaryRepository beneficiaryRepository) {
        this.beneficiaryRepository = beneficiaryRepository;
    }
    
    @Transactional
    public BeneficiaryResponse createBeneficiary(UUID customerId, CreateBeneficiaryRequest request) {
        // Check if customer has reached maximum beneficiaries limit
        long currentCount = beneficiaryRepository.countByCustomerIdAndStatus(customerId, BeneficiaryStatus.ACTIVE);
        if (currentCount >= MAX_BENEFICIARIES_PER_CUSTOMER) {
            throw new IllegalArgumentException("Maximum number of beneficiaries reached (" + MAX_BENEFICIARIES_PER_CUSTOMER + ")");
        }
        
        // Check if beneficiary with same account number and bank code already exists
        Optional<Beneficiary> existing = beneficiaryRepository.findByCustomerIdAndAccountNumberAndBankCode(
            customerId, request.accountNumber(), request.bankCode());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Beneficiary with this account number and bank code already exists");
        }
        
        Beneficiary beneficiary = new Beneficiary(
            UUID.randomUUID(),
            customerId,
            request.name(),
            request.accountNumber(),
            request.bankCode(),
            request.bankName(),
            request.currency(),
            request.type(),
            request.description()
        );
        
        beneficiaryRepository.save(beneficiary);
        return createBeneficiaryResponse(beneficiary);
    }
    
    public List<BeneficiaryResponse> getBeneficiaries(UUID customerId, BeneficiaryType type, String searchTerm) {
        List<Beneficiary> beneficiaries;
        
        if (type != null && searchTerm != null && !searchTerm.trim().isEmpty()) {
            beneficiaries = beneficiaryRepository.findByCustomerIdAndStatusAndSearchTerm(
                customerId, BeneficiaryStatus.ACTIVE, searchTerm.trim());
        } else if (type != null) {
            beneficiaries = beneficiaryRepository.findByCustomerIdAndTypeAndStatus(
                customerId, type, BeneficiaryStatus.ACTIVE);
        } else {
            beneficiaries = beneficiaryRepository.findByCustomerIdAndStatus(customerId, BeneficiaryStatus.ACTIVE);
        }
        
        return beneficiaries.stream()
                .map(this::createBeneficiaryResponse)
                .collect(Collectors.toList());
    }
    
    public Optional<BeneficiaryResponse> getBeneficiary(UUID customerId, UUID beneficiaryId) {
        return beneficiaryRepository.findById(beneficiaryId)
                .filter(beneficiary -> beneficiary.getCustomerId().equals(customerId))
                .map(this::createBeneficiaryResponse);
    }
    
    @Transactional
    public Optional<BeneficiaryResponse> updateBeneficiary(UUID customerId, UUID beneficiaryId, UpdateBeneficiaryRequest request) {
        return beneficiaryRepository.findById(beneficiaryId)
                .filter(beneficiary -> beneficiary.getCustomerId().equals(customerId))
                .map(beneficiary -> {
                    beneficiary.update(request.name(), request.description());
                    beneficiaryRepository.save(beneficiary);
                    return createBeneficiaryResponse(beneficiary);
                });
    }
    
    @Transactional
    public boolean deleteBeneficiary(UUID customerId, UUID beneficiaryId) {
        return beneficiaryRepository.findById(beneficiaryId)
                .filter(beneficiary -> beneficiary.getCustomerId().equals(customerId))
                .map(beneficiary -> {
                    beneficiary.deactivate();
                    beneficiaryRepository.save(beneficiary);
                    return true;
                })
                .orElse(false);
    }
    
    @Transactional
    public boolean activateBeneficiary(UUID customerId, UUID beneficiaryId) {
        return beneficiaryRepository.findById(beneficiaryId)
                .filter(beneficiary -> beneficiary.getCustomerId().equals(customerId))
                .map(beneficiary -> {
                    beneficiary.activate();
                    beneficiaryRepository.save(beneficiary);
                    return true;
                })
                .orElse(false);
    }
    
    public Optional<Beneficiary> getBeneficiaryForPayment(UUID customerId, UUID beneficiaryId) {
        return beneficiaryRepository.findById(beneficiaryId)
                .filter(beneficiary -> beneficiary.getCustomerId().equals(customerId))
                .filter(beneficiary -> beneficiary.getStatus() == BeneficiaryStatus.ACTIVE);
    }
    
    private BeneficiaryResponse createBeneficiaryResponse(Beneficiary beneficiary) {
        return new BeneficiaryResponse(
            beneficiary.getId(),
            beneficiary.getCustomerId(),
            beneficiary.getName(),
            beneficiary.getAccountNumber(),
            beneficiary.getBankCode(),
            beneficiary.getBankName(),
            beneficiary.getCurrency(),
            beneficiary.getType(),
            beneficiary.getStatus(),
            beneficiary.getDescription(),
            beneficiary.getCreatedAt(),
            beneficiary.getUpdatedAt()
        );
    }
}

