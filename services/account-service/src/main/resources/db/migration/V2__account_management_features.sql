-- Account Management Features Migration

-- Add new fields to accounts table
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS account_name VARCHAR(50) NOT NULL DEFAULT 'My Account';
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS paperless_statements BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS email_notifications BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS preferred_language CHAR(2) NOT NULL DEFAULT 'EN';
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS kyc_required BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS kyc_level VARCHAR(16);
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS kyc_status VARCHAR(16) DEFAULT 'NOT_REQUIRED';
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS opening_status VARCHAR(16) NOT NULL DEFAULT 'PENDING';
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS opening_reason TEXT;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS closure_reason TEXT;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS closure_requested_at TIMESTAMPTZ;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS closure_approved_at TIMESTAMPTZ;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS closure_approved_by UUID;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS interest_rate DECIMAL(5,4) NOT NULL DEFAULT 0.0000;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS fee_rate DECIMAL(5,4) NOT NULL DEFAULT 0.0000;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS minimum_balance DECIMAL(19,2) NOT NULL DEFAULT 0.00;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS maximum_balance DECIMAL(19,2);
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS monthly_fee DECIMAL(19,2) NOT NULL DEFAULT 0.00;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS overdraft_limit DECIMAL(19,2) NOT NULL DEFAULT 0.00;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS last_interest_calculation TIMESTAMPTZ;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS next_interest_calculation TIMESTAMPTZ;

-- Joint account holders table
CREATE TABLE IF NOT EXISTS joint_account_holders (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    role VARCHAR(32) NOT NULL, -- PRIMARY_OWNER, JOINT_OWNER, AUTHORIZED_USER, BENEFICIARY
    is_primary BOOLEAN NOT NULL DEFAULT false,
    added_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    approved_at TIMESTAMPTZ,
    approved_by UUID,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, REMOVED
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_joint_account FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT fk_joint_customer FOREIGN KEY (customer_id) REFERENCES accounts(customer_id)
);

-- Account opening workflow table
CREATE TABLE IF NOT EXISTS account_opening_cases (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    case_type VARCHAR(32) NOT NULL, -- NEW_ACCOUNT, JOINT_ACCOUNT, ACCOUNT_UPGRADE
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING', -- PENDING, UNDER_REVIEW, APPROVED, REJECTED, COMPLETED
    priority VARCHAR(16) NOT NULL DEFAULT 'NORMAL', -- LOW, NORMAL, HIGH, URGENT
    assigned_to UUID,
    kyc_case_id UUID,
    required_documents TEXT[],
    submitted_documents TEXT[],
    review_notes TEXT,
    approval_notes TEXT,
    rejection_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    CONSTRAINT fk_opening_account FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT fk_opening_customer FOREIGN KEY (customer_id) REFERENCES accounts(customer_id)
);

-- Account statements table
CREATE TABLE IF NOT EXISTS account_statements (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    statement_id VARCHAR(64) UNIQUE NOT NULL,
    statement_date DATE NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    opening_balance DECIMAL(19,2) NOT NULL,
    closing_balance DECIMAL(19,2) NOT NULL,
    total_debits DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    total_credits DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    currency CHAR(3) NOT NULL,
    generated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    generated_by UUID,
    file_path VARCHAR(500),
    email_sent BOOLEAN NOT NULL DEFAULT false,
    email_sent_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_statement_account FOREIGN KEY (account_id) REFERENCES accounts(id)
);

-- Account closure requests table
CREATE TABLE IF NOT EXISTS account_closure_requests (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    requested_by UUID NOT NULL,
    closure_reason VARCHAR(255) NOT NULL,
    transfer_account_id UUID,
    comments TEXT,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, COMPLETED
    approved_by UUID,
    approved_at TIMESTAMPTZ,
    rejection_reason TEXT,
    final_balance DECIMAL(19,2),
    transfer_amount DECIMAL(19,2),
    final_statement_generated BOOLEAN NOT NULL DEFAULT false,
    final_statement_sent BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    CONSTRAINT fk_closure_account FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT fk_closure_requested_by FOREIGN KEY (requested_by) REFERENCES accounts(customer_id),
    CONSTRAINT fk_closure_transfer_account FOREIGN KEY (transfer_account_id) REFERENCES accounts(id)
);

