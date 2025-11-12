-- Account Service initial schema
CREATE TABLE IF NOT EXISTS accounts (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    account_number VARCHAR(20) UNIQUE NOT NULL,
    account_name VARCHAR(50) NOT NULL,
    type VARCHAR(16) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    currency CHAR(3) NOT NULL,
    balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    available_balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    daily_transfer_limit DECIMAL(19,2) NOT NULL,
    per_transaction_limit DECIMAL(19,2) NOT NULL,
    last_transaction_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes for accounts
CREATE INDEX IF NOT EXISTS idx_accounts_customer_id ON accounts(customer_id);
CREATE INDEX IF NOT EXISTS idx_accounts_customer_status ON accounts(customer_id, status);
CREATE INDEX IF NOT EXISTS idx_accounts_customer_type ON accounts(customer_id, type);
CREATE INDEX IF NOT EXISTS idx_accounts_account_number ON accounts(account_number);
CREATE INDEX IF NOT EXISTS idx_accounts_status ON accounts(status);
CREATE INDEX IF NOT EXISTS idx_accounts_currency ON accounts(currency);
CREATE INDEX IF NOT EXISTS idx_accounts_created_at ON accounts(created_at);