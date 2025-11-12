-- Card Management Migration
-- Virtual Cards, Card Controls, Transactions, Replacement, PIN Management

-- Virtual Cards
CREATE TABLE virtual_cards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    account_id UUID NOT NULL,
    card_number_encrypted VARCHAR(255) NOT NULL,
    card_number_masked VARCHAR(19) NOT NULL, -- 1234-****-****-1234
    card_holder_name VARCHAR(255) NOT NULL,
    expiry_month INTEGER NOT NULL CHECK (expiry_month >= 1 AND expiry_month <= 12),
    expiry_year INTEGER NOT NULL,
    cvv_encrypted VARCHAR(255) NOT NULL,
    card_type VARCHAR(20) NOT NULL, -- VIRTUAL, PHYSICAL
    card_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, BLOCKED, SUSPENDED, EXPIRED, CANCELLED
    is_primary BOOLEAN DEFAULT FALSE,
    spending_limit DECIMAL(15,2),
    daily_limit DECIMAL(15,2),
    monthly_limit DECIMAL(15,2),
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activated_at TIMESTAMP,
    blocked_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    expiry_date DATE NOT NULL,
    last_used_at TIMESTAMP
);

-- Card Controls
CREATE TABLE card_controls (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    card_id UUID NOT NULL REFERENCES virtual_cards(id) ON DELETE CASCADE,
    control_type VARCHAR(50) NOT NULL, -- SPENDING_LIMIT, DAILY_LIMIT, MONTHLY_LIMIT, MERCHANT_BLOCK, COUNTRY_BLOCK, ATM_BLOCK, ONLINE_BLOCK
    control_value VARCHAR(255), -- JSON for complex controls
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP
);

-- Card Transactions
CREATE TABLE card_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    card_id UUID NOT NULL REFERENCES virtual_cards(id) ON DELETE CASCADE,
    transaction_id VARCHAR(100) NOT NULL, -- External transaction ID
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL, -- PURCHASE, ATM_WITHDRAWAL, REFUND, CHARGEBACK, FEE
    merchant_name VARCHAR(255),
    merchant_category_code VARCHAR(4),
    merchant_country VARCHAR(2),
    transaction_date TIMESTAMP NOT NULL,
    settlement_date TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, DECLINED, REFUNDED, CHARGEBACK
    description TEXT,
    reference_number VARCHAR(100),
    authorization_code VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Card Replacement
CREATE TABLE card_replacements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    original_card_id UUID NOT NULL REFERENCES virtual_cards(id) ON DELETE CASCADE,
    replacement_card_id UUID REFERENCES virtual_cards(id),
    replacement_reason VARCHAR(50) NOT NULL, -- LOST, STOLEN, DAMAGED, EXPIRED, CUSTOMER_REQUEST
    replacement_status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, PROCESSING, COMPLETED, FAILED
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    completed_at TIMESTAMP,
    delivery_method VARCHAR(20), -- DIGITAL, PHYSICAL
    delivery_address TEXT,
    tracking_number VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- PIN Management
CREATE TABLE card_pins (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    card_id UUID NOT NULL REFERENCES virtual_cards(id) ON DELETE CASCADE,
    pin_hash VARCHAR(255) NOT NULL,
    pin_attempts INTEGER DEFAULT 0,
    is_locked BOOLEAN DEFAULT FALSE,
    locked_until TIMESTAMP,
    last_changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Card Spending Limits (Materialized)
CREATE TABLE card_spending_limits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    card_id UUID NOT NULL REFERENCES virtual_cards(id) ON DELETE CASCADE,
    limit_type VARCHAR(20) NOT NULL, -- DAILY, MONTHLY, YEARLY, PER_TRANSACTION
    limit_amount DECIMAL(15,2) NOT NULL,
    current_usage DECIMAL(15,2) DEFAULT 0.00,
    usage_period_start TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Card Events (Audit Trail)
CREATE TABLE card_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    card_id UUID NOT NULL REFERENCES virtual_cards(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL, -- CREATED, ACTIVATED, BLOCKED, UNBLOCKED, CANCELLED, REPLACED, PIN_CHANGED, LIMIT_CHANGED
    event_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_virtual_cards_customer_id ON virtual_cards(customer_id);
CREATE INDEX idx_virtual_cards_account_id ON virtual_cards(account_id);
CREATE INDEX idx_virtual_cards_status ON virtual_cards(card_status);
CREATE INDEX idx_virtual_cards_created_at ON virtual_cards(created_at);
CREATE INDEX idx_virtual_cards_expiry_date ON virtual_cards(expiry_date);

CREATE INDEX idx_card_controls_card_id ON card_controls(card_id);
CREATE INDEX idx_card_controls_type ON card_controls(control_type);
CREATE INDEX idx_card_controls_active ON card_controls(is_active);

CREATE INDEX idx_card_transactions_card_id ON card_transactions(card_id);
CREATE INDEX idx_card_transactions_transaction_id ON card_transactions(transaction_id);
CREATE INDEX idx_card_transactions_date ON card_transactions(transaction_date);
CREATE INDEX idx_card_transactions_status ON card_transactions(status);
CREATE INDEX idx_card_transactions_merchant ON card_transactions(merchant_name);

CREATE INDEX idx_card_replacements_original ON card_replacements(original_card_id);
CREATE INDEX idx_card_replacements_replacement ON card_replacements(replacement_card_id);
CREATE INDEX idx_card_replacements_status ON card_replacements(replacement_status);

CREATE INDEX idx_card_pins_card_id ON card_pins(card_id);
CREATE INDEX idx_card_pins_locked ON card_pins(is_locked);

CREATE INDEX idx_card_spending_limits_card_id ON card_spending_limits(card_id);
CREATE INDEX idx_card_spending_limits_type ON card_spending_limits(limit_type);
CREATE INDEX idx_card_spending_limits_active ON card_spending_limits(is_active);

CREATE INDEX idx_card_events_card_id ON card_events(card_id);
CREATE INDEX idx_card_events_type ON card_events(event_type);
CREATE INDEX idx_card_events_created_at ON card_events(created_at);

