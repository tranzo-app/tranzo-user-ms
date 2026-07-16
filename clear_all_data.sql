-- WARNING: This script will DELETE ALL DATA from all tables in the database
-- Make sure you have a backup before running this!
-- Usage: psql -h <HOST> -U <USERNAME> -d <DATABASE_NAME> -f clear_all_data.sql

-- Disable foreign key constraints temporarily
SET session_replication_role = 'replica';

-- Delete from all tables in correct order (child tables first)
-- Using DO block to handle missing tables gracefully

DO $$
DECLARE
    table_name text;
    tables text[] := ARRAY[
        -- Notification module
        'user_notification',
        -- Chat module
        'conversation_block',
        'conversation_mute',
        'message',
        'conversation_participant',
        'conversation',
        -- Splitwise module
        'splitwise_activities',
        'splitwise_expense_splits',
        'splitwise_settlement_expenses',
        'splitwise_settlements',
        'splitwise_balances',
        'splitwise_group_members',
        'splitwise_expenses',
        'splitwise_groups',
        -- Trip module (child tables first)
        'trip_tag',
        'trip_trip_image',
        'trip_image',
        'trip_reports',
        'trip_wishlists',
        'trip_queries',
        'trip_members',
        'trip_join_requests',
        'trip_invites',
        'trip_itineraries',
        'trip_meta_data',
        'trip_policies',
        'core_trip_details',
        'tags',
        'task_lock',
        -- Trip ratings (user module)
        'trip_rating',
        'member_rating',
        'host_rating',
        -- User module (child tables first)
        'user_profile_history_table',
        'social_handle',
        'refresh_token',
        'verification',
        'user_reports',
        'travel_pal',
        'aadhar_otp',
        'user_profile',
        'users'
    ];
BEGIN
    FOREACH table_name IN ARRAY tables
    LOOP
        BEGIN
            EXECUTE format('TRUNCATE TABLE %I CASCADE', table_name);
            RAISE NOTICE 'Truncated table: %', table_name;
        EXCEPTION WHEN undefined_table THEN
            RAISE NOTICE 'Table % does not exist, skipping', table_name;
        END;
    END LOOP;
END $$;

-- Re-enable foreign key constraints
SET session_replication_role = 'origin';

-- Verify all tables are empty
DO $$
DECLARE
    table_name text;
    row_count int;
BEGIN
    FOR table_name IN
        SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename
    LOOP
        EXECUTE format('SELECT COUNT(*) FROM %I', table_name) INTO row_count;
        RAISE NOTICE 'Table % has % rows', table_name, row_count;
    END LOOP;
END $$;
