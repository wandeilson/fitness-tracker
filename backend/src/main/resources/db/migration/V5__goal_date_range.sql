-- V5__goal_date_range.sql
-- Transform goals from single-row-per-user to date-ranged history.

-- Remove the old UNIQUE constraint on user_id (one goal per user)
ALTER TABLE goals DROP CONSTRAINT IF EXISTS goals_user_id_key;

-- Add date range columns
ALTER TABLE goals ADD COLUMN valid_from DATE NOT NULL DEFAULT CURRENT_DATE;
ALTER TABLE goals ADD COLUMN valid_until DATE;
ALTER TABLE goals ADD COLUMN created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Unique constraint: one goal per user per start date
ALTER TABLE goals ADD CONSTRAINT uq_goals_user_valid_from UNIQUE (user_id, valid_from);

-- Backfill: existing goal becomes active from its updated_at date
UPDATE goals SET valid_from = COALESCE(DATE(updated_at), CURRENT_DATE);
