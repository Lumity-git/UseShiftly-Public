-- V20250723__create_shift_requirement_table.sql
-- Migration to create the shift_requirements table for auto-scheduling

CREATE TABLE shift_requirements (
    id BIGSERIAL PRIMARY KEY,
    department_id BIGINT NOT NULL REFERENCES department(id),
    shift_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    required_employees INT NOT NULL,
    notes TEXT
);

CREATE INDEX idx_shift_req_dept_date ON shift_requirements(department_id, shift_date);
