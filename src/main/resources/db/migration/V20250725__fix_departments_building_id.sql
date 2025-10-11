-- Migration: Fix departments.building_id null constraint and foreign key
-- Step 1: Add the column as nullable
ALTER TABLE departments ADD COLUMN IF NOT EXISTS building_id bigint;

-- Step 2: Set a valid building_id for all existing departments
-- Replace 1 with the actual id of your main building if different
UPDATE departments SET building_id = 1 WHERE building_id IS NULL;

-- Step 3: Set NOT NULL constraint
ALTER TABLE departments ALTER COLUMN building_id SET NOT NULL;

-- Step 4: Add the foreign key constraint
ALTER TABLE departments ADD CONSTRAINT IF NOT EXISTS fk_departments_building FOREIGN KEY (building_id) REFERENCES building(id);
