-- Test Execution Records
CREATE TABLE test_executions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    test_suite_name VARCHAR(255) NOT NULL,
    test_type VARCHAR(50) NOT NULL, -- unit, integration, performance, security, chaos
    execution_id VARCHAR(255) UNIQUE NOT NULL,
    status VARCHAR(20) NOT NULL, -- running, passed, failed, skipped, error
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    duration_ms BIGINT,
    environment VARCHAR(100) NOT NULL, -- dev, test, staging, prod
    branch_name VARCHAR(255),
    commit_hash VARCHAR(255),
    triggered_by VARCHAR(100), -- manual, ci_cd, scheduled
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Test Results
CREATE TABLE test_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    execution_id VARCHAR(255) NOT NULL,
    test_name VARCHAR(500) NOT NULL,
    test_class VARCHAR(500) NOT NULL,
    test_method VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL, -- passed, failed, skipped, error
    duration_ms BIGINT,
    error_message TEXT,
    stack_trace TEXT,
    assertions_count INTEGER DEFAULT 0,
    assertions_passed INTEGER DEFAULT 0,
    assertions_failed INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (execution_id) REFERENCES test_executions(execution_id)
);

-- Code Coverage Reports
CREATE TABLE code_coverage_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    execution_id VARCHAR(255) NOT NULL,
    service_name VARCHAR(100) NOT NULL,
    coverage_type VARCHAR(50) NOT NULL, -- line, branch, instruction, method, class
    covered_elements INTEGER NOT NULL,
    total_elements INTEGER NOT NULL,
    coverage_percentage DECIMAL(5,2) NOT NULL,
    report_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (execution_id) REFERENCES test_executions(execution_id)
);

-- Performance Test Results
CREATE TABLE performance_test_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    execution_id VARCHAR(255) NOT NULL,
    test_name VARCHAR(255) NOT NULL,
    simulation_name VARCHAR(255) NOT NULL,
    total_users INTEGER NOT NULL,
    duration_seconds INTEGER NOT NULL,
    requests_per_second DECIMAL(10,2),
    average_response_time_ms DECIMAL(10,2),
    p95_response_time_ms DECIMAL(10,2),
    p99_response_time_ms DECIMAL(10,2),
    error_rate DECIMAL(5,2),
    throughput_mb_per_second DECIMAL(10,2),
    test_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (execution_id) REFERENCES test_executions(execution_id)
);

-- Security Test Results
CREATE TABLE security_test_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    execution_id VARCHAR(255) NOT NULL,
    test_type VARCHAR(50) NOT NULL, -- vulnerability_scan, penetration_test, dependency_check, security_headers
    target_url VARCHAR(500),
    vulnerability_count INTEGER DEFAULT 0,
    critical_count INTEGER DEFAULT 0,
    high_count INTEGER DEFAULT 0,
    medium_count INTEGER DEFAULT 0,
    low_count INTEGER DEFAULT 0,
    info_count INTEGER DEFAULT 0,
    test_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (execution_id) REFERENCES test_executions(execution_id)
);

-- Chaos Engineering Experiments
CREATE TABLE chaos_experiments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    experiment_name VARCHAR(255) NOT NULL,
    experiment_type VARCHAR(50) NOT NULL, -- network_latency, service_failure, database_failure, memory_leak, cpu_spike
    target_service VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL, -- planned, running, completed, failed, cancelled
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    duration_seconds INTEGER,
    severity VARCHAR(20) NOT NULL, -- low, medium, high, critical
    impact_assessment JSONB,
    recovery_time_seconds INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Test Data Management
CREATE TABLE test_data_sets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    data_set_name VARCHAR(255) NOT NULL,
    data_type VARCHAR(50) NOT NULL, -- customer, account, transaction, payment
    data_version VARCHAR(50) NOT NULL,
    data_size INTEGER NOT NULL,
    data_hash VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Test Environment Configuration
CREATE TABLE test_environments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    environment_name VARCHAR(100) NOT NULL,
    environment_type VARCHAR(50) NOT NULL, -- dev, test, staging, prod
    base_url VARCHAR(500) NOT NULL,
    database_url VARCHAR(500),
    redis_url VARCHAR(500),
    kafka_url VARCHAR(500),
    is_active BOOLEAN DEFAULT true,
    configuration JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Test Automation Schedules
CREATE TABLE test_schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    schedule_name VARCHAR(255) NOT NULL,
    test_suite_name VARCHAR(255) NOT NULL,
    cron_expression VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    last_run TIMESTAMP,
    next_run TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Quality Gates
CREATE TABLE quality_gates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    gate_name VARCHAR(255) NOT NULL,
    gate_type VARCHAR(50) NOT NULL, -- sonar, coverage, security, performance
    threshold_value DECIMAL(10,2) NOT NULL,
    current_value DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL, -- passed, failed, warning
    service_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Test Notifications
CREATE TABLE test_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    execution_id VARCHAR(255) NOT NULL,
    notification_type VARCHAR(50) NOT NULL, -- email, slack, teams, webhook
    recipient VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL, -- sent, failed, pending
    sent_at TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (execution_id) REFERENCES test_executions(execution_id)
);

-- Test Artifacts
CREATE TABLE test_artifacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    execution_id VARCHAR(255) NOT NULL,
    artifact_type VARCHAR(50) NOT NULL, -- report, log, screenshot, video, data
    artifact_name VARCHAR(255) NOT NULL,
    artifact_path VARCHAR(500) NOT NULL,
    artifact_size BIGINT,
    artifact_hash VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (execution_id) REFERENCES test_executions(execution_id)
);

