-- Payment Features Migration
-- External Bank Transfers, International Transfers, Recurring Payments, Templates, Bulk Payments, Limits

-- External Bank Transfers
CREATE TABLE external_transfers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    from_account_id UUID NOT NULL,
    to_bank_code VARCHAR(20) NOT NULL,
    to_bank_name VARCHAR(255) NOT NULL,
    to_account_number VARCHAR(50) NOT NULL,
    to_account_name VARCHAR(255) NOT NULL,
    to_routing_number VARCHAR(20),
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    description TEXT,
    reference_number VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    external_reference VARCHAR(100),
    processing_fee DECIMAL(10,2) DEFAULT 0.00,
    exchange_rate DECIMAL(10,6),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason TEXT
);

-- International Transfers (SWIFT/SEPA)
CREATE TABLE international_transfers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    from_account_id UUID NOT NULL,
    transfer_type VARCHAR(20) NOT NULL, -- SWIFT, SEPA, WIRE
    to_swift_code VARCHAR(11),
    to_iban VARCHAR(34),
    to_bank_name VARCHAR(255) NOT NULL,
    to_bank_address TEXT,
    to_account_number VARCHAR(50) NOT NULL,
    to_account_name VARCHAR(255) NOT NULL,
    to_address TEXT,
    to_country_code VARCHAR(2) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    description TEXT,
    reference_number VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    swift_message_id VARCHAR(50),
    correspondent_bank_swift VARCHAR(11),
    correspondent_bank_name VARCHAR(255),
    processing_fee DECIMAL(10,2) DEFAULT 0.00,
    exchange_rate DECIMAL(10,6),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason TEXT
);

-- Recurring Payments
CREATE TABLE recurring_payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    from_account_id UUID NOT NULL,
    to_account_number VARCHAR(50) NOT NULL,
    to_bank_code VARCHAR(20),
    to_bank_name VARCHAR(255),
    to_account_name VARCHAR(255) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    description TEXT,
    frequency VARCHAR(20) NOT NULL, -- DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
    day_of_month INTEGER, -- For monthly payments
    day_of_week INTEGER, -- For weekly payments (1=Monday, 7=Sunday)
    start_date DATE NOT NULL,
    end_date DATE,
    next_execution_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, PAUSED, CANCELLED, COMPLETED
    max_executions INTEGER,
    execution_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_executed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancellation_reason TEXT
);

-- Payment Templates
CREATE TABLE payment_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    template_name VARCHAR(255) NOT NULL,
    from_account_id UUID NOT NULL,
    to_account_number VARCHAR(50) NOT NULL,
    to_bank_code VARCHAR(20),
    to_bank_name VARCHAR(255),
    to_account_name VARCHAR(255) NOT NULL,
    amount DECIMAL(15,2),
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    description TEXT,
    is_favorite BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bulk Payments
CREATE TABLE bulk_payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    batch_name VARCHAR(255) NOT NULL,
    from_account_id UUID NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    total_recipients INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, PROCESSING, COMPLETED, FAILED, PARTIAL
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    completed_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason TEXT
);

-- Bulk Payment Items
CREATE TABLE bulk_payment_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bulk_payment_id UUID NOT NULL REFERENCES bulk_payments(id) ON DELETE CASCADE,
    recipient_name VARCHAR(255) NOT NULL,
    recipient_account_number VARCHAR(50) NOT NULL,
    recipient_bank_code VARCHAR(20),
    recipient_bank_name VARCHAR(255),
    amount DECIMAL(15,2) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, PROCESSING, COMPLETED, FAILED
    transfer_id UUID, -- Reference to actual transfer
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason TEXT
);

-- Payment Limits Management
CREATE TABLE payment_limits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    account_id UUID,
    limit_type VARCHAR(50) NOT NULL, -- DAILY_AMOUNT, MONTHLY_AMOUNT, PER_TRANSACTION, DAILY_COUNT, MONTHLY_COUNT
    limit_value DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    current_usage DECIMAL(15,2) DEFAULT 0.00,
    usage_period_start TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP
);

-- Payment Events (for tracking all payment types)
CREATE TABLE payment_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID NOT NULL,
    payment_type VARCHAR(20) NOT NULL, -- INTERNAL, EXTERNAL, INTERNATIONAL, RECURRING, BULK
    event_type VARCHAR(50) NOT NULL, -- CREATED, PROCESSING, COMPLETED, FAILED, CANCELLED
    event_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_external_transfers_customer_id ON external_transfers(customer_id);
CREATE INDEX idx_external_transfers_status ON external_transfers(status);
CREATE INDEX idx_external_transfers_created_at ON external_transfers(created_at);

CREATE INDEX idx_international_transfers_customer_id ON international_transfers(customer_id);
CREATE INDEX idx_international_transfers_status ON international_transfers(status);
CREATE INDEX idx_international_transfers_created_at ON international_transfers(created_at);

CREATE INDEX idx_recurring_payments_customer_id ON recurring_payments(customer_id);
CREATE INDEX idx_recurring_payments_status ON recurring_payments(status);
CREATE INDEX idx_recurring_payments_next_execution ON recurring_payments(next_execution_date);

CREATE INDEX idx_payment_templates_customer_id ON payment_templates(customer_id);
CREATE INDEX idx_payment_templates_favorite ON payment_templates(is_favorite);

CREATE INDEX idx_bulk_payments_customer_id ON bulk_payments(customer_id);
CREATE INDEX idx_bulk_payments_status ON bulk_payments(status);

CREATE INDEX idx_bulk_payment_items_bulk_id ON bulk_payment_items(bulk_payment_id);
CREATE INDEX idx_bulk_payment_items_status ON bulk_payment_items(status);

CREATE INDEX idx_payment_limits_customer_id ON payment_limits(customer_id);
CREATE INDEX idx_payment_limits_account_id ON payment_limits(account_id);
CREATE INDEX idx_payment_limits_type ON payment_limits(limit_type);
CREATE INDEX idx_payment_limits_active ON payment_limits(is_active);

CREATE INDEX idx_payment_events_payment_id ON payment_events(payment_id);
CREATE INDEX idx_payment_events_type ON payment_events(payment_type);
CREATE INDEX idx_payment_events_created_at ON payment_events(created_at);

