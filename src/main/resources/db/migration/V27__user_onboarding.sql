CREATE TABLE user_onboarding (
    user_id UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    completed_at TIMESTAMPTZ NOT NULL
);
