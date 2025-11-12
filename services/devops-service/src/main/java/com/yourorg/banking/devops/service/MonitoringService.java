package com.yourorg.banking.devops.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class MonitoringService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private RestTemplate restTemplate;
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * Create monitoring alert
     */
    public MonitoringAlert createAlert(String alertName, String serviceName, String severity, String description, Map<String, String> labels) {
        MonitoringAlert alert = new MonitoringAlert();
        alert.setAlertName(alertName);
        alert.setServiceName(serviceName);
        alert.setSeverity(severity);
        alert.setStatus("firing");
        alert.setDescription(description);
        alert.setSummary(description);
        alert.setLabels(labels);
        alert.setStartedAt(LocalDateTime.now());

        try {
            // Record alert
            recordAlert(alert);

            // Send notifications
            sendAlertNotifications(alert);

            return alert;

        } catch (Exception e) {
            alert.setStatus("error");
            alert.setErrorMessage(e.getMessage());
            recordAlert(alert);
            return alert;
        }
    }

    /**
     * Resolve alert
     */
    public void resolveAlert(String alertId) {
        String sql = "UPDATE monitoring_alerts SET status = 'resolved', resolved_at = NOW() WHERE id = ?";
        jdbcTemplate.update(sql, alertId);
    }

    /**
     * Get active alerts
     */
    public List<MonitoringAlert> getActiveAlerts(String serviceName) {
        String sql = """
            SELECT * FROM monitoring_alerts 
            WHERE service_name = ? AND status = 'firing'
            ORDER BY started_at DESC
            """;
        
        return jdbcTemplate.query(sql, new Object[]{serviceName}, (rs, rowNum) -> {
            MonitoringAlert alert = new MonitoringAlert();
            alert.setId(rs.getString("id"));
            alert.setAlertName(rs.getString("alert_name"));
            alert.setServiceName(rs.getString("service_name"));
            alert.setSeverity(rs.getString("severity"));
            alert.setStatus(rs.getString("status"));
            alert.setDescription(rs.getString("description"));
            alert.setSummary(rs.getString("summary"));
            alert.setStartedAt(rs.getTimestamp("started_at").toLocalDateTime());
            if (rs.getTimestamp("resolved_at") != null) {
                alert.setResolvedAt(rs.getTimestamp("resolved_at").toLocalDateTime());
            }
            return alert;
        });
    }

    /**
     * Get alert metrics
     */
    public AlertMetrics getAlertMetrics(String serviceName) {
        AlertMetrics metrics = new AlertMetrics();
        
        String sql = """
            SELECT 
                COUNT(*) as total_alerts,
                COUNT(CASE WHEN status = 'firing' THEN 1 END) as active_alerts,
                COUNT(CASE WHEN status = 'resolved' THEN 1 END) as resolved_alerts,
                COUNT(CASE WHEN severity = 'critical' THEN 1 END) as critical_alerts,
                COUNT(CASE WHEN severity = 'warning' THEN 1 END) as warning_alerts,
                AVG(duration_seconds) as avg_duration_seconds
            FROM monitoring_alerts 
            WHERE service_name = ? AND started_at >= NOW() - INTERVAL '24 hours'
            """;
        
        Map<String, Object> result = jdbcTemplate.queryForMap(sql, serviceName);
        
        metrics.setTotalAlerts(((Number) result.get("total_alerts")).longValue());
        metrics.setActiveAlerts(((Number) result.get("active_alerts")).longValue());
        metrics.setResolvedAlerts(((Number) result.get("resolved_alerts")).longValue());
        metrics.setCriticalAlerts(((Number) result.get("critical_alerts")).longValue());
        metrics.setWarningAlerts(((Number) result.get("warning_alerts")).longValue());
        metrics.setAvgDurationSeconds(((Number) result.get("avg_duration_seconds")).doubleValue());
        
        return metrics;
    }

    /**
     * Get service health status
     */
    public ServiceHealth getServiceHealth(String serviceName) {
        ServiceHealth health = new ServiceHealth();
        health.setServiceName(serviceName);
        health.setTimestamp(LocalDateTime.now());

        try {
            // Check service endpoint
            String healthUrl = "http://" + serviceName + ":8080/actuator/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(healthUrl, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> healthData = response.getBody();
                health.setStatus("UP");
                health.setDetails(healthData);
            } else {
                health.setStatus("DOWN");
                health.setErrorMessage("Health check failed with status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            health.setStatus("DOWN");
            health.setErrorMessage("Health check failed: " + e.getMessage());
        }

        return health;
    }

    /**
     * Get system metrics
     */
    public SystemMetrics getSystemMetrics() {
        SystemMetrics metrics = new SystemMetrics();
        metrics.setTimestamp(LocalDateTime.now());

        try {
            // Get CPU usage
            metrics.setCpuUsage(getCpuUsage());
            
            // Get memory usage
            metrics.setMemoryUsage(getMemoryUsage());
            
            // Get disk usage
            metrics.setDiskUsage(getDiskUsage());
            
            // Get network usage
            metrics.setNetworkUsage(getNetworkUsage());

        } catch (Exception e) {
            metrics.setErrorMessage("Failed to collect system metrics: " + e.getMessage());
        }

        return metrics;
    }

    /**
     * Get application metrics
     */
    public ApplicationMetrics getApplicationMetrics(String serviceName) {
        ApplicationMetrics metrics = new ApplicationMetrics();
        metrics.setServiceName(serviceName);
        metrics.setTimestamp(LocalDateTime.now());

        try {
            // Get Prometheus metrics
            String metricsUrl = "http://" + serviceName + ":8080/actuator/prometheus";
            ResponseEntity<String> response = restTemplate.getForEntity(metricsUrl, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                String metricsData = response.getBody();
                parsePrometheusMetrics(metrics, metricsData);
            }

        } catch (Exception e) {
            metrics.setErrorMessage("Failed to collect application metrics: " + e.getMessage());
        }

        return metrics;
    }

    /**
     * Create dashboard
     */
    public Dashboard createDashboard(String dashboardName, String description, List<DashboardPanel> panels) {
        Dashboard dashboard = new Dashboard();
        dashboard.setDashboardName(dashboardName);
        dashboard.setDescription(description);
        dashboard.setPanels(panels);
        dashboard.setCreatedAt(LocalDateTime.now());

        // In a real implementation, this would create a Grafana dashboard
        // For now, we'll just record it in the database
        recordDashboard(dashboard);

        return dashboard;
    }

    /**
     * Get dashboard
     */
    public Dashboard getDashboard(String dashboardId) {
        String sql = "SELECT * FROM dashboards WHERE id = ?";
        
        return jdbcTemplate.queryForObject(sql, new Object[]{dashboardId}, (rs, rowNum) -> {
            Dashboard dashboard = new Dashboard();
            dashboard.setId(rs.getString("id"));
            dashboard.setDashboardName(rs.getString("dashboard_name"));
            dashboard.setDescription(rs.getString("description"));
            dashboard.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return dashboard;
        });
    }

    /**
     * Send alert notifications
     */
    private void sendAlertNotifications(MonitoringAlert alert) {
        // Send email notification
        sendEmailNotification(alert);
        
        // Send Slack notification
        sendSlackNotification(alert);
        
        // Send webhook notification
        sendWebhookNotification(alert);
    }

    private void sendEmailNotification(MonitoringAlert alert) {
        // In a real implementation, this would send an email
        System.out.println("Email notification sent for alert: " + alert.getAlertName());
    }

    private void sendSlackNotification(MonitoringAlert alert) {
        // In a real implementation, this would send a Slack message
        System.out.println("Slack notification sent for alert: " + alert.getAlertName());
    }

    private void sendWebhookNotification(MonitoringAlert alert) {
        // In a real implementation, this would send a webhook
        System.out.println("Webhook notification sent for alert: " + alert.getAlertName());
    }

    private double getCpuUsage() {
        // In a real implementation, this would query the system for CPU usage
        return Math.random() * 100;
    }

    private double getMemoryUsage() {
        // In a real implementation, this would query the system for memory usage
        return Math.random() * 100;
    }

    private double getDiskUsage() {
        // In a real implementation, this would query the system for disk usage
        return Math.random() * 100;
    }

    private double getNetworkUsage() {
        // In a real implementation, this would query the system for network usage
        return Math.random() * 100;
    }

    private void parsePrometheusMetrics(ApplicationMetrics metrics, String metricsData) {
        // In a real implementation, this would parse Prometheus metrics
        // For now, we'll set some sample values
        metrics.setRequestRate(100.0);
        metrics.setResponseTime(200.0);
        metrics.setErrorRate(0.5);
        metrics.setActiveConnections(50);
    }

    private void recordAlert(MonitoringAlert alert) {
        String sql = """
            INSERT INTO monitoring_alerts 
            (id, alert_name, service_name, severity, status, description, summary, labels, 
             started_at, resolved_at, notification_sent, notification_channels)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        jdbcTemplate.update(sql,
            alert.getId(),
            alert.getAlertName(),
            alert.getServiceName(),
            alert.getSeverity(),
            alert.getStatus(),
            alert.getDescription(),
            alert.getSummary(),
            alert.getLabels() != null ? alert.getLabels().toString() : null,
            alert.getStartedAt(),
            alert.getResolvedAt(),
            alert.isNotificationSent(),
            alert.getNotificationChannels() != null ? alert.getNotificationChannels().toString() : null
        );
    }

    private void recordDashboard(Dashboard dashboard) {
        String sql = """
            INSERT INTO dashboards 
            (id, dashboard_name, description, panels, created_at)
            VALUES (?, ?, ?, ?, ?)
            """;
        
        jdbcTemplate.update(sql,
            dashboard.getId(),
            dashboard.getDashboardName(),
            dashboard.getDescription(),
            dashboard.getPanels() != null ? dashboard.getPanels().toString() : null,
            dashboard.getCreatedAt()
        );
    }

    // Data classes
    public static class MonitoringAlert {
        private String id = UUID.randomUUID().toString();
        private String alertName;
        private String serviceName;
        private String severity;
        private String status;
        private String description;
        private String summary;
        private Map<String, String> labels;
        private LocalDateTime startedAt;
        private LocalDateTime resolvedAt;
        private boolean notificationSent = false;
        private List<String> notificationChannels;
        private String errorMessage;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getAlertName() { return alertName; }
        public void setAlertName(String alertName) { this.alertName = alertName; }
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        public Map<String, String> getLabels() { return labels; }
        public void setLabels(Map<String, String> labels) { this.labels = labels; }
        public LocalDateTime getStartedAt() { return startedAt; }
        public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
        public LocalDateTime getResolvedAt() { return resolvedAt; }
        public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
        public boolean isNotificationSent() { return notificationSent; }
        public void setNotificationSent(boolean notificationSent) { this.notificationSent = notificationSent; }
        public List<String> getNotificationChannels() { return notificationChannels; }
        public void setNotificationChannels(List<String> notificationChannels) { this.notificationChannels = notificationChannels; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class AlertMetrics {
        private Long totalAlerts;
        private Long activeAlerts;
        private Long resolvedAlerts;
        private Long criticalAlerts;
        private Long warningAlerts;
        private Double avgDurationSeconds;

        // Getters and setters
        public Long getTotalAlerts() { return totalAlerts; }
        public void setTotalAlerts(Long totalAlerts) { this.totalAlerts = totalAlerts; }
        public Long getActiveAlerts() { return activeAlerts; }
        public void setActiveAlerts(Long activeAlerts) { this.activeAlerts = activeAlerts; }
        public Long getResolvedAlerts() { return resolvedAlerts; }
        public void setResolvedAlerts(Long resolvedAlerts) { this.resolvedAlerts = resolvedAlerts; }
        public Long getCriticalAlerts() { return criticalAlerts; }
        public void setCriticalAlerts(Long criticalAlerts) { this.criticalAlerts = criticalAlerts; }
        public Long getWarningAlerts() { return warningAlerts; }
        public void setWarningAlerts(Long warningAlerts) { this.warningAlerts = warningAlerts; }
        public Double getAvgDurationSeconds() { return avgDurationSeconds; }
        public void setAvgDurationSeconds(Double avgDurationSeconds) { this.avgDurationSeconds = avgDurationSeconds; }
    }

    public static class ServiceHealth {
        private String serviceName;
        private String status;
        private LocalDateTime timestamp;
        private Map<String, Object> details;
        private String errorMessage;

        // Getters and setters
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public Map<String, Object> getDetails() { return details; }
        public void setDetails(Map<String, Object> details) { this.details = details; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class SystemMetrics {
        private LocalDateTime timestamp;
        private double cpuUsage;
        private double memoryUsage;
        private double diskUsage;
        private double networkUsage;
        private String errorMessage;

        // Getters and setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public double getCpuUsage() { return cpuUsage; }
        public void setCpuUsage(double cpuUsage) { this.cpuUsage = cpuUsage; }
        public double getMemoryUsage() { return memoryUsage; }
        public void setMemoryUsage(double memoryUsage) { this.memoryUsage = memoryUsage; }
        public double getDiskUsage() { return diskUsage; }
        public void setDiskUsage(double diskUsage) { this.diskUsage = diskUsage; }
        public double getNetworkUsage() { return networkUsage; }
        public void setNetworkUsage(double networkUsage) { this.networkUsage = networkUsage; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class ApplicationMetrics {
        private String serviceName;
        private LocalDateTime timestamp;
        private double requestRate;
        private double responseTime;
        private double errorRate;
        private int activeConnections;
        private String errorMessage;

        // Getters and setters
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public double getRequestRate() { return requestRate; }
        public void setRequestRate(double requestRate) { this.requestRate = requestRate; }
        public double getResponseTime() { return responseTime; }
        public void setResponseTime(double responseTime) { this.responseTime = responseTime; }
        public double getErrorRate() { return errorRate; }
        public void setErrorRate(double errorRate) { this.errorRate = errorRate; }
        public int getActiveConnections() { return activeConnections; }
        public void setActiveConnections(int activeConnections) { this.activeConnections = activeConnections; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class Dashboard {
        private String id = UUID.randomUUID().toString();
        private String dashboardName;
        private String description;
        private List<DashboardPanel> panels;
        private LocalDateTime createdAt;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getDashboardName() { return dashboardName; }
        public void setDashboardName(String dashboardName) { this.dashboardName = dashboardName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public List<DashboardPanel> getPanels() { return panels; }
        public void setPanels(List<DashboardPanel> panels) { this.panels = panels; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class DashboardPanel {
        private String title;
        private String type;
        private String query;
        private Map<String, Object> options;

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
        public Map<String, Object> getOptions() { return options; }
        public void setOptions(Map<String, Object> options) { this.options = options; }
    }
}

