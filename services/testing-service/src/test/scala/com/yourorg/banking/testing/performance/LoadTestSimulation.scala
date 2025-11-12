package com.yourorg.banking.testing.performance

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

/**
 * Comprehensive Performance Testing Framework using Gatling
 * 
 * This framework provides:
 * - Load testing scenarios
 * - Stress testing scenarios
 * - Spike testing scenarios
 * - Endurance testing scenarios
 * - Performance monitoring
 */
class LoadTestSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .userAgentHeader("Gatling Performance Test")

  // Test data
  val customerData = Map(
    "firstName" -> "John",
    "lastName" -> "Doe",
    "email" -> "john.doe@example.com",
    "phone" -> "+1234567890"
  )

  val accountData = Map(
    "accountType" -> "CHECKING",
    "initialBalance" -> 1000.00
  )

  val transferData = Map(
    "amount" -> 100.00,
    "description" -> "Performance test transfer"
  )

  // Scenarios
  val customerCreationScenario = scenario("Customer Creation")
    .exec(
      http("Create Customer")
        .post("/api/v1/customers")
        .body(StringBody("""{"firstName": "John", "lastName": "Doe", "email": "john.doe@example.com", "phone": "+1234567890"}"""))
        .check(status.is(201))
        .check(jsonPath("$.id").saveAs("customerId"))
    )
    .pause(1)

  val accountCreationScenario = scenario("Account Creation")
    .exec(
      http("Create Account")
        .post("/api/v1/accounts")
        .body(StringBody("""{"customerId": "${customerId}", "accountType": "CHECKING", "initialBalance": 1000.00}"""))
        .check(status.is(201))
        .check(jsonPath("$.id").saveAs("accountId"))
    )
    .pause(1)

  val transferScenario = scenario("Transfer Processing")
    .exec(
      http("Process Transfer")
        .post("/api/v1/transfers")
        .header("Idempotency-Key", "${randomUUID()}")
        .body(StringBody("""{"fromAccountId": "${accountId}", "toAccountId": "${accountId}", "amount": 100.00, "description": "Test transfer"}"""))
        .check(status.is(200))
        .check(jsonPath("$.transferId").saveAs("transferId"))
    )
    .pause(1)

  val accountBalanceScenario = scenario("Account Balance Check")
    .exec(
      http("Get Account Balance")
        .get("/api/v1/accounts/${accountId}/balance")
        .check(status.is(200))
        .check(jsonPath("$.balance").exists)
    )
    .pause(1)

  val customerListScenario = scenario("Customer List")
    .exec(
      http("Get Customer List")
        .get("/api/v1/customers")
        .check(status.is(200))
        .check(jsonPath("$[*].id").exists)
    )
    .pause(1)

  // Load Test Scenarios
  val loadTestScenario = scenario("Load Test")
    .exec(customerCreationScenario)
    .exec(accountCreationScenario)
    .exec(transferScenario)
    .exec(accountBalanceScenario)

  val stressTestScenario = scenario("Stress Test")
    .exec(customerCreationScenario)
    .exec(accountCreationScenario)
    .exec(transferScenario)
    .exec(accountBalanceScenario)
    .exec(customerListScenario)

  val spikeTestScenario = scenario("Spike Test")
    .exec(customerCreationScenario)
    .exec(accountCreationScenario)
    .exec(transferScenario)

  val enduranceTestScenario = scenario("Endurance Test")
    .exec(customerCreationScenario)
    .exec(accountCreationScenario)
    .exec(transferScenario)
    .exec(accountBalanceScenario)

  // Test Configurations
  val loadTestSetup = setUp(
    loadTestScenario.inject(
      rampUsers(10) during (30 seconds),
      constantUsers(50) during (2 minutes),
      rampUsers(10) during (30 seconds)
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.max.lt(2000),
      global.successfulRequests.percent.gt(95)
    )

  val stressTestSetup = setUp(
    stressTestScenario.inject(
      rampUsers(20) during (1 minute),
      constantUsers(100) during (3 minutes),
      rampUsers(20) during (1 minute)
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.max.lt(5000),
      global.successfulRequests.percent.gt(90)
    )

  val spikeTestSetup = setUp(
    spikeTestScenario.inject(
      rampUsers(10) during (30 seconds),
      atOnceUsers(200),
      rampUsers(10) during (30 seconds)
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.max.lt(10000),
      global.successfulRequests.percent.gt(85)
    )

  val enduranceTestSetup = setUp(
    enduranceTestScenario.inject(
      rampUsers(20) during (1 minute),
      constantUsers(50) during (10 minutes),
      rampUsers(20) during (1 minute)
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.max.lt(3000),
      global.successfulRequests.percent.gt(95)
    )

  // Performance Monitoring
  val performanceMonitoring = scenario("Performance Monitoring")
    .exec(
      http("Health Check")
        .get("/health")
        .check(status.is(200))
    )
    .exec(
      http("Metrics Check")
        .get("/actuator/metrics")
        .check(status.is(200))
    )
    .exec(
      http("Prometheus Metrics")
        .get("/actuator/prometheus")
        .check(status.is(200))
    )

  val monitoringSetup = setUp(
    performanceMonitoring.inject(
      constantUsers(1) during (5 minutes)
    )
  ).protocols(httpProtocol)

  // Database Performance Test
  val databasePerformanceScenario = scenario("Database Performance")
    .exec(
      http("Database Query Test")
        .get("/api/v1/test/database/performance")
        .check(status.is(200))
        .check(jsonPath("$.queryTime").lt(1000))
    )
    .pause(1)

  val databaseTestSetup = setUp(
    databasePerformanceScenario.inject(
      rampUsers(10) during (30 seconds),
      constantUsers(50) during (2 minutes)
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.max.lt(2000),
      global.successfulRequests.percent.gt(95)
    )

  // Cache Performance Test
  val cachePerformanceScenario = scenario("Cache Performance")
    .exec(
      http("Cache Hit Test")
        .get("/api/v1/test/cache/performance")
        .check(status.is(200))
        .check(jsonPath("$.hitRate").gt(0.8))
    )
    .pause(1)

  val cacheTestSetup = setUp(
    cachePerformanceScenario.inject(
      rampUsers(20) during (1 minute),
      constantUsers(100) during (3 minutes)
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.max.lt(1000),
      global.successfulRequests.percent.gt(95)
    )

  // API Rate Limiting Test
  val rateLimitingScenario = scenario("Rate Limiting Test")
    .exec(
      http("Rate Limit Test")
        .get("/api/v1/test/rate-limit")
        .check(status.in(200, 429))
    )
    .pause(100 milliseconds)

  val rateLimitingTestSetup = setUp(
    rateLimitingScenario.inject(
      rampUsers(100) during (10 seconds),
      constantUsers(100) during (1 minute)
    )
  ).protocols(httpProtocol)
    .assertions(
      global.successfulRequests.percent.gt(80)
    )

  // Memory Leak Test
  val memoryLeakScenario = scenario("Memory Leak Test")
    .exec(
      http("Memory Intensive Operation")
        .post("/api/v1/test/memory-intensive")
        .body(StringBody("""{"data": "large data string for memory testing"}"""))
        .check(status.is(200))
    )
    .pause(1)

  val memoryLeakTestSetup = setUp(
    memoryLeakScenario.inject(
      rampUsers(10) during (1 minute),
      constantUsers(20) during (5 minutes)
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.max.lt(5000),
      global.successfulRequests.percent.gt(90)
    )

  // Concurrent User Test
  val concurrentUserScenario = scenario("Concurrent User Test")
    .exec(
      http("Concurrent Operation")
        .post("/api/v1/test/concurrent")
        .body(StringBody("""{"operation": "concurrent_test"}"""))
        .check(status.is(200))
    )
    .pause(1)

  val concurrentUserTestSetup = setUp(
    concurrentUserScenario.inject(
      atOnceUsers(200)
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.max.lt(10000),
      global.successfulRequests.percent.gt(80)
    )

  // Set up the main simulation
  setUp(
    loadTestScenario.inject(
      rampUsers(10) during (30 seconds),
      constantUsers(50) during (2 minutes),
      rampUsers(10) during (30 seconds)
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.max.lt(2000),
      global.successfulRequests.percent.gt(95),
      global.responseTime.mean.lt(1000),
      global.responseTime.percentile3.lt(1500)
    )
}

