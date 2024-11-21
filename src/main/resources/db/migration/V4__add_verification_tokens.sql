CREATE TABLE IF NOT EXISTS verification_tokens (
    token_id UUID PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL,
    verified BOOLEAN DEFAULT FALSE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_user
        FOREIGN KEY(user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_token ON verification_tokens(token);
CREATE INDEX idx_user_id ON verification_tokens(user_id);
CREATE INDEX idx_expires_at ON verification_tokens(expires_at);

-- Add verification status to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified BOOLEAN DEFAULT FALSE;