-- Security Events and Incidents
CREATE TABLE security_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type VARCHAR(100) NOT NULL,
    severity VARCHAR(20) NOT NULL, -- low, medium, high, critical
    source_ip INET,
    user_agent TEXT,
    user_id UUID,
    session_id VARCHAR(255),
    event_data JSONB NOT NULL,
    risk_score INTEGER DEFAULT 0,
    is_resolved BOOLEAN DEFAULT false,
    resolved_at TIMESTAMP,
    resolved_by UUID,
    resolution_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Security Incidents
CREATE TABLE security_incidents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    incident_number VARCHAR(50) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    severity VARCHAR(20) NOT NULL, -- low, medium, high, critical
    status VARCHAR(20) DEFAULT 'open', -- open, investigating, resolved, closed
    category VARCHAR(50) NOT NULL, -- data_breach, ddos, malware, phishing, insider_threat
    affected_systems TEXT[],
    affected_users INTEGER DEFAULT 0,
    data_compromised BOOLEAN DEFAULT false,
    data_types TEXT[],
    discovered_at TIMESTAMP NOT NULL,
    reported_at TIMESTAMP,
    contained_at TIMESTAMP,
    resolved_at TIMESTAMP,
    assigned_to UUID,
    created_by UUID,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Encryption Keys Management
CREATE TABLE encryption_keys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key_name VARCHAR(255) NOT NULL,
    key_type VARCHAR(50) NOT NULL, -- aes, rsa, ecdsa
    key_algorithm VARCHAR(50) NOT NULL, -- AES-256-GCM, RSA-2048, ECDSA-P256
    key_version INTEGER NOT NULL,
    key_data BYTEA NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    rotated_at TIMESTAMP,
    created_by UUID
);

