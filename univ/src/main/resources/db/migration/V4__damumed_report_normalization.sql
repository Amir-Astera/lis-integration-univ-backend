ALTER TABLE damumed_report_uploads
    ADD COLUMN IF NOT EXISTS normalization_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN IF NOT EXISTS normalization_started_at TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS normalization_completed_at TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS normalization_error_message TEXT NULL,
    ADD COLUMN IF NOT EXISTS normalized_section_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS normalized_fact_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS normalized_dimension_count INTEGER NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS damumed_report_normalized_sections (
    id VARCHAR(160) PRIMARY KEY,
    upload_id VARCHAR(128) NOT NULL,
    report_kind VARCHAR(128) NOT NULL,
    sheet_id VARCHAR(128) NOT NULL,
    section_key TEXT NOT NULL,
    section_name TEXT NOT NULL,
    semantic_role VARCHAR(64) NOT NULL,
    anchor_axis_key VARCHAR(128) NULL,
    anchor_axis_value TEXT NULL,
    row_start_index INTEGER NULL,
    row_end_index INTEGER NULL,
    column_start_index INTEGER NULL,
    column_end_index INTEGER NULL,
    CONSTRAINT fk_damumed_report_normalized_sections_upload
        FOREIGN KEY (upload_id)
        REFERENCES damumed_report_uploads (id)
        ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_damumed_report_normalized_sections_upload_key
    ON damumed_report_normalized_sections (upload_id, section_key);

CREATE TABLE IF NOT EXISTS damumed_report_normalized_dimensions (
    id VARCHAR(160) PRIMARY KEY,
    upload_id VARCHAR(128) NOT NULL,
    report_kind VARCHAR(128) NOT NULL,
    axis_key VARCHAR(128) NOT NULL,
    axis_type VARCHAR(64) NOT NULL,
    raw_value TEXT NOT NULL,
    normalized_value TEXT NOT NULL,
    display_value TEXT NOT NULL,
    source_sheet_id VARCHAR(128) NULL,
    source_row_index INTEGER NULL,
    source_column_index INTEGER NULL,
    CONSTRAINT fk_damumed_report_normalized_dimensions_upload
        FOREIGN KEY (upload_id)
        REFERENCES damumed_report_uploads (id)
        ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_damumed_report_normalized_dimensions_upload_axis_value
    ON damumed_report_normalized_dimensions (upload_id, axis_key, normalized_value);

CREATE TABLE IF NOT EXISTS damumed_report_normalized_facts (
    id VARCHAR(192) PRIMARY KEY,
    upload_id VARCHAR(128) NOT NULL,
    report_kind VARCHAR(128) NOT NULL,
    sheet_id VARCHAR(128) NOT NULL,
    section_id VARCHAR(160) NOT NULL,
    row_id VARCHAR(160) NULL,
    cell_id VARCHAR(192) NULL,
    metric_key VARCHAR(128) NOT NULL,
    metric_label TEXT NOT NULL,
    numeric_value DOUBLE PRECISION NULL,
    value_text TEXT NULL,
    formula_text TEXT NULL,
    period_text TEXT NULL,
    source_row_index INTEGER NOT NULL,
    source_column_index INTEGER NOT NULL,
    CONSTRAINT fk_damumed_report_normalized_facts_upload
        FOREIGN KEY (upload_id)
        REFERENCES damumed_report_uploads (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_damumed_report_normalized_facts_section
        FOREIGN KEY (section_id)
        REFERENCES damumed_report_normalized_sections (id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_damumed_report_normalized_facts_upload_metric
    ON damumed_report_normalized_facts (upload_id, metric_key);

CREATE TABLE IF NOT EXISTS damumed_report_normalized_fact_dimensions (
    id VARCHAR(224) PRIMARY KEY,
    fact_id VARCHAR(192) NOT NULL,
    axis_key VARCHAR(128) NOT NULL,
    dimension_id VARCHAR(160) NOT NULL,
    raw_value TEXT NOT NULL,
    normalized_value TEXT NOT NULL,
    source_scope VARCHAR(32) NOT NULL,
    CONSTRAINT fk_damumed_report_normalized_fact_dimensions_fact
        FOREIGN KEY (fact_id)
        REFERENCES damumed_report_normalized_facts (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_damumed_report_normalized_fact_dimensions_dimension
        FOREIGN KEY (dimension_id)
        REFERENCES damumed_report_normalized_dimensions (id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_damumed_report_normalized_fact_dimensions_fact
    ON damumed_report_normalized_fact_dimensions (fact_id, axis_key);
