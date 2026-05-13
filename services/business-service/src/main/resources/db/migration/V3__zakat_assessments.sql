-- Zakat assessments table (Shariah-compliant wealth tax)
CREATE TABLE IF NOT EXISTS zakat_assessments (
    id                   VARCHAR(36)    PRIMARY KEY,
    customer_id          VARCHAR(36)    NOT NULL,
    assessment_date      DATE           NOT NULL,
    total_assets         DECIMAL(18,2),
    total_exclusions     DECIMAL(18,2),
    net_zakatable_wealth DECIMAL(18,2),
    nisab_threshold      DECIMAL(18,2),
    gold_nisab           DECIMAL(18,2),
    silver_nisab         DECIMAL(18,2),
    meets_nisab          BOOLEAN        DEFAULT FALSE,
    zakat_rate           DECIMAL(6,4),
    zakat_due            DECIMAL(18,2),
    status               VARCHAR(20)    NOT NULL,
    message              TEXT,
    gold_price_per_gram  DECIMAL(12,2),
    silver_price_per_gram DECIMAL(12,2),
    calculated_at        TIMESTAMPTZ    DEFAULT NOW()
);

CREATE INDEX idx_zakat_customer       ON zakat_assessments(customer_id);
CREATE INDEX idx_zakat_customer_date  ON zakat_assessments(customer_id, assessment_date);
CREATE INDEX idx_zakat_status         ON zakat_assessments(status);
