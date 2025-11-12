package com.yourorg.banking.admin.service;

import com.yourorg.banking.admin.model.*;
import com.yourorg.banking.admin.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardService {
    
    private final SystemHealthMetricsRepository healthMetricsRepository;
    private final SystemAlertsRepository alertsRepository;
    private final AdminUserRepository adminUserRepository;
    private final SystemEventsRepository eventsRepository;
    private final BackupRecordsRepository backupRecordsRepository;
    private final PerformanceMetricsRepository performanceMetricsRepository;
    
    @Cacheable(value = "dashboard-overview", unless = "#result == null")
    public Map<String, Object> getDashboardOverview() {
        log.info("Generating dashboard overview");
        
        Map<String, Object> overview = new HashMap<>();
        
        // System Health Summary
        overview.put("systemHealth", getSystemHealthSummary());
        
        // Active Alerts
        overview.put("activeAlerts", getActiveAlertsCount());
        
        // User Statistics
        overview.put("userStats", getUserStatistics());
        
        // Recent Events
        overview.put("recentEvents", getRecentEvents());
        
        // Backup Status
        overview.put("backupStatus", getBackupStatus());
        
        // Performance Metrics
        overview.put("performanceMetrics", getPerformanceMetrics());
        
        return overview;
    }
    
    @Cacheable(value = "system-health", unless = "#result == null")
    public Map<String, Object> getSystemHealthSummary() {
        Map<String, Object> health = new HashMap<>();
        
        // Get latest health metrics for each service
        List<SystemHealthMetrics> latestMetrics = healthMetricsRepository.findLatestMetricsByService();
        
        int healthyServices = 0;
        int warningServices = 0;
        int criticalServices = 0;
        
        for (SystemHealthMetrics metric : latestMetrics) {
            switch (metric.getStatus()) {
                case HEALTHY:
                    healthyServices++;
                    break;
                case WARNING:
                    warningServices++;
                    break;
                case CRITICAL:
                    criticalServices++;
                    break;
            }
        }
        
        health.put("totalServices", latestMetrics.size());
        health.put("healthyServices", healthyServices);
        health.put("warningServices", warningServices);
        health.put("criticalServices", criticalServices);
        health.put("overallStatus", determineOverallStatus(healthyServices, warningServices, criticalServices));
        
        return health;
    }
    
    public long getActiveAlertsCount() {
        return alertsRepository.countByStatus(AlertStatus.OPEN);
    }
    
    @Cacheable(value = "user-statistics", unless = "#result == null")
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalUsers = adminUserRepository.count();
        long activeUsers = adminUserRepository.countByIsActiveTrue();
        long lockedUsers = adminUserRepository.countByLockedUntilIsNotNull();
        
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("lockedUsers", lockedUsers);
        stats.put("inactiveUsers", totalUsers - activeUsers);
        
        return stats;
    }
    
    public List<SystemEvents> getRecentEvents() {
        return eventsRepository.findTop10ByOrderByCreatedAtDesc();
    }
    
    @Cacheable(value = "backup-status", unless = "#result == null")
    public Map<String, Object> getBackupStatus() {
        Map<String, Object> status = new HashMap<>();
        
        long totalBackups = backupRecordsRepository.count();
        long successfulBackups = backupRecordsRepository.countByStatus(BackupStatus.COMPLETED);
        long failedBackups = backupRecordsRepository.countByStatus(BackupStatus.FAILED);
        long inProgressBackups = backupRecordsRepository.countByStatus(BackupStatus.IN_PROGRESS);
        
        status.put("totalBackups", totalBackups);
        status.put("successfulBackups", successfulBackups);
        status.put("failedBackups", failedBackups);
        status.put("inProgressBackups", inProgressBackups);
        status.put("successRate", totalBackups > 0 ? (double) successfulBackups / totalBackups * 100 : 0);
        
        return status;
    }
    
    @Cacheable(value = "performance-metrics", unless = "#result == null")
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Get average response time for the last hour
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<PerformanceMetrics> recentMetrics = performanceMetricsRepository.findByTimestampAfter(oneHourAgo);
        
        if (!recentMetrics.isEmpty()) {
            double avgResponseTime = recentMetrics.stream()
                .mapToInt(PerformanceMetrics::getResponseTimeMs)
                .average()
                .orElse(0.0);
            
            double avgCpuUsage = recentMetrics.stream()
                .filter(m -> m.getCpuUsagePercent() != null)
                .mapToDouble(m -> m.getCpuUsagePercent().doubleValue())
                .average()
                .orElse(0.0);
            
            double avgMemoryUsage = recentMetrics.stream()
                .filter(m -> m.getMemoryUsageMb() != null)
                .mapToDouble(m -> m.getMemoryUsageMb().doubleValue())
                .average()
                .orElse(0.0);
            
            metrics.put("avgResponseTime", avgResponseTime);
            metrics.put("avgCpuUsage", avgCpuUsage);
            metrics.put("avgMemoryUsage", avgMemoryUsage);
            metrics.put("totalRequests", recentMetrics.size());
        } else {
            metrics.put("avgResponseTime", 0.0);
            metrics.put("avgCpuUsage", 0.0);
            metrics.put("avgMemoryUsage", 0.0);
            metrics.put("totalRequests", 0);
        }
        
        return metrics;
    }
    
    private String determineOverallStatus(int healthy, int warning, int critical) {
        if (critical > 0) {
            return "CRITICAL";
        } else if (warning > 0) {
            return "WARNING";
        } else {
            return "HEALTHY";
        }
    }
}

