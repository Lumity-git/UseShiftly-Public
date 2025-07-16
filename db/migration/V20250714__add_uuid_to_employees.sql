-- Add uuid column to employees table if not exists
ALTER TABLE employees ADD COLUMN IF NOT EXISTS uuid UUID;

-- Update existing rows with a generated UUID if null
do $$
begin
  update employees set uuid = gen_random_uuid() where uuid is null;
end $$;

-- Optional: Add a unique constraint to ensure no duplicate UUIDs
ALTER TABLE employees ADD CONSTRAINT employees_uuid_unique UNIQUE (uuid);
