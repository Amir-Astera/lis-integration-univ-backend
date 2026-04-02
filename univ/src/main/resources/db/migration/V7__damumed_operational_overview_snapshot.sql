CREATE TABLE IF NOT EXISTS damumed_operational_overview_snapshots (
    id VARCHAR(128) PRIMARY KEY,
    snapshot_key VARCHAR(128) NOT NULL UNIQUE,
    payload_json TEXT NOT NULL,
    source_signature VARCHAR(1024) NOT NULL,
    generated_at TIMESTAMP NOT NULL,
    version BIGINT
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_damumed_operational_overview_snapshots_key
    ON damumed_operational_overview_snapshots (snapshot_key);
