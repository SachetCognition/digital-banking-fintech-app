CREATE TABLE interest_accruals (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    amount DECIMAL(15, 8) NOT NULL,
    rate DECIMAL(10, 8) NOT NULL,
    posting_date DATE NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_interest_accruals_account_id ON interest_accruals(account_id);
CREATE INDEX idx_interest_accruals_posting_date ON interest_accruals(posting_date);
