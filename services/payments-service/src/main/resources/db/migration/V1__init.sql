-- Payments Service initial schema

-- Transfers table
CREATE TABLE IF NOT EXISTS transfers (
    id UUID PRIMARY KEY,
    payer_account_id UUID NOT NULL,
    payee_account_id UUID NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency CHAR(3) NOT NULL,
    status VARCHAR(16) NOT NULL,
    description VARCHAR(255),
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    failure_reason TEXT,
    current_step VARCHAR(16),
    last_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Reservations table
CREATE TABLE IF NOT EXISTS reservations (
    id UUID PRIMARY KEY,
    transfer_id UUID NOT NULL,
    account_id UUID NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency CHAR(3) NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    released_at TIMESTAMPTZ,
    CONSTRAINT fk_reservation_transfer FOREIGN KEY (transfer_id) REFERENCES transfers(id)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_transfers_payer_account ON transfers(payer_account_id);
CREATE INDEX IF NOT EXISTS idx_transfers_payee_account ON transfers(payee_account_id);
CREATE INDEX IF NOT EXISTS idx_transfers_status ON transfers(status);
CREATE INDEX IF NOT EXISTS idx_transfers_created_at ON transfers(created_at);
CREATE INDEX IF NOT EXISTS idx_transfers_idempotency_key ON transfers(idempotency_key);

CREATE INDEX IF NOT EXISTS idx_reservations_transfer_id ON reservations(transfer_id);
CREATE INDEX IF NOT EXISTS idx_reservations_account_id ON reservations(account_id);
CREATE INDEX IF NOT EXISTS idx_reservations_status ON reservations(status);
CREATE INDEX IF NOT EXISTS idx_reservations_created_at ON reservations(created_at);

-- Transfer events table for audit trail
CREATE TABLE IF NOT EXISTS transfer_events (
    id UUID PRIMARY KEY,
    transfer_id UUID NOT NULL,
    event_type VARCHAR(32) NOT NULL,
    event_data JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_event_transfer FOREIGN KEY (transfer_id) REFERENCES transfers(id)
);

CREATE INDEX IF NOT EXISTS idx_transfer_events_transfer_id ON transfer_events(transfer_id);
CREATE INDEX IF NOT EXISTS idx_transfer_events_event_type ON transfer_events(event_type);
CREATE INDEX IF NOT EXISTS idx_transfer_events_created_at ON transfer_events(created_at);

-- Beneficiaries table
CREATE TABLE IF NOT EXISTS beneficiaries (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    bank_code VARCHAR(20) NOT NULL,
    bank_name VARCHAR(255) NOT NULL,
    currency CHAR(3) NOT NULL,
    type VARCHAR(16) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes for beneficiaries
CREATE INDEX IF NOT EXISTS idx_beneficiaries_customer_id ON beneficiaries(customer_id);
CREATE INDEX IF NOT EXISTS idx_beneficiaries_customer_status ON beneficiaries(customer_id, status);
CREATE INDEX IF NOT EXISTS idx_beneficiaries_customer_type_status ON beneficiaries(customer_id, type, status);
CREATE INDEX IF NOT EXISTS idx_beneficiaries_account_bank ON beneficiaries(customer_id, account_number, bank_code);
CREATE INDEX IF NOT EXISTS idx_beneficiaries_created_at ON beneficiaries(created_at);