-- Account preferences table
CREATE TABLE IF NOT EXISTS account_preferences (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    preference_type VARCHAR(64) NOT NULL,
    preference_value TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_preferences_account FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT fk_preferences_customer FOREIGN KEY (customer_id) REFERENCES accounts(customer_id),
    UNIQUE(account_id, customer_id, preference_type)
);

-- Account alerts table
CREATE TABLE IF NOT EXISTS account_alerts (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    alert_type VARCHAR(64) NOT NULL, -- LOW_BALANCE, HIGH_TRANSACTION, SUSPICIOUS_ACTIVITY, etc.
    threshold_value DECIMAL(19,2),
    is_active BOOLEAN NOT NULL DEFAULT true,
    last_triggered TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_alerts_account FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT fk_alerts_customer FOREIGN KEY (customer_id) REFERENCES accounts(customer_id)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_accounts_opening_status ON accounts(opening_status);
CREATE INDEX IF NOT EXISTS idx_accounts_kyc_status ON accounts(kyc_status);
CREATE INDEX IF NOT EXISTS idx_accounts_closure_requested ON accounts(closure_requested_at);
CREATE INDEX IF NOT EXISTS idx_accounts_interest_calculation ON accounts(next_interest_calculation);

CREATE INDEX IF NOT EXISTS idx_joint_holders_account ON joint_account_holders(account_id);
CREATE INDEX IF NOT EXISTS idx_joint_holders_customer ON joint_account_holders(customer_id);
CREATE INDEX IF NOT EXISTS idx_joint_holders_status ON joint_account_holders(status);
CREATE INDEX IF NOT EXISTS idx_joint_holders_role ON joint_account_holders(role);

CREATE INDEX IF NOT EXISTS idx_opening_cases_account ON account_opening_cases(account_id);
CREATE INDEX IF NOT EXISTS idx_opening_cases_customer ON account_opening_cases(customer_id);
CREATE INDEX IF NOT EXISTS idx_opening_cases_status ON account_opening_cases(status);
CREATE INDEX IF NOT EXISTS idx_opening_cases_priority ON account_opening_cases(priority);
CREATE INDEX IF NOT EXISTS idx_opening_cases_assigned ON account_opening_cases(assigned_to);

CREATE INDEX IF NOT EXISTS idx_statements_account ON account_statements(account_id);
CREATE INDEX IF NOT EXISTS idx_statements_date ON account_statements(statement_date);
CREATE INDEX IF NOT EXISTS idx_statements_period ON account_statements(period_start, period_end);
CREATE INDEX IF NOT EXISTS idx_statements_generated ON account_statements(generated_at);

CREATE INDEX IF NOT EXISTS idx_closure_requests_account ON account_closure_requests(account_id);
CREATE INDEX IF NOT EXISTS idx_closure_requests_status ON account_closure_requests(status);
CREATE INDEX IF NOT EXISTS idx_closure_requests_created ON account_closure_requests(created_at);

CREATE INDEX IF NOT EXISTS idx_preferences_account ON account_preferences(account_id);
CREATE INDEX IF NOT EXISTS idx_preferences_customer ON account_preferences(customer_id);
CREATE INDEX IF NOT EXISTS idx_preferences_type ON account_preferences(preference_type);

CREATE INDEX IF NOT EXISTS idx_alerts_account ON account_alerts(account_id);
CREATE INDEX IF NOT EXISTS idx_alerts_customer ON account_alerts(customer_id);
CREATE INDEX IF NOT EXISTS idx_alerts_type ON account_alerts(alert_type);
CREATE INDEX IF NOT EXISTS idx_alerts_active ON account_alerts(is_active);

-- Update existing accounts with default values
UPDATE accounts SET 
    account_name = 'My Account',
    paperless_statements = true,
    email_notifications = true,
    preferred_language = 'EN',
    kyc_required = false,
    kyc_status = 'NOT_REQUIRED',
    opening_status = 'COMPLETED',
    interest_rate = 0.0000,
    fee_rate = 0.0000,
    minimum_balance = 0.00,
    monthly_fee = 0.00,
    overdraft_limit = 0.00
WHERE account_name IS NULL;

