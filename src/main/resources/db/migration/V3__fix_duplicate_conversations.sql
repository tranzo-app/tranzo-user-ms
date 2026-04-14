-- Fix duplicate one-to-one conversations
-- This migration removes duplicate conversations, keeping only the newest one for each user pair

-- First, identify and delete duplicate one-to-one conversations
WITH ranked_conversations AS (
    SELECT 
        c.conversation_id,
        ROW_NUMBER() OVER (
            PARTITION BY 
                LEAST(cp1.user_id::TEXT, cp2.user_id::TEXT) || '_' || 
                GREATEST(cp1.user_id::TEXT, cp2.user_id::TEXT)
            ORDER BY c.created_at DESC
        ) as rn
    FROM conversation c
    JOIN conversation_participant cp1 ON c.conversation_id = cp1.conversation_id
    JOIN conversation_participant cp2 ON c.conversation_id = cp2.conversation_id
    WHERE c.type = 'ONE_ON_ONE'
      AND cp1.user_id < cp2.user_id
      AND cp1.left_at IS NULL
      AND cp2.left_at IS NULL
)
-- Delete duplicate conversations (keep only the newest - rn = 1)
DELETE FROM conversation_participant 
WHERE conversation_id IN (
    SELECT conversation_id 
    FROM ranked_conversations 
    WHERE rn > 1
);

DELETE FROM message 
WHERE conversation_id IN (
    SELECT conversation_id 
    FROM ranked_conversations 
    WHERE rn > 1
);

DELETE FROM conversation_mute 
WHERE conversation_id IN (
    SELECT conversation_id 
    FROM ranked_conversations 
    WHERE rn > 1
);

DELETE FROM conversation_block 
WHERE conversation_id IN (
    SELECT conversation_id 
    FROM ranked_conversations 
    WHERE rn > 1
);

DELETE FROM conversation 
WHERE conversation_id IN (
    SELECT conversation_id 
    FROM ranked_conversations 
    WHERE rn > 1
);

-- Add comment for verification
COMMENT ON TABLE conversation IS 'Fixed: Duplicate one-to-one conversations removed on ' || CURRENT_TIMESTAMP;

-- Add trip_host_name column to core_trip_details table
-- This column stores the name of the trip host for better performance and user experience
ALTER TABLE core_trip_details 
ADD COLUMN trip_host_name VARCHAR(255);

-- Add comment to describe the new column
COMMENT ON COLUMN core_trip_details.trip_host_name IS 'Name of the trip host, populated when trip is created';
