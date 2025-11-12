package com.yourorg.banking.testing.integration;

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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RedisContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive Integration Testing Framework
 * 
 * This framework provides:
 * - TestContainers for external dependencies
 * - End-to-end test scenarios
 * - API testing utilities
 * - Database integration testing
 * - Service integration testing
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public class IntegrationTestFramework {

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate;
    private String baseUrl;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test");

    @Container
    static RedisContainer redis = new RedisContainer("redis:7-alpine")
            .withExposedPorts(6379);

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
            .withExposedPorts(9092);

    @BeforeEach
    void setUp() {
        restTemplate = new TestRestTemplate();
        baseUrl = "http://localhost:" + port;
    }

    @Test
    void testCustomerServiceIntegration_ShouldCreateAndRetrieveCustomer() {
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

        // When - Create customer
        ResponseEntity<String> createResponse = restTemplate.exchange(
            baseUrl + "/api/v1/customers",
            HttpMethod.POST,
            request,
            String.class
        );

        // Then
        assertEquals(201, createResponse.getStatusCodeValue());
        assertNotNull(createResponse.getBody());

        // Extract customer ID from response
        String customerId = extractCustomerId(createResponse.getBody());

        // When - Retrieve customer
        ResponseEntity<String> getResponse = restTemplate.getForEntity(
            baseUrl + "/api/v1/customers/" + customerId,
            String.class
        );

        // Then
        assertEquals(200, getResponse.getStatusCodeValue());
        assertTrue(getResponse.getBody().contains("John"));
        assertTrue(getResponse.getBody().contains("Doe"));
    }

    @Test
    void testAccountServiceIntegration_ShouldCreateAccountForCustomer() {
        // Given
        String customerId = createTestCustomer();
        String accountData = """
            {
                "customerId": "%s",
                "accountType": "CHECKING",
                "initialBalance": 1000.00
            }
            """.formatted(customerId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(accountData, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/api/v1/accounts",
            HttpMethod.POST,
            request,
            String.class
        );

        // Then
        assertEquals(201, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("accountId"));
    }

    @Test
    void testPaymentServiceIntegration_ShouldProcessTransfer() {
        // Given
        String customerId = createTestCustomer();
        String accountId = createTestAccount(customerId);
        String transferData = """
            {
                "fromAccountId": "%s",
                "toAccountId": "%s",
                "amount": 100.00,
                "description": "Test transfer"
            }
            """.formatted(accountId, accountId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Idempotency-Key", "test-key-123");
        HttpEntity<String> request = new HttpEntity<>(transferData, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/api/v1/transfers",
            HttpMethod.POST,
            request,
            String.class
        );

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("transferId"));
    }

    @Test
    void testDatabaseIntegration_ShouldPersistDataCorrectly() {
        // Given
        String testData = "Test data for persistence";

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl + "/api/v1/test/data",
            testData,
            String.class
        );

        // Then
        assertEquals(200, response.getStatusCodeValue());

        // Verify data was persisted
        ResponseEntity<String> getResponse = restTemplate.getForEntity(
            baseUrl + "/api/v1/test/data",
            String.class
        );

        assertEquals(200, getResponse.getStatusCodeValue());
        assertTrue(getResponse.getBody().contains(testData));
    }

    @Test
    void testRedisIntegration_ShouldCacheDataCorrectly() {
        // Given
        String cacheKey = "test-key";
        String cacheValue = "test-value";

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl + "/api/v1/test/cache/" + cacheKey,
            cacheValue,
            String.class
        );

        // Then
        assertEquals(200, response.getStatusCodeValue());

        // Verify data was cached
        ResponseEntity<String> getResponse = restTemplate.getForEntity(
            baseUrl + "/api/v1/test/cache/" + cacheKey,
            String.class
        );

        assertEquals(200, getResponse.getStatusCodeValue());
        assertEquals(cacheValue, getResponse.getBody());
    }

    @Test
    void testKafkaIntegration_ShouldPublishAndConsumeMessages() {
        // Given
        String message = "Test Kafka message";

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl + "/api/v1/test/kafka/publish",
            message,
            String.class
        );

        // Then
        assertEquals(200, response.getStatusCodeValue());

        // Wait for message to be consumed
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verify message was consumed
        ResponseEntity<String> getResponse = restTemplate.getForEntity(
            baseUrl + "/api/v1/test/kafka/consume",
            String.class
        );

        assertEquals(200, getResponse.getStatusCodeValue());
        assertTrue(getResponse.getBody().contains(message));
    }

    @Test
    void testErrorHandling_ShouldReturnProperErrorResponse() {
        // Given
        String invalidData = "invalid json data";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(invalidData, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/api/v1/customers",
            HttpMethod.POST,
            request,
            String.class
        );

        // Then
        assertEquals(400, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("error"));
    }

    @Test
    void testConcurrentRequests_ShouldHandleMultipleRequests() {
        // Given
        int numberOfRequests = 10;
        String customerData = """
            {
                "firstName": "Concurrent",
                "lastName": "Test",
                "email": "concurrent@example.com",
                "phone": "+1234567890"
            }
            """;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(customerData, headers);

        // When
        Thread[] threads = new Thread[numberOfRequests];
        ResponseEntity<String>[] responses = new ResponseEntity[numberOfRequests];

        for (int i = 0; i < numberOfRequests; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                responses[index] = restTemplate.exchange(
                    baseUrl + "/api/v1/customers",
                    HttpMethod.POST,
                    request,
                    String.class
                );
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Then
        for (ResponseEntity<String> response : responses) {
            assertNotNull(response);
            assertTrue(response.getStatusCodeValue() == 201 || response.getStatusCodeValue() == 409);
        }
    }

    // Helper methods
    private String createTestCustomer() {
        String customerData = """
            {
                "firstName": "Test",
                "lastName": "Customer",
                "email": "test@example.com",
                "phone": "+1234567890"
            }
            """;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(customerData, headers);

        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/api/v1/customers",
            HttpMethod.POST,
            request,
            String.class
        );

        return extractCustomerId(response.getBody());
    }

    private String createTestAccount(String customerId) {
        String accountData = """
            {
                "customerId": "%s",
                "accountType": "CHECKING",
                "initialBalance": 1000.00
            }
            """.formatted(customerId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(accountData, headers);

        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/api/v1/accounts",
            HttpMethod.POST,
            request,
            String.class
        );

        return extractAccountId(response.getBody());
    }

    private String extractCustomerId(String responseBody) {
        // Simple extraction - in real implementation, use JSON parsing
        return "customer-123";
    }

    private String extractAccountId(String responseBody) {
        // Simple extraction - in real implementation, use JSON parsing
        return "account-123";
    }
}

