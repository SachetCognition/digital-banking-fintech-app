-- Ledger Service initial schema

-- Journals table
CREATE TABLE IF NOT EXISTS journals (
    id UUID PRIMARY KEY,
    reference VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    posted_at TIMESTAMPTZ
);

-- Journal entries table
CREATE TABLE IF NOT EXISTS journal_entries (
    id UUID PRIMARY KEY,
    journal_id UUID NOT NULL,
    account_id UUID NOT NULL,
    type VARCHAR(8) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency CHAR(3) NOT NULL,
    description VARCHAR(255),
    reference VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_journal_entry_journal FOREIGN KEY (journal_id) REFERENCES journals(id)
);

-- Account balances table (materialized view)
CREATE TABLE IF NOT EXISTS account_balances (
    id UUID PRIMARY KEY,
    account_id UUID UNIQUE NOT NULL,
    currency CHAR(3) NOT NULL,
    balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    last_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes for journals
CREATE INDEX IF NOT EXISTS idx_journals_reference ON journals(reference);
CREATE INDEX IF NOT EXISTS idx_journals_status ON journals(status);
CREATE INDEX IF NOT EXISTS idx_journals_created_at ON journals(created_at);

-- Indexes for journal entries
CREATE INDEX IF NOT EXISTS idx_journal_entries_journal_id ON journal_entries(journal_id);
CREATE INDEX IF NOT EXISTS idx_journal_entries_account_id ON journal_entries(account_id);
CREATE INDEX IF NOT EXISTS idx_journal_entries_type ON journal_entries(type);
CREATE INDEX IF NOT EXISTS idx_journal_entries_created_at ON journal_entries(created_at);
CREATE INDEX IF NOT EXISTS idx_journal_entries_account_date ON journal_entries(account_id, created_at);

-- Indexes for account balances
CREATE INDEX IF NOT EXISTS idx_account_balances_account_id ON account_balances(account_id);
CREATE INDEX IF NOT EXISTS idx_account_balances_currency ON account_balances(currency);
CREATE INDEX IF NOT EXISTS idx_account_balances_last_updated ON account_balances(last_updated_at);