-- Deployment Records
CREATE TABLE deployments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_name VARCHAR(100) NOT NULL,
    version VARCHAR(50) NOT NULL,
    environment VARCHAR(50) NOT NULL, -- dev, test, staging, prod
    status VARCHAR(20) NOT NULL, -- pending, running, success, failed, rolled_back
    deployment_strategy VARCHAR(20) NOT NULL, -- rolling_update, blue_green, canary
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    duration_seconds INTEGER,
    triggered_by VARCHAR(100) NOT NULL, -- user, ci_cd, scheduled
    commit_hash VARCHAR(255),
    branch_name VARCHAR(255),
    pull_request_number INTEGER,
    docker_image VARCHAR(255),
    kubernetes_namespace VARCHAR(100),
    deployment_manifest TEXT,
    rollback_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- CI/CD Pipeline Records
CREATE TABLE ci_cd_pipelines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pipeline_name VARCHAR(255) NOT NULL,
    service_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL, -- running, success, failed, cancelled
    trigger_type VARCHAR(20) NOT NULL, -- push, pull_request, scheduled, manual
    trigger_source VARCHAR(100) NOT NULL, -- github, gitlab, bitbucket
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    duration_seconds INTEGER,
    build_number VARCHAR(50),
    commit_hash VARCHAR(255),
    branch_name VARCHAR(255),
    pull_request_number INTEGER,
    build_log TEXT,
    test_results JSONB,
    code_coverage DECIMAL(5,2),
    security_scan_results JSONB,
    docker_build_status VARCHAR(20),
    kubernetes_deploy_status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Kubernetes Resources
