-- Migration: Add deleted_at field to Employee table
ALTER TABLE employee ADD COLUMN deleted_at TIMESTAMP NULL;
