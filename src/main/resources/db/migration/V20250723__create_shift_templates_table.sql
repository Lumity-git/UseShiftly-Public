-- Create shift_templates table for department-specific shift templates
CREATE TABLE IF NOT EXISTS shift_templates (
    id SERIAL PRIMARY KEY,
    department_id INTEGER NOT NULL REFERENCES departments(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    days_of_week VARCHAR(20)[] NOT NULL, -- e.g., '{MONDAY,TUESDAY}'
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Index for quick lookup by department
CREATE INDEX IF NOT EXISTS idx_shift_templates_department_id ON shift_templates(department_id);
