CREATE TABLE disputes (
    id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    amount DECIMAL(15, 2) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_disputes_customer_id ON disputes(customer_id);
CREATE INDEX idx_disputes_transaction_id ON disputes(transaction_id);
CREATE INDEX idx_disputes_status ON disputes(status);
