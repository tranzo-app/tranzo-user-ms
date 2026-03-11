-- Baseline schema: all tables in dependency order.
-- Compatible with H2 and PostgreSQL (TEXT used for JSON columns for portability).
-- Do not edit after this migration has been applied; add V2, V3... for changes.

-- ---------------------------------------------------------------------------
-- 1. User domain (users first; others reference it)
-- ---------------------------------------------------------------------------
CREATE TABLE users (
    user_uuid         UUID         NOT NULL,
    country_code      VARCHAR(5),
    email             VARCHAR(255),
    mobile_number     VARCHAR(15),
    created_at        TIMESTAMP    NOT NULL,
    updated_at        TIMESTAMP,
    role              VARCHAR(255) NOT NULL,
    account_status    VARCHAR(255) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (user_uuid),
    CONSTRAINT uk_app_user_mobile UNIQUE (country_code, mobile_number),
    CONSTRAINT uk_app_user_email  UNIQUE (email)
);

CREATE TABLE user_profile (
    user_profile_uuid      UUID          NOT NULL,
    user_uuid              UUID          NOT NULL,
    "FIRST_NAME"           VARCHAR(255)  NOT NULL,
    "MIDDLE_NAME"          VARCHAR(255),
    "LAST_NAME"            VARCHAR(255),
    "PROFILE_PICTURE_URL"  VARCHAR(255),
    "BIO"                  VARCHAR(255),
    "GENDER"               VARCHAR(255)  NOT NULL,
    "DATE_OF_BIRTH"        DATE          NOT NULL,
    "LOCATION"             VARCHAR(255),
    created_at             TIMESTAMP    NOT NULL,
    updated_at             TIMESTAMP,
    verification_status    VARCHAR(255)  NOT NULL,
    version                INTEGER      NOT NULL DEFAULT 1,
    trust_score            NUMERIC(5,2),
    trust_score_updated_at TIMESTAMP,
    PRIMARY KEY (user_profile_uuid),
    CONSTRAINT fk_user_profile_user FOREIGN KEY (user_uuid) REFERENCES users (user_uuid),
    CONSTRAINT uk_user_profile_user UNIQUE (user_uuid)
);

CREATE TABLE user_profile_history_table (
    user_profile_history_id UUID          NOT NULL,
    user_profile_uuid       UUID          NOT NULL,
    user_uuid               UUID          NOT NULL,
    "FIRST_NAME"            VARCHAR(255)  NOT NULL,
    "MIDDLE_NAME"           VARCHAR(255),
    "LAST_NAME"             VARCHAR(255),
    "PROFILE_PICTURE_URL"   VARCHAR(255),
    "BIO"                   VARCHAR(255),
    "GENDER"                VARCHAR(255)  NOT NULL,
    "DATE_OF_BIRTH"         DATE          NOT NULL,
    "LOCATION"              VARCHAR(255),
    created_at              TIMESTAMP    NOT NULL,
    updated_at              TIMESTAMP,
    verification_status    VARCHAR(255)  NOT NULL,
    version                 INTEGER      NOT NULL,
    PRIMARY KEY (user_profile_history_id),
    CONSTRAINT uk_user_profile_history_user_profile_uuid_version UNIQUE (user_profile_uuid, version),
    CONSTRAINT fk_user_profile_history_user FOREIGN KEY (user_uuid) REFERENCES users (user_uuid)
);

CREATE TABLE refresh_token (
    refresh_token_uuid UUID         NOT NULL,
    user_uuid         UUID         NOT NULL,
    token_hash        VARCHAR(255) NOT NULL,
    expires_at        TIMESTAMP    NOT NULL,
    revoked           BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP    NOT NULL,
    updated_at        TIMESTAMP,
    PRIMARY KEY (refresh_token_uuid),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_uuid) REFERENCES users (user_uuid),
    CONSTRAINT uk_refresh_token_hash UNIQUE (token_hash)
);
CREATE INDEX idx_refresh_token_user_revoked ON refresh_token (user_uuid, revoked);

