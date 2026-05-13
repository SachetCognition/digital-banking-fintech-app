CREATE TABLE file_metadata (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100),
    file_size BIGINT,
    file_hash VARCHAR(255),
    storage_key VARCHAR(500) NOT NULL,
    bucket_name VARCHAR(100) NOT NULL,
    upload_status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_file_metadata_customer_id ON file_metadata(customer_id);
CREATE INDEX idx_file_metadata_status ON file_metadata(upload_status);
