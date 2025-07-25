-- Migration: Create default SUPER_ADMIN user
-- WARNING: Replace <building_id> with a valid building id from your DB

INSERT INTO employees (
    email, password, first_name, last_name, role, department_id, building_id, active, must_change_password, uuid
) VALUES (
    'superadmin@hotel.com',
    '$2a$10$BuuRV7kOJjHapTMJgEI2JuDQlGN1LlGrqzAJLwxDxWYW1weggI/26', -- bcrypt for testpassword123!
    'Super',
    'Admin',
    'SUPER_ADMIN',
    NULL,
    1, -- <building_id>: update as needed
    true,
    false,
    gen_random_uuid()
);
