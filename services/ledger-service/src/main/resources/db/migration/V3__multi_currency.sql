CREATE TABLE exchange_rates (
    from_currency CHAR(3) NOT NULL,
    to_currency CHAR(3) NOT NULL,
    rate DECIMAL(15, 8) NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    PRIMARY KEY (from_currency, to_currency)
);

INSERT INTO exchange_rates (from_currency, to_currency, rate) VALUES
    ('USD', 'EUR', 0.92000000),
    ('USD', 'GBP', 0.79000000),
    ('USD', 'JPY', 149.50000000),
    ('USD', 'CAD', 1.36000000),
    ('USD', 'AUD', 1.53000000),
    ('USD', 'CHF', 0.88000000),
    ('EUR', 'USD', 1.08700000),
    ('GBP', 'USD', 1.26600000),
    ('JPY', 'USD', 0.00669000),
    ('CAD', 'USD', 0.73500000),
    ('AUD', 'USD', 0.65400000),
    ('CHF', 'USD', 1.13600000);
