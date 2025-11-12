-- Admin Users
CREATE TABLE admin_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL,
    permissions TEXT[],
    is_active BOOLEAN DEFAULT true,
    last_login_at TIMESTAMP,
    failed_login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- System Configuration
CREATE TABLE system_configurations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    config_key VARCHAR(255) UNIQUE NOT NULL,
    config_value TEXT NOT NULL,
    config_type VARCHAR(50) NOT NULL, -- string, number, boolean, json
    description TEXT,
    category VARCHAR(100) NOT NULL,
    is_encrypted BOOLEAN DEFAULT false,
    is_sensitive BOOLEAN DEFAULT false,
    is_readonly BOOLEAN DEFAULT false,
    validation_rules JSONB,
    created_by UUID REFERENCES admin_users(id),
    updated_by UUID REFERENCES admin_users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- System Health Metrics
CREATE TABLE system_health_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_name VARCHAR(100) NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    metric_value DECIMAL(15,4) NOT NULL,
    metric_unit VARCHAR(50),
    status VARCHAR(20) NOT NULL, -- healthy, warning, critical, unknown
    timestamp TIMESTAMP NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- System Alerts
CREATE TABLE system_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL, -- low, medium, high, critical
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    service_name VARCHAR(100),
    metric_name VARCHAR(100),
    threshold_value DECIMAL(15,4),
    current_value DECIMAL(15,4),
    status VARCHAR(20) DEFAULT 'open', -- open, acknowledged, resolved, closed
    acknowledged_by UUID REFERENCES admin_users(id),
    acknowledged_at TIMESTAMP,
    resolved_at TIMESTAMP,
    resolved_by UUID REFERENCES admin_users(id),
    resolution_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Backup Records
CREATE TABLE backup_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    backup_name VARCHAR(255) NOT NULL,
    backup_type VARCHAR(50) NOT NULL, -- full, incremental, differential
    service_name VARCHAR(100) NOT NULL,
    database_name VARCHAR(100),
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL, -- in_progress, completed, failed, expired
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    expires_at TIMESTAMP,
    retention_days INTEGER NOT NULL,
    checksum VARCHAR(255),
    compression_type VARCHAR(50),
    encryption_enabled BOOLEAN DEFAULT false,
    created_by UUID REFERENCES admin_users(id),
    error_message TEXT,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Recovery Operations
CREATE TABLE recovery_operations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    operation_name VARCHAR(255) NOT NULL,
    backup_id UUID REFERENCES backup_records(id),
    target_service VARCHAR(100) NOT NULL,
    target_database VARCHAR(100),
    status VARCHAR(20) NOT NULL, -- pending, in_progress, completed, failed
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    created_by UUID REFERENCES admin_users(id),
    error_message TEXT,
    recovery_notes TEXT,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Audit Logs
CREATE TABLE admin_audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    admin_user_id UUID REFERENCES admin_users(id),
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id VARCHAR(255),
    old_values JSONB,
    new_values JSONB,
    ip_address INET,
    user_agent TEXT,
    session_id VARCHAR(100),
    request_id VARCHAR(100),
    status VARCHAR(20) NOT NULL, -- success, failure, error
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User Sessions
CREATE TABLE admin_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    admin_user_id UUID REFERENCES admin_users(id),
    session_token VARCHAR(255) UNIQUE NOT NULL,
    ip_address INET,
    user_agent TEXT,
    is_active BOOLEAN DEFAULT true,
    last_activity_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- System Events
CREATE TABLE system_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type VARCHAR(100) NOT NULL,
    event_category VARCHAR(50) NOT NULL,
    service_name VARCHAR(100),
    severity VARCHAR(20) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    event_data JSONB,
    is_resolved BOOLEAN DEFAULT false,
    resolved_at TIMESTAMP,
    resolved_by UUID REFERENCES admin_users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Maintenance Windows
CREATE TABLE maintenance_windows (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    service_name VARCHAR(100),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) DEFAULT 'scheduled', -- scheduled, in_progress, completed, cancelled
    impact_level VARCHAR(20) NOT NULL, -- low, medium, high, critical
    created_by UUID REFERENCES admin_users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Feature Flags
CREATE TABLE feature_flags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    flag_name VARCHAR(100) UNIQUE NOT NULL,
    flag_key VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    is_enabled BOOLEAN DEFAULT false,
    rollout_percentage INTEGER DEFAULT 0,
    target_audience JSONB,
    conditions JSONB,
    created_by UUID REFERENCES admin_users(id),
    updated_by UUID REFERENCES admin_users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- API Keys
CREATE TABLE api_keys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key_name VARCHAR(255) NOT NULL,
    key_value VARCHAR(255) UNIQUE NOT NULL,
    key_hash VARCHAR(255) NOT NULL,
    permissions TEXT[],
    is_active BOOLEAN DEFAULT true,
    expires_at TIMESTAMP,
    last_used_at TIMESTAMP,
    created_by UUID REFERENCES admin_users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- System Logs
