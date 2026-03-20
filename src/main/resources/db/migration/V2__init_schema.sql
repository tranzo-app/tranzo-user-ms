-- ========== users & profile ==========
CREATE TABLE users (
  user_uuid         UUID PRIMARY KEY,
  country_code      VARCHAR(16),
  email             VARCHAR(255),
  mobile_number     VARCHAR(32),
  created_at        TIMESTAMP NOT NULL,
  updated_at        TIMESTAMP,
  role              VARCHAR(32) NOT NULL,
  account_status    VARCHAR(32) NOT NULL,
  CONSTRAINT uk_app_user_mobile UNIQUE (country_code, mobile_number),
  CONSTRAINT uk_app_user_email UNIQUE (email)
);

CREATE TABLE user_profile (
  user_profile_uuid     UUID PRIMARY KEY,
  user_uuid             UUID NOT NULL UNIQUE REFERENCES users(user_uuid),
  FIRST_NAME            VARCHAR(255) NOT NULL,
  MIDDLE_NAME           VARCHAR(255),
  LAST_NAME             VARCHAR(255),
  PROFILE_PICTURE_URL   VARCHAR(2048),
  BIO                   TEXT,
  GENDER                VARCHAR(32) NOT NULL,
  DATE_OF_BIRTH         DATE NOT NULL,
  LOCATION              VARCHAR(512),
  created_at            TIMESTAMP NOT NULL,
  updated_at            TIMESTAMP,
  verification_status   VARCHAR(32) NOT NULL,
  version               INTEGER NOT NULL,
  trust_score           NUMERIC(5,2),
  trust_score_updated_at TIMESTAMP,
  CONSTRAINT fk_user_profile_user FOREIGN KEY (user_uuid) REFERENCES users(user_uuid)
);

CREATE TABLE user_profile_history_table (
  user_profile_history_id UUID PRIMARY KEY,
  user_profile_uuid       UUID NOT NULL,
  user_uuid               UUID NOT NULL REFERENCES users(user_uuid),
  FIRST_NAME              VARCHAR(255) NOT NULL,
  MIDDLE_NAME             VARCHAR(255),
  LAST_NAME               VARCHAR(255),
  PROFILE_PICTURE_URL     VARCHAR(2048),
  BIO                     TEXT,
  GENDER                  VARCHAR(32) NOT NULL,
  DATE_OF_BIRTH           DATE NOT NULL,
  LOCATION                VARCHAR(512),
  created_at              TIMESTAMP NOT NULL,
  updated_at              TIMESTAMP,
  verification_status     VARCHAR(32) NOT NULL,
  version                 INTEGER NOT NULL,
  CONSTRAINT uk_user_profile_history_user_profile_uuid_version UNIQUE (user_profile_uuid, version)
);

CREATE TABLE social_handle (
  social_handle_uuid UUID PRIMARY KEY,
  user_uuid          UUID NOT NULL REFERENCES users(user_uuid),
  platform           VARCHAR(32) NOT NULL,
  platform_url       VARCHAR(2048) NOT NULL,
  created_at         TIMESTAMP NOT NULL,
  updated_at       TIMESTAMP
);

CREATE TABLE refresh_token (
  refresh_token_uuid UUID PRIMARY KEY,
  user_uuid          UUID NOT NULL REFERENCES users(user_uuid),
  token_hash         VARCHAR(512) NOT NULL UNIQUE,
  expires_at         TIMESTAMP NOT NULL,
  revoked            BOOLEAN NOT NULL,
  created_at         TIMESTAMP NOT NULL,
  updated_at         TIMESTAMP
);

CREATE TABLE verification (
  verification_uuid     UUID PRIMARY KEY,
  user_uuid             UUID NOT NULL REFERENCES users(user_uuid),
  document_type         VARCHAR(32) NOT NULL,
  document_number       VARCHAR(20) NOT NULL,
  provider_reference_id VARCHAR(255),
  verification_status   VARCHAR(32) NOT NULL,
  verification_remarks  TEXT,
  verified_at           TIMESTAMP,
  verified_by           VARCHAR(255),
  created_at            TIMESTAMP,
  updated_at            TIMESTAMP,
  CONSTRAINT uk_verification_user_doc UNIQUE (user_uuid, document_type)
);

