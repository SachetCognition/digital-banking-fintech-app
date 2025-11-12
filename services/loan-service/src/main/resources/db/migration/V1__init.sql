-- Loan Applications
CREATE TABLE loan_applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    application_number VARCHAR(50) UNIQUE NOT NULL,
    loan_type VARCHAR(50) NOT NULL,
    requested_amount DECIMAL(15,2) NOT NULL,
    requested_term_months INTEGER NOT NULL,
    purpose VARCHAR(255),
    employment_status VARCHAR(50),
    annual_income DECIMAL(15,2),
    monthly_expenses DECIMAL(15,2),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    credit_score INTEGER,
    risk_level VARCHAR(50),
    interest_rate DECIMAL(5,2),
    approved_amount DECIMAL(15,2),
    approved_term_months INTEGER,
    rejection_reason TEXT,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP,
    approved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Loans
CREATE TABLE loans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    loan_application_id UUID REFERENCES loan_applications(id),
    loan_number VARCHAR(50) UNIQUE NOT NULL,
    loan_type VARCHAR(50) NOT NULL,
    principal_amount DECIMAL(15,2) NOT NULL,
    interest_rate DECIMAL(5,2) NOT NULL,
    term_months INTEGER NOT NULL,
    monthly_payment DECIMAL(15,2) NOT NULL,
    remaining_balance DECIMAL(15,2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    disbursement_date DATE,
    maturity_date DATE,
    next_payment_date DATE,
    grace_period_days INTEGER DEFAULT 15,
    late_fee_rate DECIMAL(5,2) DEFAULT 0.05,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Loan Payments
CREATE TABLE loan_payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    loan_id UUID NOT NULL REFERENCES loans(id),
    payment_number INTEGER NOT NULL,
    scheduled_date DATE NOT NULL,
    principal_amount DECIMAL(15,2) NOT NULL,
    interest_amount DECIMAL(15,2) NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    paid_amount DECIMAL(15,2) DEFAULT 0,
    paid_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    late_fee DECIMAL(15,2) DEFAULT 0,
    payment_method VARCHAR(50),
    reference_number VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Credit Lines
CREATE TABLE credit_lines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    credit_line_number VARCHAR(50) UNIQUE NOT NULL,
    credit_limit DECIMAL(15,2) NOT NULL,
    available_credit DECIMAL(15,2) NOT NULL,
    used_credit DECIMAL(15,2) DEFAULT 0,
    interest_rate DECIMAL(5,2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    credit_score INTEGER,
    risk_level VARCHAR(50),
    annual_fee DECIMAL(15,2) DEFAULT 0,
    minimum_payment_rate DECIMAL(5,2) DEFAULT 0.02,
    grace_period_days INTEGER DEFAULT 25,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Credit Transactions
CREATE TABLE credit_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    credit_line_id UUID NOT NULL REFERENCES credit_lines(id),
    transaction_type VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    description TEXT,
    merchant_name VARCHAR(255),
    merchant_category VARCHAR(100),
    reference_number VARCHAR(100),
    transaction_date TIMESTAMP NOT NULL,
    posted_date TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Credit Payments
CREATE TABLE credit_payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    credit_line_id UUID NOT NULL REFERENCES credit_lines(id),
    payment_amount DECIMAL(15,2) NOT NULL,
    payment_date DATE NOT NULL,
    payment_method VARCHAR(50),
    reference_number VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Interest Calculations
CREATE TABLE interest_calculations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    loan_id UUID REFERENCES loans(id),
    credit_line_id UUID REFERENCES credit_lines(id),
    calculation_date DATE NOT NULL,
    principal_balance DECIMAL(15,2) NOT NULL,
    interest_rate DECIMAL(5,2) NOT NULL,
    daily_interest DECIMAL(15,2) NOT NULL,
    monthly_interest DECIMAL(15,2) NOT NULL,
    accrued_interest DECIMAL(15,2) NOT NULL,
    calculation_method VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Credit Scores
CREATE TABLE credit_scores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    score INTEGER NOT NULL,
    score_type VARCHAR(50) NOT NULL,
    risk_level VARCHAR(50) NOT NULL,
    factors JSONB,
    calculated_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Loan Events (Audit Trail)
CREATE TABLE loan_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    loan_id UUID REFERENCES loans(id),
    credit_line_id UUID REFERENCES credit_lines(id),
    event_type VARCHAR(100) NOT NULL,
    event_data JSONB,
    user_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_loan_applications_customer_id ON loan_applications(customer_id);
CREATE INDEX idx_loan_applications_status ON loan_applications(status);
CREATE INDEX idx_loan_applications_submitted_at ON loan_applications(submitted_at);

CREATE INDEX idx_loans_customer_id ON loans(customer_id);
CREATE INDEX idx_loans_status ON loans(status);
CREATE INDEX idx_loans_next_payment_date ON loans(next_payment_date);

CREATE INDEX idx_loan_payments_loan_id ON loan_payments(loan_id);
CREATE INDEX idx_loan_payments_scheduled_date ON loan_payments(scheduled_date);
CREATE INDEX idx_loan_payments_status ON loan_payments(status);

CREATE INDEX idx_credit_lines_customer_id ON credit_lines(customer_id);
CREATE INDEX idx_credit_lines_status ON credit_lines(status);

CREATE INDEX idx_credit_transactions_credit_line_id ON credit_transactions(credit_line_id);
CREATE INDEX idx_credit_transactions_transaction_date ON credit_transactions(transaction_date);
CREATE INDEX idx_credit_transactions_status ON credit_transactions(status);

CREATE INDEX idx_credit_payments_credit_line_id ON credit_payments(credit_line_id);
CREATE INDEX idx_credit_payments_payment_date ON credit_payments(payment_date);

CREATE INDEX idx_interest_calculations_loan_id ON interest_calculations(loan_id);
CREATE INDEX idx_interest_calculations_credit_line_id ON interest_calculations(credit_line_id);
CREATE INDEX idx_interest_calculations_calculation_date ON interest_calculations(calculation_date);

CREATE INDEX idx_credit_scores_customer_id ON credit_scores(customer_id);
CREATE INDEX idx_credit_scores_calculated_at ON credit_scores(calculated_at);

CREATE INDEX idx_loan_events_loan_id ON loan_events(loan_id);
CREATE INDEX idx_loan_events_credit_line_id ON loan_events(credit_line_id);
CREATE INDEX idx_loan_events_event_type ON loan_events(event_type);
CREATE INDEX idx_loan_events_created_at ON loan_events(created_at);

