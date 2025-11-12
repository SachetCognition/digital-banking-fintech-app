-- Investment Accounts
CREATE TABLE investment_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    account_number VARCHAR(50) UNIQUE NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    account_name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    initial_deposit DECIMAL(15,2) NOT NULL,
    current_balance DECIMAL(15,2) NOT NULL,
    available_cash DECIMAL(15,2) NOT NULL,
    invested_amount DECIMAL(15,2) DEFAULT 0,
    unrealized_pnl DECIMAL(15,2) DEFAULT 0,
    realized_pnl DECIMAL(15,2) DEFAULT 0,
    total_return DECIMAL(15,2) DEFAULT 0,
    total_return_percentage DECIMAL(5,2) DEFAULT 0,
    risk_tolerance VARCHAR(50) DEFAULT 'MODERATE',
    investment_objective VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Investment Products
CREATE TABLE investment_products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    symbol VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    product_type VARCHAR(50) NOT NULL,
    asset_class VARCHAR(50) NOT NULL,
    sector VARCHAR(100),
    exchange VARCHAR(50),
    currency VARCHAR(3) DEFAULT 'USD',
    is_active BOOLEAN DEFAULT true,
    min_investment DECIMAL(15,2) DEFAULT 1.0,
    expense_ratio DECIMAL(5,4) DEFAULT 0.0,
    management_fee DECIMAL(5,4) DEFAULT 0.0,
    inception_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Market Data
