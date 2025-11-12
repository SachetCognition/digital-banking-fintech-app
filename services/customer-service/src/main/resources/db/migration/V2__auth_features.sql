-- Authentication & Authorization features migration

-- Add password and email verification fields to customers table
ALTER TABLE customers ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);
ALTER TABLE customers ADD COLUMN IF NOT EXISTS email_verified BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS phone_verified BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS mfa_enabled BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMPTZ;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS failed_login_attempts INT NOT NULL DEFAULT 0;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS locked_until TIMESTAMPTZ;

-- Email verification tokens table
CREATE TABLE IF NOT EXISTS email_verification_tokens (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    token VARCHAR(255) UNIQUE NOT NULL,
    type VARCHAR(32) NOT NULL, -- EMAIL_VERIFICATION, PASSWORD_RESET, MFA_VERIFICATION
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_email_token_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- MFA secrets and backup codes table
CREATE TABLE IF NOT EXISTS mfa_secrets (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    secret_key VARCHAR(255) NOT NULL,
    backup_codes TEXT[], -- Array of backup codes
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_mfa_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- User roles and permissions
CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY,
    name VARCHAR(64) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS permissions (
    id UUID PRIMARY KEY,
    name VARCHAR(64) UNIQUE NOT NULL,
    resource VARCHAR(64) NOT NULL,
    action VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES roles(id),
    CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id) REFERENCES permissions(id)
);

CREATE TABLE IF NOT EXISTS customer_roles (
    customer_id UUID NOT NULL,
    role_id UUID NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    assigned_by UUID,
    PRIMARY KEY (customer_id, role_id),
    CONSTRAINT fk_customer_role_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_customer_role_role FOREIGN KEY (role_id) REFERENCES roles(id),
    CONSTRAINT fk_customer_role_assigner FOREIGN KEY (assigned_by) REFERENCES customers(id)
);

-- User sessions table
CREATE TABLE IF NOT EXISTS user_sessions (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    session_token VARCHAR(255) UNIQUE NOT NULL,
    device_fingerprint VARCHAR(255),
    ip_address INET,
    user_agent TEXT,
    expires_at TIMESTAMPTZ NOT NULL,
    last_activity_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_session_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- Audit log for authentication events
CREATE TABLE IF NOT EXISTS auth_audit_log (
    id UUID PRIMARY KEY,
    customer_id UUID,
    event_type VARCHAR(64) NOT NULL,
    event_data JSONB,
    ip_address INET,
    user_agent TEXT,
    success BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_audit_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_customers_email_verified ON customers(email_verified);
CREATE INDEX IF NOT EXISTS idx_customers_mfa_enabled ON customers(mfa_enabled);
CREATE INDEX IF NOT EXISTS idx_customers_locked_until ON customers(locked_until);

CREATE INDEX IF NOT EXISTS idx_email_tokens_customer ON email_verification_tokens(customer_id);
CREATE INDEX IF NOT EXISTS idx_email_tokens_token ON email_verification_tokens(token);
CREATE INDEX IF NOT EXISTS idx_email_tokens_type ON email_verification_tokens(type);
CREATE INDEX IF NOT EXISTS idx_email_tokens_expires_at ON email_verification_tokens(expires_at);

CREATE INDEX IF NOT EXISTS idx_mfa_customer ON mfa_secrets(customer_id);

CREATE INDEX IF NOT EXISTS idx_role_permissions_role ON role_permissions(role_id);
CREATE INDEX IF NOT EXISTS idx_role_permissions_permission ON role_permissions(permission_id);

CREATE INDEX IF NOT EXISTS idx_customer_roles_customer ON customer_roles(customer_id);
CREATE INDEX IF NOT EXISTS idx_customer_roles_role ON customer_roles(role_id);

CREATE INDEX IF NOT EXISTS idx_user_sessions_customer ON user_sessions(customer_id);
CREATE INDEX IF NOT EXISTS idx_user_sessions_token ON user_sessions(session_token);
CREATE INDEX IF NOT EXISTS idx_user_sessions_expires_at ON user_sessions(expires_at);
CREATE INDEX IF NOT EXISTS idx_user_sessions_last_activity ON user_sessions(last_activity_at);

CREATE INDEX IF NOT EXISTS idx_auth_audit_customer ON auth_audit_log(customer_id);
CREATE INDEX IF NOT EXISTS idx_auth_audit_event_type ON auth_audit_log(event_type);
CREATE INDEX IF NOT EXISTS idx_auth_audit_created_at ON auth_audit_log(created_at);

-- Insert default roles and permissions
INSERT INTO roles (id, name, description) VALUES 
    ('550e8400-e29b-41d4-a716-446655440001', 'CUSTOMER', 'Regular customer with basic banking access'),
    ('550e8400-e29b-41d4-a716-446655440002', 'ADMIN', 'Administrator with full system access'),
    ('550e8400-e29b-41d4-a716-446655440003', 'SUPPORT', 'Customer support representative'),
    ('550e8400-e29b-41d4-a716-446655440004', 'AUDITOR', 'Audit and compliance access')
ON CONFLICT (id) DO NOTHING;

INSERT INTO permissions (id, name, resource, action) VALUES 
    -- Account permissions
    ('650e8400-e29b-41d4-a716-446655440001', 'ACCOUNT_READ', 'ACCOUNT', 'READ'),
    ('650e8400-e29b-41d4-a716-446655440002', 'ACCOUNT_CREATE', 'ACCOUNT', 'CREATE'),
    ('650e8400-e29b-41d4-a716-446655440003', 'ACCOUNT_UPDATE', 'ACCOUNT', 'UPDATE'),
    ('650e8400-e29b-41d4-a716-446655440004', 'ACCOUNT_DELETE', 'ACCOUNT', 'DELETE'),
    
    -- Transfer permissions
    ('650e8400-e29b-41d4-a716-446655440005', 'TRANSFER_READ', 'TRANSFER', 'READ'),
    ('650e8400-e29b-41d4-a716-446655440006', 'TRANSFER_CREATE', 'TRANSFER', 'CREATE'),
    ('650e8400-e29b-41d4-a716-446655440007', 'TRANSFER_APPROVE', 'TRANSFER', 'APPROVE'),
    
    -- Customer permissions
    ('650e8400-e29b-41d4-a716-446655440008', 'CUSTOMER_READ', 'CUSTOMER', 'READ'),
    ('650e8400-e29b-41d4-a716-446655440009', 'CUSTOMER_UPDATE', 'CUSTOMER', 'UPDATE'),
    ('650e8400-e29b-41d4-a716-446655440010', 'CUSTOMER_DELETE', 'CUSTOMER', 'DELETE'),
    
    -- Admin permissions
    ('650e8400-e29b-41d4-a716-446655440011', 'ADMIN_READ', 'ADMIN', 'READ'),
    ('650e8400-e29b-41d4-a716-446655440012', 'ADMIN_WRITE', 'ADMIN', 'WRITE'),
    ('650e8400-e29b-41d4-a716-446655440013', 'AUDIT_READ', 'AUDIT', 'READ')
ON CONFLICT (id) DO NOTHING;

-- Assign permissions to roles
INSERT INTO role_permissions (role_id, permission_id) VALUES 
    -- CUSTOMER role permissions
    ('550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440001'), -- ACCOUNT_READ
    ('550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440002'), -- ACCOUNT_CREATE
    ('550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440003'), -- ACCOUNT_UPDATE
    ('550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440005'), -- TRANSFER_READ
    ('550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440006'), -- TRANSFER_CREATE
    ('550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440008'), -- CUSTOMER_READ
    ('550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440009'), -- CUSTOMER_UPDATE
    
    -- ADMIN role permissions (all permissions)
    ('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440001'), -- ACCOUNT_READ
    ('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440002'), -- ACCOUNT_CREATE
    ('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440003'), -- ACCOUNT_UPDATE
    ('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440004'), -- ACCOUNT_DELETE
    ('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440005'), -- TRANSFER_READ
    ('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440006'), -- TRANSFER_CREATE
    ('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440007'), -- TRANSFER_APPROVE
    ('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440008'), -- CUSTOMER_READ
    ('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440009'), -- CUSTOMER_UPDATE
    ('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440010'), -- CUSTOMER_DELETE
    ('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440011'), -- ADMIN_READ
    ('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440012'), -- ADMIN_WRITE
    ('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440013'), -- AUDIT_READ
    
    -- SUPPORT role permissions
    ('550e8400-e29b-41d4-a716-446655440003', '650e8400-e29b-41d4-a716-446655440001'), -- ACCOUNT_READ
    ('550e8400-e29b-41d4-a716-446655440003', '650e8400-e29b-41d4-a716-446655440005'), -- TRANSFER_READ
    ('550e8400-e29b-41d4-a716-446655440003', '650e8400-e29b-41d4-a716-446655440008'), -- CUSTOMER_READ
    ('550e8400-e29b-41d4-a716-446655440003', '650e8400-e29b-41d4-a716-446655440009'), -- CUSTOMER_UPDATE
    
    -- AUDITOR role permissions
    ('550e8400-e29b-41d4-a716-446655440004', '650e8400-e29b-41d4-a716-446655440001'), -- ACCOUNT_READ
    ('550e8400-e29b-41d4-a716-446655440004', '650e8400-e29b-41d4-a716-446655440005'), -- TRANSFER_READ
    ('550e8400-e29b-41d4-a716-446655440004', '650e8400-e29b-41d4-a716-446655440008'), -- CUSTOMER_READ
    ('550e8400-e29b-41d4-a716-446655440004', '650e8400-e29b-41d4-a716-446655440013')  -- AUDIT_READ
ON CONFLICT (role_id, permission_id) DO NOTHING;

