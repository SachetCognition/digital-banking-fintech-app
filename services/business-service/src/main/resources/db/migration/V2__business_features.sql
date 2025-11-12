-- This migration adds the business features tables that were defined in V1__init.sql
-- The tables are already defined in V1, this is just a placeholder for any additional business features

-- Add any additional business feature tables here if needed
-- The main business tables are already defined in V1__init.sql:
-- - fee_schedules, fee_transactions
-- - interest_rates, interest_calculations, interest_payments  
-- - tax_documents, tax_document_items
-- - loyalty_accounts, loyalty_transactions, loyalty_rewards, loyalty_redemptions
-- - referral_programs, referrals, referral_codes
-- - business_metrics

-- Add indexes for better performance if not already present
CREATE INDEX IF NOT EXISTS idx_fee_transactions_customer_id ON fee_transactions(customer_id);
CREATE INDEX IF NOT EXISTS idx_fee_transactions_account_id ON fee_transactions(account_id);
CREATE INDEX IF NOT EXISTS idx_fee_transactions_transaction_id ON fee_transactions(transaction_id);
CREATE INDEX IF NOT EXISTS idx_fee_transactions_fee_type ON fee_transactions(fee_type);
CREATE INDEX IF NOT EXISTS idx_fee_transactions_status ON fee_transactions(status);
CREATE INDEX IF NOT EXISTS idx_fee_transactions_created_at ON fee_transactions(created_at);

CREATE INDEX IF NOT EXISTS idx_interest_calculations_customer_id ON interest_calculations(customer_id);
CREATE INDEX IF NOT EXISTS idx_interest_calculations_account_id ON interest_calculations(account_id);
CREATE INDEX IF NOT EXISTS idx_interest_calculations_calculation_date ON interest_calculations(calculation_date);
CREATE INDEX IF NOT EXISTS idx_interest_calculations_is_processed ON interest_calculations(is_processed);

CREATE INDEX IF NOT EXISTS idx_interest_payments_customer_id ON interest_payments(customer_id);
CREATE INDEX IF NOT EXISTS idx_interest_payments_account_id ON interest_payments(account_id);
CREATE INDEX IF NOT EXISTS idx_interest_payments_payment_date ON interest_payments(payment_date);
CREATE INDEX IF NOT EXISTS idx_interest_payments_status ON interest_payments(status);

CREATE INDEX IF NOT EXISTS idx_tax_documents_customer_id ON tax_documents(customer_id);
CREATE INDEX IF NOT EXISTS idx_tax_documents_tax_year ON tax_documents(tax_year);
CREATE INDEX IF NOT EXISTS idx_tax_documents_document_type ON tax_documents(document_type);
CREATE INDEX IF NOT EXISTS idx_tax_documents_document_status ON tax_documents(document_status);

CREATE INDEX IF NOT EXISTS idx_loyalty_accounts_customer_id ON loyalty_accounts(customer_id);
CREATE INDEX IF NOT EXISTS idx_loyalty_accounts_account_number ON loyalty_accounts(account_number);
CREATE INDEX IF NOT EXISTS idx_loyalty_accounts_tier_level ON loyalty_accounts(tier_level);
CREATE INDEX IF NOT EXISTS idx_loyalty_accounts_is_active ON loyalty_accounts(is_active);

CREATE INDEX IF NOT EXISTS idx_loyalty_transactions_loyalty_account_id ON loyalty_transactions(loyalty_account_id);
CREATE INDEX IF NOT EXISTS idx_loyalty_transactions_transaction_type ON loyalty_transactions(transaction_type);
CREATE INDEX IF NOT EXISTS idx_loyalty_transactions_status ON loyalty_transactions(status);
CREATE INDEX IF NOT EXISTS idx_loyalty_transactions_created_at ON loyalty_transactions(created_at);

CREATE INDEX IF NOT EXISTS idx_loyalty_redemptions_loyalty_account_id ON loyalty_redemptions(loyalty_account_id);
CREATE INDEX IF NOT EXISTS idx_loyalty_redemptions_reward_id ON loyalty_redemptions(reward_id);
CREATE INDEX IF NOT EXISTS idx_loyalty_redemptions_status ON loyalty_redemptions(status);

CREATE INDEX IF NOT EXISTS idx_referrals_referrer_customer_id ON referrals(referrer_customer_id);
CREATE INDEX IF NOT EXISTS idx_referrals_referee_customer_id ON referrals(referee_customer_id);
CREATE INDEX IF NOT EXISTS idx_referrals_referral_code ON referrals(referral_code);
CREATE INDEX IF NOT EXISTS idx_referrals_status ON referrals(status);
CREATE INDEX IF NOT EXISTS idx_referrals_created_at ON referrals(created_at);

CREATE INDEX IF NOT EXISTS idx_referral_codes_customer_id ON referral_codes(customer_id);
CREATE INDEX IF NOT EXISTS idx_referral_codes_referral_code ON referral_codes(referral_code);
CREATE INDEX IF NOT EXISTS idx_referral_codes_is_active ON referral_codes(is_active);

CREATE INDEX IF NOT EXISTS idx_business_metrics_metric_name ON business_metrics(metric_name);
CREATE INDEX IF NOT EXISTS idx_business_metrics_metric_category ON business_metrics(metric_category);
CREATE INDEX IF NOT EXISTS idx_business_metrics_calculation_date ON business_metrics(calculation_date);
CREATE INDEX IF NOT EXISTS idx_business_metrics_period_type ON business_metrics(period_type);