CREATE TABLE market_data (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    symbol VARCHAR(20) NOT NULL,
    price DECIMAL(15,4) NOT NULL,
    previous_close DECIMAL(15,4),
    open_price DECIMAL(15,4),
    high_price DECIMAL(15,4),
    low_price DECIMAL(15,4),
    volume BIGINT,
    market_cap BIGINT,
    pe_ratio DECIMAL(10,2),
    dividend_yield DECIMAL(5,4),
    price_change DECIMAL(15,4),
    price_change_percentage DECIMAL(5,2),
    last_updated TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Portfolio Holdings
CREATE TABLE portfolio_holdings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL REFERENCES investment_accounts(id),
    symbol VARCHAR(20) NOT NULL,
    quantity DECIMAL(15,6) NOT NULL,
    average_cost DECIMAL(15,4) NOT NULL,
    current_price DECIMAL(15,4) NOT NULL,
    market_value DECIMAL(15,2) NOT NULL,
    cost_basis DECIMAL(15,2) NOT NULL,
    unrealized_pnl DECIMAL(15,2) NOT NULL,
    unrealized_pnl_percentage DECIMAL(5,2) NOT NULL,
    weight_percentage DECIMAL(5,2) NOT NULL,
    last_updated TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Trading Orders
CREATE TABLE trading_orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL REFERENCES investment_accounts(id),
    order_number VARCHAR(50) UNIQUE NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    order_type VARCHAR(50) NOT NULL,
    side VARCHAR(10) NOT NULL,
    quantity DECIMAL(15,6) NOT NULL,
    price DECIMAL(15,4),
    stop_price DECIMAL(15,4),
    limit_price DECIMAL(15,4),
    time_in_force VARCHAR(20) DEFAULT 'DAY',
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    filled_quantity DECIMAL(15,6) DEFAULT 0,
    filled_price DECIMAL(15,4),
    filled_value DECIMAL(15,2),
    commission DECIMAL(15,2) DEFAULT 0,
    fees DECIMAL(15,2) DEFAULT 0,
    total_cost DECIMAL(15,2),
    submitted_at TIMESTAMP NOT NULL,
    filled_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Trading Transactions
CREATE TABLE trading_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL REFERENCES investment_accounts(id),
    order_id UUID REFERENCES trading_orders(id),
    transaction_type VARCHAR(50) NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    quantity DECIMAL(15,6) NOT NULL,
    price DECIMAL(15,4) NOT NULL,
    value DECIMAL(15,2) NOT NULL,
    commission DECIMAL(15,2) DEFAULT 0,
    fees DECIMAL(15,2) DEFAULT 0,
    net_amount DECIMAL(15,2) NOT NULL,
    transaction_date TIMESTAMP NOT NULL,
    settlement_date DATE,
    reference_number VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Portfolio Performance
CREATE TABLE portfolio_performance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL REFERENCES investment_accounts(id),
    calculation_date DATE NOT NULL,
    total_value DECIMAL(15,2) NOT NULL,
    total_cost DECIMAL(15,2) NOT NULL,
    total_return DECIMAL(15,2) NOT NULL,
    total_return_percentage DECIMAL(5,2) NOT NULL,
    daily_return DECIMAL(15,2) DEFAULT 0,
    daily_return_percentage DECIMAL(5,2) DEFAULT 0,
    benchmark_return DECIMAL(5,2) DEFAULT 0,
    alpha DECIMAL(5,2) DEFAULT 0,
    beta DECIMAL(5,2) DEFAULT 0,
    sharpe_ratio DECIMAL(5,2) DEFAULT 0,
    max_drawdown DECIMAL(5,2) DEFAULT 0,
    volatility DECIMAL(5,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Dividend Payments
CREATE TABLE dividend_payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL REFERENCES investment_accounts(id),
    symbol VARCHAR(20) NOT NULL,
    ex_dividend_date DATE NOT NULL,
    record_date DATE NOT NULL,
    payment_date DATE NOT NULL,
    dividend_per_share DECIMAL(15,6) NOT NULL,
    quantity DECIMAL(15,6) NOT NULL,
    total_dividend DECIMAL(15,2) NOT NULL,
    tax_withheld DECIMAL(15,2) DEFAULT 0,
    net_dividend DECIMAL(15,2) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Watchlists
CREATE TABLE watchlists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_default BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Watchlist Items
CREATE TABLE watchlist_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    watchlist_id UUID NOT NULL REFERENCES watchlists(id),
    symbol VARCHAR(20) NOT NULL,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT
);

-- Investment Goals
CREATE TABLE investment_goals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    account_id UUID REFERENCES investment_accounts(id),
    goal_name VARCHAR(255) NOT NULL,
    goal_type VARCHAR(50) NOT NULL,
    target_amount DECIMAL(15,2) NOT NULL,
    current_amount DECIMAL(15,2) DEFAULT 0,
    target_date DATE,
    monthly_contribution DECIMAL(15,2) DEFAULT 0,
    risk_tolerance VARCHAR(50),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Investment Events (Audit Trail)
CREATE TABLE investment_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID REFERENCES investment_accounts(id),
    event_type VARCHAR(100) NOT NULL,
    event_data JSONB,
    user_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_investment_accounts_customer_id ON investment_accounts(customer_id);
CREATE INDEX idx_investment_accounts_status ON investment_accounts(status);

CREATE INDEX idx_investment_products_symbol ON investment_products(symbol);
CREATE INDEX idx_investment_products_type ON investment_products(product_type);
CREATE INDEX idx_investment_products_asset_class ON investment_products(asset_class);
CREATE INDEX idx_investment_products_active ON investment_products(is_active);

CREATE INDEX idx_market_data_symbol ON market_data(symbol);
CREATE INDEX idx_market_data_last_updated ON market_data(last_updated);

CREATE INDEX idx_portfolio_holdings_account_id ON portfolio_holdings(account_id);
CREATE INDEX idx_portfolio_holdings_symbol ON portfolio_holdings(symbol);

CREATE INDEX idx_trading_orders_account_id ON trading_orders(account_id);
CREATE INDEX idx_trading_orders_status ON trading_orders(status);
CREATE INDEX idx_trading_orders_submitted_at ON trading_orders(submitted_at);

CREATE INDEX idx_trading_transactions_account_id ON trading_transactions(account_id);
CREATE INDEX idx_trading_transactions_transaction_date ON trading_transactions(transaction_date);

CREATE INDEX idx_portfolio_performance_account_id ON portfolio_performance(account_id);
CREATE INDEX idx_portfolio_performance_calculation_date ON portfolio_performance(calculation_date);

CREATE INDEX idx_dividend_payments_account_id ON dividend_payments(account_id);
CREATE INDEX idx_dividend_payments_payment_date ON dividend_payments(payment_date);

CREATE INDEX idx_watchlists_customer_id ON watchlists(customer_id);
CREATE INDEX idx_watchlist_items_watchlist_id ON watchlist_items(watchlist_id);

CREATE INDEX idx_investment_goals_customer_id ON investment_goals(customer_id);
CREATE INDEX idx_investment_goals_status ON investment_goals(status);

CREATE INDEX idx_investment_events_account_id ON investment_events(account_id);
CREATE INDEX idx_investment_events_event_type ON investment_events(event_type);
CREATE INDEX idx_investment_events_created_at ON investment_events(created_at);

