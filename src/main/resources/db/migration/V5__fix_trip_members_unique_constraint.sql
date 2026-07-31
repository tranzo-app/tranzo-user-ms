-- Drop the existing unique constraint on trip_members
ALTER TABLE trip_members DROP CONSTRAINT IF EXISTS trip_members_trip_id_user_id_key;

-- Create a partial unique index that only applies to active members
-- This allows multiple records for same trip+user (history) but only one active
CREATE UNIQUE INDEX idx_trip_members_active_unique 
ON trip_members (trip_id, user_id) 
WHERE status = 'ACTIVE';