CREATE TABLE aadhar_otp (
  id             UUID PRIMARY KEY,
  user_id        UUID NOT NULL,
  reference_id   VARCHAR(255) NOT NULL,
  aadhaar_number VARCHAR(64) NOT NULL,
  used           BOOLEAN NOT NULL,
  otp_status     VARCHAR(32) NOT NULL,
  expires_at     TIMESTAMP NOT NULL,
  created_at     TIMESTAMP NOT NULL
);

CREATE TABLE user_reports (
  report_id         UUID PRIMARY KEY,
  reported_user_id  UUID NOT NULL,
  reporting_user_id UUID NOT NULL,
  message           TEXT NOT NULL,
  reported_at       TIMESTAMP NOT NULL,
  CONSTRAINT uk_reported_reporting_user UNIQUE (reported_user_id, reporting_user_id)
);

CREATE TABLE travel_pal (
  travel_pal_uuid UUID PRIMARY KEY,
  user_low_id     UUID NOT NULL,
  user_high_id    UUID NOT NULL,
  requested_by    UUID NOT NULL,
  status          VARCHAR(32) NOT NULL,
  created_at      TIMESTAMP,
  updated_at      TIMESTAMP,
  CONSTRAINT uk_travel_pal_unique_pair UNIQUE (user_low_id, user_high_id)
);

-- ========== trips ==========
CREATE TABLE core_trip_details (
  trip_id               UUID PRIMARY KEY,
  trip_description      TEXT,
  trip_title            VARCHAR(512),
  trip_destination      VARCHAR(1024),
  trip_start_date       DATE,
  trip_end_date         DATE,
  trip_status           VARCHAR(32) NOT NULL,
  estimated_budget      DOUBLE PRECISION,
  max_participants      INTEGER,
  current_participants    INTEGER,
  is_full               BOOLEAN,
  trip_full_reason      VARCHAR(512),
  full_marked_at        TIMESTAMP,
  join_policy           VARCHAR(32),
  visibility_status     VARCHAR(32),
  created_at            TIMESTAMP NOT NULL,
  updated_at            TIMESTAMP,
  conversation_id       UUID,
  splitwise_group_id    UUID
);
CREATE INDEX idx_trip_status ON core_trip_details(trip_status);
CREATE INDEX idx_trip_status_start_date ON core_trip_details(trip_status, trip_start_date);
CREATE INDEX idx_trip_status_end_date ON core_trip_details(trip_status, trip_end_date);