-- Test Metrics
CREATE TABLE test_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    execution_id VARCHAR(255) NOT NULL,
    metric_name VARCHAR(255) NOT NULL,
    metric_value DECIMAL(15,4) NOT NULL,
    metric_unit VARCHAR(50),
    metric_category VARCHAR(50) NOT NULL, -- performance, coverage, security, reliability
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (execution_id) REFERENCES test_executions(execution_id)
);

-- Indexes for Performance
CREATE INDEX idx_test_executions_test_suite_name ON test_executions(test_suite_name);
CREATE INDEX idx_test_executions_test_type ON test_executions(test_type);
CREATE INDEX idx_test_executions_status ON test_executions(status);
CREATE INDEX idx_test_executions_start_time ON test_executions(start_time);
CREATE INDEX idx_test_executions_environment ON test_executions(environment);

CREATE INDEX idx_test_results_execution_id ON test_results(execution_id);
CREATE INDEX idx_test_results_test_name ON test_results(test_name);
CREATE INDEX idx_test_results_status ON test_results(status);
CREATE INDEX idx_test_results_test_class ON test_results(test_class);

CREATE INDEX idx_code_coverage_reports_execution_id ON code_coverage_reports(execution_id);
CREATE INDEX idx_code_coverage_reports_service_name ON code_coverage_reports(service_name);
CREATE INDEX idx_code_coverage_reports_coverage_type ON code_coverage_reports(coverage_type);

CREATE INDEX idx_performance_test_results_execution_id ON performance_test_results(execution_id);
CREATE INDEX idx_performance_test_results_test_name ON performance_test_results(test_name);
CREATE INDEX idx_performance_test_results_simulation_name ON performance_test_results(simulation_name);

CREATE INDEX idx_security_test_results_execution_id ON security_test_results(execution_id);
CREATE INDEX idx_security_test_results_test_type ON security_test_results(test_type);
CREATE INDEX idx_security_test_results_vulnerability_count ON security_test_results(vulnerability_count);

CREATE INDEX idx_chaos_experiments_experiment_name ON chaos_experiments(experiment_name);
CREATE INDEX idx_chaos_experiments_experiment_type ON chaos_experiments(experiment_type);
CREATE INDEX idx_chaos_experiments_target_service ON chaos_experiments(target_service);
CREATE INDEX idx_chaos_experiments_status ON chaos_experiments(status);

CREATE INDEX idx_test_data_sets_data_set_name ON test_data_sets(data_set_name);
CREATE INDEX idx_test_data_sets_data_type ON test_data_sets(data_type);
CREATE INDEX idx_test_data_sets_is_active ON test_data_sets(is_active);

CREATE INDEX idx_test_environments_environment_name ON test_environments(environment_name);
CREATE INDEX idx_test_environments_environment_type ON test_environments(environment_type);
CREATE INDEX idx_test_environments_is_active ON test_environments(is_active);

CREATE INDEX idx_test_schedules_schedule_name ON test_schedules(schedule_name);
CREATE INDEX idx_test_schedules_is_active ON test_schedules(is_active);
CREATE INDEX idx_test_schedules_next_run ON test_schedules(next_run);

CREATE INDEX idx_quality_gates_gate_name ON quality_gates(gate_name);
CREATE INDEX idx_quality_gates_gate_type ON quality_gates(gate_type);
CREATE INDEX idx_quality_gates_status ON quality_gates(status);
CREATE INDEX idx_quality_gates_service_name ON quality_gates(service_name);

CREATE INDEX idx_test_notifications_execution_id ON test_notifications(execution_id);
CREATE INDEX idx_test_notifications_notification_type ON test_notifications(notification_type);
CREATE INDEX idx_test_notifications_status ON test_notifications(status);

CREATE INDEX idx_test_artifacts_execution_id ON test_artifacts(execution_id);
CREATE INDEX idx_test_artifacts_artifact_type ON test_artifacts(artifact_type);

CREATE INDEX idx_test_metrics_execution_id ON test_metrics(execution_id);
CREATE INDEX idx_test_metrics_metric_name ON test_metrics(metric_name);
CREATE INDEX idx_test_metrics_metric_category ON test_metrics(metric_category);

-- Views for common queries
CREATE VIEW test_execution_summary AS
SELECT 
    test_suite_name,
    test_type,
    COUNT(*) as total_executions,
    COUNT(CASE WHEN status = 'passed' THEN 1 END) as passed_count,
    COUNT(CASE WHEN status = 'failed' THEN 1 END) as failed_count,
    COUNT(CASE WHEN status = 'skipped' THEN 1 END) as skipped_count,
    AVG(duration_ms) as avg_duration_ms,
    MAX(start_time) as last_execution
FROM test_executions
GROUP BY test_suite_name, test_type;

CREATE VIEW code_coverage_summary AS
SELECT 
    service_name,
    coverage_type,
    AVG(coverage_percentage) as avg_coverage,
    MAX(coverage_percentage) as max_coverage,
    MIN(coverage_percentage) as min_coverage,
    COUNT(*) as report_count
FROM code_coverage_reports
GROUP BY service_name, coverage_type;

CREATE VIEW performance_test_summary AS
SELECT 
    simulation_name,
    AVG(requests_per_second) as avg_rps,
    AVG(average_response_time_ms) as avg_response_time,
    AVG(p95_response_time_ms) as avg_p95_response_time,
    AVG(error_rate) as avg_error_rate,
    COUNT(*) as test_count
FROM performance_test_results
GROUP BY simulation_name;

