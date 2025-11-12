-- Customer Service initial schema
CREATE TABLE IF NOT EXISTS customers (
    id UUID PRIMARY KEY,
    user_no VARCHAR(32) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(32) UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    dob DATE,
    country CHAR(2) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS customer_devices (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    device_fingerprint VARCHAR(255) NOT NULL,
    platform VARCHAR(32),
    mfa_enrolled BOOLEAN NOT NULL DEFAULT false,
    last_seen_at TIMESTAMPTZ,
    CONSTRAINT fk_device_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE INDEX IF NOT EXISTS idx_customers_email ON customers(email);
CREATE INDEX IF NOT EXISTS idx_customers_phone ON customers(phone);
CREATE INDEX IF NOT EXISTS idx_devices_customer ON customer_devices(customer_id);

CREATE TABLE IF NOT EXISTS kyc_cases (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    level VARCHAR(16) NOT NULL,
    status VARCHAR(16) NOT NULL,
    provider VARCHAR(64),
    provider_ref VARCHAR(128),
    risk_score INT,
    rejection_reason TEXT,
    submitted_at TIMESTAMPTZ,
    reviewed_at TIMESTAMPTZ,
    CONSTRAINT fk_kyc_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE INDEX IF NOT EXISTS idx_kyc_customer ON kyc_cases(customer_id);

CREATE TABLE IF NOT EXISTS consents (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    type VARCHAR(64) NOT NULL,
    granted BOOLEAN NOT NULL,
    granted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_consents_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- KYC Cases table
CREATE TABLE IF NOT EXISTS kyc_cases (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    level VARCHAR(16) NOT NULL,
    status VARCHAR(16) NOT NULL,
    provider VARCHAR(64),
    provider_ref VARCHAR(128),
    risk_score INT,
    rejection_reason TEXT,
    submitted_at TIMESTAMPTZ,
    reviewed_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_kyc_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- KYC Documents table
CREATE TABLE IF NOT EXISTS kyc_documents (
    id UUID PRIMARY KEY,
    kyc_case_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    type VARCHAR(32) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    file_hash VARCHAR(64) NOT NULL,
    storage_id VARCHAR(255) NOT NULL,
    status VARCHAR(16) NOT NULL,
    rejection_reason TEXT,
    uploaded_at TIMESTAMPTZ,
    processed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_kyc_doc_case FOREIGN KEY (kyc_case_id) REFERENCES kyc_cases(id),
    CONSTRAINT fk_kyc_doc_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- Indexes for KYC tables
CREATE INDEX IF NOT EXISTS idx_kyc_cases_customer_id ON kyc_cases(customer_id);
CREATE INDEX IF NOT EXISTS idx_kyc_cases_status ON kyc_cases(status);
CREATE INDEX IF NOT EXISTS idx_kyc_cases_provider_ref ON kyc_cases(provider_ref);
CREATE INDEX IF NOT EXISTS idx_kyc_cases_created_at ON kyc_cases(created_at);

CREATE INDEX IF NOT EXISTS idx_kyc_documents_kyc_case_id ON kyc_documents(kyc_case_id);
CREATE INDEX IF NOT EXISTS idx_kyc_documents_customer_id ON kyc_documents(customer_id);
CREATE INDEX IF NOT EXISTS idx_kyc_documents_type ON kyc_documents(type);
CREATE INDEX IF NOT EXISTS idx_kyc_documents_status ON kyc_documents(status);
CREATE INDEX IF NOT EXISTS idx_kyc_documents_file_hash ON kyc_documents(file_hash);
