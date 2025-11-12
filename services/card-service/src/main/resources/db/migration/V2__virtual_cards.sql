-- Virtual Cards
CREATE TABLE virtual_cards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    account_id UUID NOT NULL,
    card_number VARCHAR(16) NOT NULL,
    cvv VARCHAR(3) NOT NULL,
    expiry_date VARCHAR(5) NOT NULL, -- MM/YY format
    card_token VARCHAR(100) NOT NULL UNIQUE,
    card_type VARCHAR(20) NOT NULL, -- visa, mastercard, amex
    card_name VARCHAR(100) NOT NULL,
    spending_limit DECIMAL(15,2) NOT NULL,
    daily_limit DECIMAL(15,2) NOT NULL,
    monthly_limit DECIMAL(15,2) NOT NULL,
    international_enabled BOOLEAN DEFAULT true,
    online_enabled BOOLEAN DEFAULT true,
    atm_enabled BOOLEAN DEFAULT false,
    contactless_enabled BOOLEAN DEFAULT true,
    status VARCHAR(20) NOT NULL DEFAULT 'active', -- active, blocked, expired, cancelled
    issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Card Transactions
CREATE TABLE card_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    card_id UUID NOT NULL,
    transaction_date TIMESTAMP NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    merchant_name VARCHAR(100),
    merchant_category VARCHAR(50),
    transaction_type VARCHAR(20) NOT NULL, -- online, pos, atm, contactless
    status VARCHAR(20) NOT NULL DEFAULT 'pending', -- pending, completed, failed, declined
    description TEXT,
    external_transaction_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (card_id) REFERENCES virtual_cards(id)
);

-- Card Control Changes
CREATE TABLE card_control_changes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    card_id UUID NOT NULL,
    daily_limit DECIMAL(15,2),
    monthly_limit DECIMAL(15,2),
    international_enabled BOOLEAN,
    online_enabled BOOLEAN,
    atm_enabled BOOLEAN,
    contactless_enabled BOOLEAN,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (card_id) REFERENCES virtual_cards(id)
);

-- Card Blocks
CREATE TABLE card_blocks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    card_id UUID NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'blocking', -- blocking, blocked, failed
    blocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (card_id) REFERENCES virtual_cards(id)
);

-- Card Unblocks
CREATE TABLE card_unblocks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    card_id UUID NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'unblocking', -- unblocking, unblocked, failed
    unblocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (card_id) REFERENCES virtual_cards(id)
);

-- Indexes for Performance
CREATE INDEX idx_virtual_cards_customer_id ON virtual_cards(customer_id);
CREATE INDEX idx_virtual_cards_account_id ON virtual_cards(account_id);
CREATE INDEX idx_virtual_cards_card_token ON virtual_cards(card_token);
CREATE INDEX idx_virtual_cards_status ON virtual_cards(status);
CREATE INDEX idx_virtual_cards_issued_at ON virtual_cards(issued_at);

CREATE INDEX idx_card_transactions_card_id ON card_transactions(card_id);
CREATE INDEX idx_card_transactions_transaction_date ON card_transactions(transaction_date);
CREATE INDEX idx_card_transactions_status ON card_transactions(status);
CREATE INDEX idx_card_transactions_merchant_name ON card_transactions(merchant_name);

CREATE INDEX idx_card_control_changes_card_id ON card_control_changes(card_id);
CREATE INDEX idx_card_control_changes_updated_at ON card_control_changes(updated_at);

CREATE INDEX idx_card_blocks_card_id ON card_blocks(card_id);
CREATE INDEX idx_card_blocks_status ON card_blocks(status);

CREATE INDEX idx_card_unblocks_card_id ON card_unblocks(card_id);
CREATE INDEX idx_card_unblocks_status ON card_unblocks(status);

