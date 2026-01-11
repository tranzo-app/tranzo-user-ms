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
    created_at, updated_at, verification_status, version
)
VALUES
('b1111111-1111-4b11-8111-111111111111', '11111111-1111-4111-8111-111111111111', 'John', NULL, 'Doe', NULL, 'Bio 1', 'MALE', '1995-01-01', 'Bangalore', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'PENDING', 1),
('b2222222-2222-4b22-8222-222222222222', '22222222-2222-4222-8222-222222222222', 'Mary', NULL, 'Jane', NULL, 'Bio 2', 'FEMALE', '1997-02-02', 'Hyderabad', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'PENDING', 1),
('b3333333-3333-4b33-8333-333333333333', '33333333-3333-4333-8333-333333333333', 'Ravi', 'K', 'Sharma', NULL, 'Bio 3', 'MALE', '1993-03-03', 'Chennai', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'APPROVED', 1),
('b4444444-4444-4b44-8444-444444444444', '44444444-4444-4444-8444-444444444444', 'Admin', NULL, 'User', NULL, 'Admin Bio', 'MALE', '1990-04-04', 'Delhi', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'REJECTED', 1),
('b5555555-5555-4b55-8555-555555555555', '55555555-5555-4555-8555-555555555555', 'Priya', NULL, 'Singh', NULL, 'Bio 5', 'FEMALE', '1996-05-05', 'Mumbai', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'PENDING', 1),
('b6666666-6666-4b66-8666-666666666666', '66666666-6666-4666-8666-666666666666', 'Amit', NULL, 'Khan', NULL, 'Bio 6', 'MALE', '1992-06-06', 'Pune', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'APPROVED', 1),
('b7777777-7777-4b77-8777-777777777777', '77777777-7777-4777-8777-777777777777', 'Neha', NULL, 'Verma', NULL, 'Bio 7', 'FEMALE', '1994-07-07', 'Kolkata', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'PENDING', 1),
('b8888888-8888-4b88-8888-888888888888', '88888888-8888-4888-8888-888888888888', 'Arun', NULL, 'Nair', NULL, 'Bio 8', 'MALE', '1991-08-08', 'Delhi', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'APPROVED', 1),
('b9999999-9999-4b99-8999-999999999999', '99999999-9999-4999-8999-999999999999', 'Sara', NULL, 'Mathews', NULL, 'Bio 9', 'FEMALE', '1998-09-09', 'Kochi', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'PENDING', 1),
('baaaaaaa-aaaa-4baa-8aaa-aaaaaaaaaaaa', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', 'Kiran', NULL, 'Rao', NULL, 'Bio 10', 'MALE', '1995-10-10', 'Bangalore', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'REJECTED', 1);

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
    verification_uuid, user_uuid, document_type, document_number, document_image_url,
    verification_status, verification_remarks, verified_at, verified_by, created_at, updated_at
)
VALUES
('11111111-1111-4111-8111-111111111111', '11111111-1111-4111-8111-111111111111', 'AADHAAR', 'DOC001', 'url1', 'PENDING', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('22222222-2222-4222-8222-222222222222', '22222222-2222-4222-8222-222222222222', 'PAN', 'DOC002', 'url2', 'PENDING', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('33333333-3333-4333-8333-333333333333', '33333333-3333-4333-8333-333333333333', 'AADHAAR', 'DOC003', 'url3', 'APPROVED', 'Verified OK', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('44444444-4444-4444-8444-444444444444', '44444444-4444-4444-8444-444444444444', 'WORK_EMAIL', 'DOC004', 'url4', 'REJECTED', 'Blurry image', NULL, 'verifier1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('55555555-5555-4555-8555-555555555555', '55555555-5555-4555-8555-555555555555', 'AADHAAR', 'DOC005', 'url5', 'PENDING', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('66666666-6666-4666-8666-666666666666', '66666666-6666-4666-8666-666666666666', 'AADHAAR', 'DOC006', 'url6', 'APPROVED', 'Looks good', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('77777777-7777-4777-8777-777777777777', '77777777-7777-4777-8777-777777777777', 'PAN', 'DOC007', 'url7', 'PENDING', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('88888888-8888-4888-8888-888888888888', '88888888-8888-4888-8888-888888888888', 'WORK_EMAIL', 'DOC008', 'url8', 'APPROVED', 'Verified', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('99999999-9999-4999-8999-999999999999', '99999999-9999-4999-8999-999999999999', 'AADHAAR', 'DOC009', 'url9', 'PENDING', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', 'PAN', 'DOC010', 'url10', 'REJECTED', 'Mismatch', NULL, 'verifier2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);




----------------------------------------------------------------
-- USERS (Assumed to exist from User Service)
----------------------------------------------------------------
-- HOST        -> 11111111-1111-1111-1111-111111111111
-- MEMBER      -> 22222222-2222-2222-2222-222222222222
-- MEMBER      -> 33333333-3333-3333-3333-333333333333
-- RANDOM_USER -> 44444444-4444-4444-4444-444444444444
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

-- Published public trip (normal flow)
(
    '11111111-2111-4111-8111-111111111111',
    'Manali Trek',
    'Snow trek',
    'goa',
    '2025-12-01',
    '2025-12-05',
    'PUBLISHED',
    'PUBLIC',
    'OPEN',
    9999,
    10,
    1,
    false,
    '2025-11-01T10:00:00'
),

-- Completed trip (QnA blocked)
(
    '11111111-3111-4111-8111-111111111111',
    'Oty Trip',
    'beach trek',
    'chennai',
    '2025-12-01',
    '2025-12-05',
    'COMPLETED',
    'PUBLIC',
    'OPEN',
    10000,
    4,
    4,
    true,
    '2025-11-01T10:00:00'
),

-- Private trip (approval required)
(
    '11111111-4111-4111-8111-111111111111',
    'Oty Trip',
    'beach trek',
    'chennai',
    '2025-12-01',
    '2025-12-05',
    'PUBLISHED',
    'PRIVATE',
    'APPROVAL_REQUIRED',
    10000,
    4,
    4,
    true,
    '2025-11-01T10:00:00'
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

-- Host
(
    '11111111-2111-4111-8112-111111111111',
    '11111111-2111-4111-8111-111111111111',
    '11111111-1111-1111-1111-111111111111',
    'HOST',
    'ACTIVE',
    '2026-01-01T10:00:00'
),


(
    '11111111-2111-4111-8113-111111111111',
    '11111111-2111-4111-8111-111111111111',
    '22222222-2222-2222-2222-222222222222',
    'MEMBER',
    'ACTIVE',
    '2026-01-02T10:00:00'
),

-- Member
(
    '11111111-2111-4111-8114-111111111111',
    '11111111-2111-4111-8111-111111111111',
    '33333333-3333-3333-3333-333333333333',
    'MEMBER',
    'ACTIVE',
    '2026-01-03T10:00:00'
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

-- Unanswered question
(
    '33333333-7333-3333-3333-333333333331',
    '11111111-2111-4111-8111-111111111111',
    '55555555-5555-4555-8555-555555555555',
    'Is food included?',
    NULL,
    NULL,
    'HOST_AND_CO_HOSTS',
    '2026-01-05T10:00:00'
),

-- Answered question
(
    '33333333-8333-3333-3333-333333333332',
    '11111111-2111-4111-8111-111111111111',
    '33333333-3333-3333-3333-333333333333',
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

-- Direct in-app invite
(
    '11111111-1111-1111-9111-111111111112',
    '11111111-2111-4111-8111-111111111111',
    '88888888-8888-4888-8888-888888888888',
    'IN_APPLICATION',
    'DIRECT',
    'PENDING',
    '2026-01-10T10:00:00',
    '2026-01-15T10:00:00',
    '11111111-1111-1111-1111-111111111111'
),

-- Broadcast link invite
(
    '22222222-2222-2222-9222-222222222223',
    '11111111-2111-4111-8111-111111111111',
    '99999999-9999-4999-8999-999999999999',
    'IN_APPLICATION',
    'DIRECT',
    'ACCEPTED',
    '2026-01-08T10:00:00',
    '2026-01-08T10:00:00',
    '11111111-1111-1111-1111-111111111111'
);

----------------------------------------------------------------
-- TRIP JOIN REQUESTS
----------------------------------------------------------------
INSERT INTO trip_join_requests (
    request_id,
    trip_id,
    user_id,
    source,
    status,
    created_at
) VALUES

-- Pending join request
(
    '22111111-1111-1111-1111-111111111111',
    '11111111-2111-4111-8111-111111111111',
    '44444444-4444-4444-8444-444444444444',
    'DIRECT',
    'PENDING',
    '2026-01-11T10:00:00'
),

-- Auto-approved join
(
    '33222222-2222-2222-2222-222222222222',
    '11111111-2111-4111-8111-111111111111',
    '66666666-6666-4666-8666-666666666666',
    'DIRECT',
    'AUTO_APPROVED',
    '2026-01-03T10:00:00'
);

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
        '91111111-1111-1111-1111-111111111111',
        '11111111-2111-4111-8111-111111111111',
        '33333333-3333-3333-3333-333333333333',
        'Spam content',
        'OPEN',
        '2026-01-07T10:00:00'
    );
