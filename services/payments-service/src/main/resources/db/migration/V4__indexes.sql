CREATE INDEX IF NOT EXISTS idx_transfers_payer_account ON transfers(payer_account_id);
CREATE INDEX IF NOT EXISTS idx_transfers_payee_account ON transfers(payee_account_id);
CREATE INDEX IF NOT EXISTS idx_transfers_status ON transfers(status);
CREATE INDEX IF NOT EXISTS idx_transfers_idempotency ON transfers(idempotency_key);
