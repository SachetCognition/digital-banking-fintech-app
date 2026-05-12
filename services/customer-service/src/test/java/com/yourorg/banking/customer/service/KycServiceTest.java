package com.yourorg.banking.customer.service;

import com.yourorg.banking.customer.model.*;
import com.yourorg.banking.customer.repo.KycCaseRepository;
import com.yourorg.banking.customer.repo.KycDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KycServiceTest {

    @Mock
    private KycCaseRepository kycCaseRepository;

    @Mock
    private KycDocumentRepository kycDocumentRepository;

    @Mock
    private KycValidationService validationService;

    @Mock
    private KycCallbackService callbackService;

    @InjectMocks
    private KycService kycService;

    private UUID customerId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
    }

    @Test
    void submitKyc_createsKycCaseAndDocuments() {
        when(kycCaseRepository.findByCustomerIdAndStatus(any(), any())).thenReturn(Optional.empty());

        SubmitKycRequest request = new SubmitKycRequest(
            customerId,
            KycLevel.ENHANCED,
            List.of(new SubmitKycRequest.DocumentRequest(
                DocumentType.PASSPORT, "passport.pdf", "application/pdf", 1024L, "abc123hash", "storage-1"
            ))
        );

        when(kycCaseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        KycResponse response = kycService.submitKyc(request);

        assertNotNull(response);
        verify(kycCaseRepository, times(2)).save(any(KycCase.class));
        verify(kycDocumentRepository).saveAll(anyList());
        verify(callbackService).scheduleCallback(any(), any());
    }

    @Test
    void submitKyc_rejectsDuplicatePendingKyc() {
        KycCase existingCase = new KycCase(UUID.randomUUID(), customerId, KycLevel.BASIC);
        when(kycCaseRepository.findByCustomerIdAndStatus(customerId, KycStatus.PENDING))
            .thenReturn(Optional.of(existingCase));

        SubmitKycRequest request = new SubmitKycRequest(
            customerId,
            KycLevel.BASIC,
            List.of(new SubmitKycRequest.DocumentRequest(
                DocumentType.PASSPORT, "passport.pdf", "application/pdf", 1024L, "abc123hash", "storage-1"
            ))
        );

        assertThrows(IllegalArgumentException.class, () -> kycService.submitKyc(request));
    }

    @Test
    void processCallback_approvesKycCase() {
        UUID caseId = UUID.randomUUID();
        KycCase kycCase = new KycCase(caseId, customerId, KycLevel.BASIC);
        kycCase.submit("PROVIDER", "REF-123");

        when(kycCaseRepository.findById(caseId)).thenReturn(Optional.of(kycCase));

        KycCallbackRequest callback = new KycCallbackRequest(
            KycStatus.APPROVED, 85, null, null
        );

        kycService.processCallback(caseId, callback);

        verify(kycCaseRepository).save(any(KycCase.class));
        assertEquals(KycStatus.APPROVED, kycCase.getStatus());
    }

    @Test
    void processCallback_rejectsKycCase() {
        UUID caseId = UUID.randomUUID();
        KycCase kycCase = new KycCase(caseId, customerId, KycLevel.BASIC);
        kycCase.submit("PROVIDER", "REF-123");

        when(kycCaseRepository.findById(caseId)).thenReturn(Optional.of(kycCase));

        KycCallbackRequest callback = new KycCallbackRequest(
            KycStatus.REJECTED, 0, "Document expired", null
        );

        kycService.processCallback(caseId, callback);

        verify(kycCaseRepository).save(any(KycCase.class));
        assertEquals(KycStatus.REJECTED, kycCase.getStatus());
    }
}
