-- Fee Management
CREATE TABLE fee_schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fee_type VARCHAR(50) NOT NULL, -- transaction, atm, overdraft, monthly_maintenance, wire_transfer, international_transfer
    fee_name VARCHAR(100) NOT NULL,
    description TEXT,
    fee_structure VARCHAR(20) NOT NULL, -- fixed, percentage, tiered
    base_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    percentage_rate DECIMAL(5,4) NOT NULL DEFAULT 0.0000,
    minimum_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    maximum_fee DECIMAL(10,2) NOT NULL DEFAULT 999999.99,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    is_active BOOLEAN DEFAULT true,
    effective_date DATE NOT NULL,
    expiration_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE fee_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    account_id UUID NOT NULL,
    transaction_id UUID NOT NULL,
    fee_type VARCHAR(50) NOT NULL,
    fee_amount DECIMAL(10,2) NOT NULL,
    base_amount DECIMAL(10,2) NOT NULL,
    fee_rate DECIMAL(5,4),
    calculation_method VARCHAR(100),
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(20) NOT NULL DEFAULT 'pending', -- pending, collected, waived, refunded
    collected_at TIMESTAMP,
    waived_at TIMESTAMP,
    waived_reason TEXT,
    refunded_at TIMESTAMP,
    refunded_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Interest Calculations
CREATE TABLE interest_rates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_type VARCHAR(50) NOT NULL, -- savings, checking, cd_3_month, cd_6_month, cd_12_month, cd_24_month, cd_36_month, cd_60_month
    rate_name VARCHAR(100) NOT NULL,
    annual_percentage_yield DECIMAL(5,4) NOT NULL,
    interest_rate DECIMAL(5,4) NOT NULL,
    compounding_frequency VARCHAR(20) NOT NULL, -- daily, monthly, quarterly, annually
    minimum_balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    maximum_balance DECIMAL(15,2),
    tier_1_balance DECIMAL(15,2),
    tier_1_rate DECIMAL(5,4),
    tier_2_balance DECIMAL(15,2),
    tier_2_rate DECIMAL(5,4),
    tier_3_balance DECIMAL(15,2),
    tier_3_rate DECIMAL(5,4),
    is_active BOOLEAN DEFAULT true,
    effective_date DATE NOT NULL,
    expiration_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE interest_calculations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    account_id UUID NOT NULL,
    calculation_date DATE NOT NULL,
    account_balance DECIMAL(15,2) NOT NULL,
    interest_rate DECIMAL(5,4) NOT NULL,
    daily_interest DECIMAL(15,4) NOT NULL,
    monthly_interest DECIMAL(15,2) NOT NULL,
    year_to_date_interest DECIMAL(15,2) NOT NULL,
    compounding_frequency VARCHAR(20) NOT NULL,
    calculation_method VARCHAR(100),
    is_processed BOOLEAN DEFAULT false,
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE interest_payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    account_id UUID NOT NULL,
    payment_date DATE NOT NULL,
    interest_amount DECIMAL(15,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL, -- credit, compound
    transaction_id UUID,
    status VARCHAR(20) NOT NULL DEFAULT 'pending', -- pending, completed, failed
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tax Reporting
CREATE TABLE tax_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    tax_year INTEGER NOT NULL,
    document_type VARCHAR(20) NOT NULL, -- 1099_INT, 1099_DIV, 1099_B, 1099_MISC, 1042_S
    document_number VARCHAR(20) NOT NULL,
    total_interest DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_dividends DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_capital_gains DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_other_income DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    federal_tax_withheld DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    state_tax_withheld DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    document_status VARCHAR(20) NOT NULL DEFAULT 'draft', -- draft, generated, mailed, delivered
    generated_at TIMESTAMP,
    mailed_at TIMESTAMP,
    delivered_at TIMESTAMP,
    file_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tax_document_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tax_document_id UUID NOT NULL,
    account_id UUID NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    interest_income DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    dividend_income DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    capital_gains DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    other_income DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    federal_withholding DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    state_withholding DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tax_document_id) REFERENCES tax_documents(id)
);

