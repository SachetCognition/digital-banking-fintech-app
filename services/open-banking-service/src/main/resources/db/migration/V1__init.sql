-- Third-party Applications
CREATE TABLE third_party_applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id VARCHAR(255) UNIQUE NOT NULL,
    client_secret_hash VARCHAR(255) NOT NULL,
    application_name VARCHAR(255) NOT NULL,
    description TEXT,
    redirect_uris TEXT[],
    scopes TEXT[] NOT NULL,
    status VARCHAR(20) DEFAULT 'active', -- active, inactive, suspended, revoked
    created_by UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- API Keys
CREATE TABLE api_keys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key_name VARCHAR(255) NOT NULL,
    key_value VARCHAR(255) UNIQUE NOT NULL,
    key_hash VARCHAR(255) NOT NULL,
    application_id UUID REFERENCES third_party_applications(id),
    permissions TEXT[],
    rate_limit_per_hour INTEGER DEFAULT 1000,
    is_active BOOLEAN DEFAULT true,
    expires_at TIMESTAMP,
    last_used_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Webhook Subscriptions
CREATE TABLE webhook_subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID REFERENCES third_party_applications(id),
    webhook_url VARCHAR(500) NOT NULL,
    events TEXT[] NOT NULL,
    secret VARCHAR(255),
    status VARCHAR(20) DEFAULT 'active', -- active, inactive, failed
    retry_count INTEGER DEFAULT 0,
    last_delivery_at TIMESTAMP,
    last_failure_at TIMESTAMP,
    last_failure_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Webhook Deliveries
CREATE TABLE webhook_deliveries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subscription_id UUID REFERENCES webhook_subscriptions(id),
    event_type VARCHAR(100) NOT NULL,
    event_id VARCHAR(255) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(20) DEFAULT 'pending', -- pending, delivered, failed, retrying
    http_status INTEGER,
    response_body TEXT,
    attempt_count INTEGER DEFAULT 0,
    max_attempts INTEGER DEFAULT 3,
    next_retry_at TIMESTAMP,
    delivered_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Rate Limiting
CREATE TABLE rate_limits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    identifier VARCHAR(255) NOT NULL, -- API key, IP address, user ID
    identifier_type VARCHAR(50) NOT NULL, -- api_key, ip, user
    endpoint VARCHAR(255) NOT NULL,
    request_count INTEGER DEFAULT 0,
    window_start TIMESTAMP NOT NULL,
    window_duration INTEGER NOT NULL, -- in seconds
    limit_value INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(identifier, identifier_type, endpoint, window_start)
);

-- API Usage Analytics
CREATE TABLE api_usage_analytics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID REFERENCES third_party_applications(id),
    endpoint VARCHAR(255) NOT NULL,
    method VARCHAR(10) NOT NULL,
    status_code INTEGER NOT NULL,
    response_time_ms INTEGER NOT NULL,
    request_size_bytes BIGINT,
    response_size_bytes BIGINT,
    user_agent TEXT,
    ip_address INET,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Open Banking Consents
CREATE TABLE open_banking_consents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    consent_id VARCHAR(255) UNIQUE NOT NULL,
    application_id UUID REFERENCES third_party_applications(id),
    customer_id UUID NOT NULL,
    consent_type VARCHAR(50) NOT NULL, -- account_access, payment_initiation, funds_confirmation
    status VARCHAR(20) DEFAULT 'pending', -- pending, authorized, rejected, expired, revoked
    permissions TEXT[] NOT NULL,
    accounts TEXT[],
    expiration_date TIMESTAMP,
    authorized_at TIMESTAMP,
    revoked_at TIMESTAMP,
    consent_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Account Access Tokens
CREATE TABLE account_access_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token_value VARCHAR(255) UNIQUE NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    consent_id UUID REFERENCES open_banking_consents(id),
    application_id UUID REFERENCES third_party_applications(id),
    customer_id UUID NOT NULL,
    scopes TEXT[] NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT true,
    last_used_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Payment Initiation Requests
CREATE TABLE payment_initiation_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id VARCHAR(255) UNIQUE NOT NULL,
    application_id UUID REFERENCES third_party_applications(id),
    consent_id UUID REFERENCES open_banking_consents(id),
    customer_id UUID NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    debtor_account VARCHAR(255) NOT NULL,
    creditor_account VARCHAR(255) NOT NULL,
    creditor_name VARCHAR(255) NOT NULL,
    payment_purpose VARCHAR(255),
    status VARCHAR(20) DEFAULT 'pending', -- pending, authorized, executed, failed, cancelled
    execution_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Third-party Integration Logs
