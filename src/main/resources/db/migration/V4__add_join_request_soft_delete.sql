-- Add soft delete columns to trip_join_requests table
ALTER TABLE trip_join_requests 
ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN deleted_at TIMESTAMP;
