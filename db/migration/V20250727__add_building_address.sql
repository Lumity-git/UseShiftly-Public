-- Add address column to building table
ALTER TABLE building ADD COLUMN IF NOT EXISTS address VARCHAR(255) NOT NULL;
