ALTER TABLE password_reset_tokens
ALTER COLUMN token_hash TYPE VARCHAR(64)
    USING token_hash::VARCHAR(64);

ALTER TABLE refresh_tokens
ALTER COLUMN token_hash TYPE VARCHAR(64)
    USING token_hash::VARCHAR(64);