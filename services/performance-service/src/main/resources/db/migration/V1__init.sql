-- Performance Metrics
CREATE TABLE performance_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_name VARCHAR(100) NOT NULL,
    endpoint VARCHAR(255) NOT NULL,
    method VARCHAR(10) NOT NULL,
    response_time_ms INTEGER NOT NULL,
    status_code INTEGER NOT NULL,
    request_size_bytes BIGINT,
    response_size_bytes BIGINT,
    user_id UUID,
    session_id VARCHAR(255),
    ip_address INET,
    user_agent TEXT,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Database Performance Metrics
CREATE TABLE database_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    database_name VARCHAR(100) NOT NULL,
    query_text TEXT NOT NULL,
    execution_time_ms INTEGER NOT NULL,
    rows_affected INTEGER,
    rows_returned INTEGER,
    connection_id VARCHAR(255),
    transaction_id VARCHAR(255),
    is_slow_query BOOLEAN DEFAULT false,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Cache Performance Metrics
CREATE TABLE cache_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cache_name VARCHAR(100) NOT NULL,
    operation VARCHAR(20) NOT NULL, -- GET, PUT, EVICT, CLEAR
    key_hash VARCHAR(255) NOT NULL,
    hit BOOLEAN NOT NULL,
    response_time_ms INTEGER NOT NULL,
    key_size_bytes INTEGER,
    value_size_bytes INTEGER,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Load Balancer Metrics
CREATE TABLE load_balancer_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_name VARCHAR(100) NOT NULL,
    instance_id VARCHAR(255) NOT NULL,
    endpoint VARCHAR(255) NOT NULL,
    response_time_ms INTEGER NOT NULL,
    status_code INTEGER NOT NULL,
    active_connections INTEGER DEFAULT 0,
    total_requests BIGINT DEFAULT 0,
    error_count BIGINT DEFAULT 0,
    is_healthy BOOLEAN DEFAULT true,
    last_health_check TIMESTAMP,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- CDN Metrics
CREATE TABLE cdn_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resource_url VARCHAR(500) NOT NULL,
    resource_type VARCHAR(50) NOT NULL, -- image, css, js, html, api
    cache_status VARCHAR(20) NOT NULL, -- HIT, MISS, BYPASS, ERROR
    response_time_ms INTEGER NOT NULL,
    file_size_bytes BIGINT,
    bandwidth_bytes BIGINT,
    country_code VARCHAR(2),
    city VARCHAR(100),
    user_agent TEXT,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- System Performance Metrics
CREATE TABLE system_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_name VARCHAR(100) NOT NULL,
    instance_id VARCHAR(255) NOT NULL,
    cpu_usage_percent DECIMAL(5,2) NOT NULL,
    memory_usage_percent DECIMAL(5,2) NOT NULL,
    memory_used_mb BIGINT NOT NULL,
    memory_total_mb BIGINT NOT NULL,
    disk_usage_percent DECIMAL(5,2) NOT NULL,
    disk_used_gb DECIMAL(10,2) NOT NULL,
    disk_total_gb DECIMAL(10,2) NOT NULL,
    network_in_bytes BIGINT NOT NULL,
    network_out_bytes BIGINT NOT NULL,
    active_threads INTEGER NOT NULL,
    gc_collections BIGINT NOT NULL,
    gc_time_ms BIGINT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Performance Alerts
CREATE TABLE performance_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    alert_type VARCHAR(50) NOT NULL, -- response_time, error_rate, cpu_usage, memory_usage, disk_usage
    service_name VARCHAR(100) NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    threshold_value DECIMAL(10,2) NOT NULL,
    actual_value DECIMAL(10,2) NOT NULL,
    severity VARCHAR(20) NOT NULL, -- low, medium, high, critical
    message TEXT NOT NULL,
    is_resolved BOOLEAN DEFAULT false,
    resolved_at TIMESTAMP,
    resolved_by UUID,
    resolution_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Performance Optimization Rules
CREATE TABLE optimization_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_name VARCHAR(255) NOT NULL,
    rule_type VARCHAR(50) NOT NULL, -- query_optimization, cache_strategy, load_balancing, cdn_config
    service_name VARCHAR(100) NOT NULL,
    condition_expression TEXT NOT NULL,
    action_expression TEXT NOT NULL,
    priority INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Cache Configuration
