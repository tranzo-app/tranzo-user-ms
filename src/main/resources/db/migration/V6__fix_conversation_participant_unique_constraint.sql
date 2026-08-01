-- Drop the existing unique constraint on conversation_participant
ALTER TABLE conversation_participant DROP CONSTRAINT IF EXISTS uk_conversation_user;

-- Create a partial unique index that only applies to active conversation participants
-- This allows multiple records for same conversation+user (history) but only one active
CREATE UNIQUE INDEX idx_conversation_participant_active_unique 
ON conversation_participant (conversation_id, user_id) 
WHERE left_at IS NULL;

-- Drop the existing unique constraints on trip_invites
ALTER TABLE trip_invites DROP CONSTRAINT IF EXISTS UniqueTripIdAndInvitedUserId;
ALTER TABLE trip_invites DROP CONSTRAINT IF EXISTS UniqueTripIdAndInvitedEmail;
ALTER TABLE trip_invites DROP CONSTRAINT IF EXISTS UniqueTripIdAndInvitedPhone;

-- Create partial unique indexes that only apply to pending invites
-- This allows multiple records for same trip+user/email/phone (history) but only one pending
CREATE UNIQUE INDEX idx_trip_invites_user_pending_unique 
ON trip_invites (trip_id, invited_user_id) 
WHERE invited_user_id IS NOT NULL AND status = 'PENDING';

CREATE UNIQUE INDEX idx_trip_invites_email_pending_unique 
ON trip_invites (trip_id, invited_email) 
WHERE invited_email IS NOT NULL AND status = 'PENDING';

CREATE UNIQUE INDEX idx_trip_invites_phone_pending_unique 
ON trip_invites (trip_id, invited_phone) 
WHERE invited_phone IS NOT NULL AND status = 'PENDING';
