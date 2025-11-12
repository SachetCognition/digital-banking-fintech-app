package com.yourorg.banking.performance.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class DatabaseOptimizationService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private DataSource dataSource;
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * Analyze and optimize slow queries
     */
    public List<QueryOptimization> analyzeSlowQueries() {
        List<QueryOptimization> optimizations = new ArrayList<>();
        
        // Get slow queries from performance metrics
        String sql = """
            SELECT 
                query_text,
                AVG(execution_time_ms) as avg_execution_time,
                COUNT(*) as execution_count,
                MAX(execution_time_ms) as max_execution_time,
                MIN(execution_time_ms) as min_execution_time
            FROM database_metrics 
            WHERE is_slow_query = true 
            AND timestamp >= NOW() - INTERVAL '24 hours'
            GROUP BY query_text
            ORDER BY avg_execution_time DESC
            LIMIT 20
            """;
        
        List<Map<String, Object>> slowQueries = jdbcTemplate.queryForList(sql);
        
        for (Map<String, Object> query : slowQueries) {
            String queryText = (String) query.get("query_text");
            QueryOptimization optimization = analyzeQuery(queryText);
            if (optimization != null) {
                optimizations.add(optimization);
            }
        }
        
        return optimizations;
    }

    /**
     * Analyze a specific query for optimization opportunities
     */
    public QueryOptimization analyzeQuery(String queryText) {
        QueryOptimization optimization = new QueryOptimization();
        optimization.setQueryText(queryText);
        optimization.setAnalysisTimestamp(new Date());
        
        try {
            // Analyze query execution plan
            String explainQuery = "EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON) " + queryText;
            List<Map<String, Object>> explainResult = jdbcTemplate.queryForList(explainQuery);
            
            if (!explainResult.isEmpty()) {
                Map<String, Object> plan = (Map<String, Object>) explainResult.get(0).get("QUERY PLAN");
                optimization.setExecutionPlan(plan);
                
                // Extract optimization suggestions
                List<String> suggestions = extractOptimizationSuggestions(plan);
                optimization.setSuggestions(suggestions);
                
                // Calculate optimization score
                int score = calculateOptimizationScore(plan);
                optimization.setOptimizationScore(score);
            }
            
            // Check for missing indexes
            List<String> missingIndexes = findMissingIndexes(queryText);
            optimization.setMissingIndexes(missingIndexes);
            
            // Check for table statistics
            List<String> tableStats = checkTableStatistics(queryText);
            optimization.setTableStatistics(tableStats);
            
        } catch (Exception e) {
            optimization.setError("Error analyzing query: " + e.getMessage());
        }
        
        return optimization;
    }

    /**
     * Create recommended indexes
     */
    public void createRecommendedIndexes() {
        List<IndexRecommendation> recommendations = getIndexRecommendations();
        
        for (IndexRecommendation recommendation : recommendations) {
            try {
                jdbcTemplate.execute(recommendation.getCreateStatement());
                recommendation.setStatus("CREATED");
            } catch (Exception e) {
                recommendation.setStatus("FAILED: " + e.getMessage());
            }
        }
    }

    /**
     * Get index recommendations based on query patterns
     */
    public List<IndexRecommendation> getIndexRecommendations() {
        List<IndexRecommendation> recommendations = new ArrayList<>();
        
        // Analyze query patterns to suggest indexes
        String sql = """
            SELECT 
                table_name,
                column_name,
                COUNT(*) as usage_count,
                AVG(execution_time_ms) as avg_execution_time
            FROM database_metrics dm
            JOIN (
                SELECT DISTINCT 
                    table_name,
                    column_name,
                    query_text
                FROM database_metrics
                WHERE query_text LIKE '%WHERE%'
                AND timestamp >= NOW() - INTERVAL '7 days'
            ) qp ON dm.query_text = qp.query_text
            WHERE dm.timestamp >= NOW() - INTERVAL '7 days'
            GROUP BY table_name, column_name
            HAVING COUNT(*) > 10
            ORDER BY usage_count DESC, avg_execution_time DESC
            """;
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        
        for (Map<String, Object> result : results) {
            String tableName = (String) result.get("table_name");
            String columnName = (String) result.get("column_name");
            Long usageCount = (Long) result.get("usage_count");
            Double avgExecutionTime = (Double) result.get("avg_execution_time");
            
            IndexRecommendation recommendation = new IndexRecommendation();
            recommendation.setTableName(tableName);
            recommendation.setColumnName(columnName);
            recommendation.setUsageCount(usageCount);
            recommendation.setAvgExecutionTime(avgExecutionTime);
            recommendation.setCreateStatement(
                String.format("CREATE INDEX idx_%s_%s ON %s (%s)", 
                    tableName, columnName, tableName, columnName)
            );
            recommendation.setPriority(calculateIndexPriority(usageCount, avgExecutionTime));
            
            recommendations.add(recommendation);
        }
        
        return recommendations;
    }

    /**
     * Optimize database connection pool
     */
    public ConnectionPoolOptimization optimizeConnectionPool() {
        ConnectionPoolOptimization optimization = new ConnectionPoolOptimization();
        
        try (Connection connection = dataSource.getConnection()) {
            // Get current connection pool metrics
            Map<String, Object> poolMetrics = getConnectionPoolMetrics();
            optimization.setCurrentMetrics(poolMetrics);
            
            // Analyze connection usage patterns
            List<Map<String, Object>> usagePatterns = analyzeConnectionUsage();
            optimization.setUsagePatterns(usagePatterns);
            
            // Calculate optimal pool size
            int optimalPoolSize = calculateOptimalPoolSize(usagePatterns);
            optimization.setRecommendedPoolSize(optimalPoolSize);
            
            // Calculate optimal timeout settings
            Map<String, Integer> timeoutSettings = calculateOptimalTimeouts(usagePatterns);
            optimization.setRecommendedTimeouts(timeoutSettings);
            
            // Generate optimization recommendations
            List<String> recommendations = generatePoolOptimizationRecommendations(poolMetrics, usagePatterns);
            optimization.setRecommendations(recommendations);
            
        } catch (SQLException e) {
            optimization.setError("Error analyzing connection pool: " + e.getMessage());
        }
        
        return optimization;
    }

    /**
     * Perform database maintenance tasks
     */
    @Transactional
    public void performMaintenanceTasks() {
        // Update table statistics
        updateTableStatistics();
        
        // Vacuum and analyze tables
        vacuumAndAnalyzeTables();
        
        // Clean up old performance metrics
        cleanupOldMetrics();
        
        // Reindex frequently used tables
        reindexFrequentlyUsedTables();
    }

    /**
     * Monitor database performance in real-time
     */
    public DatabasePerformanceMetrics getDatabasePerformanceMetrics() {
        DatabasePerformanceMetrics metrics = new DatabasePerformanceMetrics();
        
        try {
            // Get connection pool metrics
            Map<String, Object> poolMetrics = getConnectionPoolMetrics();
            metrics.setConnectionPoolMetrics(poolMetrics);
            
            // Get query performance metrics
            List<Map<String, Object>> queryMetrics = getQueryPerformanceMetrics();
            metrics.setQueryMetrics(queryMetrics);
            
            // Get table size metrics
            List<Map<String, Object>> tableMetrics = getTableSizeMetrics();
            metrics.setTableMetrics(tableMetrics);
            
            // Get index usage metrics
            List<Map<String, Object>> indexMetrics = getIndexUsageMetrics();
            metrics.setIndexMetrics(indexMetrics);
            
            // Calculate overall health score
            int healthScore = calculateDatabaseHealthScore(poolMetrics, queryMetrics, tableMetrics);
            metrics.setHealthScore(healthScore);
            
        } catch (Exception e) {
            metrics.setError("Error collecting database metrics: " + e.getMessage());
        }
        
        return metrics;
    }

    // Private helper methods
    private List<String> extractOptimizationSuggestions(Map<String, Object> plan) {
        List<String> suggestions = new ArrayList<>();
        
        // Analyze execution plan for optimization opportunities
        if (plan.containsKey("Plan")) {
            Map<String, Object> planNode = (Map<String, Object>) plan.get("Plan");
            analyzePlanNode(planNode, suggestions);
        }
        
        return suggestions;
    }

    private void analyzePlanNode(Map<String, Object> planNode, List<String> suggestions) {
        String nodeType = (String) planNode.get("Node Type");
        
        if ("Seq Scan".equals(nodeType)) {
            suggestions.add("Consider adding an index for sequential scan on " + planNode.get("Relation Name"));
        } else if ("Nested Loop".equals(nodeType)) {
            suggestions.add("Consider optimizing nested loop join");
        } else if ("Hash Join".equals(nodeType)) {
            suggestions.add("Consider optimizing hash join");
        }
        
        // Check for high cost operations
        if (planNode.containsKey("Total Cost")) {
            Double totalCost = (Double) planNode.get("Total Cost");
            if (totalCost > 1000) {
                suggestions.add("High cost operation detected: " + totalCost);
            }
        }
        
        // Recursively analyze child nodes
        if (planNode.containsKey("Plans")) {
            List<Map<String, Object>> childPlans = (List<Map<String, Object>>) planNode.get("Plans");
            for (Map<String, Object> childPlan : childPlans) {
                analyzePlanNode(childPlan, suggestions);
            }
        }
    }

    private int calculateOptimizationScore(Map<String, Object> plan) {
        int score = 100; // Start with perfect score
        
        // Deduct points for inefficient operations
        if (plan.containsKey("Plan")) {
            Map<String, Object> planNode = (Map<String, Object>) plan.get("Plan");
            score = calculateNodeScore(planNode, score);
        }
        
        return Math.max(0, score);
    }

    private int calculateNodeScore(Map<String, Object> planNode, int currentScore) {
        String nodeType = (String) planNode.get("Node Type");
        
        // Deduct points for inefficient operations
        if ("Seq Scan".equals(nodeType)) {
            currentScore -= 20;
        } else if ("Nested Loop".equals(nodeType)) {
            currentScore -= 10;
        }
        
        // Check cost
        if (planNode.containsKey("Total Cost")) {
            Double totalCost = (Double) planNode.get("Total Cost");
            if (totalCost > 1000) {
                currentScore -= 15;
            }
        }
        
        // Recursively check child nodes
        if (planNode.containsKey("Plans")) {
            List<Map<String, Object>> childPlans = (List<Map<String, Object>>) planNode.get("Plans");
            for (Map<String, Object> childPlan : childPlans) {
                currentScore = calculateNodeScore(childPlan, currentScore);
            }
        }
        
        return currentScore;
    }

    private List<String> findMissingIndexes(String queryText) {
        List<String> missingIndexes = new ArrayList<>();
        
        // Simple heuristic to find potential missing indexes
        if (queryText.toLowerCase().contains("where")) {
            // Extract WHERE clause columns
            String[] whereClause = queryText.toLowerCase().split("where")[1].split("order by|group by|limit")[0];
            String[] conditions = whereClause.split("and|or");
            
            for (String condition : conditions) {
                if (condition.contains("=") || condition.contains(">") || condition.contains("<")) {
                    String[] parts = condition.split("=");
                    if (parts.length > 0) {
                        String column = parts[0].trim();
                        missingIndexes.add("Consider index on: " + column);
                    }
                }
            }
        }
        
        return missingIndexes;
    }

    private List<String> checkTableStatistics(String queryText) {
        List<String> stats = new ArrayList<>();
        
        // Check if tables have recent statistics
        String[] tables = extractTableNames(queryText);
        for (String table : tables) {
            String sql = "SELECT schemaname, tablename, last_analyze FROM pg_stat_user_tables WHERE tablename = ?";
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, table);
            
            if (result.isEmpty()) {
                stats.add("Table " + table + " not found in statistics");
            } else {
                Map<String, Object> tableStats = result.get(0);
                if (tableStats.get("last_analyze") == null) {
                    stats.add("Table " + table + " needs statistics update");
                }
            }
        }
        
        return stats;
    }

    private String[] extractTableNames(String queryText) {
        // Simple regex to extract table names from SQL
        return queryText.toLowerCase()
            .replaceAll("from\\s+", "FROM ")
            .replaceAll("join\\s+", "JOIN ")
            .split("FROM |JOIN ")[1]
            .split("\\s+")[0]
            .split(",");
    }

    private int calculateIndexPriority(Long usageCount, Double avgExecutionTime) {
        int priority = 0;
        
        if (usageCount > 100) priority += 3;
        else if (usageCount > 50) priority += 2;
        else if (usageCount > 10) priority += 1;
        
        if (avgExecutionTime > 1000) priority += 3;
        else if (avgExecutionTime > 500) priority += 2;
        else if (avgExecutionTime > 100) priority += 1;
        
        return priority;
    }

    private Map<String, Object> getConnectionPoolMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            // Get connection pool information
            metrics.put("active_connections", getActiveConnectionCount());
            metrics.put("idle_connections", getIdleConnectionCount());
            metrics.put("total_connections", getTotalConnectionCount());
            metrics.put("max_connections", getMaxConnectionCount());
        } catch (SQLException e) {
            metrics.put("error", e.getMessage());
        }
        
        return metrics;
    }

    private List<Map<String, Object>> analyzeConnectionUsage() {
        // Analyze connection usage patterns from performance metrics
        String sql = """
            SELECT 
                DATE_TRUNC('hour', timestamp) as hour,
                COUNT(*) as connection_usage,
                AVG(response_time_ms) as avg_response_time
            FROM performance_metrics
            WHERE timestamp >= NOW() - INTERVAL '24 hours'
            GROUP BY DATE_TRUNC('hour', timestamp)
            ORDER BY hour
            """;
        
        return jdbcTemplate.queryForList(sql);
    }

    private int calculateOptimalPoolSize(List<Map<String, Object>> usagePatterns) {
        // Calculate optimal pool size based on usage patterns
        int maxUsage = usagePatterns.stream()
            .mapToInt(pattern -> ((Number) pattern.get("connection_usage")).intValue())
            .max()
            .orElse(10);
        
        // Add 20% buffer
        return (int) (maxUsage * 1.2);
    }

    private Map<String, Integer> calculateOptimalTimeouts(List<Map<String, Object>> usagePatterns) {
        Map<String, Integer> timeouts = new HashMap<>();
        
        // Calculate optimal timeouts based on usage patterns
        double avgResponseTime = usagePatterns.stream()
            .mapToDouble(pattern -> ((Number) pattern.get("avg_response_time")).doubleValue())
            .average()
            .orElse(1000.0);
        
        timeouts.put("connection_timeout", (int) (avgResponseTime * 2));
        timeouts.put("idle_timeout", 600000); // 10 minutes
        timeouts.put("max_lifetime", 1800000); // 30 minutes
        
        return timeouts;
    }

    private List<String> generatePoolOptimizationRecommendations(Map<String, Object> poolMetrics, List<Map<String, Object>> usagePatterns) {
        List<String> recommendations = new ArrayList<>();
        
        int activeConnections = (Integer) poolMetrics.get("active_connections");
        int maxConnections = (Integer) poolMetrics.get("max_connections");
        
        if (activeConnections > maxConnections * 0.8) {
            recommendations.add("Consider increasing max connections - currently at " + (activeConnections * 100 / maxConnections) + "% capacity");
        }
        
        if (usagePatterns.size() > 0) {
            double avgResponseTime = usagePatterns.stream()
                .mapToDouble(pattern -> ((Number) pattern.get("avg_response_time")).doubleValue())
                .average()
                .orElse(0.0);
            
            if (avgResponseTime > 2000) {
                recommendations.add("High average response time detected: " + avgResponseTime + "ms");
            }
        }
        
        return recommendations;
    }

    private void updateTableStatistics() {
        String sql = "ANALYZE";
        jdbcTemplate.execute(sql);
    }

    private void vacuumAndAnalyzeTables() {
        String sql = "VACUUM ANALYZE";
        jdbcTemplate.execute(sql);
    }

    private void cleanupOldMetrics() {
        String sql = "DELETE FROM performance_metrics WHERE timestamp < NOW() - INTERVAL '30 days'";
        jdbcTemplate.execute(sql);
    }

    private void reindexFrequentlyUsedTables() {
        String sql = "REINDEX DATABASE performance_db";
        jdbcTemplate.execute(sql);
    }

    private List<Map<String, Object>> getQueryPerformanceMetrics() {
        String sql = """
            SELECT 
                endpoint,
                AVG(response_time_ms) as avg_response_time,
                COUNT(*) as request_count,
                COUNT(CASE WHEN status_code >= 400 THEN 1 END) as error_count
            FROM performance_metrics
            WHERE timestamp >= NOW() - INTERVAL '1 hour'
            GROUP BY endpoint
            ORDER BY avg_response_time DESC
            """;
        
        return jdbcTemplate.queryForList(sql);
    }

    private List<Map<String, Object>> getTableSizeMetrics() {
        String sql = """
            SELECT 
                schemaname,
                tablename,
                pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size,
                pg_total_relation_size(schemaname||'.'||tablename) as size_bytes
            FROM pg_tables
            WHERE schemaname = 'public'
            ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC
            """;
        
        return jdbcTemplate.queryForList(sql);
    }

    private List<Map<String, Object>> getIndexUsageMetrics() {
        String sql = """
            SELECT 
                schemaname,
                tablename,
                indexname,
                idx_scan,
                idx_tup_read,
                idx_tup_fetch
            FROM pg_stat_user_indexes
            ORDER BY idx_scan DESC
            """;
        
        return jdbcTemplate.queryForList(sql);
    }

    private int calculateDatabaseHealthScore(Map<String, Object> poolMetrics, List<Map<String, Object>> queryMetrics, List<Map<String, Object>> tableMetrics) {
        int score = 100;
        
        // Deduct points for connection pool issues
        int activeConnections = (Integer) poolMetrics.get("active_connections");
        int maxConnections = (Integer) poolMetrics.get("max_connections");
        if (activeConnections > maxConnections * 0.8) {
            score -= 20;
        }
        
        // Deduct points for slow queries
        for (Map<String, Object> query : queryMetrics) {
            double avgResponseTime = ((Number) query.get("avg_response_time")).doubleValue();
            if (avgResponseTime > 2000) {
                score -= 10;
            }
        }
        
        return Math.max(0, score);
    }

    private int getActiveConnectionCount() {
        // This would typically come from the connection pool
        return 5; // Placeholder
    }

    private int getIdleConnectionCount() {
        return 3; // Placeholder
    }

    private int getTotalConnectionCount() {
        return 8; // Placeholder
    }

    private int getMaxConnectionCount() {
        return 20; // Placeholder
    }

    // Data classes
    public static class QueryOptimization {
        private String queryText;
        private Date analysisTimestamp;
        private Map<String, Object> executionPlan;
        private List<String> suggestions;
        private int optimizationScore;
        private List<String> missingIndexes;
        private List<String> tableStatistics;
        private String error;

        // Getters and setters
        public String getQueryText() { return queryText; }
        public void setQueryText(String queryText) { this.queryText = queryText; }
        public Date getAnalysisTimestamp() { return analysisTimestamp; }
        public void setAnalysisTimestamp(Date analysisTimestamp) { this.analysisTimestamp = analysisTimestamp; }
        public Map<String, Object> getExecutionPlan() { return executionPlan; }
        public void setExecutionPlan(Map<String, Object> executionPlan) { this.executionPlan = executionPlan; }
        public List<String> getSuggestions() { return suggestions; }
        public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }
        public int getOptimizationScore() { return optimizationScore; }
        public void setOptimizationScore(int optimizationScore) { this.optimizationScore = optimizationScore; }
        public List<String> getMissingIndexes() { return missingIndexes; }
        public void setMissingIndexes(List<String> missingIndexes) { this.missingIndexes = missingIndexes; }
        public List<String> getTableStatistics() { return tableStatistics; }
        public void setTableStatistics(List<String> tableStatistics) { this.tableStatistics = tableStatistics; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    public static class IndexRecommendation {
        private String tableName;
        private String columnName;
        private Long usageCount;
        private Double avgExecutionTime;
        private String createStatement;
        private int priority;
        private String status;

        // Getters and setters
        public String getTableName() { return tableName; }
        public void setTableName(String tableName) { this.tableName = tableName; }
        public String getColumnName() { return columnName; }
        public void setColumnName(String columnName) { this.columnName = columnName; }
        public Long getUsageCount() { return usageCount; }
        public void setUsageCount(Long usageCount) { this.usageCount = usageCount; }
        public Double getAvgExecutionTime() { return avgExecutionTime; }
        public void setAvgExecutionTime(Double avgExecutionTime) { this.avgExecutionTime = avgExecutionTime; }
        public String getCreateStatement() { return createStatement; }
        public void setCreateStatement(String createStatement) { this.createStatement = createStatement; }
        public int getPriority() { return priority; }
        public void setPriority(int priority) { this.priority = priority; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class ConnectionPoolOptimization {
        private Map<String, Object> currentMetrics;
        private List<Map<String, Object>> usagePatterns;
        private int recommendedPoolSize;
        private Map<String, Integer> recommendedTimeouts;
        private List<String> recommendations;
        private String error;

        // Getters and setters
        public Map<String, Object> getCurrentMetrics() { return currentMetrics; }
        public void setCurrentMetrics(Map<String, Object> currentMetrics) { this.currentMetrics = currentMetrics; }
        public List<Map<String, Object>> getUsagePatterns() { return usagePatterns; }
        public void setUsagePatterns(List<Map<String, Object>> usagePatterns) { this.usagePatterns = usagePatterns; }
        public int getRecommendedPoolSize() { return recommendedPoolSize; }
        public void setRecommendedPoolSize(int recommendedPoolSize) { this.recommendedPoolSize = recommendedPoolSize; }
        public Map<String, Integer> getRecommendedTimeouts() { return recommendedTimeouts; }
        public void setRecommendedTimeouts(Map<String, Integer> recommendedTimeouts) { this.recommendedTimeouts = recommendedTimeouts; }
        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    public static class DatabasePerformanceMetrics {
        private Map<String, Object> connectionPoolMetrics;
        private List<Map<String, Object>> queryMetrics;
        private List<Map<String, Object>> tableMetrics;
        private List<Map<String, Object>> indexMetrics;
        private int healthScore;
        private String error;

        // Getters and setters
        public Map<String, Object> getConnectionPoolMetrics() { return connectionPoolMetrics; }
        public void setConnectionPoolMetrics(Map<String, Object> connectionPoolMetrics) { this.connectionPoolMetrics = connectionPoolMetrics; }
        public List<Map<String, Object>> getQueryMetrics() { return queryMetrics; }
        public void setQueryMetrics(List<Map<String, Object>> queryMetrics) { this.queryMetrics = queryMetrics; }
        public List<Map<String, Object>> getTableMetrics() { return tableMetrics; }
        public void setTableMetrics(List<Map<String, Object>> tableMetrics) { this.tableMetrics = tableMetrics; }
        public List<Map<String, Object>> getIndexMetrics() { return indexMetrics; }
        public void setIndexMetrics(List<Map<String, Object>> indexMetrics) { this.indexMetrics = indexMetrics; }
        public int getHealthScore() { return healthScore; }
        public void setHealthScore(int healthScore) { this.healthScore = healthScore; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}

