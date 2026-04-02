CREATE TABLE IF NOT EXISTS local_auth_credentials (
    user_id VARCHAR(128) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT
);

CREATE TABLE IF NOT EXISTS local_auth_sessions (
    token VARCHAR(128) PRIMARY KEY,
    user_id VARCHAR(128) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    version BIGINT,
    CONSTRAINT fk_local_auth_sessions_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_local_auth_sessions_user_id
    ON local_auth_sessions (user_id);

CREATE INDEX IF NOT EXISTS idx_local_auth_sessions_expires_at
    ON local_auth_sessions (expires_at);
