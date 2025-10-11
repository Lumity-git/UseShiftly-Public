-- Migration: Create BillingLog table
CREATE TABLE billing_log (
    id SERIAL PRIMARY KEY,
    admin_id INTEGER NOT NULL,
    billing_period DATE NOT NULL,
    employee_ids TEXT NOT NULL, -- comma-separated IDs for simplicity
    billable_users INTEGER NOT NULL,
    amount NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
