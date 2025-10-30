-- V1__initial_schema.sql
-- Initial database schema for the UseShiftly application
-- Creates all core tables with proper constraints and indexes

-- Create custom enum types
CREATE TYPE shift_trade_status_enum AS ENUM (
    'PENDING',
    'APPROVED',
    'REJECTED',
    'CANCELLED',
    'POSTED_TO_EVERYONE',
    'PENDING_APPROVAL'
);

CREATE TYPE shift_status_enum AS ENUM (
    'SCHEDULED',
    'COMPLETED',
    'CANCELLED',
    'PENDING',
    'AVAILABLE_FOR_PICKUP'
);

CREATE TYPE employee_role_enum AS ENUM (
    'EMPLOYEE',
    'MANAGER',
    'ADMIN',
    'SUPER_ADMIN'
);

-- Building table
CREATE TABLE building (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    address VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Departments table
CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    building_id BIGINT REFERENCES building(id) ON DELETE CASCADE,
    active BOOLEAN DEFAULT TRUE NOT NULL,
    min_staffing INTEGER DEFAULT 1,
    max_staffing INTEGER DEFAULT 10,
    total_shifts INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT uk_department_name_building UNIQUE (name, building_id)
);

-- Employees table
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role employee_role_enum DEFAULT 'EMPLOYEE' NOT NULL,
    phone_number VARCHAR(20),
    address VARCHAR(500),
    department_id BIGINT REFERENCES departments(id) ON DELETE SET NULL,
    building_id BIGINT REFERENCES building(id) ON DELETE SET NULL,
    active BOOLEAN DEFAULT TRUE NOT NULL,
    must_change_password BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_employees_department ON employees(department_id);
CREATE INDEX idx_employees_building ON employees(building_id);
CREATE INDEX idx_employees_email ON employees(email);

-- Shifts table
CREATE TABLE shifts (
    id BIGSERIAL PRIMARY KEY,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    employee_id BIGINT REFERENCES employees(id) ON DELETE SET NULL,
    department_id BIGINT REFERENCES departments(id) ON DELETE CASCADE,
    status shift_status_enum DEFAULT 'SCHEDULED' NOT NULL,
    notes TEXT,
    available_for_pickup BOOLEAN DEFAULT FALSE,
    created_by_id BIGINT REFERENCES employees(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_shifts_employee ON shifts(employee_id);
CREATE INDEX idx_shifts_department ON shifts(department_id);
CREATE INDEX idx_shifts_start_time ON shifts(start_time);
CREATE INDEX idx_shifts_status ON shifts(status);

-- Shift Trades table
CREATE TABLE shift_trades (
    id BIGSERIAL PRIMARY KEY,
    shift_id BIGINT REFERENCES shifts(id) ON DELETE CASCADE,
    requesting_employee_id BIGINT REFERENCES employees(id) ON DELETE CASCADE,
    pickup_employee_id BIGINT REFERENCES employees(id) ON DELETE SET NULL,
    status shift_trade_status_enum DEFAULT 'PENDING' NOT NULL,
    requested_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    notes TEXT
);

CREATE INDEX idx_shift_trades_shift ON shift_trades(shift_id);
CREATE INDEX idx_shift_trades_requesting ON shift_trades(requesting_employee_id);
CREATE INDEX idx_shift_trades_pickup ON shift_trades(pickup_employee_id);
CREATE INDEX idx_shift_trades_status ON shift_trades(status);

-- Employee Availability table
CREATE TABLE employee_availability (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT REFERENCES employees(id) ON DELETE CASCADE,
    day VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    CONSTRAINT chk_availability_day CHECK (day IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'))
);

CREATE INDEX idx_availability_employee ON employee_availability(employee_id);

-- Shift Templates table
CREATE TABLE shift_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    department_id BIGINT REFERENCES departments(id) ON DELETE CASCADE,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    days_of_week VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_shift_templates_department ON shift_templates(department_id);

-- Shift Requirements table
CREATE TABLE shift_requirements (
    id BIGSERIAL PRIMARY KEY,
    department_id BIGINT REFERENCES departments(id) ON DELETE CASCADE,
    shift_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    required_employees INTEGER DEFAULT 1 NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_shift_requirements_department ON shift_requirements(department_id);
CREATE INDEX idx_shift_requirements_date ON shift_requirements(shift_date);

-- Notifications table
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES employees(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    read BOOLEAN DEFAULT FALSE,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_read ON notifications(read);

-- Invitations table
CREATE TABLE invitation (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    token VARCHAR(255) NOT NULL UNIQUE,
    role employee_role_enum DEFAULT 'ADMIN' NOT NULL,
    building_id BIGINT REFERENCES building(id) ON DELETE CASCADE,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_invitation_token ON invitation(token);
CREATE INDEX idx_invitation_email ON invitation(email);

-- Waitlist table
CREATE TABLE waitlist (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User Action Log table
CREATE TABLE user_action_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES employees(id) ON DELETE SET NULL,
    action VARCHAR(255) NOT NULL,
    entity_type VARCHAR(100),
    entity_id BIGINT,
    details TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_action_log_user ON user_action_log(user_id);
CREATE INDEX idx_action_log_timestamp ON user_action_log(timestamp);
CREATE INDEX idx_action_log_action ON user_action_log(action);