CREATE TABLE cache_configurations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cache_name VARCHAR(100) UNIQUE NOT NULL,
    cache_type VARCHAR(50) NOT NULL, -- redis, hazelcast, caffeine, ehcache
    ttl_seconds INTEGER NOT NULL,
    max_entries INTEGER,
    eviction_policy VARCHAR(50) DEFAULT 'LRU',
    compression_enabled BOOLEAN DEFAULT false,
    statistics_enabled BOOLEAN DEFAULT true,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Load Balancer Configuration
CREATE TABLE load_balancer_configurations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_name VARCHAR(100) NOT NULL,
    instance_url VARCHAR(255) NOT NULL,
    weight INTEGER DEFAULT 1,
    health_check_url VARCHAR(255),
    health_check_interval_seconds INTEGER DEFAULT 30,
    timeout_seconds INTEGER DEFAULT 5,
    max_retries INTEGER DEFAULT 3,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- CDN Configuration
CREATE TABLE cdn_configurations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resource_pattern VARCHAR(255) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    cdn_provider VARCHAR(50) NOT NULL,
    cache_ttl_seconds INTEGER NOT NULL,
    compression_enabled BOOLEAN DEFAULT true,
    image_optimization_enabled BOOLEAN DEFAULT false,
    minify_enabled BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Performance Baselines
CREATE TABLE performance_baselines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_name VARCHAR(100) NOT NULL,
    endpoint VARCHAR(255) NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    baseline_value DECIMAL(10,2) NOT NULL,
    standard_deviation DECIMAL(10,2) NOT NULL,
    sample_size INTEGER NOT NULL,
    measurement_period_days INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Performance Reports
CREATE TABLE performance_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_name VARCHAR(255) NOT NULL,
    report_type VARCHAR(50) NOT NULL, -- daily, weekly, monthly, custom
    service_name VARCHAR(100),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    report_data JSONB NOT NULL,
    generated_by UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for Performance
CREATE INDEX idx_performance_metrics_service_name ON performance_metrics(service_name);
CREATE INDEX idx_performance_metrics_endpoint ON performance_metrics(endpoint);
CREATE INDEX idx_performance_metrics_timestamp ON performance_metrics(timestamp);
CREATE INDEX idx_performance_metrics_response_time ON performance_metrics(response_time_ms);
CREATE INDEX idx_performance_metrics_status_code ON performance_metrics(status_code);

CREATE INDEX idx_database_metrics_database_name ON database_metrics(database_name);
CREATE INDEX idx_database_metrics_execution_time ON database_metrics(execution_time_ms);
CREATE INDEX idx_database_metrics_timestamp ON database_metrics(timestamp);
CREATE INDEX idx_database_metrics_slow_query ON database_metrics(is_slow_query);

CREATE INDEX idx_cache_metrics_cache_name ON cache_metrics(cache_name);
CREATE INDEX idx_cache_metrics_operation ON cache_metrics(operation);
CREATE INDEX idx_cache_metrics_hit ON cache_metrics(hit);
CREATE INDEX idx_cache_metrics_timestamp ON cache_metrics(timestamp);

CREATE INDEX idx_load_balancer_metrics_service_name ON load_balancer_metrics(service_name);
CREATE INDEX idx_load_balancer_metrics_instance_id ON load_balancer_metrics(instance_id);
CREATE INDEX idx_load_balancer_metrics_timestamp ON load_balancer_metrics(timestamp);
CREATE INDEX idx_load_balancer_metrics_is_healthy ON load_balancer_metrics(is_healthy);

CREATE INDEX idx_cdn_metrics_resource_type ON cdn_metrics(resource_type);
CREATE INDEX idx_cdn_metrics_cache_status ON cdn_metrics(cache_status);
CREATE INDEX idx_cdn_metrics_timestamp ON cdn_metrics(timestamp);
CREATE INDEX idx_cdn_metrics_country_code ON cdn_metrics(country_code);