CREATE TABLE verification (
    verification_uuid     UUID          NOT NULL,
    user_uuid            UUID          NOT NULL,
    document_type        VARCHAR(255) NOT NULL,
    document_number      VARCHAR(20)  NOT NULL,
    provider_reference_id VARCHAR(255),
    verification_status  VARCHAR(255) NOT NULL,
    verification_remarks  TEXT,
    verified_at          TIMESTAMP,
    verified_by          VARCHAR(255),
    created_at           TIMESTAMP,
    updated_at           TIMESTAMP,
    PRIMARY KEY (verification_uuid),
    CONSTRAINT fk_verification_user FOREIGN KEY (user_uuid) REFERENCES users (user_uuid),
    CONSTRAINT uk_verification_user_document_type UNIQUE (user_uuid, document_type)
);

CREATE TABLE aadhar_otp (
    id             UUID         NOT NULL,
    user_id        UUID         NOT NULL,
    reference_id   VARCHAR(255) NOT NULL,
    aadhaar_number VARCHAR(255) NOT NULL,
    used           BOOLEAN      NOT NULL DEFAULT FALSE,
    otp_status     VARCHAR(255) NOT NULL,
    expires_at     TIMESTAMP    NOT NULL,
    created_at     TIMESTAMP    NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE social_handle (
    social_handle_uuid UUID          NOT NULL,
    user_uuid         UUID          NOT NULL,
    platform          VARCHAR(255)  NOT NULL,
    platform_url      VARCHAR(255)  NOT NULL,
    created_at        TIMESTAMP     NOT NULL,
    updated_at        TIMESTAMP,
    PRIMARY KEY (social_handle_uuid),
    CONSTRAINT fk_social_handle_user FOREIGN KEY (user_uuid) REFERENCES users (user_uuid)
);
CREATE INDEX idx_social_handle_user ON social_handle (user_uuid);

CREATE TABLE travel_pal (
    travel_pal_uuid UUID         NOT NULL,
    user_low_id     UUID         NOT NULL,
    user_high_id    UUID         NOT NULL,
    requested_by    UUID         NOT NULL,
    status          VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,
    PRIMARY KEY (travel_pal_uuid),
    CONSTRAINT uk_travel_pal_unique_pair UNIQUE (user_low_id, user_high_id)
);
CREATE INDEX idx_user_low  ON travel_pal (user_low_id);
CREATE INDEX idx_user_high ON travel_pal (user_high_id);

CREATE TABLE user_reports (
    report_id          UUID         NOT NULL,
    reported_user_id   UUID         NOT NULL,
    reporting_user_id  UUID         NOT NULL,
    message            VARCHAR(255) NOT NULL,
    reported_at        TIMESTAMP    NOT NULL,
    PRIMARY KEY (report_id),
    CONSTRAINT uk_reported_reporting_user UNIQUE (reported_user_id, reporting_user_id)
);

-- ---------------------------------------------------------------------------
-- 2. Trip domain (core_trip_details first; others reference it)
-- ---------------------------------------------------------------------------
CREATE TABLE core_trip_details (
    trip_id             UUID      NOT NULL,
    trip_description    TEXT,
    trip_title          VARCHAR(255),
    trip_destination    VARCHAR(255),
    trip_start_date     DATE,
    trip_end_date       DATE,
    trip_status         VARCHAR(255) NOT NULL,
    estimated_budget    DOUBLE PRECISION,
    max_participants    INTEGER,
    current_participants INTEGER DEFAULT 0,
    is_full             BOOLEAN   DEFAULT FALSE,
    trip_full_reason    VARCHAR(255),
    full_marked_at      TIMESTAMP,
    join_policy         VARCHAR(255),
    visibility_status   VARCHAR(255),
    created_at          TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP,
    conversation_id     UUID,
    PRIMARY KEY (trip_id)
);
CREATE INDEX idx_trip_status            ON core_trip_details (trip_status);
CREATE INDEX idx_trip_status_start_date ON core_trip_details (trip_status, trip_start_date);
CREATE INDEX idx_trip_status_end_date   ON core_trip_details (trip_status, trip_end_date);

CREATE TABLE trip_policies (
    trip_id              UUID          NOT NULL,
    cancellation_policy  VARCHAR(500),
    refund_policy        VARCHAR(500),
    PRIMARY KEY (trip_id),
    CONSTRAINT fk_trip_policy_trip FOREIGN KEY (trip_id) REFERENCES core_trip_details (trip_id)
);

CREATE TABLE trip_meta_data (
    trip_id        UUID NOT NULL,
    trip_summary   TEXT,
    whats_included TEXT,
    whats_excluded TEXT,
    PRIMARY KEY (trip_id),
    CONSTRAINT fk_trip_meta_data_trip FOREIGN KEY (trip_id) REFERENCES core_trip_details (trip_id)
);

CREATE TABLE tags (
    tag_id   UUID         NOT NULL,
    tag_name VARCHAR(255) NOT NULL,
    PRIMARY KEY (tag_id),
    CONSTRAINT uk_tags_tag_name UNIQUE (tag_name)
);

CREATE TABLE trip_tag (
    trip_id UUID NOT NULL,
    tag_id  UUID NOT NULL,
    PRIMARY KEY (trip_id, tag_id),
    CONSTRAINT fk_trip_tag_trip FOREIGN KEY (trip_id) REFERENCES core_trip_details (trip_id),
    CONSTRAINT fk_trip_tag_tag  FOREIGN KEY (tag_id)  REFERENCES tags (tag_id)
);

CREATE TABLE trip_itineraries (
    itinerary_id UUID      NOT NULL,
    trip_id      UUID      NOT NULL,
    day_number   INTEGER   NOT NULL,
    title        VARCHAR(255),
    description  TEXT,
    activities   TEXT,
    meals        TEXT,
    stay         TEXT,
    created_at   TIMESTAMP NOT NULL,
    PRIMARY KEY (itinerary_id),
    CONSTRAINT fk_trip_itinerary_trip FOREIGN KEY (trip_id) REFERENCES core_trip_details (trip_id),
    CONSTRAINT uk_trip_itinerary_trip_day UNIQUE (trip_id, day_number)
);

CREATE TABLE trip_invites (
    invite_id       UUID         NOT NULL,
    trip_id         UUID         NOT NULL,
    invited_by      UUID         NOT NULL,
    invite_type     VARCHAR(20)  NOT NULL,
    invite_source   VARCHAR(30)  NOT NULL,
    invited_user_id UUID,
    invited_email   VARCHAR(255),
    invited_phone   VARCHAR(30),
    token_hash      VARCHAR(255),
    status          VARCHAR(20)  NOT NULL,
    expires_at      TIMESTAMP,
    last_reminded_at TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL,
    PRIMARY KEY (invite_id),
    CONSTRAINT fk_trip_invite_trip FOREIGN KEY (trip_id) REFERENCES core_trip_details (trip_id),
    CONSTRAINT UniqueTripIdAndInvitedUserId UNIQUE (trip_id, invited_user_id),
    CONSTRAINT UniqueTripIdAndInvitedEmail  UNIQUE (trip_id, invited_email),
    CONSTRAINT UniqueTripIdAndInvitedPhone UNIQUE (trip_id, invited_phone),
    CONSTRAINT uk_trip_invite_token UNIQUE (token_hash)
);
CREATE INDEX idx_trip_invite_trip   ON trip_invites (trip_id);
CREATE INDEX idx_trip_invite_status ON trip_invites (status);
CREATE INDEX idx_trip_invite_token  ON trip_invites (token_hash);

CREATE TABLE trip_join_requests (
    request_id   UUID         NOT NULL,
    trip_id      UUID         NOT NULL,
    user_id      UUID         NOT NULL,
    source       VARCHAR(20)  NOT NULL,
    status       VARCHAR(20)  NOT NULL,
    reviewed_by  UUID,
    reviewed_at  TIMESTAMP,
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP,
    PRIMARY KEY (request_id),
    CONSTRAINT fk_trip_join_request_trip FOREIGN KEY (trip_id) REFERENCES core_trip_details (trip_id)
);
CREATE INDEX idx_join_requests_trip_status ON trip_join_requests (trip_id, status);
CREATE INDEX idx_join_requests_trip_user  ON trip_join_requests (trip_id, user_id);

CREATE TABLE trip_members (
    membership_id  UUID         NOT NULL,
    trip_id        UUID         NOT NULL,
    user_id        UUID         NOT NULL,
    role           VARCHAR(20)  NOT NULL,
    status         VARCHAR(20)  NOT NULL,
    joined_at      TIMESTAMP    NOT NULL,
    exited_at      TIMESTAMP,
    exited_by      UUID,
    removal_reason VARCHAR(255),
    PRIMARY KEY (membership_id),
    CONSTRAINT fk_trip_member_trip FOREIGN KEY (trip_id) REFERENCES core_trip_details (trip_id),
    CONSTRAINT uk_trip_member_trip_user UNIQUE (trip_id, user_id)
);
CREATE INDEX idx_members_trip_status ON trip_members (trip_id, status);
CREATE INDEX idx_members_user_id    ON trip_members (user_id);

CREATE TABLE trip_queries (
    query_id    UUID         NOT NULL,
    trip_id     UUID         NOT NULL,
    asked_by    UUID         NOT NULL,
    question    VARCHAR(255) NOT NULL,
    answer      TEXT,
    visibility  VARCHAR(30)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL,
    answered_at TIMESTAMP,
    PRIMARY KEY (query_id),
    CONSTRAINT fk_trip_query_trip FOREIGN KEY (trip_id) REFERENCES core_trip_details (trip_id)
);
CREATE INDEX idx_queries_trip ON trip_queries (trip_id);

CREATE TABLE trip_reports (
    report_id   UUID         NOT NULL,
    trip_id     UUID         NOT NULL,
    reported_by UUID         NOT NULL,
    reason      VARCHAR(255) NOT NULL,
    status      VARCHAR(20)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL,
    PRIMARY KEY (report_id),
    CONSTRAINT fk_trip_report_trip FOREIGN KEY (trip_id) REFERENCES core_trip_details (trip_id),
    CONSTRAINT uk_trip_report_trip_reporter UNIQUE (trip_id, reported_by)
);
CREATE INDEX idx_trip_reports_trip   ON trip_reports (trip_id);
CREATE INDEX idx_trip_reports_status ON trip_reports (status);

CREATE TABLE trip_wishlists (
    trip_wishlist_id UUID      NOT NULL,
    trip_id          UUID      NOT NULL,
    user_id          UUID      NOT NULL,
    created_at       TIMESTAMP NOT NULL,
    PRIMARY KEY (trip_wishlist_id),
    CONSTRAINT fk_trip_wishlist_trip FOREIGN KEY (trip_id) REFERENCES core_trip_details (trip_id),
    CONSTRAINT uk_trip_wishlist_trip_user UNIQUE (trip_id, user_id)
);
CREATE INDEX idx_wishlist_user_created ON trip_wishlists (user_id, created_at);

CREATE TABLE trip_rating (
    id                 UUID      NOT NULL,
    trip_id            UUID      NOT NULL,
    rater_user_id      UUID      NOT NULL,
    destination_rating INTEGER   NOT NULL,
    itinerary_rating   INTEGER   NOT NULL,
    overall_rating     INTEGER   NOT NULL,
    created_at         TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_trip_rating_trip FOREIGN KEY (trip_id) REFERENCES core_trip_details (trip_id),
    CONSTRAINT uk_trip_rating_trip_rater UNIQUE (trip_id, rater_user_id)
);
CREATE INDEX idx_trip_rating_trip_id       ON trip_rating (trip_id);
CREATE INDEX idx_trip_rating_rater_user_id ON trip_rating (rater_user_id);

CREATE TABLE member_rating (
    id            UUID         NOT NULL,
    trip_id       UUID         NOT NULL,
    rater_user_id UUID         NOT NULL,
    rated_user_id UUID         NOT NULL,
    rating_score  INTEGER      NOT NULL,
    vibe_tag      VARCHAR(30),
    review_text   TEXT,
    visible_at    TIMESTAMP,
    created_at    TIMESTAMP    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_member_rating_trip FOREIGN KEY (trip_id) REFERENCES core_trip_details (trip_id),
    CONSTRAINT uk_member_rating_trip_rater_rated UNIQUE (trip_id, rater_user_id, rated_user_id)
);
CREATE INDEX idx_member_rating_trip_id       ON member_rating (trip_id);
CREATE INDEX idx_member_rating_rater_user_id  ON member_rating (rater_user_id);
CREATE INDEX idx_member_rating_rated_user_id  ON member_rating (rated_user_id);
CREATE INDEX idx_member_rating_visible_at     ON member_rating (visible_at);

CREATE TABLE host_rating (
    id                   UUID         NOT NULL,
    trip_id              UUID         NOT NULL,
    host_user_id         UUID         NOT NULL,
    rater_user_id        UUID         NOT NULL,
    coordination_rating  INTEGER      NOT NULL,
    communication_rating INTEGER      NOT NULL,
    leadership_rating    INTEGER      NOT NULL,
    review_text          TEXT,
    created_at           TIMESTAMP    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_host_rating_trip FOREIGN KEY (trip_id) REFERENCES core_trip_details (trip_id),
    CONSTRAINT uk_host_rating_trip_rater UNIQUE (trip_id, rater_user_id)
);
CREATE INDEX idx_host_rating_trip_id       ON host_rating (trip_id);
CREATE INDEX idx_host_rating_host_user_id   ON host_rating (host_user_id);
CREATE INDEX idx_host_rating_rater_user_id  ON host_rating (rater_user_id);

CREATE TABLE task_lock (
    task_id        VARCHAR(255) NOT NULL,
    last_execution BIGINT       NOT NULL,
    PRIMARY KEY (task_id)
);

-- ---------------------------------------------------------------------------
-- 3. Chat domain
-- ---------------------------------------------------------------------------
CREATE TABLE conversation (
    conversation_id UUID         NOT NULL,
    type            VARCHAR(255) NOT NULL,
    created_by      UUID         NOT NULL,
    created_at      TIMESTAMP    NOT NULL,
    name            VARCHAR(255),
    PRIMARY KEY (conversation_id)
);

CREATE TABLE conversation_participant (
    id              UUID         NOT NULL,
    conversation_id UUID         NOT NULL,
    user_id         UUID         NOT NULL,
    role            VARCHAR(255) NOT NULL,
    joined_at       TIMESTAMP    NOT NULL,
    left_at         TIMESTAMP,
    last_read_at    TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_cp_conversation FOREIGN KEY (conversation_id) REFERENCES conversation (conversation_id),
    CONSTRAINT uk_conversation_user UNIQUE (conversation_id, user_id)
);
CREATE INDEX idx_cp_user_left ON conversation_participant (user_id, left_at);

CREATE TABLE message (
    message_id      UUID         NOT NULL,
    conversation_id UUID         NOT NULL,
    sender_id       UUID,
    content         VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP    NOT NULL,
    PRIMARY KEY (message_id),
    CONSTRAINT fk_message_conversation FOREIGN KEY (conversation_id) REFERENCES conversation (conversation_id)
);
CREATE INDEX idx_msg_conversation_created ON message (conversation_id, created_at);

CREATE TABLE conversation_mute (
    id              UUID      NOT NULL,
    conversation_id UUID      NOT NULL,
    user_id         UUID      NOT NULL,
    muted_at        TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_mute_conversation FOREIGN KEY (conversation_id) REFERENCES conversation (conversation_id),
    CONSTRAINT uk_mute_unique UNIQUE (conversation_id, user_id)
);

CREATE TABLE conversation_block (
    id              UUID         NOT NULL,
    conversation_id UUID         NOT NULL,
    blocked_by      UUID         NOT NULL,
    created_at      TIMESTAMP    NOT NULL,
    status          VARCHAR(255) NOT NULL DEFAULT 'BLOCKED',
    PRIMARY KEY (id),
    CONSTRAINT fk_block_conversation FOREIGN KEY (conversation_id) REFERENCES conversation (conversation_id),
    CONSTRAINT uk_block_unique UNIQUE (conversation_id, blocked_by)
);

-- ---------------------------------------------------------------------------
-- 4. Notification domain
-- ---------------------------------------------------------------------------
CREATE TABLE user_notification (
    notification_id UUID         NOT NULL,
    user_id         UUID         NOT NULL,
    trip_id         UUID,
    type            VARCHAR(50)  NOT NULL,
    title           VARCHAR(255) NOT NULL,
    body            VARCHAR(1000),
    read_at         TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL,
    PRIMARY KEY (notification_id)
);
CREATE INDEX idx_user_notification_user_id  ON user_notification (user_id);
CREATE INDEX idx_user_notification_read_at ON user_notification (user_id, read_at);
