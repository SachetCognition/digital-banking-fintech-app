package com.yourorg.banking.customer.kyc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UaePassIntegrationServiceTest {

    @Mock
    private UaePassClient uaePassClient;

    @InjectMocks
    private UaePassIntegrationService service;

    @Test
    void exchangeTokenAndGetIdentity_validToken_returnsAssertions() {
        // TC-KY-004: Valid token returns identity assertions
        when(uaePassClient.exchangeToken("valid-code")).thenReturn("valid-token");
        when(uaePassClient.getIdentityAssertions("valid-token"))
                .thenReturn(new UaePassIdentity(
                        "John Doe", "784-1990-1234567-1", "UAE", LocalDate.of(1990, 1, 1)));

        String token = service.exchangeToken("valid-code");
        assertEquals("valid-token", token);

        UaePassIdentity identity = service.getIdentityAssertions(token);
        assertNotNull(identity);
        assertEquals("John Doe", identity.fullName());
        assertEquals("784-1990-1234567-1", identity.emiratesId());
        assertEquals("UAE", identity.nationality());
        assertEquals(LocalDate.of(1990, 1, 1), identity.dateOfBirth());
    }

    @Test
    void getIdentityAssertions_expiredToken_throwsException() {
        // TC-KY-005: Expired token throws exception
        when(uaePassClient.getIdentityAssertions("expired-token"))
                .thenThrow(new UaePassTokenExpiredException("Token expired"));

        assertThrows(UaePassTokenExpiredException.class,
                () -> service.getIdentityAssertions("expired-token"));
    }
}
