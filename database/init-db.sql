-- Investment Plans Database Initialization Script for PostgreSQL
-- Creates all tables for the event-driven architecture

-- Plans Table
CREATE TABLE IF NOT EXISTS plans (
    plan_id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    execution_day INT NOT NULL CHECK (execution_day >= 1 AND execution_day <= 31),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    total_amount DECIMAL(19,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_plans_user_id ON plans(user_id);
CREATE INDEX IF NOT EXISTS idx_plans_execution_day ON plans(execution_day);
CREATE INDEX IF NOT EXISTS idx_plans_status ON plans(status);

-- Plan Investments (breakdown of what to invest)
CREATE TABLE IF NOT EXISTS plan_investments (
    id BIGSERIAL PRIMARY KEY,
    plan_id UUID NOT NULL REFERENCES plans(plan_id) ON DELETE CASCADE,
    asset_id VARCHAR(50) NOT NULL,
    amount DECIMAL(19,2) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_plan_investments_plan_id ON plan_investments(plan_id);

-- Transactional Outbox: Events waiting to be published to Kafka
-- THIS IS THE HEART OF THE PATTERN
CREATE TABLE IF NOT EXISTS outbox (
    outbox_id BIGSERIAL PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    published BOOLEAN DEFAULT FALSE,
    published_at TIMESTAMP NULL,
    scheduled_for TIMESTAMP NULL,
    retry_count INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_outbox_published ON outbox(published);
CREATE INDEX IF NOT EXISTS idx_outbox_aggregate_id ON outbox(aggregate_id);
CREATE INDEX IF NOT EXISTS idx_outbox_created_at ON outbox(created_at);

-- Plan Executions: When a plan is executed
CREATE TABLE IF NOT EXISTS plan_executions (
    execution_id UUID PRIMARY KEY,
    plan_id UUID NOT NULL REFERENCES plans(plan_id) ON DELETE CASCADE,
    user_id VARCHAR(255) NOT NULL,
    triggered_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP NULL,
    status VARCHAR(50) NOT NULL,
    total_executed DECIMAL(19,2) NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_plan_executions_plan_id ON plan_executions(plan_id);
CREATE INDEX IF NOT EXISTS idx_plan_executions_user_id ON plan_executions(user_id);
CREATE INDEX IF NOT EXISTS idx_plan_executions_status ON plan_executions(status);

-- Order Commands: Orders created from plan execution
CREATE TABLE IF NOT EXISTS order_commands (
    order_id UUID PRIMARY KEY,
    execution_id UUID NOT NULL REFERENCES plan_executions(execution_id) ON DELETE CASCADE,
    plan_id UUID NOT NULL REFERENCES plans(plan_id) ON DELETE CASCADE,
    user_id VARCHAR(255) NOT NULL,
    asset_id VARCHAR(50) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_order_commands_execution_id ON order_commands(execution_id);
CREATE INDEX IF NOT EXISTS idx_order_commands_status ON order_commands(status);

-- Cash Balances: Track available cash per user
CREATE TABLE IF NOT EXISTS cash_balances (
    user_id VARCHAR(255) PRIMARY KEY,
    balance DECIMAL(19,2) NOT NULL DEFAULT 10000.00,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_cash_balances_updated_at ON cash_balances(updated_at);

-- Initialize default cash balances
INSERT INTO cash_balances (user_id, balance, updated_at) VALUES
('user_123', 10000.00, NOW()),
('user_456', 15000.00, NOW()),
('user_789', 20000.00, NOW()),
('demo_user', 50000.00, NOW())
ON CONFLICT (user_id) DO UPDATE SET updated_at = NOW();

-- Log table for tracking events (optional but helpful)
CREATE TABLE IF NOT EXISTS event_log (
    log_id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    source_service VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_event_log_event_type ON event_log(event_type);
CREATE INDEX IF NOT EXISTS idx_event_log_created_at ON event_log(created_at);

COMMIT;
