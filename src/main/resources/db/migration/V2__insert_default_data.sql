-- V2__insert_default_data.sql
-- Inserts default building, departments, and ensures SUPER_ADMIN role exists
-- This migration ensures production environment has minimal required data

-- Insert default building
INSERT INTO building (id, name, address, created_at)
VALUES (1, 'Main Building', '123 Main St, Headquarters', CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- Reset sequence to ensure next insert gets correct ID
SELECT setval('building_id_seq', (SELECT MAX(id) FROM building));

-- Insert default departments for Main Building
INSERT INTO departments (name, description, building_id, active, created_at)
VALUES 
    ('Front Desk', 'Reception and guest services', 1, true, CURRENT_TIMESTAMP),
    ('Housekeeping', 'Room cleaning and maintenance', 1, true, CURRENT_TIMESTAMP),
    ('Restaurant', 'Food and beverage service', 1, true, CURRENT_TIMESTAMP),
    ('Maintenance', 'Building and equipment maintenance', 1, true, CURRENT_TIMESTAMP)
ON CONFLICT (name, building_id) DO NOTHING;

-- Note: Admin users should be created via application or manual scripts
-- SUPER_ADMIN role is already defined in the employee_role_enum type
-- Password hashing must be done by the application layer

-- Create a comment to document the SUPER_ADMIN creation process
COMMENT ON TYPE employee_role_enum IS 'Employee roles: EMPLOYEE, MANAGER, ADMIN, SUPER_ADMIN. SUPER_ADMIN must be created manually or via application initialization.';
