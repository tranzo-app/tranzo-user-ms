-- Splitwise Test Data for Dashboard API Tests
-- Simplified version without travel pals (table not available in test schema)

-- Insert test users (using MERGE to be idempotent)
MERGE INTO users (user_uuid, created_at, role, account_status) KEY (user_uuid)
VALUES ('11111111-1111-4111-8111-111111111111', CURRENT_TIMESTAMP, 'NORMAL_USER', 'ACTIVE');

MERGE INTO users (user_uuid, created_at, role, account_status) KEY (user_uuid)
VALUES ('22222222-2222-4222-8222-222222222222', CURRENT_TIMESTAMP, 'NORMAL_USER', 'ACTIVE');

MERGE INTO users (user_uuid, created_at, role, account_status) KEY (user_uuid)
VALUES ('33333333-3333-4333-8333-333333333333', CURRENT_TIMESTAMP, 'NORMAL_USER', 'ACTIVE');

-- Delete and insert user profiles (to handle unique constraint)
DELETE FROM user_profile WHERE user_uuid IN ('11111111-1111-4111-8111-111111111111', '22222222-2222-4222-8222-222222222222', '33333333-3333-4333-8333-333333333333');
INSERT INTO user_profile (user_profile_uuid, user_uuid, "FIRST_NAME", "LAST_NAME", "GENDER", "DATE_OF_BIRTH", verification_status, version, trust_score, created_at)
VALUES ('11111111-1111-4111-8111-111111111112', '11111111-1111-4111-8111-111111111111', 'John', 'Doe', 'MALE', DATE '1990-01-01', 'VERIFIED', 1, 4.50, CURRENT_TIMESTAMP);
INSERT INTO user_profile (user_profile_uuid, user_uuid, "FIRST_NAME", "LAST_NAME", "GENDER", "DATE_OF_BIRTH", verification_status, version, trust_score, created_at)
VALUES ('22222222-2222-4222-8222-222222222223', '22222222-2222-4222-8222-222222222222', 'Jane', 'Smith', 'FEMALE', DATE '1992-02-02', 'VERIFIED', 1, 4.00, CURRENT_TIMESTAMP);
INSERT INTO user_profile (user_profile_uuid, user_uuid, "FIRST_NAME", "LAST_NAME", "GENDER", "DATE_OF_BIRTH", verification_status, version, trust_score, created_at)
VALUES ('33333333-3333-4333-8333-333333333334', '33333333-3333-4333-8333-333333333333', 'Bob', 'Wilson', 'MALE', DATE '1988-03-03', 'VERIFIED', 1, 3.50, CURRENT_TIMESTAMP);

-- Insert test trips
MERGE INTO core_trip_details (trip_id, trip_title, trip_status, visibility_status, created_at) KEY (trip_id)
VALUES ('77777777-7777-4777-8777-777777777777', 'Summer Adventure in Bali', 'COMPLETED', 'PUBLIC', CURRENT_TIMESTAMP);

MERGE INTO core_trip_details (trip_id, trip_title, trip_status, visibility_status, created_at) KEY (trip_id)
VALUES ('88888888-8888-4888-8888-888888888888', 'Weekend Getaway', 'ONGOING', 'PUBLIC', CURRENT_TIMESTAMP);

