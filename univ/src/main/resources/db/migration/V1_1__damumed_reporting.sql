CREATE TABLE IF NOT EXISTS damumed_report_source_settings (
    id VARCHAR(128) PRIMARY KEY,
    mode VARCHAR(32) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(255),
    version BIGINT
);

CREATE TABLE IF NOT EXISTS damumed_report_uploads (
    id VARCHAR(128) PRIMARY KEY,
    report_kind VARCHAR(128) NOT NULL,
    source_mode VARCHAR(32) NOT NULL,
    original_file_name VARCHAR(512) NOT NULL,
    stored_file_name VARCHAR(512) NOT NULL,
    storage_path VARCHAR(1024) NOT NULL,
    format VARCHAR(32) NOT NULL,
    content_type VARCHAR(255),
    checksum_sha256 VARCHAR(64) NOT NULL,
    size_bytes BIGINT NOT NULL,
    uploaded_at TIMESTAMP NOT NULL,
    uploaded_by VARCHAR(255),
    version BIGINT
);

CREATE INDEX IF NOT EXISTS idx_damumed_report_uploads_report_kind
    ON damumed_report_uploads (report_kind);

CREATE INDEX IF NOT EXISTS idx_damumed_report_uploads_uploaded_at
    ON damumed_report_uploads (uploaded_at DESC);

INSERT INTO damumed_report_source_settings (id, mode, updated_at, updated_by, version)
VALUES ('damumed-lab-report-source', 'MANUAL', NOW(), NULL, 0)
ON CONFLICT (id) DO NOTHING;
