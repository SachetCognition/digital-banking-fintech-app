-- KYC applications table
CREATE TABLE IF NOT EXISTS kyc_applications (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    provider_ref VARCHAR(128),
    rejection_reason VARCHAR(512),
    submitted_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_kyc_customer ON kyc_applications(customer_id);
CREATE INDEX IF NOT EXISTS idx_kyc_status ON kyc_applications(status);