CREATE INDEX idx_system_metrics_service_name ON system_metrics(service_name);
CREATE INDEX idx_system_metrics_instance_id ON system_metrics(instance_id);
CREATE INDEX idx_system_metrics_timestamp ON system_metrics(timestamp);

CREATE INDEX idx_performance_alerts_alert_type ON performance_alerts(alert_type);
CREATE INDEX idx_performance_alerts_service_name ON performance_alerts(service_name);
CREATE INDEX idx_performance_alerts_severity ON performance_alerts(severity);
CREATE INDEX idx_performance_alerts_is_resolved ON performance_alerts(is_resolved);
CREATE INDEX idx_performance_alerts_created_at ON performance_alerts(created_at);

CREATE INDEX idx_optimization_rules_rule_type ON optimization_rules(rule_type);
CREATE INDEX idx_optimization_rules_service_name ON optimization_rules(service_name);
CREATE INDEX idx_optimization_rules_is_active ON optimization_rules(is_active);

CREATE INDEX idx_cache_configurations_cache_name ON cache_configurations(cache_name);
CREATE INDEX idx_cache_configurations_cache_type ON cache_configurations(cache_type);
CREATE INDEX idx_cache_configurations_is_active ON cache_configurations(is_active);

CREATE INDEX idx_load_balancer_configurations_service_name ON load_balancer_configurations(service_name);
CREATE INDEX idx_load_balancer_configurations_is_active ON load_balancer_configurations(is_active);

CREATE INDEX idx_cdn_configurations_resource_pattern ON cdn_configurations(resource_pattern);
CREATE INDEX idx_cdn_configurations_resource_type ON cdn_configurations(resource_type);
CREATE INDEX idx_cdn_configurations_is_active ON cdn_configurations(is_active);

CREATE INDEX idx_performance_baselines_service_name ON performance_baselines(service_name);
CREATE INDEX idx_performance_baselines_endpoint ON performance_baselines(endpoint);
CREATE INDEX idx_performance_baselines_metric_name ON performance_baselines(metric_name);

CREATE INDEX idx_performance_reports_report_type ON performance_reports(report_type);
CREATE INDEX idx_performance_reports_service_name ON performance_reports(service_name);
CREATE INDEX idx_performance_reports_start_date ON performance_reports(start_date);
CREATE INDEX idx_performance_reports_end_date ON performance_reports(end_date);

-- Partitioning for large tables
CREATE TABLE performance_metrics_y2024m01 PARTITION OF performance_metrics
    FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

CREATE TABLE performance_metrics_y2024m02 PARTITION OF performance_metrics
    FOR VALUES FROM ('2024-02-01') TO ('2024-03-01');

-- Add more partitions as needed

-- Views for common queries
CREATE VIEW performance_summary AS
SELECT 
    service_name,
    endpoint,
    COUNT(*) as total_requests,
    AVG(response_time_ms) as avg_response_time,
    PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY response_time_ms) as p95_response_time,
    PERCENTILE_CONT(0.99) WITHIN GROUP (ORDER BY response_time_ms) as p99_response_time,
    COUNT(CASE WHEN status_code >= 400 THEN 1 END) as error_count,
    ROUND(COUNT(CASE WHEN status_code >= 400 THEN 1 END) * 100.0 / COUNT(*), 2) as error_rate
FROM performance_metrics
WHERE timestamp >= NOW() - INTERVAL '1 hour'
GROUP BY service_name, endpoint;

CREATE VIEW slow_queries AS
SELECT 
    database_name,
    query_text,
    execution_time_ms,
    rows_affected,
    rows_returned,
    timestamp
FROM database_metrics
WHERE is_slow_query = true
ORDER BY execution_time_ms DESC;

CREATE VIEW cache_hit_rates AS
SELECT 
    cache_name,
    COUNT(*) as total_operations,
    COUNT(CASE WHEN hit = true THEN 1 END) as hit_count,
    ROUND(COUNT(CASE WHEN hit = true THEN 1 END) * 100.0 / COUNT(*), 2) as hit_rate
FROM cache_metrics
WHERE timestamp >= NOW() - INTERVAL '1 hour'
GROUP BY cache_name;