CREATE TABLE tags (
  tag_id   UUID PRIMARY KEY,
  tag_name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE trip_tag (
  trip_id UUID NOT NULL REFERENCES core_trip_details(trip_id) ON DELETE CASCADE,
  tag_id  UUID NOT NULL REFERENCES tags(tag_id) ON DELETE CASCADE,
  PRIMARY KEY (trip_id, tag_id)
);

CREATE TABLE trip_policies (
  trip_id              UUID PRIMARY KEY REFERENCES core_trip_details(trip_id) ON DELETE CASCADE,
  cancellation_policy  VARCHAR(500),
  refund_policy        VARCHAR(500)
);

CREATE TABLE trip_meta_data (
  trip_id        UUID PRIMARY KEY REFERENCES core_trip_details(trip_id) ON DELETE CASCADE,
  trip_summary   TEXT,
  whats_included TEXT,
  whats_excluded TEXT
);

CREATE TABLE trip_itineraries (
  itinerary_id UUID PRIMARY KEY,
  trip_id      UUID NOT NULL REFERENCES core_trip_details(trip_id) ON DELETE CASCADE,
  day_number   INTEGER NOT NULL,
  title        VARCHAR(255),
  description  TEXT,
  activities   TEXT,
  meals        TEXT,
  stay         TEXT,
  created_at   TIMESTAMP NOT NULL,
  UNIQUE (trip_id, day_number)
);

CREATE TABLE trip_invites (
  invite_id        UUID PRIMARY KEY,
  trip_id          UUID NOT NULL REFERENCES core_trip_details(trip_id) ON DELETE CASCADE,
  invited_by       UUID NOT NULL,
  invite_type      VARCHAR(20) NOT NULL,
  invite_source    VARCHAR(30) NOT NULL,
  invited_user_id  UUID,
  invited_email    VARCHAR(255),
  invited_phone    VARCHAR(30),
  token_hash       VARCHAR(255) UNIQUE,
  status           VARCHAR(20) NOT NULL,
  expires_at       TIMESTAMP,
  last_reminded_at TIMESTAMP,
  created_at       TIMESTAMP NOT NULL,
  CONSTRAINT UniqueTripIdAndInvitedUserId UNIQUE (trip_id, invited_user_id),
  CONSTRAINT UniqueTripIdAndInvitedEmail UNIQUE (trip_id, invited_email),
  CONSTRAINT UniqueTripIdAndInvitedPhone UNIQUE (trip_id, invited_phone)
);

CREATE TABLE trip_join_requests (
  request_id  UUID PRIMARY KEY,
  trip_id     UUID NOT NULL REFERENCES core_trip_details(trip_id) ON DELETE CASCADE,
  user_id     UUID NOT NULL,
  source      VARCHAR(20) NOT NULL,
  status      VARCHAR(20) NOT NULL,
  reviewed_by UUID,
  reviewed_at TIMESTAMP,
  created_at  TIMESTAMP NOT NULL,
  updated_at  TIMESTAMP
);
CREATE INDEX idx_join_requests_trip_status ON trip_join_requests(trip_id, status);
CREATE INDEX idx_join_requests_trip_user ON trip_join_requests(trip_id, user_id);

CREATE TABLE trip_members (
  membership_id UUID PRIMARY KEY,
  user_id       UUID NOT NULL,
  trip_id       UUID NOT NULL REFERENCES core_trip_details(trip_id) ON DELETE CASCADE,
  role          VARCHAR(20) NOT NULL,
  status        VARCHAR(20) NOT NULL,
  joined_at     TIMESTAMP NOT NULL,
  exited_at     TIMESTAMP,
  exited_by     UUID,
  removal_reason VARCHAR(512),
  UNIQUE (trip_id, user_id)
);
CREATE INDEX idx_members_trip_status ON trip_members(trip_id, status);
CREATE INDEX idx_members_user_id ON trip_members(user_id);

CREATE TABLE trip_queries (
  query_id    UUID PRIMARY KEY,
  trip_id     UUID NOT NULL REFERENCES core_trip_details(trip_id) ON DELETE CASCADE,
  asked_by    UUID NOT NULL,
  question    TEXT NOT NULL,
  answer      TEXT,
  visibility  VARCHAR(30) NOT NULL,
  created_at  TIMESTAMP NOT NULL,
  answered_at TIMESTAMP,
  answered_by UUID
);
CREATE INDEX idx_queries_trip ON trip_queries(trip_id);

CREATE TABLE trip_reports (
  report_id   UUID PRIMARY KEY,
  trip_id     UUID NOT NULL REFERENCES core_trip_details(trip_id) ON DELETE CASCADE,
  reported_by UUID NOT NULL,
  reason      TEXT NOT NULL,
  status      VARCHAR(20) NOT NULL,
  created_at  TIMESTAMP NOT NULL,
  UNIQUE (trip_id, reported_by)
);
CREATE INDEX idx_trip_reports_trip ON trip_reports(trip_id);
CREATE INDEX idx_trip_reports_status ON trip_reports(status);

CREATE TABLE trip_wishlists (
  trip_wishlist_id UUID PRIMARY KEY,
  trip_id          UUID NOT NULL REFERENCES core_trip_details(trip_id) ON DELETE CASCADE,
  user_id          UUID NOT NULL,
  created_at       TIMESTAMP NOT NULL,
  UNIQUE (trip_id, user_id)
);
CREATE INDEX idx_wishlist_user_created ON trip_wishlists(user_id, created_at);

CREATE TABLE trip_rating (
  id                  UUID PRIMARY KEY,
  trip_id             UUID NOT NULL REFERENCES core_trip_details(trip_id) ON DELETE CASCADE,
  rater_user_id       UUID NOT NULL,
  destination_rating  INTEGER NOT NULL,
  itinerary_rating    INTEGER NOT NULL,
  overall_rating      INTEGER NOT NULL,
  created_at          TIMESTAMP NOT NULL,
  UNIQUE (trip_id, rater_user_id)
);

CREATE TABLE member_rating (
  id             UUID PRIMARY KEY,
  trip_id        UUID NOT NULL REFERENCES core_trip_details(trip_id) ON DELETE CASCADE,
  rater_user_id  UUID NOT NULL,
  rated_user_id  UUID NOT NULL,
  rating_score   INTEGER NOT NULL,
  vibe_tag       VARCHAR(30),
  review_text    TEXT,
  visible_at     TIMESTAMP,
  created_at     TIMESTAMP NOT NULL,
  UNIQUE (trip_id, rater_user_id, rated_user_id)
);

CREATE TABLE host_rating (
  id                    UUID PRIMARY KEY,
  trip_id               UUID NOT NULL REFERENCES core_trip_details(trip_id) ON DELETE CASCADE,
  host_user_id          UUID NOT NULL,
  rater_user_id         UUID NOT NULL,
  coordination_rating   INTEGER NOT NULL,
  communication_rating  INTEGER NOT NULL,
  leadership_rating     INTEGER NOT NULL,
  review_text           TEXT,
  created_at            TIMESTAMP NOT NULL,
  UNIQUE (trip_id, rater_user_id)
);

-- ========== chat ==========
CREATE TABLE conversation (
  conversation_id   UUID PRIMARY KEY,
  type              VARCHAR(32) NOT NULL,
  created_by        UUID NOT NULL,
  created_at        TIMESTAMP NOT NULL,
  conversation_name VARCHAR(512)
);

CREATE TABLE conversation_participant (
  id               UUID PRIMARY KEY,
  conversation_id  UUID NOT NULL REFERENCES conversation(conversation_id) ON DELETE CASCADE,
  user_id          UUID NOT NULL,
  role             VARCHAR(32) NOT NULL,
  joined_at        TIMESTAMP NOT NULL,
  left_at          TIMESTAMP,
  last_read_at     TIMESTAMP,
  CONSTRAINT uk_conversation_user UNIQUE (conversation_id, user_id)
);
CREATE INDEX idx_cp_user_left ON conversation_participant(user_id, left_at);

CREATE TABLE message (
  message_id       UUID PRIMARY KEY,
  conversation_id  UUID NOT NULL REFERENCES conversation(conversation_id) ON DELETE CASCADE,
  sender_id        UUID,
  content          TEXT NOT NULL,
  created_at       TIMESTAMP NOT NULL
);
CREATE INDEX idx_msg_conversation_created ON message(conversation_id, created_at);

CREATE TABLE conversation_mute (
  id              UUID PRIMARY KEY,
  conversation_id UUID NOT NULL REFERENCES conversation(conversation_id) ON DELETE CASCADE,
  user_id         UUID NOT NULL,
  muted_at        TIMESTAMP NOT NULL,
  CONSTRAINT uk_mute_unique UNIQUE (conversation_id, user_id)
);

CREATE TABLE conversation_block (
  id              UUID PRIMARY KEY,
  conversation_id UUID NOT NULL UNIQUE REFERENCES conversation(conversation_id) ON DELETE CASCADE,
  blocked_by      UUID NOT NULL,
  created_at      TIMESTAMP NOT NULL,
  status          VARCHAR(32)
);

-- ========== splitwise ==========
CREATE TABLE splitwise_groups (
  id          UUID PRIMARY KEY,
  trip_id     UUID NOT NULL UNIQUE REFERENCES core_trip_details(trip_id) ON DELETE CASCADE,
  description VARCHAR(500),
  created_by  UUID NOT NULL,
  created_at  TIMESTAMP NOT NULL,
  updated_at  TIMESTAMP NOT NULL
);

CREATE TABLE splitwise_group_members (
  id        UUID PRIMARY KEY,
  group_id  UUID NOT NULL REFERENCES splitwise_groups(id) ON DELETE CASCADE,
  user_id   UUID NOT NULL,
  role      VARCHAR(32) NOT NULL,
  joined_at TIMESTAMP NOT NULL
);

CREATE TABLE splitwise_expenses (
  id           UUID PRIMARY KEY,
  name         VARCHAR(200) NOT NULL,
  description  VARCHAR(1000),
  amount       NUMERIC(19,2) NOT NULL,
  paid_by      UUID NOT NULL,
  group_id     UUID NOT NULL REFERENCES splitwise_groups(id) ON DELETE CASCADE,
  split_type   VARCHAR(32) NOT NULL,
  expense_date TIMESTAMP,
  category     VARCHAR(50),
  receipt_url  VARCHAR(500),
  created_at   TIMESTAMP NOT NULL,
  updated_at   TIMESTAMP NOT NULL
);

CREATE TABLE splitwise_expense_splits (
  id         UUID PRIMARY KEY,
  expense_id UUID NOT NULL REFERENCES splitwise_expenses(id) ON DELETE CASCADE,
  user_id    UUID NOT NULL,
  amount     NUMERIC(19,2) NOT NULL,
  percentage NUMERIC(5,2),
  created_at TIMESTAMP NOT NULL
);

CREATE TABLE splitwise_balances (
  id        UUID PRIMARY KEY,
  group_id  UUID NOT NULL REFERENCES splitwise_groups(id) ON DELETE CASCADE,
  owed_by   UUID NOT NULL,
  owed_to   UUID NOT NULL,
  amount    NUMERIC(19,2) NOT NULL,
  last_updated TIMESTAMP NOT NULL,
  UNIQUE (group_id, owed_by, owed_to)
);

CREATE TABLE splitwise_settlements (
  id              UUID PRIMARY KEY,
  group_id        UUID NOT NULL REFERENCES splitwise_groups(id) ON DELETE CASCADE,
  paid_by         UUID NOT NULL,
  paid_to         UUID NOT NULL,
  amount          NUMERIC(19,2) NOT NULL,
  payment_method  VARCHAR(50),
  transaction_id  UUID,
  notes           VARCHAR(500),
  status          VARCHAR(20),
  settled_at      TIMESTAMP NOT NULL
);

CREATE TABLE splitwise_settlement_expenses (
  id            UUID PRIMARY KEY,
  settlement_id UUID NOT NULL REFERENCES splitwise_settlements(id) ON DELETE CASCADE,
  expense_id    UUID NOT NULL REFERENCES splitwise_expenses(id) ON DELETE CASCADE,
  amount          NUMERIC(19,2) NOT NULL,
  created_at      TIMESTAMP NOT NULL,
  UNIQUE (settlement_id, expense_id)
);

CREATE TABLE splitwise_activities (
  id            UUID PRIMARY KEY,
  group_id      UUID REFERENCES splitwise_groups(id) ON DELETE SET NULL,
  user_id       UUID,
  activity_type VARCHAR(64) NOT NULL,
  description   VARCHAR(1000),
  related_id    UUID,
  related_type  VARCHAR(50),
  old_value     VARCHAR(500),
  new_value     VARCHAR(500),
  created_at    TIMESTAMP NOT NULL
);

-- ========== misc ==========
CREATE TABLE task_lock (
  task_id         VARCHAR(255) PRIMARY KEY,
  last_execution  BIGINT NOT NULL
);

CREATE TABLE user_notification (
  notification_id UUID PRIMARY KEY,
  user_id         UUID NOT NULL,
  trip_id         UUID,
  type            VARCHAR(50) NOT NULL,
  title           VARCHAR(255) NOT NULL,
  body            VARCHAR(1000),
  read_at         TIMESTAMP,
  created_at      TIMESTAMP NOT NULL
);
CREATE INDEX idx_user_notification_user_id ON user_notification(user_id);
CREATE INDEX idx_user_notification_read_at ON user_notification(user_id, read_at);