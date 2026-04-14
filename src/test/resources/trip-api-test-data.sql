-- Minimal data for TripApiTest, RatingApiTest, DataSqlIntegrationTest.
-- Idempotent: MERGE for users/trip/members; DELETE+INSERT for user_profile (unique on user_uuid).
-- UserRole in DB: NORMAL_USER (enum has no USER).

MERGE INTO users (user_uuid, created_at, role, account_status) KEY (user_uuid)
VALUES ('11111111-1111-4111-8111-111111111111', CURRENT_TIMESTAMP, 'NORMAL_USER', 'ACTIVE');

MERGE INTO users (user_uuid, created_at, role, account_status) KEY (user_uuid)
VALUES ('22222222-2222-4222-8222-222222222222', CURRENT_TIMESTAMP, 'NORMAL_USER', 'ACTIVE');

MERGE INTO users (user_uuid, country_code, mobile_number, created_at, role, account_status) KEY (user_uuid)
VALUES ('99999999-9999-4999-8999-999999999991', '+91', '9000000001', CURRENT_TIMESTAMP, 'NORMAL_USER', 'ACTIVE');

DELETE FROM user_profile WHERE user_uuid IN ('11111111-1111-4111-8111-111111111111', '22222222-2222-4222-8222-222222222222', '99999999-9999-4999-8999-999999999991');
INSERT INTO user_profile (user_profile_uuid, user_uuid, "FIRST_NAME", "GENDER", "DATE_OF_BIRTH", verification_status, version, trust_score, created_at)
VALUES ('11111111-1111-4111-8111-111111111112', '11111111-1111-4111-8111-111111111111', 'Test', 'MALE', DATE '1990-01-01', 'VERIFIED', 1, 4.50, CURRENT_TIMESTAMP);
INSERT INTO user_profile (user_profile_uuid, user_uuid, "FIRST_NAME", "GENDER", "DATE_OF_BIRTH", verification_status, version, trust_score, created_at)
VALUES ('22222222-2222-4222-8222-222222222223', '22222222-2222-4222-8222-222222222222', 'Test2', 'FEMALE', DATE '1992-01-01', 'NOT_VERIFIED', 1, NULL, CURRENT_TIMESTAMP);
INSERT INTO user_profile (user_profile_uuid, user_uuid, "FIRST_NAME", "GENDER", "DATE_OF_BIRTH", verification_status, version, trust_score, created_at)
VALUES ('99999999-9999-4999-8999-999999999992', '99999999-9999-4999-8999-999999999991', 'AuthUser', 'MALE', DATE '1995-01-01', 'NOT_VERIFIED', 1, NULL, CURRENT_TIMESTAMP);

MERGE INTO core_trip_details (trip_id, trip_title, trip_status, visibility_status, created_at) KEY (trip_id)
VALUES ('eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee', 'Test completed trip', 'COMPLETED', 'PUBLIC', CURRENT_TIMESTAMP);

MERGE INTO trip_members (membership_id, trip_id, user_id, role, status, joined_at) KEY (membership_id)
VALUES ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', 'eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee', '11111111-1111-4111-8111-111111111111', 'HOST', 'ACTIVE', CURRENT_TIMESTAMP);

MERGE INTO trip_members (membership_id, trip_id, user_id, role, status, joined_at) KEY (membership_id)
VALUES ('bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb', 'eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee', '22222222-2222-4222-8222-222222222222', 'MEMBER', 'ACTIVE', CURRENT_TIMESTAMP);
