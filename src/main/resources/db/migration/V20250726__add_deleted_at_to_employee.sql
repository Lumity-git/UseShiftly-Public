-- Migration: Add deleted_at field to Employee table
ALTER TABLE employees ADD COLUMN deleted_at TIMESTAMP NULL;
