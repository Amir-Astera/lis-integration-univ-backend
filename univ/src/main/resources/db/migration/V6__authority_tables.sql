CREATE TABLE IF NOT EXISTS authority (
    id VARCHAR(128) PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_authority_name
    ON authority (name);

CREATE TABLE IF NOT EXISTS user_authority (
    id VARCHAR(256) PRIMARY KEY,
    user_id VARCHAR(128) NOT NULL,
    authority_id VARCHAR(128) NOT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT,
    CONSTRAINT fk_user_authority_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user_authority_authority
        FOREIGN KEY (authority_id)
        REFERENCES authority (id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_authority_user_id
    ON user_authority (user_id);

CREATE INDEX IF NOT EXISTS idx_user_authority_authority_id
    ON user_authority (authority_id);

CREATE UNIQUE INDEX IF NOT EXISTS idx_user_authority_user_authority
    ON user_authority (user_id, authority_id);
