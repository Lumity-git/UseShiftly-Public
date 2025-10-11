-- V20250728__create_shift_requirements_table.sql
-- Migration to create the shift_requirements table for auto-scheduling

CREATE TABLE IF NOT EXISTS shift_requirements (
    id BIGSERIAL PRIMARY KEY,
    department_id BIGINT NOT NULL REFERENCES departments(id),
    shift_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    required_employees INT NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_shift_req_dept_date ON shift_requirements(department_id, shift_date);
