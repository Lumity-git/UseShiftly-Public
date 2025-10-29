-- V3__add_missing_employee_columns.sql
-- Add missing columns to employees table that exist in the Employee entity

ALTER TABLE employees ADD COLUMN IF NOT EXISTS uuid VARCHAR(36) DEFAULT gen_random_uuid()::text;
ALTER TABLE employees ADD COLUMN IF NOT EXISTS date_of_birth VARCHAR(255);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS emergency_contact_name VARCHAR(255);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS emergency_contact_relation VARCHAR(255);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS emergency_contact_phone VARCHAR(20);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE employees ADD COLUMN IF NOT EXISTS package_type VARCHAR(50) DEFAULT 'standard';

-- Make uuid unique and not null after adding it
ALTER TABLE employees ALTER COLUMN uuid SET NOT NULL;
ALTER TABLE employees ADD CONSTRAINT uk_employees_uuid UNIQUE (uuid);

-- Update existing records to have uuids
UPDATE employees SET uuid = gen_random_uuid()::text WHERE uuid IS NULL;

-- Add index for uuid
CREATE INDEX IF NOT EXISTS idx_employees_uuid ON employees(uuid);