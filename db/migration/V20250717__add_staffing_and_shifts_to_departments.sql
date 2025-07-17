-- Migration for Department entity: add min_staffing, max_staffing, total_shifts columns
-- Assumes table name is 'departments'

ALTER TABLE departments
    ADD COLUMN min_staffing INTEGER,
    ADD COLUMN max_staffing INTEGER,
    ADD COLUMN total_shifts INTEGER;
