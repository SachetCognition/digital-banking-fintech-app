package com.yourorg.banking.customer.service;

import com.yourorg.banking.customer.kyc.EmiratesIdValidationService;
import com.yourorg.banking.customer.kyc.UaePassIdentity;
import com.yourorg.banking.customer.kyc.UaePassIntegrationService;
import com.yourorg.banking.customer.model.*;
import com.yourorg.banking.customer.repo.KycCaseRepository;
import com.yourorg.banking.customer.repo.KycDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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

    @Mock
    private EmiratesIdValidationService emiratesIdValidationService;

    @Mock
    private UaePassIntegrationService uaePassIntegrationService;

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
            List.of(new SubmitKycRequest.DocumentSubmission(
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
            List.of(new SubmitKycRequest.DocumentSubmission(
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
            "KYC_PROVIDER", "REF-123", KycStatus.APPROVED, 85, null, null
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
            "KYC_PROVIDER", "REF-123", KycStatus.REJECTED, 0, "Document expired", null
        );

        kycService.processCallback(caseId, callback);

        verify(kycCaseRepository).save(any(KycCase.class));
        assertEquals(KycStatus.REJECTED, kycCase.getStatus());
    }

    @Test
    void submitKyc_withIqamaDocument_accepted() {
        // TC-KY-006: Iqama document acceptance
        when(kycCaseRepository.findByCustomerIdAndStatus(any(), any())).thenReturn(Optional.empty());
        when(kycCaseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SubmitKycRequest request = new SubmitKycRequest(
            customerId,
            KycLevel.STANDARD,
            List.of(new SubmitKycRequest.DocumentSubmission(
                DocumentType.IQAMA, "iqama.pdf", "application/pdf", 2048L, "iqamahash", "storage-2"
            ))
        );

        KycResponse response = kycService.submitKyc(request);

        assertNotNull(response);
        verify(kycCaseRepository, times(2)).save(any(KycCase.class));
        verify(kycDocumentRepository).saveAll(anyList());
    }

    @Test
    void submitKyc_withTradeLicense_verified() {
        // TC-KY-007: Trade License verification (integration-style test)
        when(kycCaseRepository.findByCustomerIdAndStatus(any(), any())).thenReturn(Optional.empty());
        when(kycCaseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SubmitKycRequest request = new SubmitKycRequest(
            customerId,
            KycLevel.ENHANCED,
            List.of(new SubmitKycRequest.DocumentSubmission(
                DocumentType.TRADE_LICENSE, "trade-license.pdf", "application/pdf", 4096L,
                "tradehash", "storage-3"
            ))
        );

        KycResponse response = kycService.submitKyc(request);

        assertNotNull(response);
        verify(validationService).validateDocuments(any(), eq(KycLevel.ENHANCED));
        verify(kycDocumentRepository).saveAll(anyList());
        verify(callbackService).scheduleCallback(any(), any());
    }

    @Test
    void elevateKycWithUaePass_fullFlow() {
        // TC-KY-008: Full KYC flow with Emirates ID + UAE Pass
        when(uaePassIntegrationService.exchangeToken("auth-code")).thenReturn("access-token");
        when(uaePassIntegrationService.getIdentityAssertions("access-token"))
            .thenReturn(new UaePassIdentity(
                "Ahmed Al Maktoum", "784-1990-1234567-1", "UAE", LocalDate.of(1990, 1, 1)));
        when(kycCaseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        KycResponse response = kycService.elevateKycWithUaePass(customerId, "auth-code");

        assertNotNull(response);
        assertEquals(KycLevel.ENHANCED, response.level());
        assertEquals(KycStatus.APPROVED, response.status());
        assertEquals(100, response.riskScore());
        verify(emiratesIdValidationService).validate("784-1990-1234567-1");
        verify(kycDocumentRepository).save(any(KycDocument.class));
        verify(kycCaseRepository, times(2)).save(any(KycCase.class));
    }
}