-- Data Classification
CREATE TABLE data_classification (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    table_name VARCHAR(255) NOT NULL,
    column_name VARCHAR(255) NOT NULL,
    classification VARCHAR(20) NOT NULL, -- public, internal, confidential, restricted
    data_type VARCHAR(50) NOT NULL, -- pii, financial, health, commercial
    encryption_required BOOLEAN DEFAULT false,
    encryption_algorithm VARCHAR(50),
    masking_required BOOLEAN DEFAULT false,
    masking_pattern VARCHAR(100),
    retention_period_days INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Security Policies
CREATE TABLE security_policies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    policy_name VARCHAR(255) NOT NULL,
    policy_type VARCHAR(50) NOT NULL, -- password, access_control, data_protection, incident_response
    version VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    is_active BOOLEAN DEFAULT true,
    effective_date TIMESTAMP NOT NULL,
    expiry_date TIMESTAMP,
    created_by UUID,
    approved_by UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Compliance Assessments
CREATE TABLE compliance_assessments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assessment_name VARCHAR(255) NOT NULL,
    framework VARCHAR(50) NOT NULL, -- pci_dss, soc2, gdpr, iso27001
    version VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending', -- pending, in_progress, completed, failed
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP,
    assessor VARCHAR(255),
    scope TEXT NOT NULL,
    findings JSONB,
    recommendations JSONB,
    score INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Vulnerability Scans
CREATE TABLE vulnerability_scans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    scan_name VARCHAR(255) NOT NULL,
    scan_type VARCHAR(50) NOT NULL, -- network, web, database, mobile
    target VARCHAR(255) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending', -- pending, running, completed, failed
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    vulnerabilities_found INTEGER DEFAULT 0,
    critical_count INTEGER DEFAULT 0,
    high_count INTEGER DEFAULT 0,
    medium_count INTEGER DEFAULT 0,
    low_count INTEGER DEFAULT 0,
    scan_results JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Penetration Test Results
CREATE TABLE penetration_tests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    test_name VARCHAR(255) NOT NULL,
    test_type VARCHAR(50) NOT NULL, -- external, internal, web, mobile, social_engineering
    target VARCHAR(255) NOT NULL,
    tester VARCHAR(255) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending', -- pending, in_progress, completed, failed
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP,
    findings JSONB,
    recommendations JSONB,
    risk_level VARCHAR(20), -- low, medium, high, critical
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Security Headers Configuration
CREATE TABLE security_headers_config (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    header_name VARCHAR(100) NOT NULL,
    header_value TEXT NOT NULL,
    is_enabled BOOLEAN DEFAULT true,
    applies_to TEXT[], -- specific endpoints or all
    priority INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Rate Limiting Rules
CREATE TABLE rate_limiting_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_name VARCHAR(255) NOT NULL,
    identifier_type VARCHAR(50) NOT NULL, -- ip, user, api_key, endpoint
    identifier_value VARCHAR(255),
    endpoint_pattern VARCHAR(255),
    max_requests INTEGER NOT NULL,
    window_duration_seconds INTEGER NOT NULL,
    block_duration_seconds INTEGER DEFAULT 3600,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- DDoS Protection Rules
CREATE TABLE ddos_protection_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_name VARCHAR(255) NOT NULL,
    source_type VARCHAR(50) NOT NULL, -- ip, country, asn, user_agent
    source_value VARCHAR(255),
    max_requests_per_minute INTEGER NOT NULL,
    max_requests_per_hour INTEGER NOT NULL,
    block_duration_minutes INTEGER DEFAULT 60,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Security Monitoring Alerts
CREATE TABLE security_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    alert_type VARCHAR(100) NOT NULL,
    severity VARCHAR(20) NOT NULL, -- low, medium, high, critical
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    source_system VARCHAR(100),
    affected_resource VARCHAR(255),
    alert_data JSONB,
    is_acknowledged BOOLEAN DEFAULT false,
    acknowledged_by UUID,
    acknowledged_at TIMESTAMP,
    is_resolved BOOLEAN DEFAULT false,
    resolved_by UUID,
    resolved_at TIMESTAMP,
    resolution_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Audit Logs
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    timestamp TIMESTAMP NOT NULL,
    actor_id UUID,
    actor_type VARCHAR(50) NOT NULL, -- user, system, api
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id VARCHAR(255),
    old_values JSONB,
    new_values JSONB,
    ip_address INET,
    user_agent TEXT,
    session_id VARCHAR(255),
    request_id VARCHAR(255),
    status VARCHAR(20) NOT NULL, -- success, failure, error
    error_message TEXT,
    risk_level VARCHAR(20) DEFAULT 'low', -- low, medium, high, critical
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Threat Intelligence
CREATE TABLE threat_intelligence (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    threat_type VARCHAR(100) NOT NULL, -- malware, phishing, ddos, botnet
    threat_name VARCHAR(255) NOT NULL,
    description TEXT,
    severity VARCHAR(20) NOT NULL, -- low, medium, high, critical
    source VARCHAR(100) NOT NULL,
    confidence_score INTEGER DEFAULT 0,
    indicators JSONB NOT NULL, -- IPs, domains, hashes, patterns
    first_seen TIMESTAMP NOT NULL,
    last_seen TIMESTAMP,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Security Training Records
CREATE TABLE security_training (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    training_type VARCHAR(100) NOT NULL, -- phishing, password_security, data_protection
    training_name VARCHAR(255) NOT NULL,
    completed_at TIMESTAMP,
    score INTEGER,
    certificate_url VARCHAR(500),
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_security_events_event_type ON security_events(event_type);
CREATE INDEX idx_security_events_severity ON security_events(severity);
CREATE INDEX idx_security_events_created_at ON security_events(created_at);
CREATE INDEX idx_security_events_source_ip ON security_events(source_ip);
CREATE INDEX idx_security_events_user_id ON security_events(user_id);

CREATE INDEX idx_security_incidents_incident_number ON security_incidents(incident_number);
CREATE INDEX idx_security_incidents_severity ON security_incidents(severity);
CREATE INDEX idx_security_incidents_status ON security_incidents(status);
CREATE INDEX idx_security_incidents_category ON security_incidents(category);
CREATE INDEX idx_security_incidents_created_at ON security_incidents(created_at);

CREATE INDEX idx_encryption_keys_key_name ON encryption_keys(key_name);
CREATE INDEX idx_encryption_keys_key_type ON encryption_keys(key_type);
CREATE INDEX idx_encryption_keys_is_active ON encryption_keys(is_active);

CREATE INDEX idx_data_classification_table_name ON data_classification(table_name);
CREATE INDEX idx_data_classification_classification ON data_classification(classification);
CREATE INDEX idx_data_classification_data_type ON data_classification(data_type);

CREATE INDEX idx_security_policies_policy_type ON security_policies(policy_type);
CREATE INDEX idx_security_policies_is_active ON security_policies(is_active);
CREATE INDEX idx_security_policies_effective_date ON security_policies(effective_date);

CREATE INDEX idx_compliance_assessments_framework ON compliance_assessments(framework);
CREATE INDEX idx_compliance_assessments_status ON compliance_assessments(status);
CREATE INDEX idx_compliance_assessments_start_date ON compliance_assessments(start_date);

CREATE INDEX idx_vulnerability_scans_scan_type ON vulnerability_scans(scan_type);
CREATE INDEX idx_vulnerability_scans_status ON vulnerability_scans(status);
CREATE INDEX idx_vulnerability_scans_started_at ON vulnerability_scans(started_at);

CREATE INDEX idx_penetration_tests_test_type ON penetration_tests(test_type);
CREATE INDEX idx_penetration_tests_status ON penetration_tests(status);
CREATE INDEX idx_penetration_tests_start_date ON penetration_tests(start_date);

CREATE INDEX idx_security_headers_config_header_name ON security_headers_config(header_name);
CREATE INDEX idx_security_headers_config_is_enabled ON security_headers_config(is_enabled);

CREATE INDEX idx_rate_limiting_rules_identifier_type ON rate_limiting_rules(identifier_type);
CREATE INDEX idx_rate_limiting_rules_is_active ON rate_limiting_rules(is_active);

CREATE INDEX idx_ddos_protection_rules_source_type ON ddos_protection_rules(source_type);
CREATE INDEX idx_ddos_protection_rules_is_active ON ddos_protection_rules(is_active);

CREATE INDEX idx_security_alerts_alert_type ON security_alerts(alert_type);
CREATE INDEX idx_security_alerts_severity ON security_alerts(severity);
CREATE INDEX idx_security_alerts_is_acknowledged ON security_alerts(is_acknowledged);
CREATE INDEX idx_security_alerts_created_at ON security_alerts(created_at);

CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_logs_actor_id ON audit_logs(actor_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_resource_type ON audit_logs(resource_type);
CREATE INDEX idx_audit_logs_status ON audit_logs(status);
CREATE INDEX idx_audit_logs_risk_level ON audit_logs(risk_level);

CREATE INDEX idx_threat_intelligence_threat_type ON threat_intelligence(threat_type);
CREATE INDEX idx_threat_intelligence_severity ON threat_intelligence(severity);
CREATE INDEX idx_threat_intelligence_is_active ON threat_intelligence(is_active);
CREATE INDEX idx_threat_intelligence_first_seen ON threat_intelligence(first_seen);

CREATE INDEX idx_security_training_user_id ON security_training(user_id);
CREATE INDEX idx_security_training_training_type ON security_training(training_type);
CREATE INDEX idx_security_training_completed_at ON security_training(completed_at);

