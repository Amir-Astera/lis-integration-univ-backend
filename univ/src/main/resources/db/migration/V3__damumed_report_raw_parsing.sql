ALTER TABLE damumed_report_uploads
    ADD COLUMN IF NOT EXISTS parse_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN IF NOT EXISTS parse_started_at TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS parse_completed_at TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS parse_error_message TEXT NULL,
    ADD COLUMN IF NOT EXISTS parsed_sheet_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS parsed_row_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS parsed_cell_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS parsed_merged_region_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS detected_report_title TEXT NULL,
    ADD COLUMN IF NOT EXISTS detected_period_text TEXT NULL;

CREATE TABLE IF NOT EXISTS damumed_report_parsed_workbooks (
    upload_id VARCHAR(128) PRIMARY KEY,
    report_kind VARCHAR(128) NOT NULL,
    workbook_format VARCHAR(32) NOT NULL,
    sheet_count INTEGER NOT NULL,
    active_sheet_index INTEGER NULL,
    first_visible_sheet_index INTEGER NULL,
    parsed_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_damumed_report_parsed_workbooks_upload
        FOREIGN KEY (upload_id)
        REFERENCES damumed_report_uploads (id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS damumed_report_parsed_sheets (
    id VARCHAR(128) PRIMARY KEY,
    upload_id VARCHAR(128) NOT NULL,
    sheet_index INTEGER NOT NULL,
    sheet_name TEXT NOT NULL,
    hidden BOOLEAN NOT NULL,
    very_hidden BOOLEAN NOT NULL,
    first_row_index INTEGER NULL,
    last_row_index INTEGER NULL,
    physical_row_count INTEGER NOT NULL,
    merged_region_count INTEGER NOT NULL,
    default_column_width INTEGER NULL,
    default_row_height SMALLINT NULL,
    CONSTRAINT fk_damumed_report_parsed_sheets_upload
        FOREIGN KEY (upload_id)
        REFERENCES damumed_report_uploads (id)
        ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_damumed_report_parsed_sheets_upload_sheet
    ON damumed_report_parsed_sheets (upload_id, sheet_index);

CREATE TABLE IF NOT EXISTS damumed_report_parsed_rows (
    id VARCHAR(160) PRIMARY KEY,
    upload_id VARCHAR(128) NOT NULL,
    sheet_id VARCHAR(128) NOT NULL,
    row_index INTEGER NOT NULL,
    first_cell_index INTEGER NULL,
    last_cell_index INTEGER NULL,
    physical_cell_count INTEGER NOT NULL,
    height SMALLINT NULL,
    zero_height BOOLEAN NOT NULL,
    outline_level SMALLINT NULL,
    CONSTRAINT fk_damumed_report_parsed_rows_upload
        FOREIGN KEY (upload_id)
        REFERENCES damumed_report_uploads (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_damumed_report_parsed_rows_sheet
        FOREIGN KEY (sheet_id)
        REFERENCES damumed_report_parsed_sheets (id)
        ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_damumed_report_parsed_rows_sheet_row
    ON damumed_report_parsed_rows (sheet_id, row_index);

CREATE TABLE IF NOT EXISTS damumed_report_parsed_cells (
    id VARCHAR(192) PRIMARY KEY,
    upload_id VARCHAR(128) NOT NULL,
    sheet_id VARCHAR(128) NOT NULL,
    row_id VARCHAR(160) NOT NULL,
    row_index INTEGER NOT NULL,
    column_index INTEGER NOT NULL,
    cell_reference VARCHAR(32) NOT NULL,
    cell_type VARCHAR(32) NOT NULL,
    cached_formula_result_type VARCHAR(32) NULL,
    raw_value_text TEXT NULL,
    formatted_value_text TEXT NULL,
    formula_text TEXT NULL,
    numeric_value DOUBLE PRECISION NULL,
    boolean_value BOOLEAN NULL,
    error_code INTEGER NULL,
    is_date_formatted BOOLEAN NOT NULL,
    date_value TIMESTAMP NULL,
    style_index SMALLINT NULL,
    data_format SMALLINT NULL,
    data_format_string TEXT NULL,
    comment_text TEXT NULL,
    hyperlink_address TEXT NULL,
    merged_region_id VARCHAR(160) NULL,
    CONSTRAINT fk_damumed_report_parsed_cells_upload
        FOREIGN KEY (upload_id)
        REFERENCES damumed_report_uploads (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_damumed_report_parsed_cells_sheet
        FOREIGN KEY (sheet_id)
        REFERENCES damumed_report_parsed_sheets (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_damumed_report_parsed_cells_row
        FOREIGN KEY (row_id)
        REFERENCES damumed_report_parsed_rows (id)
        ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_damumed_report_parsed_cells_sheet_row_col
    ON damumed_report_parsed_cells (sheet_id, row_index, column_index);

CREATE TABLE IF NOT EXISTS damumed_report_parsed_merged_regions (
    id VARCHAR(160) PRIMARY KEY,
    upload_id VARCHAR(128) NOT NULL,
    sheet_id VARCHAR(128) NOT NULL,
    region_index INTEGER NOT NULL,
    first_row INTEGER NOT NULL,
    last_row INTEGER NOT NULL,
    first_column INTEGER NOT NULL,
    last_column INTEGER NOT NULL,
    first_cell_reference VARCHAR(32) NOT NULL,
    last_cell_reference VARCHAR(32) NOT NULL,
    CONSTRAINT fk_damumed_report_parsed_merged_regions_upload
        FOREIGN KEY (upload_id)
        REFERENCES damumed_report_uploads (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_damumed_report_parsed_merged_regions_sheet
        FOREIGN KEY (sheet_id)
        REFERENCES damumed_report_parsed_sheets (id)
        ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_damumed_report_parsed_merged_regions_sheet_region
    ON damumed_report_parsed_merged_regions (sheet_id, region_index);
