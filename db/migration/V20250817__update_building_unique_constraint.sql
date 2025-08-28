-- Remove the old unique constraint on building name
DROP INDEX IF EXISTS uk_oyx9p4qp0ot5mw2vdn1qgax00;

-- Add a new unique constraint on (name, admin_id) combination
ALTER TABLE building ADD CONSTRAINT uk_building_name_admin UNIQUE (name, admin_id);
