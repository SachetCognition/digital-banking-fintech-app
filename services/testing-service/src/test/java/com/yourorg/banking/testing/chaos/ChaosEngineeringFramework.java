package com.yourorg.banking.testing.chaos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive Chaos Engineering Framework
 * 
 * This framework provides:
 * - Network latency simulation
 * - Service failure simulation
 * - Database failure simulation
 * - Memory leak simulation
 * - CPU spike simulation
 * - Resilience testing
 * - Recovery testing
 * - Fault tolerance testing
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ChaosEngineeringFramework {

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate;
    private String baseUrl;
    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        restTemplate = new TestRestTemplate();
        baseUrl = "http://localhost:" + port;
        executorService = Executors.newFixedThreadPool(10);
    }

    @Test
    void testNetworkLatency_ShouldHandleDelayedResponses() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(headers);

        // When - Simulate network latency
        long startTime = System.currentTimeMillis();
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/api/v1/test/network-latency?delay=2000",
            HttpMethod.GET,
            request,
            String.class
        );
        long endTime = System.currentTimeMillis();

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertTrue((endTime - startTime) >= 2000, "Response should be delayed by at least 2 seconds");
    }

    @Test
    void testServiceFailure_ShouldHandleServiceUnavailability() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(headers);

        // When - Simulate service failure
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/api/v1/test/service-failure",
            HttpMethod.GET,
            request,
            String.class
        );

        // Then
        assertTrue(response.getStatusCodeValue() == 503 || response.getStatusCodeValue() == 500);
        assertTrue(response.getBody().contains("service unavailable") || 
                  response.getBody().contains("internal server error"));
    }

    @Test
    void testDatabaseFailure_ShouldHandleDatabaseUnavailability() {
        // Given
        String customerData = """
            {
                "firstName": "John",
                "lastName": "Doe",
                "email": "john.doe@example.com",
                "phone": "+1234567890"
            }
            """;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(customerData, headers);

        // When - Simulate database failure
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/api/v1/test/database-failure",
            HttpMethod.POST,
            request,
            String.class
        );

        // Then
        assertTrue(response.getStatusCodeValue() == 503 || response.getStatusCodeValue() == 500);
        assertTrue(response.getBody().contains("database") || response.getBody().contains("unavailable"));
    }

    @Test
    void testMemoryLeak_ShouldHandleMemoryPressure() {
        // Given
        String largeData = "x".repeat(10000); // 10KB of data
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(largeData, headers);

        // When - Simulate memory leak
        for (int i = 0; i < 100; i++) {
            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/v1/test/memory-leak",
                HttpMethod.POST,
                request,
                String.class
            );

            // Then
            assertTrue(response.getStatusCodeValue() == 200 || response.getStatusCodeValue() == 503);
            
            if (response.getStatusCodeValue() == 503) {
                assertTrue(response.getBody().contains("memory") || response.getBody().contains("pressure"));
                break;
            }
        }
    }

    @Test
    void testCpuSpike_ShouldHandleHighCpuUsage() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(headers);

        // When - Simulate CPU spike
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/api/v1/test/cpu-spike?duration=5000",
            HttpMethod.GET,
            request,
            String.class
        );

        // Then
        assertTrue(response.getStatusCodeValue() == 200 || response.getStatusCodeValue() == 503);
    }

    @Test
    void testConcurrentFailures_ShouldHandleMultipleFailures() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(headers);

        // When - Simulate multiple concurrent failures
        CompletableFuture<ResponseEntity<String>>[] futures = new CompletableFuture[10];
        
        for (int i = 0; i < 10; i++) {
            final int index = i;
            futures[i] = CompletableFuture.supplyAsync(() -> {
                return restTemplate.exchange(
                    baseUrl + "/api/v1/test/concurrent-failure?index=" + index,
                    HttpMethod.GET,
                    request,
                    String.class
                );
            }, executorService);
        }

        // Wait for all requests to complete
        CompletableFuture.allOf(futures).join();

        // Then
        int successCount = 0;
        int failureCount = 0;
        
        for (CompletableFuture<ResponseEntity<String>> future : futures) {
            try {
                ResponseEntity<String> response = future.get();
                if (response.getStatusCodeValue() == 200) {
                    successCount++;
                } else {
                    failureCount++;
                }
            } catch (Exception e) {
                failureCount++;
            }
        }

        assertTrue(successCount > 0, "Some requests should succeed");
        assertTrue(failureCount > 0, "Some requests should fail");
    }

    @Test
    void testRecovery_ShouldRecoverFromFailures() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(headers);

        // When - Simulate failure and recovery
        ResponseEntity<String> failureResponse = restTemplate.exchange(
            baseUrl + "/api/v1/test/simulate-failure",
            HttpMethod.POST,
            request,
            String.class
        );

        // Wait for recovery
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        ResponseEntity<String> recoveryResponse = restTemplate.exchange(
            baseUrl + "/api/v1/test/check-recovery",
            HttpMethod.GET,
            request,
            String.class
        );

        // Then
        assertEquals(200, recoveryResponse.getStatusCodeValue());
        assertTrue(recoveryResponse.getBody().contains("recovered") || 
                  recoveryResponse.getBody().contains("healthy"));
    }

    @Test
    void testCircuitBreaker_ShouldOpenAndCloseCircuit() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(headers);

        // When - Make requests to trigger circuit breaker
        int failureCount = 0;
        int successCount = 0;
        
        for (int i = 0; i < 20; i++) {
            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/v1/test/circuit-breaker",
                HttpMethod.GET,
                request,
                String.class
            );
            
            if (response.getStatusCodeValue() == 200) {
                successCount++;
            } else if (response.getStatusCodeValue() == 503) {
                failureCount++;
            }
        }

        // Then
        assertTrue(failureCount > 0, "Circuit breaker should open after failures");
        assertTrue(successCount > 0, "Some requests should succeed");
    }

    @Test
    void testLoadBalancerFailure_ShouldHandleBackendFailures() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(headers);

        // When - Simulate backend failures
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/api/v1/test/load-balancer-failure",
            HttpMethod.GET,
            request,
            String.class
        );

        // Then
        assertTrue(response.getStatusCodeValue() == 200 || response.getStatusCodeValue() == 503);
    }

    @Test
    void testDataCorruption_ShouldHandleCorruptedData() {
        // Given
        String corruptedData = "corrupted-data-payload";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(corruptedData, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/api/v1/test/data-corruption",
            HttpMethod.POST,
            request,
            String.class
        );

        // Then
        assertTrue(response.getStatusCodeValue() == 400 || response.getStatusCodeValue() == 500);
        assertTrue(response.getBody().contains("corrupted") || response.getBody().contains("invalid"));
    }

    @Test
    void testTimeout_ShouldHandleRequestTimeouts() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(headers);

        // When - Simulate timeout
        long startTime = System.currentTimeMillis();
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/api/v1/test/timeout?duration=10000",
            HttpMethod.GET,
            request,
            String.class
        );
        long endTime = System.currentTimeMillis();

        // Then
        assertTrue(response.getStatusCodeValue() == 408 || response.getStatusCodeValue() == 500);
        assertTrue((endTime - startTime) < 15000, "Request should timeout before 15 seconds");
    }

    @Test
    void testResourceExhaustion_ShouldHandleResourceLimits() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(headers);

        // When - Simulate resource exhaustion
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/api/v1/test/resource-exhaustion",
            HttpMethod.GET,
            request,
            String.class
        );

        // Then
        assertTrue(response.getStatusCodeValue() == 503 || response.getStatusCodeValue() == 500);
        assertTrue(response.getBody().contains("resource") || response.getBody().contains("limit"));
    }

    @Test
    void testChaosMonkey_ShouldRandomlyFailServices() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(headers);

        // When - Enable chaos monkey and make requests
        restTemplate.exchange(
            baseUrl + "/api/v1/test/chaos-monkey/enable",
            HttpMethod.POST,
            request,
            String.class
        );

        int failureCount = 0;
        int successCount = 0;
        
        for (int i = 0; i < 50; i++) {
            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/v1/test/chaos-monkey/request",
                HttpMethod.GET,
                request,
                String.class
            );
            
            if (response.getStatusCodeValue() == 200) {
                successCount++;
            } else {
                failureCount++;
            }
        }

        // Disable chaos monkey
        restTemplate.exchange(
            baseUrl + "/api/v1/test/chaos-monkey/disable",
            HttpMethod.POST,
            request,
            String.class
        );

        // Then
        assertTrue(successCount > 0, "Some requests should succeed");
        assertTrue(failureCount > 0, "Some requests should fail due to chaos monkey");
    }

    @Test
    void testResiliencePatterns_ShouldImplementResiliencePatterns() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(headers);

        // When - Test retry pattern
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/api/v1/test/retry-pattern",
            HttpMethod.GET,
            request,
            String.class
        );

        // Then
        assertTrue(response.getStatusCodeValue() == 200 || response.getStatusCodeValue() == 503);
    }

    @Test
    void testBulkhead_ShouldIsolateFailures() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(headers);

        // When - Test bulkhead pattern
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/api/v1/test/bulkhead-pattern",
            HttpMethod.GET,
            request,
            String.class
        );

        // Then
        assertTrue(response.getStatusCodeValue() == 200 || response.getStatusCodeValue() == 503);
    }

    @Test
    void testGracefulDegradation_ShouldDegradeGracefully() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(headers);

        // When - Simulate degraded service
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/api/v1/test/graceful-degradation",
            HttpMethod.GET,
            request,
            String.class
        );

        // Then
        assertTrue(response.getStatusCodeValue() == 200 || response.getStatusCodeValue() == 503);
        if (response.getStatusCodeValue() == 200) {
            assertTrue(response.getBody().contains("degraded") || response.getBody().contains("limited"));
        }
    }
}

