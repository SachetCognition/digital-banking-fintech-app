-- External Bank Transfers
CREATE TABLE external_bank_transfers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    from_account_id UUID NOT NULL,
    to_bank_account VARCHAR(50) NOT NULL,
    to_bank_name VARCHAR(100) NOT NULL,
    to_bank_code VARCHAR(20) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'initiated', -- initiated, processing, completed, failed, cancelled
    initiated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    external_transaction_id VARCHAR(100),
    processing_fee DECIMAL(10,2),
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- International Transfers
CREATE TABLE international_transfers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    from_account_id UUID NOT NULL,
    to_account_number VARCHAR(50) NOT NULL,
    to_bank_name VARCHAR(100) NOT NULL,
    to_bank_code VARCHAR(20) NOT NULL,
    to_country VARCHAR(3) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    from_currency VARCHAR(3) NOT NULL,
    to_currency VARCHAR(3) NOT NULL,
    converted_amount DECIMAL(15,2) NOT NULL,
    exchange_rate DECIMAL(10,6) NOT NULL,
    fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'initiated', -- initiated, processing, completed, failed, cancelled
    initiated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    external_transaction_id VARCHAR(100),
    swift_code VARCHAR(20),
    processing_fee DECIMAL(10,2),
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Exchange Rates
CREATE TABLE exchange_rates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    from_currency VARCHAR(3) NOT NULL,
    to_currency VARCHAR(3) NOT NULL,
    rate DECIMAL(10,6) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for Performance
CREATE INDEX idx_external_bank_transfers_customer_id ON external_bank_transfers(customer_id);
CREATE INDEX idx_external_bank_transfers_from_account_id ON external_bank_transfers(from_account_id);
CREATE INDEX idx_external_bank_transfers_status ON external_bank_transfers(status);
CREATE INDEX idx_external_bank_transfers_initiated_at ON external_bank_transfers(initiated_at);

CREATE INDEX idx_international_transfers_customer_id ON international_transfers(customer_id);
CREATE INDEX idx_international_transfers_from_account_id ON international_transfers(from_account_id);
CREATE INDEX idx_international_transfers_status ON international_transfers(status);
CREATE INDEX idx_international_transfers_initiated_at ON international_transfers(initiated_at);

CREATE INDEX idx_exchange_rates_currencies ON exchange_rates(from_currency, to_currency);
CREATE INDEX idx_exchange_rates_timestamp ON exchange_rates(timestamp);