CREATE TABLE kubernetes_resources (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resource_name VARCHAR(255) NOT NULL,
    resource_type VARCHAR(50) NOT NULL, -- deployment, service, ingress, configmap, secret
    namespace VARCHAR(100) NOT NULL,
    cluster_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL, -- active, inactive, error, pending
    replicas INTEGER DEFAULT 1,
    ready_replicas INTEGER DEFAULT 0,
    available_replicas INTEGER DEFAULT 0,
    resource_version VARCHAR(50),
    creation_timestamp TIMESTAMP,
    last_updated TIMESTAMP,
    labels JSONB,
    annotations JSONB,
    spec JSONB,
    status_details JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Monitoring Alerts
CREATE TABLE monitoring_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    alert_name VARCHAR(255) NOT NULL,
    service_name VARCHAR(100) NOT NULL,
    severity VARCHAR(20) NOT NULL, -- critical, warning, info
    status VARCHAR(20) NOT NULL, -- firing, resolved, silenced
    description TEXT NOT NULL,
    summary TEXT,
    labels JSONB,
    annotations JSONB,
    started_at TIMESTAMP NOT NULL,
    resolved_at TIMESTAMP,
    duration_seconds INTEGER,
    notification_sent BOOLEAN DEFAULT false,
    notification_channels JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Log Aggregation
CREATE TABLE log_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_name VARCHAR(100) NOT NULL,
    level VARCHAR(20) NOT NULL, -- debug, info, warn, error, fatal
    message TEXT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    thread_name VARCHAR(100),
    logger_name VARCHAR(255),
    exception_stack_trace TEXT,
    mdc_data JSONB,
    tags JSONB,
    source_file VARCHAR(255),
    source_line INTEGER,
    correlation_id VARCHAR(255),
    trace_id VARCHAR(255),
    span_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Infrastructure as Code
CREATE TABLE infrastructure_resources (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resource_name VARCHAR(255) NOT NULL,
    resource_type VARCHAR(50) NOT NULL, -- aws_instance, aws_s3_bucket, aws_rds_instance, azure_vm, gcp_compute_instance
    provider VARCHAR(20) NOT NULL, -- aws, azure, gcp, terraform, cloudformation
    region VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL, -- planned, created, updated, destroyed, error
    state_file_path VARCHAR(500),
    terraform_state JSONB,
    cloudformation_stack_id VARCHAR(255),
    resource_arn VARCHAR(500),
    tags JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Environment Management
CREATE TABLE environments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    environment_name VARCHAR(100) NOT NULL,
    environment_type VARCHAR(50) NOT NULL, -- dev, test, staging, prod
    cluster_name VARCHAR(100) NOT NULL,
    namespace VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL, -- active, inactive, maintenance
    kubernetes_config TEXT,
    ingress_url VARCHAR(500),
    monitoring_url VARCHAR(500),
    logging_url VARCHAR(500),
    database_url VARCHAR(500),
    redis_url VARCHAR(500),
    kafka_url VARCHAR(500),
    configuration JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Release Management
CREATE TABLE releases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    release_name VARCHAR(255) NOT NULL,
    version VARCHAR(50) NOT NULL,
    environment VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL, -- planned, in_progress, completed, failed, rolled_back
    release_notes TEXT,
    changelog TEXT,
    planned_release_date TIMESTAMP,
    actual_release_date TIMESTAMP,
    rollback_date TIMESTAMP,
    rollback_reason TEXT,
    created_by VARCHAR(100) NOT NULL,
    approved_by VARCHAR(100),
    approval_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Service Dependencies
CREATE TABLE service_dependencies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_name VARCHAR(100) NOT NULL,
    dependency_service VARCHAR(100) NOT NULL,
    dependency_type VARCHAR(50) NOT NULL, -- database, cache, message_queue, api, storage
    is_required BOOLEAN DEFAULT true,
    health_check_url VARCHAR(500),
    timeout_seconds INTEGER DEFAULT 30,
    retry_attempts INTEGER DEFAULT 3,
    circuit_breaker_enabled BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Deployment Strategies
CREATE TABLE deployment_strategies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    strategy_name VARCHAR(100) NOT NULL,
    strategy_type VARCHAR(50) NOT NULL, -- rolling_update, blue_green, canary
    service_name VARCHAR(100) NOT NULL,
    environment VARCHAR(50) NOT NULL,
    configuration JSONB NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Notification Channels
CREATE TABLE notification_channels (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    channel_name VARCHAR(100) NOT NULL,
    channel_type VARCHAR(50) NOT NULL, -- email, slack, teams, webhook, sms
    configuration JSONB NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Deployment Metrics
CREATE TABLE deployment_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    deployment_id UUID NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    metric_value DECIMAL(15,4) NOT NULL,
    metric_unit VARCHAR(50),
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (deployment_id) REFERENCES deployments(id)
);

-- Indexes for Performance
CREATE INDEX idx_deployments_service_name ON deployments(service_name);
CREATE INDEX idx_deployments_environment ON deployments(environment);
CREATE INDEX idx_deployments_status ON deployments(status);
CREATE INDEX idx_deployments_started_at ON deployments(started_at);

CREATE INDEX idx_ci_cd_pipelines_pipeline_name ON ci_cd_pipelines(pipeline_name);
CREATE INDEX idx_ci_cd_pipelines_service_name ON ci_cd_pipelines(service_name);
CREATE INDEX idx_ci_cd_pipelines_status ON ci_cd_pipelines(status);
CREATE INDEX idx_ci_cd_pipelines_started_at ON ci_cd_pipelines(started_at);

CREATE INDEX idx_kubernetes_resources_resource_name ON kubernetes_resources(resource_name);
CREATE INDEX idx_kubernetes_resources_resource_type ON kubernetes_resources(resource_type);
CREATE INDEX idx_kubernetes_resources_namespace ON kubernetes_resources(namespace);
CREATE INDEX idx_kubernetes_resources_status ON kubernetes_resources(status);

CREATE INDEX idx_monitoring_alerts_alert_name ON monitoring_alerts(alert_name);
CREATE INDEX idx_monitoring_alerts_service_name ON monitoring_alerts(service_name);
CREATE INDEX idx_monitoring_alerts_severity ON monitoring_alerts(severity);
CREATE INDEX idx_monitoring_alerts_status ON monitoring_alerts(status);
CREATE INDEX idx_monitoring_alerts_started_at ON monitoring_alerts(started_at);

CREATE INDEX idx_log_entries_service_name ON log_entries(service_name);
CREATE INDEX idx_log_entries_level ON log_entries(level);
CREATE INDEX idx_log_entries_timestamp ON log_entries(timestamp);
CREATE INDEX idx_log_entries_correlation_id ON log_entries(correlation_id);
CREATE INDEX idx_log_entries_trace_id ON log_entries(trace_id);

CREATE INDEX idx_infrastructure_resources_resource_name ON infrastructure_resources(resource_name);
CREATE INDEX idx_infrastructure_resources_resource_type ON infrastructure_resources(resource_type);
CREATE INDEX idx_infrastructure_resources_provider ON infrastructure_resources(provider);
CREATE INDEX idx_infrastructure_resources_status ON infrastructure_resources(status);

CREATE INDEX idx_environments_environment_name ON environments(environment_name);
CREATE INDEX idx_environments_environment_type ON environments(environment_type);
CREATE INDEX idx_environments_status ON environments(status);

CREATE INDEX idx_releases_release_name ON releases(release_name);
CREATE INDEX idx_releases_version ON releases(version);
CREATE INDEX idx_releases_environment ON releases(environment);
CREATE INDEX idx_releases_status ON releases(status);

CREATE INDEX idx_service_dependencies_service_name ON service_dependencies(service_name);
CREATE INDEX idx_service_dependencies_dependency_service ON service_dependencies(dependency_service);

CREATE INDEX idx_deployment_strategies_strategy_name ON deployment_strategies(strategy_name);
CREATE INDEX idx_deployment_strategies_service_name ON deployment_strategies(service_name);
CREATE INDEX idx_deployment_strategies_is_active ON deployment_strategies(is_active);

CREATE INDEX idx_notification_channels_channel_name ON notification_channels(channel_name);
CREATE INDEX idx_notification_channels_channel_type ON notification_channels(channel_type);
CREATE INDEX idx_notification_channels_is_active ON notification_channels(is_active);

CREATE INDEX idx_deployment_metrics_deployment_id ON deployment_metrics(deployment_id);
CREATE INDEX idx_deployment_metrics_metric_name ON deployment_metrics(metric_name);
CREATE INDEX idx_deployment_metrics_timestamp ON deployment_metrics(timestamp);

-- Views for common queries
CREATE VIEW deployment_summary AS
SELECT 
    service_name,
    environment,
    COUNT(*) as total_deployments,
    COUNT(CASE WHEN status = 'success' THEN 1 END) as successful_deployments,
    COUNT(CASE WHEN status = 'failed' THEN 1 END) as failed_deployments,
    COUNT(CASE WHEN status = 'rolled_back' THEN 1 END) as rolled_back_deployments,
    AVG(duration_seconds) as avg_duration_seconds,
    MAX(started_at) as last_deployment
FROM deployments
GROUP BY service_name, environment;

CREATE VIEW pipeline_summary AS
SELECT 
    pipeline_name,
    service_name,
    COUNT(*) as total_pipelines,
    COUNT(CASE WHEN status = 'success' THEN 1 END) as successful_pipelines,
    COUNT(CASE WHEN status = 'failed' THEN 1 END) as failed_pipelines,
    AVG(duration_seconds) as avg_duration_seconds,
    AVG(code_coverage) as avg_code_coverage,
    MAX(started_at) as last_pipeline
FROM ci_cd_pipelines
GROUP BY pipeline_name, service_name;

CREATE VIEW alert_summary AS
SELECT 
    service_name,
    severity,
    COUNT(*) as total_alerts,
    COUNT(CASE WHEN status = 'firing' THEN 1 END) as active_alerts,
    COUNT(CASE WHEN status = 'resolved' THEN 1 END) as resolved_alerts,
    AVG(duration_seconds) as avg_duration_seconds
FROM monitoring_alerts
WHERE started_at >= NOW() - INTERVAL '24 hours'
GROUP BY service_name, severity;

