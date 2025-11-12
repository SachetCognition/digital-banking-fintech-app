package com.yourorg.banking.performance.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class PerformanceMonitoringService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private final Map<String, Timer> timers = new HashMap<>();
    private final Map<String, Counter> counters = new HashMap<>();

    /**
     * Record performance metrics
     */
    public void recordPerformanceMetrics(PerformanceMetrics metrics) {
        // Record to database
        recordMetricsToDatabase(metrics);
        
        // Record to Micrometer
        recordMetricsToMicrometer(metrics);
        
        // Check for performance alerts
        checkPerformanceAlerts(metrics);
    }

    /**
     * Get performance dashboard data
     */
    public PerformanceDashboard getPerformanceDashboard() {
        PerformanceDashboard dashboard = new PerformanceDashboard();
        
        // Get system metrics
        SystemMetrics systemMetrics = getSystemMetrics();
        dashboard.setSystemMetrics(systemMetrics);
        
        // Get application metrics
        ApplicationMetrics applicationMetrics = getApplicationMetrics();
        dashboard.setApplicationMetrics(applicationMetrics);
        
        // Get database metrics
        DatabaseMetrics databaseMetrics = getDatabaseMetrics();
        dashboard.setDatabaseMetrics(databaseMetrics);
        
        // Get cache metrics
        CacheMetrics cacheMetrics = getCacheMetrics();
        dashboard.setCacheMetrics(cacheMetrics);
        
        // Get performance alerts
        List<PerformanceAlert> alerts = getPerformanceAlerts();
        dashboard.setAlerts(alerts);
        
        // Calculate overall health score
        int healthScore = calculateOverallHealthScore(systemMetrics, applicationMetrics, databaseMetrics, cacheMetrics);
        dashboard.setHealthScore(healthScore);
        
        return dashboard;
    }

    /**
     * Get performance trends
     */
    public PerformanceTrends getPerformanceTrends(String timeRange) {
        PerformanceTrends trends = new PerformanceTrends();
        
        // Get response time trends
        List<DataPoint> responseTimeTrends = getResponseTimeTrends(timeRange);
        trends.setResponseTimeTrends(responseTimeTrends);
        
        // Get throughput trends
        List<DataPoint> throughputTrends = getThroughputTrends(timeRange);
        trends.setThroughputTrends(throughputTrends);
        
        // Get error rate trends
        List<DataPoint> errorRateTrends = getErrorRateTrends(timeRange);
        trends.setErrorRateTrends(errorRateTrends);
        
        // Get resource usage trends
        List<DataPoint> cpuTrends = getCpuUsageTrends(timeRange);
        trends.setCpuTrends(cpuTrends);
        
        List<DataPoint> memoryTrends = getMemoryUsageTrends(timeRange);
        trends.setMemoryTrends(memoryTrends);
        
        return trends;
    }

    /**
     * Generate performance report
     */
    public PerformanceReport generatePerformanceReport(String startDate, String endDate) {
        PerformanceReport report = new PerformanceReport();
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setGeneratedAt(LocalDateTime.now());
        
        // Get performance summary
        PerformanceSummary summary = getPerformanceSummary(startDate, endDate);
        report.setSummary(summary);
        
        // Get top slow queries
        List<SlowQuery> slowQueries = getTopSlowQueries(startDate, endDate);
        report.setSlowQueries(slowQueries);
        
        // Get performance recommendations
        List<PerformanceRecommendation> recommendations = getPerformanceRecommendations();
        report.setRecommendations(recommendations);
        
        // Get performance comparisons
        PerformanceComparison comparison = getPerformanceComparison(startDate, endDate);
        report.setComparison(comparison);
        
        return report;
    }

    /**
     * Set up performance monitoring
     */
    public void setupPerformanceMonitoring() {
        // Register custom metrics
        registerCustomMetrics();
        
        // Set up performance alerts
        setupPerformanceAlerts();
        
        // Start background monitoring
        startBackgroundMonitoring();
    }

    // Private helper methods
    private void recordMetricsToDatabase(PerformanceMetrics metrics) {
        String sql = """
            INSERT INTO performance_metrics 
            (service_name, endpoint, method, response_time_ms, status_code, request_size_bytes, response_size_bytes, user_id, session_id, ip_address, user_agent, timestamp)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        jdbcTemplate.update(sql,
            metrics.getServiceName(),
            metrics.getEndpoint(),
            metrics.getMethod(),
            metrics.getResponseTimeMs(),
            metrics.getStatusCode(),
            metrics.getRequestSizeBytes(),
            metrics.getResponseSizeBytes(),
            metrics.getUserId(),
            metrics.getSessionId(),
            metrics.getIpAddress(),
            metrics.getUserAgent(),
            metrics.getTimestamp()
        );
    }

    private void recordMetricsToMicrometer(PerformanceMetrics metrics) {
        // Record response time
        Timer timer = getOrCreateTimer(metrics.getServiceName(), metrics.getEndpoint());
        timer.record(metrics.getResponseTimeMs(), java.util.concurrent.TimeUnit.MILLISECONDS);
        
        // Record request count
        Counter counter = getOrCreateCounter(metrics.getServiceName(), metrics.getEndpoint());
        counter.increment();
        
        // Record error count if applicable
        if (metrics.getStatusCode() >= 400) {
            Counter errorCounter = getOrCreateCounter(metrics.getServiceName() + ".errors", metrics.getEndpoint());
            errorCounter.increment();
        }
    }

    private Timer getOrCreateTimer(String serviceName, String endpoint) {
        String key = serviceName + "." + endpoint;
        return timers.computeIfAbsent(key, k -> 
            Timer.builder("http.server.requests")
                .tag("service", serviceName)
                .tag("endpoint", endpoint)
                .register(meterRegistry)
        );
    }

    private Counter getOrCreateCounter(String serviceName, String endpoint) {
        String key = serviceName + "." + endpoint;
        return counters.computeIfAbsent(key, k -> 
            Counter.builder("http.server.requests")
                .tag("service", serviceName)
                .tag("endpoint", endpoint)
                .register(meterRegistry)
        );
    }

    private void checkPerformanceAlerts(PerformanceMetrics metrics) {
        // Check response time alerts
        if (metrics.getResponseTimeMs() > 2000) {
            createPerformanceAlert("HIGH_RESPONSE_TIME", 
                "Response time exceeded threshold: " + metrics.getResponseTimeMs() + "ms", 
                "HIGH", metrics);
        }
        
        // Check error rate alerts
        if (metrics.getStatusCode() >= 400) {
            createPerformanceAlert("HIGH_ERROR_RATE", 
                "Error response detected: " + metrics.getStatusCode(), 
                "MEDIUM", metrics);
        }
    }

    private void createPerformanceAlert(String alertType, String message, String severity, PerformanceMetrics metrics) {
        String sql = """
            INSERT INTO performance_alerts 
            (alert_type, service_name, metric_name, threshold_value, actual_value, severity, message, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        jdbcTemplate.update(sql,
            alertType,
            metrics.getServiceName(),
            "response_time",
            2000.0,
            (double) metrics.getResponseTimeMs(),
            severity,
            message,
            LocalDateTime.now()
        );
    }

    private SystemMetrics getSystemMetrics() {
        SystemMetrics metrics = new SystemMetrics();
        
        // Get CPU usage
        double cpuUsage = getCpuUsage();
        metrics.setCpuUsage(cpuUsage);
        
        // Get memory usage
        MemoryUsage memoryUsage = getMemoryUsage();
        metrics.setMemoryUsage(memoryUsage);
        
        // Get disk usage
        DiskUsage diskUsage = getDiskUsage();
        metrics.setDiskUsage(diskUsage);
        
        // Get network usage
        NetworkUsage networkUsage = getNetworkUsage();
        metrics.setNetworkUsage(networkUsage);
        
        return metrics;
    }

    private ApplicationMetrics getApplicationMetrics() {
        ApplicationMetrics metrics = new ApplicationMetrics();
        
        // Get response time metrics
        ResponseTimeMetrics responseTime = getResponseTimeMetrics();
        metrics.setResponseTime(responseTime);
        
        // Get throughput metrics
        ThroughputMetrics throughput = getThroughputMetrics();
        metrics.setThroughput(throughput);
        
        // Get error rate metrics
        ErrorRateMetrics errorRate = getErrorRateMetrics();
        metrics.setErrorRate(errorRate);
        
        return metrics;
    }

    private DatabaseMetrics getDatabaseMetrics() {
        DatabaseMetrics metrics = new DatabaseMetrics();
        
        // Get connection pool metrics
        ConnectionPoolMetrics connectionPool = getConnectionPoolMetrics();
        metrics.setConnectionPool(connectionPool);
        
        // Get query performance metrics
        QueryPerformanceMetrics queryPerformance = getQueryPerformanceMetrics();
        metrics.setQueryPerformance(queryPerformance);
        
        return metrics;
    }

    private CacheMetrics getCacheMetrics() {
        CacheMetrics metrics = new CacheMetrics();
        
        // Get cache hit rates
        Map<String, Double> hitRates = getCacheHitRates();
        metrics.setHitRates(hitRates);
        
        // Get cache sizes
        Map<String, Long> cacheSizes = getCacheSizes();
        metrics.setCacheSizes(cacheSizes);
        
        return metrics;
    }

    private List<PerformanceAlert> getPerformanceAlerts() {
        String sql = """
            SELECT * FROM performance_alerts 
            WHERE is_resolved = false 
            ORDER BY created_at DESC 
            LIMIT 10
            """;
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            PerformanceAlert alert = new PerformanceAlert();
            alert.setId(rs.getString("id"));
            alert.setAlertType(rs.getString("alert_type"));
            alert.setServiceName(rs.getString("service_name"));
            alert.setSeverity(rs.getString("severity"));
            alert.setMessage(rs.getString("message"));
            alert.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return alert;
        });
    }

    private int calculateOverallHealthScore(SystemMetrics system, ApplicationMetrics application, DatabaseMetrics database, CacheMetrics cache) {
        int score = 100;
        
        // Deduct points for system issues
        if (system.getCpuUsage() > 80) score -= 20;
        if (system.getMemoryUsage().getUsagePercent() > 80) score -= 20;
        if (system.getDiskUsage().getUsagePercent() > 90) score -= 15;
        
        // Deduct points for application issues
        if (application.getResponseTime().getAverage() > 2000) score -= 25;
        if (application.getErrorRate().getRate() > 0.05) score -= 30;
        
        // Deduct points for database issues
        if (database.getConnectionPool().getUsagePercent() > 80) score -= 15;
        if (database.getQueryPerformance().getAverageExecutionTime() > 1000) score -= 20;
        
        return Math.max(0, score);
    }

    private List<DataPoint> getResponseTimeTrends(String timeRange) {
        String sql = """
            SELECT 
                DATE_TRUNC('hour', timestamp) as hour,
                AVG(response_time_ms) as avg_response_time
            FROM performance_metrics
            WHERE timestamp >= NOW() - INTERVAL ? 
            GROUP BY hour
            ORDER BY hour
            """;
        
        return jdbcTemplate.query(sql, new Object[]{timeRange}, (rs, rowNum) -> {
            DataPoint point = new DataPoint();
            point.setTimestamp(rs.getTimestamp("hour").toLocalDateTime());
            point.setValue(rs.getDouble("avg_response_time"));
            return point;
        });
    }

    private List<DataPoint> getThroughputTrends(String timeRange) {
        String sql = """
            SELECT 
                DATE_TRUNC('hour', timestamp) as hour,
                COUNT(*) as request_count
            FROM performance_metrics
            WHERE timestamp >= NOW() - INTERVAL ? 
            GROUP BY hour
            ORDER BY hour
            """;
        
        return jdbcTemplate.query(sql, new Object[]{timeRange}, (rs, rowNum) -> {
            DataPoint point = new DataPoint();
            point.setTimestamp(rs.getTimestamp("hour").toLocalDateTime());
            point.setValue(rs.getDouble("request_count"));
            return point;
        });
    }

    private List<DataPoint> getErrorRateTrends(String timeRange) {
        String sql = """
            SELECT 
                DATE_TRUNC('hour', timestamp) as hour,
                COUNT(CASE WHEN status_code >= 400 THEN 1 END) * 100.0 / COUNT(*) as error_rate
            FROM performance_metrics
            WHERE timestamp >= NOW() - INTERVAL ? 
            GROUP BY hour
            ORDER BY hour
            """;
        
        return jdbcTemplate.query(sql, new Object[]{timeRange}, (rs, rowNum) -> {
            DataPoint point = new DataPoint();
            point.setTimestamp(rs.getTimestamp("hour").toLocalDateTime());
            point.setValue(rs.getDouble("error_rate"));
            return point;
        });
    }

    private List<DataPoint> getCpuUsageTrends(String timeRange) {
        String sql = """
            SELECT 
                DATE_TRUNC('hour', timestamp) as hour,
                AVG(cpu_usage_percent) as avg_cpu_usage
            FROM system_metrics
            WHERE timestamp >= NOW() - INTERVAL ? 
            GROUP BY hour
            ORDER BY hour
            """;
        
        return jdbcTemplate.query(sql, new Object[]{timeRange}, (rs, rowNum) -> {
            DataPoint point = new DataPoint();
            point.setTimestamp(rs.getTimestamp("hour").toLocalDateTime());
            point.setValue(rs.getDouble("avg_cpu_usage"));
            return point;
        });
    }

    private List<DataPoint> getMemoryUsageTrends(String timeRange) {
        String sql = """
            SELECT 
                DATE_TRUNC('hour', timestamp) as hour,
                AVG(memory_usage_percent) as avg_memory_usage
            FROM system_metrics
            WHERE timestamp >= NOW() - INTERVAL ? 
            GROUP BY hour
            ORDER BY hour
            """;
        
        return jdbcTemplate.query(sql, new Object[]{timeRange}, (rs, rowNum) -> {
            DataPoint point = new DataPoint();
            point.setTimestamp(rs.getTimestamp("hour").toLocalDateTime());
            point.setValue(rs.getDouble("avg_memory_usage"));
            return point;
        });
    }

    private PerformanceSummary getPerformanceSummary(String startDate, String endDate) {
        PerformanceSummary summary = new PerformanceSummary();
        
        String sql = """
            SELECT 
                COUNT(*) as total_requests,
                AVG(response_time_ms) as avg_response_time,
                PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY response_time_ms) as p95_response_time,
                COUNT(CASE WHEN status_code >= 400 THEN 1 END) as error_count
            FROM performance_metrics
            WHERE timestamp BETWEEN ? AND ?
            """;
        
        Map<String, Object> result = jdbcTemplate.queryForMap(sql, startDate, endDate);
        
        summary.setTotalRequests(((Number) result.get("total_requests")).longValue());
        summary.setAverageResponseTime(((Number) result.get("avg_response_time")).doubleValue());
        summary.setP95ResponseTime(((Number) result.get("p95_response_time")).doubleValue());
        summary.setErrorCount(((Number) result.get("error_count")).longValue());
        
        return summary;
    }

    private List<SlowQuery> getTopSlowQueries(String startDate, String endDate) {
        String sql = """
            SELECT 
                query_text,
                AVG(execution_time_ms) as avg_execution_time,
                COUNT(*) as execution_count
            FROM database_metrics
            WHERE timestamp BETWEEN ? AND ?
            GROUP BY query_text
            ORDER BY avg_execution_time DESC
            LIMIT 10
            """;
        
        return jdbcTemplate.query(sql, new Object[]{startDate, endDate}, (rs, rowNum) -> {
            SlowQuery query = new SlowQuery();
            query.setQueryText(rs.getString("query_text"));
            query.setAverageExecutionTime(rs.getDouble("avg_execution_time"));
            query.setExecutionCount(rs.getLong("execution_count"));
            return query;
        });
    }

    private List<PerformanceRecommendation> getPerformanceRecommendations() {
        List<PerformanceRecommendation> recommendations = new ArrayList<>();
        
        // Add performance recommendations based on current metrics
        recommendations.add(new PerformanceRecommendation(
            "Optimize slow queries",
            "Consider adding indexes to frequently queried columns",
            "HIGH"
        ));
        
        recommendations.add(new PerformanceRecommendation(
            "Increase cache TTL",
            "Consider increasing cache TTL for static resources",
            "MEDIUM"
        ));
        
        return recommendations;
    }

    private PerformanceComparison getPerformanceComparison(String startDate, String endDate) {
        PerformanceComparison comparison = new PerformanceComparison();
        
        // Compare with previous period
        String previousStartDate = LocalDateTime.parse(startDate).minusDays(7).toString();
        String previousEndDate = LocalDateTime.parse(endDate).minusDays(7).toString();
        
        PerformanceSummary current = getPerformanceSummary(startDate, endDate);
        PerformanceSummary previous = getPerformanceSummary(previousStartDate, previousEndDate);
        
        comparison.setCurrentPeriod(current);
        comparison.setPreviousPeriod(previous);
        
        // Calculate improvements
        double responseTimeImprovement = (previous.getAverageResponseTime() - current.getAverageResponseTime()) / previous.getAverageResponseTime() * 100;
        comparison.setResponseTimeImprovement(responseTimeImprovement);
        
        return comparison;
    }

    private void registerCustomMetrics() {
        // Register custom gauges
        Gauge.builder("custom.health.score")
            .description("Overall system health score")
            .register(meterRegistry, this, PerformanceMonitoringService::getCurrentHealthScore);
    }

    private void setupPerformanceAlerts() {
        // Set up performance alert thresholds
        // This would typically configure alerting rules
    }

    private void startBackgroundMonitoring() {
        // Start background monitoring tasks
        CompletableFuture.runAsync(() -> {
            while (true) {
                try {
                    // Collect system metrics
                    collectSystemMetrics();
                    
                    // Check performance alerts
                    checkPerformanceAlerts();
                    
                    Thread.sleep(60000); // Check every minute
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, executorService);
    }

    private void collectSystemMetrics() {
        // Collect and store system metrics
        SystemMetrics metrics = getSystemMetrics();
        
        String sql = """
            INSERT INTO system_metrics 
            (service_name, instance_id, cpu_usage_percent, memory_usage_percent, memory_used_mb, memory_total_mb, disk_usage_percent, disk_used_gb, disk_total_gb, network_in_bytes, network_out_bytes, active_threads, gc_collections, gc_time_ms, timestamp)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        jdbcTemplate.update(sql,
            "performance-service",
            "instance-1",
            metrics.getCpuUsage(),
            metrics.getMemoryUsage().getUsagePercent(),
            metrics.getMemoryUsage().getUsedMb(),
            metrics.getMemoryUsage().getTotalMb(),
            metrics.getDiskUsage().getUsagePercent(),
            metrics.getDiskUsage().getUsedGb(),
            metrics.getDiskUsage().getTotalGb(),
            0L, // network_in_bytes
            0L, // network_out_bytes
            10, // active_threads
            0L, // gc_collections
            0L, // gc_time_ms
            LocalDateTime.now()
        );
    }

    private void checkPerformanceAlerts() {
        // Check for performance alerts
        String sql = """
            SELECT 
                service_name,
                AVG(response_time_ms) as avg_response_time,
                COUNT(CASE WHEN status_code >= 400 THEN 1 END) * 100.0 / COUNT(*) as error_rate
            FROM performance_metrics
            WHERE timestamp >= NOW() - INTERVAL '5 minutes'
            GROUP BY service_name
            """;
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        
        for (Map<String, Object> result : results) {
            String serviceName = (String) result.get("service_name");
            double avgResponseTime = ((Number) result.get("avg_response_time")).doubleValue();
            double errorRate = ((Number) result.get("error_rate")).doubleValue();
            
            if (avgResponseTime > 2000) {
                createPerformanceAlert("HIGH_RESPONSE_TIME", 
                    "High response time detected: " + avgResponseTime + "ms", 
                    "HIGH", serviceName);
            }
            
            if (errorRate > 5.0) {
                createPerformanceAlert("HIGH_ERROR_RATE", 
                    "High error rate detected: " + errorRate + "%", 
                    "HIGH", serviceName);
            }
        }
    }

    private void createPerformanceAlert(String alertType, String message, String severity, String serviceName) {
        String sql = """
            INSERT INTO performance_alerts 
            (alert_type, service_name, metric_name, threshold_value, actual_value, severity, message, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        jdbcTemplate.update(sql,
            alertType,
            serviceName,
            "performance",
            0.0,
            0.0,
            severity,
            message,
            LocalDateTime.now()
        );
    }

    private double getCurrentHealthScore() {
        // Calculate current health score
        return 85.0; // Placeholder
    }

    // Placeholder methods for system metrics
    private double getCpuUsage() { return 45.0; }
    private MemoryUsage getMemoryUsage() { return new MemoryUsage(60.0, 1024, 2048); }
    private DiskUsage getDiskUsage() { return new DiskUsage(30.0, 100.0, 500.0); }
    private NetworkUsage getNetworkUsage() { return new NetworkUsage(1000L, 2000L); }
    private ResponseTimeMetrics getResponseTimeMetrics() { return new ResponseTimeMetrics(500.0, 1000.0, 2000.0); }
    private ThroughputMetrics getThroughputMetrics() { return new ThroughputMetrics(1000.0, 2000.0); }
    private ErrorRateMetrics getErrorRateMetrics() { return new ErrorRateMetrics(0.02, 0.05); }
    private ConnectionPoolMetrics getConnectionPoolMetrics() { return new ConnectionPoolMetrics(10, 20, 50.0); }
    private QueryPerformanceMetrics getQueryPerformanceMetrics() { return new QueryPerformanceMetrics(200.0, 500.0); }
    private Map<String, Double> getCacheHitRates() { return Map.of("user-cache", 0.95, "account-cache", 0.90); }
    private Map<String, Long> getCacheSizes() { return Map.of("user-cache", 1000L, "account-cache", 500L); }

    // Data classes
    public static class PerformanceMetrics {
        private String serviceName;
        private String endpoint;
        private String method;
        private int responseTimeMs;
        private int statusCode;
        private Long requestSizeBytes;
        private Long responseSizeBytes;
        private String userId;
        private String sessionId;
        private String ipAddress;
        private String userAgent;
        private LocalDateTime timestamp;

        // Getters and setters
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
        public int getResponseTimeMs() { return responseTimeMs; }
        public void setResponseTimeMs(int responseTimeMs) { this.responseTimeMs = responseTimeMs; }
        public int getStatusCode() { return statusCode; }
        public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
        public Long getRequestSizeBytes() { return requestSizeBytes; }
        public void setRequestSizeBytes(Long requestSizeBytes) { this.requestSizeBytes = requestSizeBytes; }
        public Long getResponseSizeBytes() { return responseSizeBytes; }
        public void setResponseSizeBytes(Long responseSizeBytes) { this.responseSizeBytes = responseSizeBytes; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    // Additional data classes would be defined here...
    public static class PerformanceDashboard {
        private SystemMetrics systemMetrics;
        private ApplicationMetrics applicationMetrics;
        private DatabaseMetrics databaseMetrics;
        private CacheMetrics cacheMetrics;
        private List<PerformanceAlert> alerts;
        private int healthScore;

        // Getters and setters
        public SystemMetrics getSystemMetrics() { return systemMetrics; }
        public void setSystemMetrics(SystemMetrics systemMetrics) { this.systemMetrics = systemMetrics; }
        public ApplicationMetrics getApplicationMetrics() { return applicationMetrics; }
        public void setApplicationMetrics(ApplicationMetrics applicationMetrics) { this.applicationMetrics = applicationMetrics; }
        public DatabaseMetrics getDatabaseMetrics() { return databaseMetrics; }
        public void setDatabaseMetrics(DatabaseMetrics databaseMetrics) { this.databaseMetrics = databaseMetrics; }
        public CacheMetrics getCacheMetrics() { return cacheMetrics; }
        public void setCacheMetrics(CacheMetrics cacheMetrics) { this.cacheMetrics = cacheMetrics; }
        public List<PerformanceAlert> getAlerts() { return alerts; }
        public void setAlerts(List<PerformanceAlert> alerts) { this.alerts = alerts; }
        public int getHealthScore() { return healthScore; }
        public void setHealthScore(int healthScore) { this.healthScore = healthScore; }
    }

    // Additional classes would be defined here...
    public static class SystemMetrics {
        private double cpuUsage;
        private MemoryUsage memoryUsage;
        private DiskUsage diskUsage;
        private NetworkUsage networkUsage;

        // Getters and setters
        public double getCpuUsage() { return cpuUsage; }
        public void setCpuUsage(double cpuUsage) { this.cpuUsage = cpuUsage; }
        public MemoryUsage getMemoryUsage() { return memoryUsage; }
        public void setMemoryUsage(MemoryUsage memoryUsage) { this.memoryUsage = memoryUsage; }
        public DiskUsage getDiskUsage() { return diskUsage; }
        public void setDiskUsage(DiskUsage diskUsage) { this.diskUsage = diskUsage; }
        public NetworkUsage getNetworkUsage() { return networkUsage; }
        public void setNetworkUsage(NetworkUsage networkUsage) { this.networkUsage = networkUsage; }
    }

    public static class MemoryUsage {
        private double usagePercent;
        private long usedMb;
        private long totalMb;

        public MemoryUsage(double usagePercent, long usedMb, long totalMb) {
            this.usagePercent = usagePercent;
            this.usedMb = usedMb;
            this.totalMb = totalMb;
        }

        // Getters and setters
        public double getUsagePercent() { return usagePercent; }
        public void setUsagePercent(double usagePercent) { this.usagePercent = usagePercent; }
        public long getUsedMb() { return usedMb; }
        public void setUsedMb(long usedMb) { this.usedMb = usedMb; }
        public long getTotalMb() { return totalMb; }
        public void setTotalMb(long totalMb) { this.totalMb = totalMb; }
    }

    public static class DiskUsage {
        private double usagePercent;
        private double usedGb;
        private double totalGb;

        public DiskUsage(double usagePercent, double usedGb, double totalGb) {
            this.usagePercent = usagePercent;
            this.usedGb = usedGb;
            this.totalGb = totalGb;
        }

        // Getters and setters
        public double getUsagePercent() { return usagePercent; }
        public void setUsagePercent(double usagePercent) { this.usagePercent = usagePercent; }
        public double getUsedGb() { return usedGb; }
        public void setUsedGb(double usedGb) { this.usedGb = usedGb; }
        public double getTotalGb() { return totalGb; }
        public void setTotalGb(double totalGb) { this.totalGb = totalGb; }
    }

    public static class NetworkUsage {
        private long inBytes;
        private long outBytes;

        public NetworkUsage(long inBytes, long outBytes) {
            this.inBytes = inBytes;
            this.outBytes = outBytes;
        }

        // Getters and setters
        public long getInBytes() { return inBytes; }
        public void setInBytes(long inBytes) { this.inBytes = inBytes; }
        public long getOutBytes() { return outBytes; }
        public void setOutBytes(long outBytes) { this.outBytes = outBytes; }
    }

    // Additional classes would be defined here...
    public static class ApplicationMetrics {
        private ResponseTimeMetrics responseTime;
        private ThroughputMetrics throughput;
        private ErrorRateMetrics errorRate;

        // Getters and setters
        public ResponseTimeMetrics getResponseTime() { return responseTime; }
        public void setResponseTime(ResponseTimeMetrics responseTime) { this.responseTime = responseTime; }
        public ThroughputMetrics getThroughput() { return throughput; }
        public void setThroughput(ThroughputMetrics throughput) { this.throughput = throughput; }
        public ErrorRateMetrics getErrorRate() { return errorRate; }
        public void setErrorRate(ErrorRateMetrics errorRate) { this.errorRate = errorRate; }
    }

    public static class ResponseTimeMetrics {
        private double average;
        private double p95;
        private double p99;

        public ResponseTimeMetrics(double average, double p95, double p99) {
            this.average = average;
            this.p95 = p95;
            this.p99 = p99;
        }

        // Getters and setters
        public double getAverage() { return average; }
        public void setAverage(double average) { this.average = average; }
        public double getP95() { return p95; }
        public void setP95(double p95) { this.p95 = p95; }
        public double getP99() { return p99; }
        public void setP99(double p99) { this.p99 = p99; }
    }

    public static class ThroughputMetrics {
        private double requestsPerSecond;
        private double requestsPerMinute;

        public ThroughputMetrics(double requestsPerSecond, double requestsPerMinute) {
            this.requestsPerSecond = requestsPerSecond;
            this.requestsPerMinute = requestsPerMinute;
        }

        // Getters and setters
        public double getRequestsPerSecond() { return requestsPerSecond; }
        public void setRequestsPerSecond(double requestsPerSecond) { this.requestsPerSecond = requestsPerSecond; }
        public double getRequestsPerMinute() { return requestsPerMinute; }
        public void setRequestsPerMinute(double requestsPerMinute) { this.requestsPerMinute = requestsPerMinute; }
    }

    public static class ErrorRateMetrics {
        private double rate;
        private double threshold;

        public ErrorRateMetrics(double rate, double threshold) {
            this.rate = rate;
            this.threshold = threshold;
        }

        // Getters and setters
        public double getRate() { return rate; }
        public void setRate(double rate) { this.rate = rate; }
        public double getThreshold() { return threshold; }
        public void setThreshold(double threshold) { this.threshold = threshold; }
    }

    public static class DatabaseMetrics {
        private ConnectionPoolMetrics connectionPool;
        private QueryPerformanceMetrics queryPerformance;

        // Getters and setters
        public ConnectionPoolMetrics getConnectionPool() { return connectionPool; }
        public void setConnectionPool(ConnectionPoolMetrics connectionPool) { this.connectionPool = connectionPool; }
        public QueryPerformanceMetrics getQueryPerformance() { return queryPerformance; }
        public void setQueryPerformance(QueryPerformanceMetrics queryPerformance) { this.queryPerformance = queryPerformance; }
    }

    public static class ConnectionPoolMetrics {
        private int activeConnections;
        private int maxConnections;
        private double usagePercent;

        public ConnectionPoolMetrics(int activeConnections, int maxConnections, double usagePercent) {
            this.activeConnections = activeConnections;
            this.maxConnections = maxConnections;
            this.usagePercent = usagePercent;
        }

        // Getters and setters
        public int getActiveConnections() { return activeConnections; }
        public void setActiveConnections(int activeConnections) { this.activeConnections = activeConnections; }
        public int getMaxConnections() { return maxConnections; }
        public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }
        public double getUsagePercent() { return usagePercent; }
        public void setUsagePercent(double usagePercent) { this.usagePercent = usagePercent; }
    }

    public static class QueryPerformanceMetrics {
        private double averageExecutionTime;
        private double maxExecutionTime;

        public QueryPerformanceMetrics(double averageExecutionTime, double maxExecutionTime) {
            this.averageExecutionTime = averageExecutionTime;
            this.maxExecutionTime = maxExecutionTime;
        }

        // Getters and setters
        public double getAverageExecutionTime() { return averageExecutionTime; }
        public void setAverageExecutionTime(double averageExecutionTime) { this.averageExecutionTime = averageExecutionTime; }
        public double getMaxExecutionTime() { return maxExecutionTime; }
        public void setMaxExecutionTime(double maxExecutionTime) { this.maxExecutionTime = maxExecutionTime; }
    }

    public static class CacheMetrics {
        private Map<String, Double> hitRates;
        private Map<String, Long> cacheSizes;

        // Getters and setters
        public Map<String, Double> getHitRates() { return hitRates; }
        public void setHitRates(Map<String, Double> hitRates) { this.hitRates = hitRates; }
        public Map<String, Long> getCacheSizes() { return cacheSizes; }
        public void setCacheSizes(Map<String, Long> cacheSizes) { this.cacheSizes = cacheSizes; }
    }

    public static class PerformanceAlert {
        private String id;
        private String alertType;
        private String serviceName;
        private String severity;
        private String message;
        private LocalDateTime createdAt;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getAlertType() { return alertType; }
        public void setAlertType(String alertType) { this.alertType = alertType; }
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class PerformanceTrends {
        private List<DataPoint> responseTimeTrends;
        private List<DataPoint> throughputTrends;
        private List<DataPoint> errorRateTrends;
        private List<DataPoint> cpuTrends;
        private List<DataPoint> memoryTrends;

        // Getters and setters
        public List<DataPoint> getResponseTimeTrends() { return responseTimeTrends; }
        public void setResponseTimeTrends(List<DataPoint> responseTimeTrends) { this.responseTimeTrends = responseTimeTrends; }
        public List<DataPoint> getThroughputTrends() { return throughputTrends; }
        public void setThroughputTrends(List<DataPoint> throughputTrends) { this.throughputTrends = throughputTrends; }
        public List<DataPoint> getErrorRateTrends() { return errorRateTrends; }
        public void setErrorRateTrends(List<DataPoint> errorRateTrends) { this.errorRateTrends = errorRateTrends; }
        public List<DataPoint> getCpuTrends() { return cpuTrends; }
        public void setCpuTrends(List<DataPoint> cpuTrends) { this.cpuTrends = cpuTrends; }
        public List<DataPoint> getMemoryTrends() { return memoryTrends; }
        public void setMemoryTrends(List<DataPoint> memoryTrends) { this.memoryTrends = memoryTrends; }
    }

    public static class DataPoint {
        private LocalDateTime timestamp;
        private double value;

        // Getters and setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }
    }

    public static class PerformanceReport {
        private String startDate;
        private String endDate;
        private LocalDateTime generatedAt;
        private PerformanceSummary summary;
        private List<SlowQuery> slowQueries;
        private List<PerformanceRecommendation> recommendations;
        private PerformanceComparison comparison;

        // Getters and setters
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
        public PerformanceSummary getSummary() { return summary; }
        public void setSummary(PerformanceSummary summary) { this.summary = summary; }
        public List<SlowQuery> getSlowQueries() { return slowQueries; }
        public void setSlowQueries(List<SlowQuery> slowQueries) { this.slowQueries = slowQueries; }
        public List<PerformanceRecommendation> getRecommendations() { return recommendations; }
        public void setRecommendations(List<PerformanceRecommendation> recommendations) { this.recommendations = recommendations; }
        public PerformanceComparison getComparison() { return comparison; }
        public void setComparison(PerformanceComparison comparison) { this.comparison = comparison; }
    }

    public static class PerformanceSummary {
        private long totalRequests;
        private double averageResponseTime;
        private double p95ResponseTime;
        private long errorCount;

        // Getters and setters
        public long getTotalRequests() { return totalRequests; }
        public void setTotalRequests(long totalRequests) { this.totalRequests = totalRequests; }
        public double getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(double averageResponseTime) { this.averageResponseTime = averageResponseTime; }
        public double getP95ResponseTime() { return p95ResponseTime; }
        public void setP95ResponseTime(double p95ResponseTime) { this.p95ResponseTime = p95ResponseTime; }
        public long getErrorCount() { return errorCount; }
        public void setErrorCount(long errorCount) { this.errorCount = errorCount; }
    }

    public static class SlowQuery {
        private String queryText;
        private double averageExecutionTime;
        private long executionCount;

        // Getters and setters
        public String getQueryText() { return queryText; }
        public void setQueryText(String queryText) { this.queryText = queryText; }
        public double getAverageExecutionTime() { return averageExecutionTime; }
        public void setAverageExecutionTime(double averageExecutionTime) { this.averageExecutionTime = averageExecutionTime; }
        public long getExecutionCount() { return executionCount; }
        public void setExecutionCount(long executionCount) { this.executionCount = executionCount; }
    }

    public static class PerformanceRecommendation {
        private String title;
        private String description;
        private String priority;

        public PerformanceRecommendation(String title, String description, String priority) {
            this.title = title;
            this.description = description;
            this.priority = priority;
        }

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
    }

    public static class PerformanceComparison {
        private PerformanceSummary currentPeriod;
        private PerformanceSummary previousPeriod;
        private double responseTimeImprovement;

        // Getters and setters
        public PerformanceSummary getCurrentPeriod() { return currentPeriod; }
        public void setCurrentPeriod(PerformanceSummary currentPeriod) { this.currentPeriod = currentPeriod; }
        public PerformanceSummary getPreviousPeriod() { return previousPeriod; }
        public void setPreviousPeriod(PerformanceSummary previousPeriod) { this.previousPeriod = previousPeriod; }
        public double getResponseTimeImprovement() { return responseTimeImprovement; }
        public void setResponseTimeImprovement(double responseTimeImprovement) { this.responseTimeImprovement = responseTimeImprovement; }
    }
}

