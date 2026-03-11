---------------------------------------------------------
-- USERS (10 users)
---------------------------------------------------------
INSERT INTO users (user_uuid, email, mobile_number, role, created_at, updated_at, account_status)
VALUES
('11111111-1111-4111-8111-111111111111', 'user1@mail.com', '9000000001', 'NORMAL_USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACTIVE'),
('22222222-2222-4222-8222-222222222222', 'user2@mail.com', '9000000002', 'NORMAL_USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACTIVE'),
('33333333-3333-4333-8333-333333333333', 'user3@mail.com', '9000000003', 'NORMAL_USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACTIVE'),
('44444444-4444-4444-8444-444444444444', 'user4@mail.com', '9000000004', 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACTIVE'),
('55555555-5555-4555-8555-555555555555', 'user5@mail.com', '9000000005', 'NORMAL_USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACTIVE'),
('66666666-6666-4666-8666-666666666666', 'user6@mail.com', '9000000006', 'NORMAL_USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACTIVE'),
('77777777-7777-4777-8777-777777777777', 'user7@mail.com', '9000000007', 'NORMAL_USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACTIVE'),
('88888888-8888-4888-8888-888888888888', 'user8@mail.com', '9000000008', 'NORMAL_USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACTIVE'),
('99999999-9999-4999-8999-999999999999', 'user9@mail.com', '9000000009', 'NORMAL_USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACTIVE'),
('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', 'user10@mail.com', '9000000010', 'NORMAL_USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACTIVE');

---------------------------------------------------------
-- USER PROFILE (10 profiles)
---------------------------------------------------------
INSERT INTO user_profile (
    user_profile_uuid, user_uuid, first_name, middle_name, last_name,
    profile_picture_url, bio, gender, date_of_birth, location,
    created_at, updated_at, verification_status, version, trust_score, trust_score_updated_at
)
VALUES
('b1111111-1111-4b11-8111-111111111111', '11111111-1111-4111-8111-111111111111', 'John', NULL, 'Doe', NULL, 'Bio 1', 'MALE', '1995-01-01', 'Bangalore', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'PENDING', 1, 4.50, CURRENT_TIMESTAMP),
('b2222222-2222-4b22-8222-222222222222', '22222222-2222-4222-8222-222222222222', 'Mary', NULL, 'Jane', NULL, 'Bio 2', 'FEMALE', '1997-02-02', 'Hyderabad', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'PENDING', 1, 4.25, CURRENT_TIMESTAMP),
('b3333333-3333-4b33-8333-333333333333', '33333333-3333-4333-8333-333333333333', 'Ravi', 'K', 'Sharma', NULL, 'Bio 3', 'MALE', '1993-03-03', 'Chennai', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'APPROVED', 1, 4.75, CURRENT_TIMESTAMP),
('b4444444-4444-4b44-8444-444444444444', '44444444-4444-4444-8444-444444444444', 'Admin', NULL, 'User', NULL, 'Admin Bio', 'MALE', '1990-04-04', 'Delhi', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'REJECTED', 1, NULL, NULL),
('b5555555-5555-4b55-8555-555555555555', '55555555-5555-4555-8555-555555555555', 'Priya', NULL, 'Singh', NULL, 'Bio 5', 'FEMALE', '1996-05-05', 'Mumbai', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'PENDING', 1, 4.00, CURRENT_TIMESTAMP),
('b6666666-6666-4b66-8666-666666666666', '66666666-6666-4666-8666-666666666666', 'Amit', NULL, 'Khan', NULL, 'Bio 6', 'MALE', '1992-06-06', 'Pune', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'APPROVED', 1, 4.33, CURRENT_TIMESTAMP),
('b7777777-7777-4b77-8777-777777777777', '77777777-7777-4777-8777-777777777777', 'Neha', NULL, 'Verma', NULL, 'Bio 7', 'FEMALE', '1994-07-07', 'Kolkata', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'PENDING', 1, NULL, NULL),
('b8888888-8888-4b88-8888-888888888888', '88888888-8888-4888-8888-888888888888', 'Arun', NULL, 'Nair', NULL, 'Bio 8', 'MALE', '1991-08-08', 'Delhi', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'APPROVED', 1, 4.67, CURRENT_TIMESTAMP),
('b9999999-9999-4b99-8999-999999999999', '99999999-9999-4999-8999-999999999999', 'Sara', NULL, 'Mathews', NULL, 'Bio 9', 'FEMALE', '1998-09-09', 'Kochi', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'PENDING', 1, NULL, NULL),
('baaaaaaa-aaaa-4baa-8aaa-aaaaaaaaaaaa', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', 'Kiran', NULL, 'Rao', NULL, 'Bio 10', 'MALE', '1995-10-10', 'Bangalore', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'REJECTED', 1, 3.80, CURRENT_TIMESTAMP);

---------------------------------------------------------
-- SOCIAL HANDLES (10 entries, 5 users × 2 handles)
---------------------------------------------------------
INSERT INTO social_handle (
  social_handle_uuid, user_uuid, platform, platform_url,
  created_at, updated_at
)
VALUES
('11111111-1111-4111-8111-aaaaaaaaaaaa', '11111111-1111-4111-8111-111111111111', 'INSTAGRAM', 'https://insta.com/user1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('11111111-1111-4111-8111-bbbbbbbbbbbb', '11111111-1111-4111-8111-111111111111', 'YOUTUBE', 'https://linkedin.com/user1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('22222222-2222-4222-8222-cccccccccccc', '22222222-2222-4222-8222-222222222222', 'INSTAGRAM', 'https://insta.com/user2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('22222222-2222-4222-8222-dddddddddddd', '22222222-2222-4222-8222-222222222222', 'YOUTUBE', 'https://x.com/user2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('33333333-3333-4333-8333-eeeeeeeeeeee', '33333333-3333-4333-8333-333333333333', 'INSTAGRAM', 'https://insta.com/user3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('33333333-3333-4333-8333-ffffffffffff', '33333333-3333-4333-8333-333333333333', 'YOUTUBE', 'https://linkedin.com/user3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('44444444-4444-4444-8444-111111111111', '44444444-4444-4444-8444-444444444444', 'INSTAGRAM', 'https://insta.com/admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('44444444-4444-4444-8444-222222222222', '44444444-4444-4444-8444-444444444444', 'YOUTUBE', 'https://x.com/admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('55555555-5555-4555-8555-333333333333', '55555555-5555-4555-8555-555555555555', 'INSTAGRAM', 'https://insta.com/user5', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('55555555-5555-4555-8555-444444444444', '55555555-5555-4555-8555-555555555555', 'YOUTUBE', 'https://linkedin.com/user5', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO verification (
    verification_uuid, user_uuid, document_type, document_number,
    verification_status, verification_remarks, verified_at, verified_by, created_at, updated_at
)
VALUES
('11111111-1111-4111-8111-111111111111', '11111111-1111-4111-8111-111111111111', 'AADHAAR', 'DOC001', 'PENDING', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('22222222-2222-4222-8222-222222222222', '22222222-2222-4222-8222-222222222222', 'PAN', 'DOC002', 'PENDING', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('33333333-3333-4333-8333-333333333333', '33333333-3333-4333-8333-333333333333', 'AADHAAR', 'DOC003', 'APPROVED', 'Verified OK', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('44444444-4444-4444-8444-444444444444', '44444444-4444-4444-8444-444444444444', 'WORK_EMAIL', 'DOC004', 'REJECTED', 'Blurry image', NULL, 'verifier1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('55555555-5555-4555-8555-555555555555', '55555555-5555-4555-8555-555555555555', 'AADHAAR', 'DOC005', 'PENDING', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('66666666-6666-4666-8666-666666666666', '66666666-6666-4666-8666-666666666666', 'AADHAAR', 'DOC006', 'APPROVED', 'Looks good', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('77777777-7777-4777-8777-777777777777', '77777777-7777-4777-8777-777777777777', 'PAN', 'DOC007', 'PENDING', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('88888888-8888-4888-8888-888888888888', '88888888-8888-4888-8888-888888888888', 'WORK_EMAIL', 'DOC008', 'APPROVED', 'Verified', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('99999999-9999-4999-8999-999999999999', '99999999-9999-4999-8999-999999999999', 'AADHAAR', 'DOC009', 'PENDING', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', 'PAN', 'DOC010', 'REJECTED', 'Mismatch', NULL, 'verifier2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);




----------------------------------------------------------------
-- USERS (Assumed to exist from User Service)
----------------------------------------------------------------
-- HOST        -> 11111111-1111-4111-8111-111111111111
-- MEMBER      -> 22222222-2222-4222-8222-222222222222
-- MEMBER      -> 33333333-3333-4333-8333-333333333333
-- RANDOM_USER -> 44444444-4444-4444-8444-444444444444
----------------------------------------------------------------


----------------------------------------------------------------
-- TRIPS
----------------------------------------------------------------
INSERT INTO core_trip_details (
    trip_id,
    trip_title,
    trip_description,
    trip_destination,
    trip_start_date,
    trip_end_date,
    trip_status,
    visibility_status,
    join_policy,
    estimated_budget,
    max_participants,
    current_participants,
    is_full,
    created_at
) VALUES

-- PUBLIC + OPEN
(
    'aaaaaaaa-1111-4111-8111-aaaaaaaaaaaa',
    'Manali Backpacking',
    'Himalayan backpacking trip',
    'Manali',
    '2026-03-01',
    '2026-03-06',
    'PUBLISHED',
    'PUBLIC',
    'OPEN',
    15000,
    10,
    2,
    false,
    CURRENT_TIMESTAMP
),

-- PUBLIC + APPROVAL_REQUIRED
(
    'bbbbbbbb-2222-4222-8222-bbbbbbbbbbbb',
    'Spiti Valley Ride',
    'Bike expedition to Spiti',
    'Spiti',
    '2026-04-10',
    '2026-04-18',
    'PUBLISHED',
    'PUBLIC',
    'APPROVAL_REQUIRED',
    25000,
    8,
    1,
    false,
    CURRENT_TIMESTAMP
),

-- PRIVATE + APPROVAL_REQUIRED
(
    'cccccccc-3333-4333-8333-cccccccccccc',
    'Friends Only Goa Trip',
    'Private beach trip',
    'Goa',
    '2026-02-05',
    '2026-02-10',
    'PUBLISHED',
    'PRIVATE',
    'APPROVAL_REQUIRED',
    12000,
    6,
    1,
    false,
    CURRENT_TIMESTAMP
);

----------------------------------------------------------------
-- TRIP METADATA
----------------------------------------------------------------

--INSERT INTO trip_meta_data (trip_metadata_id,
--    trip_id,
--    trip_summary,
--    whats_included,
--    whats_excluded
--) VALUES (
--    '11111111-2111-4111-8111-111111113451',
--    '11111111-2111-4111-8111-111111111111',
--    null,
--    null,
--    null
--);

----------------------------------------------------------------
-- TRIP MEMBERS
----------------------------------------------------------------
INSERT INTO trip_members (
    membership_id,
    trip_id,
    user_id,
    role,
    status,
    joined_at
) VALUES
(
    'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaa0001',
    'aaaaaaaa-1111-4111-8111-aaaaaaaaaaaa',
    '11111111-1111-4111-8111-111111111111',
    'HOST',
    'ACTIVE',
    CURRENT_TIMESTAMP
),
(
    'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaa0002',
    'aaaaaaaa-1111-4111-8111-aaaaaaaaaaaa',
    '22222222-2222-4222-8222-222222222222',
    'MEMBER',
    'ACTIVE',
    CURRENT_TIMESTAMP
),
(
    'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbb0001',
    'bbbbbbbb-2222-4222-8222-bbbbbbbbbbbb',
    '11111111-1111-4111-8111-111111111111',
    'HOST',
    'ACTIVE',
    CURRENT_TIMESTAMP
),
(
    'cccccccc-cccc-4ccc-8ccc-cccccccc0001',
    'cccccccc-3333-4333-8333-cccccccccccc',
    '88888888-8888-4888-8888-888888888888',
    'HOST',
    'ACTIVE',
    CURRENT_TIMESTAMP
);

----------------------------------------------------------------
-- TRIP QnA (Answered + Unanswered)
----------------------------------------------------------------
INSERT INTO trip_queries (
    query_id,
    trip_id,
    asked_by,
    question,
    answer,
    answered_at,
    visibility,
    created_at
) VALUES

-- Unanswered question (asked by non-member user)
(
    '33333333-7333-4333-8333-333333333331',
    'aaaaaaaa-1111-4111-8111-aaaaaaaaaaaa',
    '55555555-5555-4555-8555-555555555555',
    'Is food included?',
    NULL,
    NULL,
    'HOST_AND_CO_HOSTS',
    '2026-01-05T10:00:00'
),

-- Answered question (asked by member)
(
    '33333333-8333-4333-8333-333333333332',
    'aaaaaaaa-1111-4111-8111-aaaaaaaaaaaa',
    '22222222-2222-4222-8222-222222222222',
    'What accommodation is provided?',
    'Hotels and homestays',
    '2026-01-06T12:00:00',
    'HOST_AND_CO_HOSTS',
    '2026-01-04T09:00:00'
);

----------------------------------------------------------------
-- TRIP INVITES
----------------------------------------------------------------
INSERT INTO trip_invites (
    invite_id,
    trip_id,
    invited_user_id,
    invite_type,
    invite_source,
    status,
    created_at,
    expires_at,
    invited_by
) VALUES

-- Direct in-app invite (private trip)
(
    '11111111-1111-4111-9111-111111111112',
    'cccccccc-3333-4333-8333-cccccccccccc',
    '88888888-8888-4888-8888-888888888888',
    'IN_APPLICATION',
    'DIRECT',
    'PENDING',
    '2026-01-10T10:00:00',
    '2026-01-15T10:00:00',
    '11111111-1111-4111-8111-111111111111'
),

-- Accepted invite
(
    '22222222-2222-4222-9222-222222222223',
    'cccccccc-3333-4333-8333-cccccccccccc',
    '99999999-9999-4999-8999-999999999999',
    'IN_APPLICATION',
    'DIRECT',
    'ACCEPTED',
    '2026-01-08T10:00:00',
    '2026-01-08T10:00:00',
    '11111111-1111-4111-8111-111111111111'
);

----------------------------------------------------------------
-- TRIP JOIN REQUESTS (CORRECT UUIDs)
----------------------------------------------------------------
INSERT INTO trip_join_requests (
    request_id,
    trip_id,
    user_id,
    source,
    status,
    created_at
) VALUES

-- Pending join request for PUBLIC + APPROVAL_REQUIRED trip
(
    'dddddddd-1111-4111-8111-dddddddddddd',
    'bbbbbbbb-2222-4222-8222-bbbbbbbbbbbb',
    '33333333-3333-4333-8333-333333333333',
    'DIRECT',
    'PENDING',
    CURRENT_TIMESTAMP
),

-- Pending join request for PRIVATE trip
(
    'eeeeeeee-2222-4222-8222-eeeeeeeeeeee',
    'cccccccc-3333-4333-8333-cccccccccccc',
    '44444444-4444-4444-8444-444444444444',
    'DIRECT',
    'PENDING',
    CURRENT_TIMESTAMP
);

---------------------------------------------------------
-- SPLITWISE GROUPS (3 groups for testing)
---------------------------------------------------------
INSERT INTO splitwise_groups (id, trip_id, description, created_by, created_at, updated_at)
VALUES
(1, 'aaaaaaaa-1111-4111-8111-aaaaaaaaaaaa', 'Manali Backpacking Group', '11111111-1111-4111-8111-111111111111', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'bbbbbbbb-2222-4222-8222-bbbbbbbbbbbb', 'Spiti Valley Ride Group', '11111111-1111-4111-8111-111111111111', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'cccccccc-3333-4333-8333-cccccccccccc', 'Goa Friends Trip Group', '88888888-8888-4888-8888-888888888888', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

---------------------------------------------------------
-- SPLITWISE GROUP MEMBERS
---------------------------------------------------------
INSERT INTO splitwise_group_members (id, group_id, user_id, role, joined_at)
VALUES
-- Group 1 (Manali) - 4 members
(1, 1, '11111111-1111-4111-8111-111111111111', 'ADMIN', CURRENT_TIMESTAMP),
(2, 1, '22222222-2222-4222-8222-222222222222', 'MEMBER', CURRENT_TIMESTAMP),
(3, 1, '33333333-3333-4333-8333-333333333333', 'MEMBER', CURRENT_TIMESTAMP),
(4, 1, '55555555-5555-4555-8555-555555555555', 'MEMBER', CURRENT_TIMESTAMP),

-- Group 2 (Spiti) - 3 members
(5, 2, '11111111-1111-4111-8111-111111111111', 'ADMIN', CURRENT_TIMESTAMP),
(6, 2, '33333333-3333-4333-8333-333333333333', 'MEMBER', CURRENT_TIMESTAMP),
(7, 2, '66666666-6666-4666-8666-666666666666', 'MEMBER', CURRENT_TIMESTAMP),

-- Group 3 (Goa) - 5 members
(8, 3, '88888888-8888-4888-8888-888888888888', 'ADMIN', CURRENT_TIMESTAMP),
(9, 3, '99999999-9999-4999-8999-999999999999', 'MEMBER', CURRENT_TIMESTAMP),
(10, 3, 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', 'MEMBER', CURRENT_TIMESTAMP),
(11, 3, '77777777-7777-4777-8777-777777777777', 'MEMBER', CURRENT_TIMESTAMP),
(12, 3, '44444444-4444-4444-8444-444444444444', 'MEMBER', CURRENT_TIMESTAMP);

---------------------------------------------------------
-- SPLITWISE EXPENSES (6 expenses across groups)
---------------------------------------------------------
INSERT INTO splitwise_expenses (id, name, description, amount, paid_by, group_id, split_type, expense_date, category, created_at, updated_at)
VALUES
-- Group 1 Expenses
(1, 'Hotel Booking', '3 nights at Mountain View Hotel', 12000.00, '11111111-1111-4111-8111-111111111111', 1, 'EQUAL', '2026-03-01 10:00:00', 'ACCOMMODATION', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Dinner at Restaurant', 'Team dinner at local restaurant', 2400.00, '22222222-2222-4222-8222-222222222222', 1, 'EQUAL', '2026-03-02 19:00:00', 'FOOD', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Taxi to Airport', 'Shared taxi to airport', 800.00, '33333333-3333-4333-8333-333333333333', 1, 'EQUAL', '2026-03-06 08:00:00', 'TRANSPORT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Group 2 Expenses
(4, 'Fuel for Bike Trip', 'Petrol for entire Spiti trip', 8500.00, '11111111-1111-4111-8111-111111111111', 2, 'UNEQUAL', '2026-04-10 09:00:00', 'TRANSPORT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Camping Equipment', 'Tents and camping gear rental', 3600.00, '66666666-6666-4666-8666-666666666666', 2, 'EQUAL', '2026-04-11 14:00:00', 'EQUIPMENT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Group 3 Expenses
(6, 'Beach Resort Booking', '2 nights at beach resort', 15000.00, '88888888-8888-4888-8888-888888888888', 3, 'EQUAL', '2026-02-05 15:00:00', 'ACCOMMODATION', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

---------------------------------------------------------
-- SPLITWISE EXPENSE SPLITS
---------------------------------------------------------
INSERT INTO splitwise_expense_splits (id, expense_id, user_id, amount, percentage, created_at)
VALUES
-- Expense 1 Splits (12000.00 / 4 = 3000.00 each)
(1, 1, '11111111-1111-4111-8111-111111111111', 3000.00, 25.00, CURRENT_TIMESTAMP),
(2, 1, '22222222-2222-4222-8222-222222222222', 3000.00, 25.00, CURRENT_TIMESTAMP),
(3, 1, '33333333-3333-4333-8333-333333333333', 3000.00, 25.00, CURRENT_TIMESTAMP),
(4, 1, '55555555-5555-4555-8555-555555555555', 3000.00, 25.00, CURRENT_TIMESTAMP),

-- Expense 2 Splits (2400.00 / 4 = 600.00 each)
(5, 2, '11111111-1111-4111-8111-111111111111', 600.00, 25.00, CURRENT_TIMESTAMP),
(6, 2, '22222222-2222-4222-8222-222222222222', 600.00, 25.00, CURRENT_TIMESTAMP),
(7, 2, '33333333-3333-4333-8333-333333333333', 600.00, 25.00, CURRENT_TIMESTAMP),
(8, 2, '55555555-5555-4555-8555-555555555555', 600.00, 25.00, CURRENT_TIMESTAMP),

-- Expense 3 Splits (800.00 / 4 = 200.00 each)
(9, 3, '11111111-1111-4111-8111-111111111111', 200.00, 25.00, CURRENT_TIMESTAMP),
(10, 3, '22222222-2222-4222-8222-222222222222', 200.00, 25.00, CURRENT_TIMESTAMP),
(11, 3, '33333333-3333-4333-8333-333333333333', 200.00, 25.00, CURRENT_TIMESTAMP),
(12, 3, '55555555-5555-4555-8555-555555555555', 200.00, 25.00, CURRENT_TIMESTAMP),

-- Expense 4 Splits (UNEQUAL - User1 paid more, gets larger share)
(13, 4, '11111111-1111-4111-8111-111111111111', 4250.00, 50.00, CURRENT_TIMESTAMP),
(14, 4, '33333333-3333-4333-8333-333333333333', 2550.00, 30.00, CURRENT_TIMESTAMP),
(15, 4, '66666666-6666-4666-8666-666666666666', 1700.00, 20.00, CURRENT_TIMESTAMP),

-- Expense 5 Splits (3600.00 / 3 = 1200.00 each)
(16, 5, '11111111-1111-4111-8111-111111111111', 1200.00, 33.33, CURRENT_TIMESTAMP),
(17, 5, '33333333-3333-4333-8333-333333333333', 1200.00, 33.33, CURRENT_TIMESTAMP),
(18, 5, '66666666-6666-4666-8666-666666666666', 1200.00, 33.33, CURRENT_TIMESTAMP),

-- Expense 6 Splits (15000.00 / 5 = 3000.00 each)
(19, 6, '88888888-8888-4888-8888-888888888888', 3000.00, 20.00, CURRENT_TIMESTAMP),
(20, 6, '99999999-9999-4999-8999-999999999999', 3000.00, 20.00, CURRENT_TIMESTAMP),
(21, 6, 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', 3000.00, 20.00, CURRENT_TIMESTAMP),
(22, 6, '77777777-7777-4777-8777-777777777777', 3000.00, 20.00, CURRENT_TIMESTAMP),
(23, 6, '44444444-4444-4444-8444-444444444444', 3000.00, 20.00, CURRENT_TIMESTAMP);

---------------------------------------------------------
-- SPLITWISE BALANCES (calculated based on expenses)
---------------------------------------------------------
INSERT INTO splitwise_balances (id, group_id, owed_by, owed_to, amount, last_updated)
VALUES
-- Group 1 Balances (after expenses 1,2,3)
-- User1 paid 12000+800 = 12800, owes 600+200 = 800, net: +12000 (others owe him)
(1, 1, '22222222-2222-4222-8222-222222222222', '11111111-1111-4111-8111-111111111111', 2200.00, CURRENT_TIMESTAMP),
(2, 1, '33333333-3333-4333-8333-333333333333', '11111111-1111-4111-8111-111111111111', 2800.00, CURRENT_TIMESTAMP),
(3, 1, '55555555-5555-4555-8555-555555555555', '11111111-1111-4111-8111-111111111111', 2800.00, CURRENT_TIMESTAMP),
-- User2 paid 2400, owes 3000+200 = 3200, net: -800 (owes others)
(4, 1, '22222222-2222-4222-8222-222222222222', '33333333-3333-4333-8333-333333333333', 200.00, CURRENT_TIMESTAMP),
(5, 1, '22222222-2222-4222-8222-222222222222', '55555555-5555-4555-8555-555555555555', 200.00, CURRENT_TIMESTAMP),

-- Group 2 Balances (after expenses 4,5)
-- User1 paid 8500+1200 = 9700, owes 4250+1200 = 5450, net: +4250 (others owe him)
(6, 2, '33333333-3333-4333-8333-333333333333', '11111111-1111-4111-8111-111111111111', 1350.00, CURRENT_TIMESTAMP),
(7, 2, '66666666-6666-4666-8666-666666666666', '11111111-1111-4111-8111-111111111111', 2900.00, CURRENT_TIMESTAMP),

-- Group 3 Balances (after expense 6)
-- User8 paid 15000, owes 3000, net: +12000 (others owe him)
(8, 3, '99999999-9999-4999-8999-999999999999', '88888888-8888-4888-8888-888888888888', 3000.00, CURRENT_TIMESTAMP),
(9, 3, 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', '88888888-8888-4888-8888-888888888888', 3000.00, CURRENT_TIMESTAMP),
(10, 3, '77777777-7777-4777-8777-777777777777', '88888888-8888-4888-8888-888888888888', 3000.00, CURRENT_TIMESTAMP),
(11, 3, '44444444-4444-4444-8444-444444444444', '88888888-8888-4888-8888-888888888888', 3000.00, CURRENT_TIMESTAMP);

---------------------------------------------------------
-- SPLITWISE SETTLEMENTS (3 settlements to test settlement functionality)
---------------------------------------------------------
INSERT INTO splitwise_settlements (id, group_id, paid_by, paid_to, amount, payment_method, transaction_id, notes, status, settled_at)
VALUES
-- Settlement 1: User2 pays User1 partial amount
(1, 1, '22222222-2222-4222-8222-222222222222', '11111111-1111-4111-8111-111111111111', 1000.00, 'CASH', 'TXN123456', 'Partial settlement for hotel expenses', 'COMPLETED', CURRENT_TIMESTAMP),

-- Settlement 2: User3 pays User1 partial amount  
(2, 1, '33333333-3333-4333-8333-333333333333', '11111111-1111-4111-8111-111111111111', 1500.00, 'UPI', 'TXN123457', 'Settlement for dinner and taxi', 'COMPLETED', CURRENT_TIMESTAMP),

-- Settlement 3: User6 pays User1 partial amount
(3, 2, '66666666-6666-4666-8666-666666666666', '11111111-1111-4111-8111-111111111111', 2000.00, 'BANK_TRANSFER', 'TXN123458', 'Partial settlement for fuel costs', 'PENDING', CURRENT_TIMESTAMP);

-- Reset settlement ID sequence to avoid conflicts with new settlements (H2 specific)
ALTER TABLE splitwise_settlements ALTER COLUMN id RESTART WITH 4;

---------------------------------------------------------
-- SPLITWISE ACTIVITIES (activity log for testing)
---------------------------------------------------------
INSERT INTO splitwise_activities (id, group_id, user_id, activity_type, related_id, related_type, description, created_at)
VALUES
(1, 1, '11111111-1111-4111-8111-111111111111', 'EXPENSE_ADDED', '1', 'EXPENSE', 'Added expense: Hotel Booking (₹12000.00)', CURRENT_TIMESTAMP),
(2, 1, '22222222-2222-4222-8222-222222222222', 'EXPENSE_ADDED', '2', 'EXPENSE', 'Added expense: Dinner at Restaurant (₹2400.00)', CURRENT_TIMESTAMP),
(3, 1, '33333333-3333-4333-8333-333333333333', 'EXPENSE_ADDED', '3', 'EXPENSE', 'Added expense: Taxi to Airport (₹800.00)', CURRENT_TIMESTAMP),
(4, 1, '22222222-2222-4222-8222-222222222222', 'SETTLEMENT_CREATED', '1', 'SETTLEMENT', 'Settled ₹1000.00 with User1', CURRENT_TIMESTAMP),
(5, 1, '33333333-3333-4333-8333-333333333333', 'SETTLEMENT_CREATED', '2', 'SETTLEMENT', 'Settled ₹1500.00 with User1', CURRENT_TIMESTAMP),
(6, 2, '11111111-1111-4111-8111-111111111111', 'EXPENSE_ADDED', '4', 'EXPENSE', 'Added expense: Fuel for Bike Trip (₹8500.00)', CURRENT_TIMESTAMP),
(7, 2, '66666666-6666-4666-8666-666666666666', 'EXPENSE_ADDED', '5', 'EXPENSE', 'Added expense: Camping Equipment (₹3600.00)', CURRENT_TIMESTAMP),
(8, 2, '66666666-6666-4666-8666-666666666666', 'SETTLEMENT_CREATED', '3', 'SETTLEMENT', 'Settled ₹2000.00 with User1', CURRENT_TIMESTAMP),
(9, 3, '88888888-8888-4888-8888-888888888888', 'EXPENSE_ADDED', '6', 'EXPENSE', 'Added expense: Beach Resort Booking (₹15000.00)', CURRENT_TIMESTAMP);

----------------------------------------------------------------
-- TRIP REPORTS
----------------------------------------------------------------
INSERT INTO trip_reports (
    report_id,
    trip_id,
    reported_by,
    reason,
    status,
    created_at
) VALUES
(
    '91111111-1111-4111-9111-111111111111',
    'aaaaaaaa-1111-4111-8111-aaaaaaaaaaaa',
    '33333333-3333-4333-8333-333333333333',
    'Spam content',
    'OPEN',
    '2026-01-07T10:00:00'
);

---------------------------------------------------------
-- COMPLETED TRIPS (for reputation/rating data)
---------------------------------------------------------
INSERT INTO core_trip_details (
    trip_id, trip_title, trip_description, trip_destination,
    trip_start_date, trip_end_date, trip_status, visibility_status, join_policy,
    estimated_budget, max_participants, current_participants, is_full, created_at
) VALUES
('eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee', 'Rajasthan Heritage Tour', 'Completed heritage tour', 'Jaipur', '2025-12-01', '2025-12-06', 'COMPLETED', 'PUBLIC', 'OPEN', 20000, 6, 4, false, CURRENT_TIMESTAMP),
('ffffffff-ffff-4fff-8fff-ffffffffffff', 'Kerala Backwaters Done', 'Completed backwaters trip', 'Alleppey', '2025-11-10', '2025-11-14', 'COMPLETED', 'PUBLIC', 'APPROVAL_REQUIRED', 18000, 5, 4, false, CURRENT_TIMESTAMP),
('10101010-1010-4101-8101-101010101010', 'Ladakh Road Trip Done', 'Completed Ladakh expedition', 'Leh', '2025-10-01', '2025-10-10', 'COMPLETED', 'PUBLIC', 'OPEN', 35000, 8, 5, false, CURRENT_TIMESTAMP);

---------------------------------------------------------
-- TRIP MEMBERS for COMPLETED trips (host + members)
---------------------------------------------------------
INSERT INTO trip_members (membership_id, trip_id, user_id, role, status, joined_at) VALUES
-- Trip eeeeeeee: host 11111111, members 22222222, 33333333, 55555555
('eeeeeeee-eeee-4eee-8eee-000000000001', 'eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee', '11111111-1111-4111-8111-111111111111', 'HOST', 'ACTIVE', CURRENT_TIMESTAMP),
('eeeeeeee-eeee-4eee-8eee-000000000002', 'eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee', '22222222-2222-4222-8222-222222222222', 'MEMBER', 'ACTIVE', CURRENT_TIMESTAMP),
('eeeeeeee-eeee-4eee-8eee-000000000003', 'eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee', '33333333-3333-4333-8333-333333333333', 'MEMBER', 'ACTIVE', CURRENT_TIMESTAMP),
('eeeeeeee-eeee-4eee-8eee-000000000004', 'eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee', '55555555-5555-4555-8555-555555555555', 'MEMBER', 'ACTIVE', CURRENT_TIMESTAMP),
-- Trip ffffffff: host 66666666, members 77777777, 88888888, 99999999
('ffffffff-ffff-4fff-8fff-000000000001', 'ffffffff-ffff-4fff-8fff-ffffffffffff', '66666666-6666-4666-8666-666666666666', 'HOST', 'ACTIVE', CURRENT_TIMESTAMP),
('ffffffff-ffff-4fff-8fff-000000000002', 'ffffffff-ffff-4fff-8fff-ffffffffffff', '77777777-7777-4777-8777-777777777777', 'MEMBER', 'ACTIVE', CURRENT_TIMESTAMP),
('ffffffff-ffff-4fff-8fff-000000000003', 'ffffffff-ffff-4fff-8fff-ffffffffffff', '88888888-8888-4888-8888-888888888888', 'MEMBER', 'ACTIVE', CURRENT_TIMESTAMP),
('ffffffff-ffff-4fff-8fff-000000000004', 'ffffffff-ffff-4fff-8fff-ffffffffffff', '99999999-9999-4999-8999-999999999999', 'MEMBER', 'ACTIVE', CURRENT_TIMESTAMP),
-- Trip 10101010: host 22222222, members 33333333, 44444444, 55555555, aaaaaaaa
('10101010-1010-4101-8101-000000000001', '10101010-1010-4101-8101-101010101010', '22222222-2222-4222-8222-222222222222', 'HOST', 'ACTIVE', CURRENT_TIMESTAMP),
('10101010-1010-4101-8101-000000000002', '10101010-1010-4101-8101-101010101010', '33333333-3333-4333-8333-333333333333', 'MEMBER', 'ACTIVE', CURRENT_TIMESTAMP),
('10101010-1010-4101-8101-000000000003', '10101010-1010-4101-8101-101010101010', '44444444-4444-4444-8444-444444444444', 'MEMBER', 'ACTIVE', CURRENT_TIMESTAMP),
('10101010-1010-4101-8101-000000000004', '10101010-1010-4101-8101-101010101010', '55555555-5555-4555-8555-555555555555', 'MEMBER', 'ACTIVE', CURRENT_TIMESTAMP),
('10101010-1010-4101-8101-000000000005', '10101010-1010-4101-8101-101010101010', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', 'MEMBER', 'ACTIVE', CURRENT_TIMESTAMP);

---------------------------------------------------------
-- TRIP RATINGS (10+ entries: trip experience ratings)
---------------------------------------------------------
INSERT INTO trip_rating (id, trip_id, rater_user_id, destination_rating, itinerary_rating, overall_rating, created_at) VALUES
('a1000001-0001-4001-8001-000000000001', 'eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee', '11111111-1111-4111-8111-111111111111', 5, 5, 5, CURRENT_TIMESTAMP),
('a1000002-0002-4002-8002-000000000002', 'eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee', '22222222-2222-4222-8222-222222222222', 4, 5, 5, CURRENT_TIMESTAMP),
('a1000003-0003-4003-8003-000000000003', 'eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee', '33333333-3333-4333-8333-333333333333', 5, 4, 4, CURRENT_TIMESTAMP),
('a1000004-0004-4004-8004-000000000004', 'eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee', '55555555-5555-4555-8555-555555555555', 4, 4, 5, CURRENT_TIMESTAMP),
('a1000005-0005-4005-8005-000000000005', 'ffffffff-ffff-4fff-8fff-ffffffffffff', '66666666-6666-4666-8666-666666666666', 5, 5, 5, CURRENT_TIMESTAMP),
('a1000006-0006-4006-8006-000000000006', 'ffffffff-ffff-4fff-8fff-ffffffffffff', '77777777-7777-4777-8777-777777777777', 4, 5, 4, CURRENT_TIMESTAMP),
('a1000007-0007-4007-8007-000000000007', 'ffffffff-ffff-4fff-8fff-ffffffffffff', '88888888-8888-4888-8888-888888888888', 5, 4, 5, CURRENT_TIMESTAMP),
('a1000008-0008-4008-8008-000000000008', 'ffffffff-ffff-4fff-8fff-ffffffffffff', '99999999-9999-4999-8999-999999999999', 4, 4, 4, CURRENT_TIMESTAMP),
('a1000009-0009-4009-8009-000000000009', '10101010-1010-4101-8101-101010101010', '22222222-2222-4222-8222-222222222222', 5, 5, 5, CURRENT_TIMESTAMP),
('a1000010-0010-4010-8010-000000000010', '10101010-1010-4101-8101-101010101010', '33333333-3333-4333-8333-333333333333', 4, 5, 5, CURRENT_TIMESTAMP),
('a1000011-0011-4011-8011-000000000011', '10101010-1010-4101-8101-101010101010', '44444444-4444-4444-8444-444444444444', 5, 4, 4, CURRENT_TIMESTAMP),
('a1000012-0012-4012-8012-000000000012', '10101010-1010-4101-8101-101010101010', '55555555-5555-4555-8555-555555555555', 4, 4, 5, CURRENT_TIMESTAMP);

---------------------------------------------------------
-- HOST RATINGS (10+ entries: rating the host of each trip)
---------------------------------------------------------
INSERT INTO host_rating (id, trip_id, host_user_id, rater_user_id, coordination_rating, communication_rating, leadership_rating, review_text, created_at) VALUES
('b2000001-0001-4001-8001-000000000001', 'eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee', '11111111-1111-4111-8111-111111111111', '22222222-2222-4222-8222-222222222222', 5, 5, 5, 'Excellent coordination', CURRENT_TIMESTAMP),
('b2000002-0002-4002-8002-000000000002', 'eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee', '11111111-1111-4111-8111-111111111111', '33333333-3333-4333-8333-333333333333', 4, 5, 5, 'Great host', CURRENT_TIMESTAMP),
('b2000003-0003-4003-8003-000000000003', 'eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee', '11111111-1111-4111-8111-111111111111', '55555555-5555-4555-8555-555555555555', 5, 4, 5, NULL, CURRENT_TIMESTAMP),
('b2000004-0004-4004-8004-000000000004', 'ffffffff-ffff-4fff-8fff-ffffffffffff', '66666666-6666-4666-8666-666666666666', '77777777-7777-4777-8777-777777777777', 5, 5, 4, 'Very organised', CURRENT_TIMESTAMP),
('b2000005-0005-4005-8005-000000000005', 'ffffffff-ffff-4fff-8fff-ffffffffffff', '66666666-6666-4666-8666-666666666666', '88888888-8888-4888-8888-888888888888', 4, 5, 5, 'Smooth trip', CURRENT_TIMESTAMP),
('b2000006-0006-4006-8006-000000000006', 'ffffffff-ffff-4fff-8fff-ffffffffffff', '66666666-6666-4666-8666-666666666666', '99999999-9999-4999-8999-999999999999', 5, 4, 5, NULL, CURRENT_TIMESTAMP),
('b2000007-0007-4007-8007-000000000007', '10101010-1010-4101-8101-101010101010', '22222222-2222-4222-8222-222222222222', '33333333-3333-4333-8333-333333333333', 5, 5, 5, 'Best trip leader', CURRENT_TIMESTAMP),
('b2000008-0008-4008-8008-000000000008', '10101010-1010-4101-8101-101010101010', '22222222-2222-4222-8222-222222222222', '44444444-4444-4444-8444-444444444444', 4, 5, 4, NULL, CURRENT_TIMESTAMP),
('b2000009-0009-4009-8009-000000000009', '10101010-1010-4101-8101-101010101010', '22222222-2222-4222-8222-222222222222', '55555555-5555-4555-8555-555555555555', 5, 5, 5, 'Amazing', CURRENT_TIMESTAMP),
('b2000010-0010-4010-8010-000000000010', '10101010-1010-4101-8101-101010101010', '22222222-2222-4222-8222-222222222222', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', 4, 4, 5, 'Good coordination', CURRENT_TIMESTAMP);

---------------------------------------------------------
-- MEMBER RATINGS (10+ entries: participant-to-participant; some visible)
---------------------------------------------------------
INSERT INTO member_rating (id, trip_id, rater_user_id, rated_user_id, rating_score, vibe_tag, review_text, visible_at, created_at) VALUES
('c3000001-0001-4001-8001-000000000001', 'eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee', '22222222-2222-4222-8222-222222222222', '33333333-3333-4333-8333-333333333333', 5, 'RELIABLE', 'Great travel buddy', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c3000002-0002-4002-8002-000000000002', 'eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee', '33333333-3333-4333-8333-333333333333', '22222222-2222-4222-8222-222222222222', 5, 'FUNNY', 'Made the trip fun', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c3000003-0003-4003-8003-000000000003', 'eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee', '22222222-2222-4222-8222-222222222222', '55555555-5555-4555-8555-555555555555', 4, 'EXPERT_PLANNER', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c3000004-0004-4004-8004-000000000004', 'eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee', '55555555-5555-4555-8555-555555555555', '22222222-2222-4222-8222-222222222222', 5, 'GREAT_COMPANY', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c3000005-0005-4005-8005-000000000005', 'ffffffff-ffff-4fff-8fff-ffffffffffff', '77777777-7777-4777-8777-777777777777', '88888888-8888-4888-8888-888888888888', 5, 'RELIABLE', 'Very reliable', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c3000006-0006-4006-8006-000000000006', 'ffffffff-ffff-4fff-8fff-ffffffffffff', '88888888-8888-4888-8888-888888888888', '77777777-7777-4777-8777-777777777777', 4, 'HELPFUL', 'Always helpful', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c3000007-0007-4007-8007-000000000007', 'ffffffff-ffff-4fff-8fff-ffffffffffff', '77777777-7777-4777-8777-777777777777', '99999999-9999-4999-8999-999999999999', 5, 'FUNNY', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c3000008-0008-4008-8008-000000000008', 'ffffffff-ffff-4fff-8fff-ffffffffffff', '99999999-9999-4999-8999-999999999999', '88888888-8888-4888-8888-888888888888', 5, 'PUNCTUAL', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c3000009-0009-4009-8009-000000000009', '10101010-1010-4101-8101-101010101010', '33333333-3333-4333-8333-333333333333', '44444444-4444-4444-8444-444444444444', 4, 'RELIABLE', 'Solid', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c3000010-0010-4010-8010-000000000010', '10101010-1010-4101-8101-101010101010', '44444444-4444-4444-8444-444444444444', '55555555-5555-4555-8555-555555555555', 5, 'EXPERT_PLANNER', 'Great planner', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c3000011-0011-4011-8011-000000000011', '10101010-1010-4101-8101-101010101010', '55555555-5555-4555-8555-555555555555', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', 5, 'GREAT_COMPANY', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c3000012-0012-4012-8012-000000000012', '10101010-1010-4101-8101-101010101010', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', '33333333-3333-4333-8333-333333333333', 4, 'FLEXIBLE', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

---------------------------------------------------------
-- USER NOTIFICATIONS (10+ entries)
---------------------------------------------------------
INSERT INTO user_notification (notification_id, user_id, trip_id, type, title, body, read_at, created_at) VALUES
('d4000001-0001-4001-8001-000000000001', '11111111-1111-4111-8111-111111111111', 'aaaaaaaa-1111-4111-8111-aaaaaaaaaaaa', 'TRIP_COMPLETED', 'Trip completed', 'How was your trip? Rate and share feedback.', NULL, CURRENT_TIMESTAMP),
('d4000002-0002-4002-8002-000000000002', '22222222-2222-4222-8222-222222222222', 'aaaaaaaa-1111-4111-8111-aaaaaaaaaaaa', 'TRIP_COMPLETED', 'Trip completed', 'How was your trip?', NULL, CURRENT_TIMESTAMP),
('d4000003-0003-4003-8003-000000000003', '11111111-1111-4111-8111-111111111111', NULL, 'DRAFT_TRIP_REMINDER', 'Complete your draft trip', 'Your trip is still a draft.', NULL, CURRENT_TIMESTAMP),
('d4000004-0004-4004-8004-000000000004', '33333333-3333-4333-8333-333333333333', 'bbbbbbbb-2222-4222-8222-bbbbbbbbbbbb', 'JOIN_REQUEST_APPROVED', 'Join request approved', 'Your request to join Spiti Valley Ride was approved.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('d4000005-0005-4005-8005-000000000005', '22222222-2222-4222-8222-222222222222', 'eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee', 'RATE_YOUR_JOURNEY', 'Rate your Journey', 'How was Rajasthan Heritage Tour? Share your feedback.', NULL, CURRENT_TIMESTAMP),
('d4000006-0006-4006-8006-000000000006', '33333333-3333-4333-8333-333333333333', 'eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee', 'RATE_YOUR_JOURNEY', 'Rate your Journey', 'Share your feedback.', NULL, CURRENT_TIMESTAMP),
('d4000007-0007-4007-8007-000000000007', '55555555-5555-4555-8555-555555555555', 'eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee', 'RATE_YOUR_JOURNEY', 'Rate your Journey', 'Share your feedback.', NULL, CURRENT_TIMESTAMP),
('d4000008-0008-4008-8008-000000000008', '66666666-6666-4666-8666-666666666666', 'ffffffff-ffff-4fff-8fff-ffffffffffff', 'RATE_YOUR_JOURNEY', 'Rate your Journey', 'How was Kerala Backwaters Done?', NULL, CURRENT_TIMESTAMP),
('d4000009-0009-4009-8009-000000000009', '88888888-8888-4888-8888-888888888888', 'cccccccc-3333-4333-8333-cccccccccccc', 'TRIP_INVITED', 'Trip invite', 'You were invited to Friends Only Goa Trip.', NULL, CURRENT_TIMESTAMP),
('d4000010-0010-4010-8010-000000000010', '99999999-9999-4999-8999-999999999999', 'cccccccc-3333-4333-8333-cccccccccccc', 'TRIP_INVITED', 'Trip invite', 'You were invited to Goa trip.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('d4000011-0011-4011-8011-000000000011', '44444444-4444-4444-8444-444444444444', 'aaaaaaaa-1111-4111-8111-aaaaaaaaaaaa', 'UPCOMING_TRIP', 'Trip starting soon', 'Manali Backpacking starts soon.', NULL, CURRENT_TIMESTAMP),
('d4000012-0012-4012-8012-000000000012', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', '10101010-1010-4101-8101-101010101010', 'RATE_YOUR_JOURNEY', 'Rate your Journey', 'How was Ladakh Road Trip Done?', NULL, CURRENT_TIMESTAMP);

---------------------------------------------------------
-- TRAVEL PALS (10+ entries: user pairs with status)
-- user_low_id < user_high_id (lexicographic UUID order)
---------------------------------------------------------
INSERT INTO travel_pal (travel_pal_uuid, user_low_id, user_high_id, requested_by, status, created_at, updated_at) VALUES
('e5000001-0001-4001-8001-000000000001', '11111111-1111-4111-8111-111111111111', '22222222-2222-4222-8222-222222222222', '11111111-1111-4111-8111-111111111111', 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e5000002-0002-4002-8002-000000000002', '11111111-1111-4111-8111-111111111111', '33333333-3333-4333-8333-333333333333', '33333333-3333-4333-8333-333333333333', 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e5000003-0003-4003-8003-000000000003', '11111111-1111-4111-8111-111111111111', '55555555-5555-4555-8555-555555555555', '55555555-5555-4555-8555-555555555555', 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e5000004-0004-4004-8004-000000000004', '22222222-2222-4222-8222-222222222222', '33333333-3333-4333-8333-333333333333', '22222222-2222-4222-8222-222222222222', 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e5000005-0005-4005-8005-000000000005', '22222222-2222-4222-8222-222222222222', '44444444-4444-4444-8444-444444444444', '44444444-4444-4444-8444-444444444444', 'REJECTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e5000006-0006-4006-8006-000000000006', '33333333-3333-4333-8333-333333333333', '55555555-5555-4555-8555-555555555555', '33333333-3333-4333-8333-333333333333', 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e5000007-0007-4007-8007-000000000007', '66666666-6666-4666-8666-666666666666', '77777777-7777-4777-8777-777777777777', '66666666-6666-4666-8666-666666666666', 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e5000008-0008-4008-8008-000000000008', '66666666-6666-4666-8666-666666666666', '88888888-8888-4888-8888-888888888888', '88888888-8888-4888-8888-888888888888', 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e5000009-0009-4009-8009-000000000009', '77777777-7777-4777-8777-777777777777', '99999999-9999-4999-8999-999999999999', '77777777-7777-4777-8777-777777777777', 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e5000010-0010-4010-8010-000000000010', '88888888-8888-4888-8888-888888888888', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e5000011-0011-4011-8011-000000000011', '99999999-9999-4999-8999-999999999999', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', '99999999-9999-4999-8999-999999999999', 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e5000012-0012-4012-8012-000000000012', '44444444-4444-4444-8444-444444444444', '55555555-5555-4555-8555-555555555555', '55555555-5555-4555-8555-555555555555', 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