CREATE TABLE integration_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID REFERENCES third_party_applications(id),
    integration_type VARCHAR(50) NOT NULL, -- plaid, yodlee, finicity
    operation VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL, -- success, failure, timeout
    request_data JSONB,
    response_data JSONB,
    error_message TEXT,
    duration_ms INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- API Documentation Versions
CREATE TABLE api_documentation_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version VARCHAR(20) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    openapi_spec JSONB NOT NULL,
    is_current BOOLEAN DEFAULT false,
    is_deprecated BOOLEAN DEFAULT false,
    deprecation_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- SDK Downloads
CREATE TABLE sdk_downloads (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sdk_name VARCHAR(100) NOT NULL,
    version VARCHAR(20) NOT NULL,
    language VARCHAR(50) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    download_count INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_third_party_applications_client_id ON third_party_applications(client_id);
CREATE INDEX idx_third_party_applications_status ON third_party_applications(status);

CREATE INDEX idx_api_keys_application_id ON api_keys(application_id);
CREATE INDEX idx_api_keys_key_hash ON api_keys(key_hash);
CREATE INDEX idx_api_keys_is_active ON api_keys(is_active);

CREATE INDEX idx_webhook_subscriptions_application_id ON webhook_subscriptions(application_id);
CREATE INDEX idx_webhook_subscriptions_status ON webhook_subscriptions(status);

CREATE INDEX idx_webhook_deliveries_subscription_id ON webhook_deliveries(subscription_id);
CREATE INDEX idx_webhook_deliveries_status ON webhook_deliveries(status);
CREATE INDEX idx_webhook_deliveries_created_at ON webhook_deliveries(created_at);

CREATE INDEX idx_rate_limits_identifier ON rate_limits(identifier);
CREATE INDEX idx_rate_limits_endpoint ON rate_limits(endpoint);
CREATE INDEX idx_rate_limits_window_start ON rate_limits(window_start);

CREATE INDEX idx_api_usage_analytics_application_id ON api_usage_analytics(application_id);
CREATE INDEX idx_api_usage_analytics_timestamp ON api_usage_analytics(timestamp);
CREATE INDEX idx_api_usage_analytics_endpoint ON api_usage_analytics(endpoint);

CREATE INDEX idx_open_banking_consents_consent_id ON open_banking_consents(consent_id);
CREATE INDEX idx_open_banking_consents_application_id ON open_banking_consents(application_id);
CREATE INDEX idx_open_banking_consents_customer_id ON open_banking_consents(customer_id);
CREATE INDEX idx_open_banking_consents_status ON open_banking_consents(status);

CREATE INDEX idx_account_access_tokens_token_hash ON account_access_tokens(token_hash);
CREATE INDEX idx_account_access_tokens_consent_id ON account_access_tokens(consent_id);
CREATE INDEX idx_account_access_tokens_customer_id ON account_access_tokens(customer_id);
CREATE INDEX idx_account_access_tokens_is_active ON account_access_tokens(is_active);

CREATE INDEX idx_payment_initiation_requests_payment_id ON payment_initiation_requests(payment_id);
CREATE INDEX idx_payment_initiation_requests_application_id ON payment_initiation_requests(application_id);
CREATE INDEX idx_payment_initiation_requests_customer_id ON payment_initiation_requests(customer_id);
CREATE INDEX idx_payment_initiation_requests_status ON payment_initiation_requests(status);

CREATE INDEX idx_integration_logs_application_id ON integration_logs(application_id);
CREATE INDEX idx_integration_logs_integration_type ON integration_logs(integration_type);
CREATE INDEX idx_integration_logs_created_at ON integration_logs(created_at);

CREATE INDEX idx_api_documentation_versions_version ON api_documentation_versions(version);
CREATE INDEX idx_api_documentation_versions_is_current ON api_documentation_versions(is_current);

CREATE INDEX idx_sdk_downloads_sdk_name ON sdk_downloads(sdk_name);
CREATE INDEX idx_sdk_downloads_language ON sdk_downloads(language);
CREATE INDEX idx_sdk_downloads_is_active ON sdk_downloads(is_active);

