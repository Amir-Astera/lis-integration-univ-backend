CREATE TABLE IF NOT EXISTS director_assistant_messages (
    id                      VARCHAR(36) PRIMARY KEY,
    user_login              VARCHAR(255) NOT NULL,
    message_role            VARCHAR(16) NOT NULL
        CHECK (message_role IN ('USER', 'ASSISTANT')),
    content                 TEXT NOT NULL,
    model                   VARCHAR(32),
    evidence_generated_at   TIMESTAMP,
    evidence_scope_json     TEXT,
    evidence_snapshot_json  TEXT,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version                 BIGINT
);

CREATE INDEX IF NOT EXISTS idx_director_assistant_messages_user_created
    ON director_assistant_messages(user_login, created_at DESC);

COMMENT ON TABLE director_assistant_messages IS
    'Server-side per-user history of the director operational assistant. Only sanitized operational questions and aggregate evidence snapshots are retained.';
