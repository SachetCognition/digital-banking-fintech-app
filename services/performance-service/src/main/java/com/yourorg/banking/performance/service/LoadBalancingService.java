package com.yourorg.banking.performance.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LoadBalancingService {

    @Autowired
    private RestTemplate restTemplate;
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(20);
    private final Map<String, List<ServiceInstance>> serviceInstances = new HashMap<>();
    private final Map<String, AtomicInteger> roundRobinCounters = new HashMap<>();
    private final Map<String, ServiceInstance> healthCheckCache = new HashMap<>();

    /**
     * Register a service instance
     */
    public void registerServiceInstance(String serviceName, ServiceInstance instance) {
        serviceInstances.computeIfAbsent(serviceName, k -> new ArrayList<>()).add(instance);
        roundRobinCounters.putIfAbsent(serviceName, new AtomicInteger(0));
    }

    /**
     * Unregister a service instance
     */
    public void unregisterServiceInstance(String serviceName, String instanceId) {
        serviceInstances.computeIfPresent(serviceName, (k, instances) -> {
            instances.removeIf(instance -> instance.getInstanceId().equals(instanceId));
            return instances.isEmpty() ? null : instances;
        });
    }

    /**
     * Get the best available instance for a service
     */
    public ServiceInstance getBestInstance(String serviceName, LoadBalancingStrategy strategy) {
        List<ServiceInstance> instances = serviceInstances.get(serviceName);
        if (instances == null || instances.isEmpty()) {
            throw new RuntimeException("No instances available for service: " + serviceName);
        }

        // Filter healthy instances
        List<ServiceInstance> healthyInstances = instances.stream()
            .filter(instance -> isInstanceHealthy(instance))
            .toList();

        if (healthyInstances.isEmpty()) {
            throw new RuntimeException("No healthy instances available for service: " + serviceName);
        }

        return selectInstance(healthyInstances, strategy, serviceName);
    }

    /**
     * Select instance based on strategy
     */
    private ServiceInstance selectInstance(List<ServiceInstance> instances, LoadBalancingStrategy strategy, String serviceName) {
        return switch (strategy) {
            case ROUND_ROBIN -> selectRoundRobin(instances, serviceName);
            case LEAST_CONNECTIONS -> selectLeastConnections(instances);
            case WEIGHTED_ROUND_ROBIN -> selectWeightedRoundRobin(instances, serviceName);
            case LEAST_RESPONSE_TIME -> selectLeastResponseTime(instances);
            case RANDOM -> selectRandom(instances);
            case IP_HASH -> selectIpHash(instances);
        };
    }

    /**
     * Round Robin selection
     */
    private ServiceInstance selectRoundRobin(List<ServiceInstance> instances, String serviceName) {
        AtomicInteger counter = roundRobinCounters.get(serviceName);
        int index = counter.getAndIncrement() % instances.size();
        return instances.get(index);
    }

    /**
     * Least Connections selection
     */
    private ServiceInstance selectLeastConnections(List<ServiceInstance> instances) {
        return instances.stream()
            .min(Comparator.comparingInt(ServiceInstance::getActiveConnections))
            .orElse(instances.get(0));
    }

    /**
     * Weighted Round Robin selection
     */
    private ServiceInstance selectWeightedRoundRobin(List<ServiceInstance> instances, String serviceName) {
        int totalWeight = instances.stream().mapToInt(ServiceInstance::getWeight).sum();
        AtomicInteger counter = roundRobinCounters.get(serviceName);
        int currentWeight = counter.getAndIncrement() % totalWeight;
        
        int cumulativeWeight = 0;
        for (ServiceInstance instance : instances) {
            cumulativeWeight += instance.getWeight();
            if (currentWeight < cumulativeWeight) {
                return instance;
            }
        }
        
        return instances.get(0);
    }

    /**
     * Least Response Time selection
     */
    private ServiceInstance selectLeastResponseTime(List<ServiceInstance> instances) {
        return instances.stream()
            .min(Comparator.comparingDouble(ServiceInstance::getAvgResponseTime))
            .orElse(instances.get(0));
    }

    /**
     * Random selection
     */
    private ServiceInstance selectRandom(List<ServiceInstance> instances) {
        Random random = new Random();
        return instances.get(random.nextInt(instances.size()));
    }

    /**
     * IP Hash selection
     */
    private ServiceInstance selectIpHash(List<ServiceInstance> instances) {
        // This would typically use the client IP
        String clientIp = getClientIp();
        int hash = clientIp.hashCode();
        int index = Math.abs(hash) % instances.size();
        return instances.get(index);
    }

    /**
     * Check if instance is healthy
     */
    public boolean isInstanceHealthy(ServiceInstance instance) {
        String cacheKey = instance.getInstanceId();
        ServiceInstance cached = healthCheckCache.get(cacheKey);
        
        if (cached != null && System.currentTimeMillis() - cached.getLastHealthCheck() < 30000) {
            return cached.isHealthy();
        }
        
        boolean healthy = performHealthCheck(instance);
        instance.setHealthy(healthy);
        instance.setLastHealthCheck(System.currentTimeMillis());
        healthCheckCache.put(cacheKey, instance);
        
        return healthy;
    }

    /**
     * Perform health check on instance
     */
    private boolean performHealthCheck(ServiceInstance instance) {
        try {
            String healthUrl = instance.getUrl() + "/health";
            ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Perform health checks on all instances
     */
    public void performHealthChecks() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (Map.Entry<String, List<ServiceInstance>> entry : serviceInstances.entrySet()) {
            String serviceName = entry.getKey();
            List<ServiceInstance> instances = entry.getValue();
            
            for (ServiceInstance instance : instances) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    boolean healthy = performHealthCheck(instance);
                    instance.setHealthy(healthy);
                    instance.setLastHealthCheck(System.currentTimeMillis());
                    healthCheckCache.put(instance.getInstanceId(), instance);
                }, executorService);
                
                futures.add(future);
            }
        }
        
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    /**
     * Get load balancing statistics
     */
    public LoadBalancingStatistics getLoadBalancingStatistics() {
        LoadBalancingStatistics stats = new LoadBalancingStatistics();
        
        for (Map.Entry<String, List<ServiceInstance>> entry : serviceInstances.entrySet()) {
            String serviceName = entry.getKey();
            List<ServiceInstance> instances = entry.getValue();
            
            ServiceStatistics serviceStats = new ServiceStatistics();
            serviceStats.setServiceName(serviceName);
            serviceStats.setTotalInstances(instances.size());
            serviceStats.setHealthyInstances((int) instances.stream().filter(ServiceInstance::isHealthy).count());
            serviceStats.setUnhealthyInstances((int) instances.stream().filter(instance -> !instance.isHealthy()).count());
            
            double avgResponseTime = instances.stream()
                .mapToDouble(ServiceInstance::getAvgResponseTime)
                .average()
                .orElse(0.0);
            serviceStats.setAvgResponseTime(avgResponseTime);
            
            int totalConnections = instances.stream()
                .mapToInt(ServiceInstance::getActiveConnections)
                .sum();
            serviceStats.setTotalConnections(totalConnections);
            
            stats.getServiceStatistics().put(serviceName, serviceStats);
        }
        
        return stats;
    }

    /**
     * Update instance metrics
     */
    public void updateInstanceMetrics(String serviceName, String instanceId, InstanceMetrics metrics) {
        List<ServiceInstance> instances = serviceInstances.get(serviceName);
        if (instances != null) {
            instances.stream()
                .filter(instance -> instance.getInstanceId().equals(instanceId))
                .findFirst()
                .ifPresent(instance -> {
                    instance.setActiveConnections(metrics.getActiveConnections());
                    instance.setAvgResponseTime(metrics.getAvgResponseTime());
                    instance.setTotalRequests(metrics.getTotalRequests());
                    instance.setErrorCount(metrics.getErrorCount());
                });
        }
    }

    /**
     * Get service instances
     */
    public List<ServiceInstance> getServiceInstances(String serviceName) {
        return serviceInstances.getOrDefault(serviceName, new ArrayList<>());
    }

    /**
     * Configure load balancing rules
     */
    public void configureLoadBalancingRules(String serviceName, LoadBalancingRules rules) {
        // Apply load balancing rules for a specific service
        List<ServiceInstance> instances = serviceInstances.get(serviceName);
        if (instances != null) {
            for (ServiceInstance instance : instances) {
                instance.setWeight(rules.getDefaultWeight());
                instance.setMaxConnections(rules.getMaxConnections());
                instance.setTimeout(rules.getTimeout());
            }
        }
    }

    /**
     * Enable circuit breaker for service
     */
    public void enableCircuitBreaker(String serviceName, CircuitBreakerConfig config) {
        List<ServiceInstance> instances = serviceInstances.get(serviceName);
        if (instances != null) {
            for (ServiceInstance instance : instances) {
                instance.setCircuitBreakerEnabled(true);
                instance.setCircuitBreakerConfig(config);
            }
        }
    }

    /**
     * Check circuit breaker status
     */
    public boolean isCircuitBreakerOpen(ServiceInstance instance) {
        if (!instance.isCircuitBreakerEnabled()) {
            return false;
        }
        
        CircuitBreakerConfig config = instance.getCircuitBreakerConfig();
        if (config == null) {
            return false;
        }
        
        // Check if failure threshold is exceeded
        double errorRate = (double) instance.getErrorCount() / instance.getTotalRequests();
        return errorRate > config.getFailureThreshold();
    }

    /**
     * Get client IP (placeholder implementation)
     */
    private String getClientIp() {
        // This would typically be extracted from the request context
        return "127.0.0.1";
    }

    // Data classes
    public static class ServiceInstance {
        private String instanceId;
        private String url;
        private int weight;
        private int activeConnections;
        private double avgResponseTime;
        private long totalRequests;
        private long errorCount;
        private boolean healthy;
        private long lastHealthCheck;
        private int maxConnections;
        private int timeout;
        private boolean circuitBreakerEnabled;
        private CircuitBreakerConfig circuitBreakerConfig;

        // Constructors
        public ServiceInstance() {}

        public ServiceInstance(String instanceId, String url) {
            this.instanceId = instanceId;
            this.url = url;
            this.weight = 1;
            this.healthy = true;
            this.lastHealthCheck = System.currentTimeMillis();
        }

        // Getters and setters
        public String getInstanceId() { return instanceId; }
        public void setInstanceId(String instanceId) { this.instanceId = instanceId; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public int getWeight() { return weight; }
        public void setWeight(int weight) { this.weight = weight; }
        public int getActiveConnections() { return activeConnections; }
        public void setActiveConnections(int activeConnections) { this.activeConnections = activeConnections; }
        public double getAvgResponseTime() { return avgResponseTime; }
        public void setAvgResponseTime(double avgResponseTime) { this.avgResponseTime = avgResponseTime; }
        public long getTotalRequests() { return totalRequests; }
        public void setTotalRequests(long totalRequests) { this.totalRequests = totalRequests; }
        public long getErrorCount() { return errorCount; }
        public void setErrorCount(long errorCount) { this.errorCount = errorCount; }
        public boolean isHealthy() { return healthy; }
        public void setHealthy(boolean healthy) { this.healthy = healthy; }
        public long getLastHealthCheck() { return lastHealthCheck; }
        public void setLastHealthCheck(long lastHealthCheck) { this.lastHealthCheck = lastHealthCheck; }
        public int getMaxConnections() { return maxConnections; }
        public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }
        public int getTimeout() { return timeout; }
        public void setTimeout(int timeout) { this.timeout = timeout; }
        public boolean isCircuitBreakerEnabled() { return circuitBreakerEnabled; }
        public void setCircuitBreakerEnabled(boolean circuitBreakerEnabled) { this.circuitBreakerEnabled = circuitBreakerEnabled; }
        public CircuitBreakerConfig getCircuitBreakerConfig() { return circuitBreakerConfig; }
        public void setCircuitBreakerConfig(CircuitBreakerConfig circuitBreakerConfig) { this.circuitBreakerConfig = circuitBreakerConfig; }
    }

    public static class InstanceMetrics {
        private int activeConnections;
        private double avgResponseTime;
        private long totalRequests;
        private long errorCount;

        // Getters and setters
        public int getActiveConnections() { return activeConnections; }
        public void setActiveConnections(int activeConnections) { this.activeConnections = activeConnections; }
        public double getAvgResponseTime() { return avgResponseTime; }
        public void setAvgResponseTime(double avgResponseTime) { this.avgResponseTime = avgResponseTime; }
        public long getTotalRequests() { return totalRequests; }
        public void setTotalRequests(long totalRequests) { this.totalRequests = totalRequests; }
        public long getErrorCount() { return errorCount; }
        public void setErrorCount(long errorCount) { this.errorCount = errorCount; }
    }

    public static class LoadBalancingStatistics {
        private Map<String, ServiceStatistics> serviceStatistics = new HashMap<>();

        public Map<String, ServiceStatistics> getServiceStatistics() { return serviceStatistics; }
        public void setServiceStatistics(Map<String, ServiceStatistics> serviceStatistics) { this.serviceStatistics = serviceStatistics; }
    }

    public static class ServiceStatistics {
        private String serviceName;
        private int totalInstances;
        private int healthyInstances;
        private int unhealthyInstances;
        private double avgResponseTime;
        private int totalConnections;

        // Getters and setters
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        public int getTotalInstances() { return totalInstances; }
        public void setTotalInstances(int totalInstances) { this.totalInstances = totalInstances; }
        public int getHealthyInstances() { return healthyInstances; }
        public void setHealthyInstances(int healthyInstances) { this.healthyInstances = healthyInstances; }
        public int getUnhealthyInstances() { return unhealthyInstances; }
        public void setUnhealthyInstances(int unhealthyInstances) { this.unhealthyInstances = unhealthyInstances; }
        public double getAvgResponseTime() { return avgResponseTime; }
        public void setAvgResponseTime(double avgResponseTime) { this.avgResponseTime = avgResponseTime; }
        public int getTotalConnections() { return totalConnections; }
        public void setTotalConnections(int totalConnections) { this.totalConnections = totalConnections; }
    }

    public static class LoadBalancingRules {
        private int defaultWeight;
        private int maxConnections;
        private int timeout;

        // Getters and setters
        public int getDefaultWeight() { return defaultWeight; }
        public void setDefaultWeight(int defaultWeight) { this.defaultWeight = defaultWeight; }
        public int getMaxConnections() { return maxConnections; }
        public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }
        public int getTimeout() { return timeout; }
        public void setTimeout(int timeout) { this.timeout = timeout; }
    }

    public static class CircuitBreakerConfig {
        private double failureThreshold;
        private int timeout;
        private int retryAttempts;

        // Getters and setters
        public double getFailureThreshold() { return failureThreshold; }
        public void setFailureThreshold(double failureThreshold) { this.failureThreshold = failureThreshold; }
        public int getTimeout() { return timeout; }
        public void setTimeout(int timeout) { this.timeout = timeout; }
        public int getRetryAttempts() { return retryAttempts; }
        public void setRetryAttempts(int retryAttempts) { this.retryAttempts = retryAttempts; }
    }

    public enum LoadBalancingStrategy {
        ROUND_ROBIN,
        LEAST_CONNECTIONS,
        WEIGHTED_ROUND_ROBIN,
        LEAST_RESPONSE_TIME,
        RANDOM,
        IP_HASH
    }
}

