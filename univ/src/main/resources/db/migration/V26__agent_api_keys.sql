-- Agent API keys — used by Windows log-uploader service (no Firebase required).
-- The raw key is printed once at startup; only the SHA-256 hash is stored here.
CREATE TABLE agent_api_keys (
    id           TEXT        PRIMARY KEY,
    name         TEXT        NOT NULL,
    key_hash     TEXT        NOT NULL UNIQUE,
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    last_seen_at TIMESTAMP,
    last_ip      TEXT,
    revoked_at   TIMESTAMP,
    version      BIGINT      NOT NULL DEFAULT 0
);

-- Idempotency index: same analyzer + same file content is never parsed twice.
-- Partial: only rows where analyzer_id IS NOT NULL are deduplicated per-analyzer.
CREATE UNIQUE INDEX IF NOT EXISTS uq_log_upload_analyzer_sha256
    ON analyzer_log_uploads (analyzer_id, checksum_sha256)
    WHERE analyzer_id IS NOT NULL;
