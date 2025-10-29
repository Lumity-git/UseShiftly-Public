-- V2__insert_default_data.sql
-- Inserts default building and departments for fresh installations
-- For existing databases, this migration is idempotent

-- Insert default building if none exists
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM building WHERE id = 1) THEN
        INSERT INTO building (id, name, address, admin_id, created_at)
        VALUES (1, 'Main Building', '123 Main St, Headquarters', NULL, CURRENT_TIMESTAMP);
        
        -- Reset sequence to ensure next insert gets correct ID
        PERFORM setval('building_id_seq', 1);
    END IF;
END $$;

-- Insert default departments for Main Building if they don't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM departments WHERE name = 'Front Desk' AND building_id = 1) THEN
        INSERT INTO departments (name, description, building_id, active, created_at)
        VALUES ('Front Desk', 'Reception and guest services', 1, true, CURRENT_TIMESTAMP);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM departments WHERE name = 'Housekeeping' AND building_id = 1) THEN
        INSERT INTO departments (name, description, building_id, active, created_at)
        VALUES ('Housekeeping', 'Room cleaning and maintenance', 1, true, CURRENT_TIMESTAMP);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM departments WHERE name = 'Restaurant' AND building_id = 1) THEN
        INSERT INTO departments (name, description, building_id, active, created_at)
        VALUES ('Restaurant', 'Food and beverage service', 1, true, CURRENT_TIMESTAMP);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM departments WHERE name = 'Maintenance' AND building_id = 1) THEN
        INSERT INTO departments (name, description, building_id, active, created_at)
        VALUES ('Maintenance', 'Building and equipment maintenance', 1, true, CURRENT_TIMESTAMP);
    END IF;
END $$;