-- Loyalty Program
CREATE TABLE loyalty_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    total_points BIGINT NOT NULL DEFAULT 0,
    available_points BIGINT NOT NULL DEFAULT 0,
    lifetime_points BIGINT NOT NULL DEFAULT 0,
    tier_level VARCHAR(20) NOT NULL DEFAULT 'bronze', -- bronze, silver, gold, platinum
    tier_points BIGINT NOT NULL DEFAULT 0,
    next_tier_points BIGINT NOT NULL DEFAULT 1000,
    enrollment_date DATE NOT NULL,
    last_activity_date DATE,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE loyalty_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    loyalty_account_id UUID NOT NULL,
    transaction_type VARCHAR(20) NOT NULL, -- earned, redeemed, expired, adjusted
    points_amount BIGINT NOT NULL,
    transaction_reference VARCHAR(100),
    description TEXT,
    expiration_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'pending', -- pending, completed, expired, cancelled
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (loyalty_account_id) REFERENCES loyalty_accounts(id)
);

CREATE TABLE loyalty_rewards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reward_name VARCHAR(100) NOT NULL,
    description TEXT,
    reward_type VARCHAR(20) NOT NULL, -- cash_back, gift_card, merchandise, travel, statement_credit
    points_required BIGINT NOT NULL,
    cash_value DECIMAL(10,2),
    is_active BOOLEAN DEFAULT true,
    start_date DATE NOT NULL,
    end_date DATE,
    max_redemptions INTEGER,
    current_redemptions INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE loyalty_redemptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    loyalty_account_id UUID NOT NULL,
    reward_id UUID NOT NULL,
    points_used BIGINT NOT NULL,
    cash_value DECIMAL(10,2),
    status VARCHAR(20) NOT NULL DEFAULT 'pending', -- pending, processed, completed, cancelled
    processed_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (loyalty_account_id) REFERENCES loyalty_accounts(id),
    FOREIGN KEY (reward_id) REFERENCES loyalty_rewards(id)
);

-- Referral System
CREATE TABLE referral_programs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    program_name VARCHAR(100) NOT NULL,
    description TEXT,
    referrer_bonus DECIMAL(10,2) NOT NULL,
    referee_bonus DECIMAL(10,2) NOT NULL,
    minimum_deposit DECIMAL(15,2) NOT NULL,
    maximum_referrals INTEGER,
    bonus_payment_delay INTEGER NOT NULL, -- days
    is_active BOOLEAN DEFAULT true,
    start_date DATE NOT NULL,
    end_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE referrals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    referrer_customer_id UUID NOT NULL,
    referee_customer_id UUID NOT NULL,
    referral_code VARCHAR(20) NOT NULL,
    program_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending', -- pending, qualified, paid, expired
    referee_deposit_amount DECIMAL(15,2),
    referrer_bonus_amount DECIMAL(10,2),
    referee_bonus_amount DECIMAL(10,2),
    qualification_date DATE,
    payment_date DATE,
    expiration_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (program_id) REFERENCES referral_programs(id)
);

CREATE TABLE referral_codes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    referral_code VARCHAR(20) NOT NULL UNIQUE,
    is_active BOOLEAN DEFAULT true,
    usage_count INTEGER DEFAULT 0,
    max_usage INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP
);

-- Business Metrics
CREATE TABLE business_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    metric_name VARCHAR(100) NOT NULL,
    metric_value DECIMAL(15,4) NOT NULL,
    metric_unit VARCHAR(20),
    metric_category VARCHAR(50) NOT NULL, -- fees, interest, loyalty, referral, revenue
    calculation_date DATE NOT NULL,
    period_type VARCHAR(20) NOT NULL, -- daily, monthly, quarterly, annually
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for Performance
CREATE INDEX idx_fee_transactions_customer_id ON fee_transactions(customer_id);
CREATE INDEX idx_fee_transactions_account_id ON fee_transactions(account_id);
CREATE INDEX idx_fee_transactions_transaction_id ON fee_transactions(transaction_id);
CREATE INDEX idx_fee_transactions_fee_type ON fee_transactions(fee_type);
CREATE INDEX idx_fee_transactions_status ON fee_transactions(status);
CREATE INDEX idx_fee_transactions_created_at ON fee_transactions(created_at);

CREATE INDEX idx_interest_calculations_customer_id ON interest_calculations(customer_id);
CREATE INDEX idx_interest_calculations_account_id ON interest_calculations(account_id);
CREATE INDEX idx_interest_calculations_calculation_date ON interest_calculations(calculation_date);
CREATE INDEX idx_interest_calculations_is_processed ON interest_calculations(is_processed);