-- Insert splitwise groups
MERGE INTO splitwise_groups (id, trip_id, description, created_by, created_at, updated_at) KEY (id)
VALUES ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', '77777777-7777-4777-8777-777777777777', 'Bali trip expenses', '11111111-1111-4111-8111-111111111111', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO splitwise_groups (id, trip_id, description, created_by, created_at, updated_at) KEY (id)
VALUES ('bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb', '88888888-8888-4888-8888-888888888888', 'Goa trip expenses', '11111111-1111-4111-8111-111111111111', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert group members
MERGE INTO splitwise_group_members (id, group_id, user_id, role, joined_at) KEY (id)
VALUES ('cccccccc-cccc-4ccc-8ccc-cccccccccccc', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', '11111111-1111-4111-8111-111111111111', 'ADMIN', CURRENT_TIMESTAMP);

MERGE INTO splitwise_group_members (id, group_id, user_id, role, joined_at) KEY (id)
VALUES ('dddddddd-dddd-4ddd-8ddd-dddddddddddd', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', '22222222-2222-4222-8222-222222222222', 'MEMBER', CURRENT_TIMESTAMP);

MERGE INTO splitwise_group_members (id, group_id, user_id, role, joined_at) KEY (id)
VALUES ('eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', '33333333-3333-4333-8333-333333333333', 'MEMBER', CURRENT_TIMESTAMP);

MERGE INTO splitwise_group_members (id, group_id, user_id, role, joined_at) KEY (id)
VALUES ('ffffffff-ffff-4fff-8fff-ffffffffffff', 'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb', '11111111-1111-4111-8111-111111111111', 'ADMIN', CURRENT_TIMESTAMP);

MERGE INTO splitwise_group_members (id, group_id, user_id, role, joined_at) KEY (id)
VALUES ('11111111-1111-1111-1111-111111111111', 'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb', '22222222-2222-4222-8222-222222222222', 'MEMBER', CURRENT_TIMESTAMP);

-- Insert expenses
MERGE INTO splitwise_expenses (id, group_id, name, description, amount, paid_by, split_type, created_at, updated_at) KEY (id)
VALUES ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', 'Hotel booking', 'Hotel booking for 3 nights', 1500.00, '11111111-1111-4111-8111-111111111111', 'EQUAL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO splitwise_expenses (id, group_id, name, description, amount, paid_by, split_type, created_at, updated_at) KEY (id)
VALUES ('bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', 'Dinner at restaurant', 'Dinner at local restaurant', 800.00, '22222222-2222-4222-8222-222222222222', 'EQUAL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO splitwise_expenses (id, group_id, name, description, amount, paid_by, split_type, created_at, updated_at) KEY (id)
VALUES ('cccccccc-cccc-4ccc-8ccc-cccccccccccc', 'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb', 'Taxi fare', 'Airport to hotel taxi', 500.00, '11111111-1111-4111-8111-111111111111', 'EQUAL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert expense splits
MERGE INTO splitwise_expense_splits (id, expense_id, user_id, amount, created_at) KEY (id)
VALUES ('dddddddd-dddd-4ddd-8ddd-dddddddddddd', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', '11111111-1111-4111-8111-111111111111', 500.00, CURRENT_TIMESTAMP);

MERGE INTO splitwise_expense_splits (id, expense_id, user_id, amount, created_at) KEY (id)
VALUES ('eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', '22222222-2222-4222-8222-222222222222', 500.00, CURRENT_TIMESTAMP);

MERGE INTO splitwise_expense_splits (id, expense_id, user_id, amount, created_at) KEY (id)
VALUES ('ffffffff-ffff-4fff-8fff-ffffffffffff', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', '33333333-3333-4333-8333-333333333333', 500.00, CURRENT_TIMESTAMP);

MERGE INTO splitwise_expense_splits (id, expense_id, user_id, amount, created_at) KEY (id)
VALUES ('11111111-1111-1111-1111-111111111111', 'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb', '11111111-1111-4111-8111-111111111111', 266.67, CURRENT_TIMESTAMP);

MERGE INTO splitwise_expense_splits (id, expense_id, user_id, amount, created_at) KEY (id)
VALUES ('22222222-2222-2222-2222-222222222222', 'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb', '22222222-2222-4222-8222-222222222222', 266.67, CURRENT_TIMESTAMP);

MERGE INTO splitwise_expense_splits (id, expense_id, user_id, amount, created_at) KEY (id)
VALUES ('33333333-3333-3333-3333-333333333333', 'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb', '33333333-3333-4333-8333-333333333333', 266.66, CURRENT_TIMESTAMP);

MERGE INTO splitwise_expense_splits (id, expense_id, user_id, amount, created_at) KEY (id)
VALUES ('44444444-4444-4444-4444-444444444444', 'cccccccc-cccc-4ccc-8ccc-cccccccccccc', '11111111-1111-4111-8111-111111111111', 250.00, CURRENT_TIMESTAMP);

MERGE INTO splitwise_expense_splits (id, expense_id, user_id, amount, created_at) KEY (id)
VALUES ('55555555-5555-5555-5555-555555555555', 'cccccccc-cccc-4ccc-8ccc-cccccccccccc', '22222222-2222-4222-8222-222222222222', 250.00, CURRENT_TIMESTAMP);

-- Insert balances (calculated based on expenses and splits)
MERGE INTO splitwise_balances (id, group_id, owed_by, owed_to, amount, last_updated) KEY (id)
VALUES ('66666666-6666-6666-6666-666666666666', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', '22222222-2222-4222-8222-222222222222', '11111111-1111-4111-8111-111111111111', 233.33, CURRENT_TIMESTAMP);

MERGE INTO splitwise_balances (id, group_id, owed_by, owed_to, amount, last_updated) KEY (id)
VALUES ('77777777-7777-7777-7777-777777777777', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', '33333333-3333-4333-8333-333333333333', '11111111-1111-4111-8111-111111111111', 233.33, CURRENT_TIMESTAMP);

MERGE INTO splitwise_balances (id, group_id, owed_by, owed_to, amount, last_updated) KEY (id)
VALUES ('88888888-8888-8888-8888-888888888888', 'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb', '22222222-2222-4222-8222-222222222222', '11111111-1111-4111-8111-111111111111', 250.00, CURRENT_TIMESTAMP);
