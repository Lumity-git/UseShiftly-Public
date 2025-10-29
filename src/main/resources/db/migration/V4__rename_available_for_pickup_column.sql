-- V4__rename_available_for_pickup_column.sql
-- Rename available_for_pickup to is_available_for_pickup to match the entity

ALTER TABLE shifts RENAME COLUMN available_for_pickup TO is_available_for_pickup;