CREATE INDEX idx_interest_payments_customer_id ON interest_payments(customer_id);
CREATE INDEX idx_interest_payments_account_id ON interest_payments(account_id);
CREATE INDEX idx_interest_payments_payment_date ON interest_payments(payment_date);
CREATE INDEX idx_interest_payments_status ON interest_payments(status);

CREATE INDEX idx_tax_documents_customer_id ON tax_documents(customer_id);
CREATE INDEX idx_tax_documents_tax_year ON tax_documents(tax_year);
CREATE INDEX idx_tax_documents_document_type ON tax_documents(document_type);
CREATE INDEX idx_tax_documents_document_status ON tax_documents(document_status);

CREATE INDEX idx_loyalty_accounts_customer_id ON loyalty_accounts(customer_id);
CREATE INDEX idx_loyalty_accounts_account_number ON loyalty_accounts(account_number);
CREATE INDEX idx_loyalty_accounts_tier_level ON loyalty_accounts(tier_level);
CREATE INDEX idx_loyalty_accounts_is_active ON loyalty_accounts(is_active);

CREATE INDEX idx_loyalty_transactions_loyalty_account_id ON loyalty_transactions(loyalty_account_id);
CREATE INDEX idx_loyalty_transactions_transaction_type ON loyalty_transactions(transaction_type);
CREATE INDEX idx_loyalty_transactions_status ON loyalty_transactions(status);
CREATE INDEX idx_loyalty_transactions_created_at ON loyalty_transactions(created_at);

CREATE INDEX idx_loyalty_redemptions_loyalty_account_id ON loyalty_redemptions(loyalty_account_id);
CREATE INDEX idx_loyalty_redemptions_reward_id ON loyalty_redemptions(reward_id);
CREATE INDEX idx_loyalty_redemptions_status ON loyalty_redemptions(status);

CREATE INDEX idx_referrals_referrer_customer_id ON referrals(referrer_customer_id);
CREATE INDEX idx_referrals_referee_customer_id ON referrals(referee_customer_id);
CREATE INDEX idx_referrals_referral_code ON referrals(referral_code);
CREATE INDEX idx_referrals_status ON referrals(status);
CREATE INDEX idx_referrals_created_at ON referrals(created_at);

CREATE INDEX idx_referral_codes_customer_id ON referral_codes(customer_id);
CREATE INDEX idx_referral_codes_referral_code ON referral_codes(referral_code);
CREATE INDEX idx_referral_codes_is_active ON referral_codes(is_active);

CREATE INDEX idx_business_metrics_metric_name ON business_metrics(metric_name);
CREATE INDEX idx_business_metrics_metric_category ON business_metrics(metric_category);
CREATE INDEX idx_business_metrics_calculation_date ON business_metrics(calculation_date);
CREATE INDEX idx_business_metrics_period_type ON business_metrics(period_type);

-- Views for common queries
CREATE VIEW fee_summary AS
SELECT 
    fee_type,
    COUNT(*) as total_fees,
    SUM(fee_amount) as total_amount,
    AVG(fee_amount) as average_fee,
    COUNT(CASE WHEN status = 'collected' THEN 1 END) as collected_fees,
    COUNT(CASE WHEN status = 'waived' THEN 1 END) as waived_fees,
    COUNT(CASE WHEN status = 'refunded' THEN 1 END) as refunded_fees
FROM fee_transactions
WHERE created_at >= NOW() - INTERVAL '30 days'
GROUP BY fee_type;

CREATE VIEW interest_summary AS
SELECT 
    account_type,
    COUNT(*) as total_calculations,
    SUM(monthly_interest) as total_interest,
    AVG(monthly_interest) as average_interest,
    MAX(interest_rate) as max_rate,
    MIN(interest_rate) as min_rate
FROM interest_calculations
WHERE calculation_date >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY account_type;

CREATE VIEW loyalty_summary AS
SELECT 
    tier_level,
    COUNT(*) as total_accounts,
    SUM(total_points) as total_points,
    SUM(available_points) as available_points,
    AVG(total_points) as average_points
FROM loyalty_accounts
WHERE is_active = true
GROUP BY tier_level;

CREATE VIEW referral_summary AS
SELECT 
    status,
    COUNT(*) as total_referrals,
    SUM(referrer_bonus_amount) as total_referrer_bonus,
    SUM(referee_bonus_amount) as total_referee_bonus,
    AVG(referee_deposit_amount) as average_deposit
FROM referrals
WHERE created_at >= NOW() - INTERVAL '30 days'
GROUP BY status;