CREATE TABLE system_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_name VARCHAR(100) NOT NULL,
    log_level VARCHAR(20) NOT NULL, -- DEBUG, INFO, WARN, ERROR, FATAL
    message TEXT NOT NULL,
    exception_stack TEXT,
    thread_name VARCHAR(100),
    logger_name VARCHAR(255),
    mdc_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Performance Metrics
CREATE TABLE performance_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_name VARCHAR(100) NOT NULL,
    endpoint VARCHAR(255),
    method VARCHAR(10),
    response_time_ms INTEGER NOT NULL,
    status_code INTEGER,
    request_size_bytes BIGINT,
    response_size_bytes BIGINT,
    cpu_usage_percent DECIMAL(5,2),
    memory_usage_mb DECIMAL(10,2),
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_admin_users_username ON admin_users(username);
CREATE INDEX idx_admin_users_email ON admin_users(email);
CREATE INDEX idx_admin_users_role ON admin_users(role);
CREATE INDEX idx_admin_users_is_active ON admin_users(is_active);

CREATE INDEX idx_system_configurations_config_key ON system_configurations(config_key);
CREATE INDEX idx_system_configurations_category ON system_configurations(category);
CREATE INDEX idx_system_configurations_is_sensitive ON system_configurations(is_sensitive);

CREATE INDEX idx_system_health_metrics_service_name ON system_health_metrics(service_name);
CREATE INDEX idx_system_health_metrics_metric_name ON system_health_metrics(metric_name);
CREATE INDEX idx_system_health_metrics_status ON system_health_metrics(status);
CREATE INDEX idx_system_health_metrics_timestamp ON system_health_metrics(timestamp);

CREATE INDEX idx_system_alerts_alert_type ON system_alerts(alert_type);
CREATE INDEX idx_system_alerts_severity ON system_alerts(severity);
CREATE INDEX idx_system_alerts_status ON system_alerts(status);
CREATE INDEX idx_system_alerts_service_name ON system_alerts(service_name);
CREATE INDEX idx_system_alerts_created_at ON system_alerts(created_at);

CREATE INDEX idx_backup_records_service_name ON backup_records(service_name);
CREATE INDEX idx_backup_records_backup_type ON backup_records(backup_type);
CREATE INDEX idx_backup_records_status ON backup_records(status);
CREATE INDEX idx_backup_records_started_at ON backup_records(started_at);

CREATE INDEX idx_recovery_operations_target_service ON recovery_operations(target_service);
CREATE INDEX idx_recovery_operations_status ON recovery_operations(status);
CREATE INDEX idx_recovery_operations_started_at ON recovery_operations(started_at);

CREATE INDEX idx_admin_audit_logs_admin_user_id ON admin_audit_logs(admin_user_id);
CREATE INDEX idx_admin_audit_logs_action ON admin_audit_logs(action);
CREATE INDEX idx_admin_audit_logs_resource_type ON admin_audit_logs(resource_type);
CREATE INDEX idx_admin_audit_logs_created_at ON admin_audit_logs(created_at);

CREATE INDEX idx_admin_sessions_admin_user_id ON admin_sessions(admin_user_id);
CREATE INDEX idx_admin_sessions_session_token ON admin_sessions(session_token);
CREATE INDEX idx_admin_sessions_is_active ON admin_sessions(is_active);

CREATE INDEX idx_system_events_event_type ON system_events(event_type);
CREATE INDEX idx_system_events_service_name ON system_events(service_name);
CREATE INDEX idx_system_events_severity ON system_events(severity);
CREATE INDEX idx_system_events_created_at ON system_events(created_at);

CREATE INDEX idx_maintenance_windows_service_name ON maintenance_windows(service_name);
CREATE INDEX idx_maintenance_windows_start_time ON maintenance_windows(start_time);
CREATE INDEX idx_maintenance_windows_status ON maintenance_windows(status);

CREATE INDEX idx_feature_flags_flag_key ON feature_flags(flag_key);
CREATE INDEX idx_feature_flags_is_enabled ON feature_flags(is_enabled);

CREATE INDEX idx_api_keys_key_hash ON api_keys(key_hash);
CREATE INDEX idx_api_keys_is_active ON api_keys(is_active);
CREATE INDEX idx_api_keys_created_by ON api_keys(created_by);

CREATE INDEX idx_system_logs_service_name ON system_logs(service_name);
CREATE INDEX idx_system_logs_log_level ON system_logs(log_level);
CREATE INDEX idx_system_logs_created_at ON system_logs(created_at);

CREATE INDEX idx_performance_metrics_service_name ON performance_metrics(service_name);
CREATE INDEX idx_performance_metrics_endpoint ON performance_metrics(endpoint);
CREATE INDEX idx_performance_metrics_timestamp ON performance_metrics(timestamp);

