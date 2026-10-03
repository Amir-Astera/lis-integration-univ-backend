CREATE TABLE IF NOT EXISTS onec_readonly_snapshots (
    id                  VARCHAR(36) PRIMARY KEY,
    source_name         VARCHAR(255) NOT NULL,
    source_kind         VARCHAR(64) NOT NULL,
    source_checksum     VARCHAR(64) NOT NULL UNIQUE,
    snapshot_at         TIMESTAMP,
    period_from         DATE,
    period_to           DATE,
    payload_json        TEXT NOT NULL,
    metadata_json       TEXT,
    imported_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    imported_by         VARCHAR(255),
    version             BIGINT
);

CREATE INDEX IF NOT EXISTS idx_onec_readonly_snapshots_imported_at
    ON onec_readonly_snapshots(imported_at DESC);

COMMENT ON TABLE onec_readonly_snapshots IS
    'Immutable, sanitized read-only 1C export snapshots. Raw DT backups and patient-level data are never stored here.';
