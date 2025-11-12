-- AML Cases
CREATE TABLE aml_cases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id UUID NOT NULL,
    case_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    risk_score INTEGER NOT NULL DEFAULT 0,
    description TEXT,
    assigned_to VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP,
    resolution_notes TEXT
);

-- AML Rules
CREATE TABLE aml_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_name VARCHAR(255) NOT NULL,
    rule_description TEXT,
    rule_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    threshold_amount DECIMAL(15,2),
    threshold_count INTEGER,
    time_window_minutes INTEGER,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- AML Rule Violations
CREATE TABLE aml_rule_violations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id UUID REFERENCES aml_cases(id),
    rule_id UUID REFERENCES aml_rules(id),
    violation_type VARCHAR(50) NOT NULL,
    violation_description TEXT,
    transaction_id UUID,
    amount DECIMAL(15,2),
    violation_timestamp TIMESTAMP NOT NULL,
    severity VARCHAR(20) NOT NULL,
    is_resolved BOOLEAN DEFAULT false,
    resolution_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Transaction Monitoring
CREATE TABLE transaction_monitoring (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    transaction_date TIMESTAMP NOT NULL,
    risk_score INTEGER NOT NULL DEFAULT 0,
    risk_factors JSONB,
    is_flagged BOOLEAN DEFAULT false,
    flag_reason TEXT,
    review_status VARCHAR(50) DEFAULT 'PENDING',
    reviewed_by VARCHAR(100),
    reviewed_at TIMESTAMP,
    review_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Fraud Detection
CREATE TABLE fraud_detection (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    fraud_type VARCHAR(50) NOT NULL,
    fraud_score DECIMAL(5,2) NOT NULL,
    fraud_indicators JSONB,
    detection_method VARCHAR(50) NOT NULL,
    is_confirmed BOOLEAN DEFAULT false,
    is_false_positive BOOLEAN DEFAULT false,
    investigation_status VARCHAR(50) DEFAULT 'PENDING',
    investigator VARCHAR(100),
    investigation_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Regulatory Reports
CREATE TABLE regulatory_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_type VARCHAR(50) NOT NULL,
    report_name VARCHAR(255) NOT NULL,
    reporting_period_start DATE NOT NULL,
    reporting_period_end DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    generated_by VARCHAR(100),
    generated_at TIMESTAMP,
    submitted_at TIMESTAMP,
    due_date DATE,
    file_path VARCHAR(500),
    file_size BIGINT,
    record_count INTEGER,
    report_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- SAR (Suspicious Activity Reports)
CREATE TABLE suspicious_activity_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sar_number VARCHAR(50) UNIQUE NOT NULL,
    case_id UUID REFERENCES aml_cases(id),
    customer_id UUID NOT NULL,
    activity_type VARCHAR(100) NOT NULL,
    activity_description TEXT NOT NULL,
    suspicious_amount DECIMAL(15,2),
    activity_date_range_start DATE,
    activity_date_range_end DATE,
    filing_status VARCHAR(50) DEFAULT 'DRAFT',
    filed_at TIMESTAMP,
    due_date DATE,
    regulatory_body VARCHAR(100),
    report_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- CTR (Currency Transaction Reports)
CREATE TABLE currency_transaction_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ctr_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id UUID NOT NULL,
    transaction_id UUID NOT NULL,
    transaction_amount DECIMAL(15,2) NOT NULL,
    transaction_date DATE NOT NULL,
    filing_status VARCHAR(50) DEFAULT 'DRAFT',
    filed_at TIMESTAMP,
    due_date DATE,
    regulatory_body VARCHAR(100),
    report_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Audit Logs
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_category VARCHAR(50) NOT NULL,
    user_id UUID,
    customer_id UUID,
    entity_type VARCHAR(100),
    entity_id UUID,
    action VARCHAR(100) NOT NULL,
    description TEXT,
    old_values JSONB,
    new_values JSONB,
    ip_address INET,
    user_agent TEXT,
    session_id VARCHAR(100),
    request_id VARCHAR(100),
    response_status INTEGER,
    processing_time_ms BIGINT,
    event_timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Data Retention Policies
CREATE TABLE data_retention_policies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type VARCHAR(100) NOT NULL,
    retention_years INTEGER NOT NULL,
    archive_after_years INTEGER,
    delete_after_years INTEGER,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Data Retention Actions
CREATE TABLE data_retention_actions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    policy_id UUID REFERENCES data_retention_policies(id),
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    action_type VARCHAR(50) NOT NULL, -- ARCHIVE, DELETE, EXPIRED
    action_date TIMESTAMP NOT NULL,
    records_affected INTEGER,
    file_path VARCHAR(500),
    status VARCHAR(50) DEFAULT 'PENDING',
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Risk Assessments
CREATE TABLE risk_assessments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    assessment_type VARCHAR(50) NOT NULL,
    risk_score INTEGER NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    assessment_factors JSONB,
    assessment_date TIMESTAMP NOT NULL,
    assessor VARCHAR(100),
    next_assessment_date DATE,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Compliance Violations
CREATE TABLE compliance_violations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID,
    violation_type VARCHAR(100) NOT NULL,
    violation_description TEXT NOT NULL,
    severity VARCHAR(20) NOT NULL,
    regulation_reference VARCHAR(100),
    violation_date TIMESTAMP NOT NULL,
    detected_by VARCHAR(100),
    status VARCHAR(50) DEFAULT 'OPEN',
    resolution_notes TEXT,
    resolved_at TIMESTAMP,
    resolved_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Watchlists
CREATE TABLE watchlists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    list_name VARCHAR(255) NOT NULL,
    list_type VARCHAR(50) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Watchlist Entries
CREATE TABLE watchlist_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    watchlist_id UUID REFERENCES watchlists(id),
    entity_type VARCHAR(50) NOT NULL, -- PERSON, ORGANIZATION, ACCOUNT
    entity_id UUID NOT NULL,
    entity_name VARCHAR(255),
    entity_identifier VARCHAR(100),
    reason TEXT,
    added_by VARCHAR(100),
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT true
);

-- Compliance Events
CREATE TABLE compliance_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type VARCHAR(100) NOT NULL,
    event_category VARCHAR(50) NOT NULL,
    customer_id UUID,
    entity_type VARCHAR(100),
    entity_id UUID,
    event_data JSONB,
    severity VARCHAR(20) NOT NULL,
    is_resolved BOOLEAN DEFAULT false,
    resolved_at TIMESTAMP,
    resolved_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_aml_cases_customer_id ON aml_cases(customer_id);
CREATE INDEX idx_aml_cases_status ON aml_cases(status);
CREATE INDEX idx_aml_cases_created_at ON aml_cases(created_at);

CREATE INDEX idx_aml_rules_rule_type ON aml_rules(rule_type);
CREATE INDEX idx_aml_rules_is_active ON aml_rules(is_active);

CREATE INDEX idx_aml_rule_violations_case_id ON aml_rule_violations(case_id);
CREATE INDEX idx_aml_rule_violations_rule_id ON aml_rule_violations(rule_id);
CREATE INDEX idx_aml_rule_violations_violation_timestamp ON aml_rule_violations(violation_timestamp);

CREATE INDEX idx_transaction_monitoring_customer_id ON transaction_monitoring(customer_id);
CREATE INDEX idx_transaction_monitoring_transaction_id ON transaction_monitoring(transaction_id);
CREATE INDEX idx_transaction_monitoring_transaction_date ON transaction_monitoring(transaction_date);
CREATE INDEX idx_transaction_monitoring_is_flagged ON transaction_monitoring(is_flagged);

CREATE INDEX idx_fraud_detection_customer_id ON fraud_detection(customer_id);
CREATE INDEX idx_fraud_detection_transaction_id ON fraud_detection(transaction_id);
CREATE INDEX idx_fraud_detection_fraud_type ON fraud_detection(fraud_type);
CREATE INDEX idx_fraud_detection_created_at ON fraud_detection(created_at);

CREATE INDEX idx_regulatory_reports_report_type ON regulatory_reports(report_type);
CREATE INDEX idx_regulatory_reports_status ON regulatory_reports(status);
CREATE INDEX idx_regulatory_reports_generated_at ON regulatory_reports(generated_at);

CREATE INDEX idx_suspicious_activity_reports_customer_id ON suspicious_activity_reports(customer_id);
CREATE INDEX idx_suspicious_activity_reports_filing_status ON suspicious_activity_reports(filing_status);
CREATE INDEX idx_suspicious_activity_reports_activity_date_range_start ON suspicious_activity_reports(activity_date_range_start);

CREATE INDEX idx_currency_transaction_reports_customer_id ON currency_transaction_reports(customer_id);
CREATE INDEX idx_currency_transaction_reports_transaction_date ON currency_transaction_reports(transaction_date);
CREATE INDEX idx_currency_transaction_reports_filing_status ON currency_transaction_reports(filing_status);

CREATE INDEX idx_audit_logs_event_type ON audit_logs(event_type);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_customer_id ON audit_logs(customer_id);
CREATE INDEX idx_audit_logs_entity_type ON audit_logs(entity_type);
CREATE INDEX idx_audit_logs_entity_id ON audit_logs(entity_id);
CREATE INDEX idx_audit_logs_event_timestamp ON audit_logs(event_timestamp);

CREATE INDEX idx_data_retention_policies_entity_type ON data_retention_policies(entity_type);
CREATE INDEX idx_data_retention_policies_is_active ON data_retention_policies(is_active);

CREATE INDEX idx_data_retention_actions_policy_id ON data_retention_actions(policy_id);
CREATE INDEX idx_data_retention_actions_entity_type ON data_retention_actions(entity_type);
CREATE INDEX idx_data_retention_actions_action_date ON data_retention_actions(action_date);

CREATE INDEX idx_risk_assessments_customer_id ON risk_assessments(customer_id);
CREATE INDEX idx_risk_assessments_assessment_type ON risk_assessments(assessment_type);
CREATE INDEX idx_risk_assessments_assessment_date ON risk_assessments(assessment_date);

CREATE INDEX idx_compliance_violations_customer_id ON compliance_violations(customer_id);
CREATE INDEX idx_compliance_violations_violation_type ON compliance_violations(violation_type);
CREATE INDEX idx_compliance_violations_status ON compliance_violations(status);

CREATE INDEX idx_watchlist_entries_watchlist_id ON watchlist_entries(watchlist_id);
CREATE INDEX idx_watchlist_entries_entity_type ON watchlist_entries(entity_type);
CREATE INDEX idx_watchlist_entries_entity_id ON watchlist_entries(entity_id);

CREATE INDEX idx_compliance_events_event_type ON compliance_events(event_type);
CREATE INDEX idx_compliance_events_customer_id ON compliance_events(customer_id);
CREATE INDEX idx_compliance_events_created_at ON compliance_events(created_at);

