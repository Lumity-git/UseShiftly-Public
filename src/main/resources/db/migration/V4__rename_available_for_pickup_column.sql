-- V4__rename_available_for_pickup_column.sql
-- Rename available_for_pickup to is_available_for_pickup to match the entity

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'shifts' AND column_name = 'available_for_pickup') THEN
        ALTER TABLE shifts RENAME COLUMN available_for_pickup TO is_available_for_pickup;
    END IF;
END $$;
