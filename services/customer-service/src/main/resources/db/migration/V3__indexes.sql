CREATE INDEX IF NOT EXISTS idx_customers_email ON customers(email);
CREATE INDEX IF NOT EXISTS idx_kyc_cases_customer_status ON kyc_cases(customer_id, status);
CREATE INDEX IF NOT EXISTS idx_kyc_documents_case_id ON kyc_documents(kyc_case_id);
