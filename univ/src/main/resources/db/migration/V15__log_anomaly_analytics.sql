-- Migration: V15 - Log Anomaly Analytics
-- Purpose: Store anomaly records extracted from analyzer logs for fraud/unauthorized usage detection

-- =============================================================================
-- Table: log_anomaly_records
-- Each row = one anomalous sample detected from AppLogs or Errors.xml
-- "Anomaly" = sample without LIS order / suspicious / error classification
-- =============================================================================
CREATE TABLE IF NOT EXISTS log_anomaly_records (
    id                      VARCHAR(36) PRIMARY KEY,
    parsed_sample_id        VARCHAR(36),        -- FK to parsed_analyzer_samples (nullable for xml-sourced)
    log_upload_id           VARCHAR(36) NOT NULL,
    analyzer_id             VARCHAR(36),        -- FK to analyzers
    anomaly_date            DATE NOT NULL,      -- Date of the anomaly (extracted from log timestamp)
    anomaly_timestamp       TIMESTAMP,          -- Precise timestamp if available
    barcode                 VARCHAR(100),       -- Barcode from the log (often missing for unauthorized)
    device_system_name      VARCHAR(100),       -- WorkPlaceID / SystemName from analyzer
    lis_analyzer_id         INTEGER,            -- AnalyzerId from LIS JSON
    anomaly_type            VARCHAR(50) NOT NULL -- NO_LIS_ORDER, SUSPICIOUS, ERROR, XML_RESULT
        CHECK (anomaly_type IN ('NO_LIS_ORDER', 'SUSPICIOUS', 'ERROR', 'XML_RESULT')),
    classification_reason   TEXT,
    service_id              INTEGER,
    service_name            TEXT,
    service_category        VARCHAR(100),
    test_mode               VARCHAR(50),
    wbc_value               DOUBLE PRECISION,
    rbc_value               DOUBLE PRECISION,
    hgb_value               DOUBLE PRECISION,
    plt_value               DOUBLE PRECISION,
    estimated_reagents_json TEXT,               -- JSON: [{reagentName, quantity, unit}] per anomaly row
    matched_damumed_fact_id VARCHAR(36),        -- FK to damumed_report_normalized_facts if matched
    cross_ref_status        VARCHAR(30) DEFAULT 'NOT_CHECKED'
        CHECK (cross_ref_status IN ('NOT_CHECKED', 'NO_MATCH', 'MATCHED', 'PARTIAL_MATCH')),

    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_anomaly_upload FOREIGN KEY (log_upload_id)
        REFERENCES analyzer_log_uploads(id) ON DELETE CASCADE,
    CONSTRAINT fk_anomaly_sample FOREIGN KEY (parsed_sample_id)
        REFERENCES parsed_analyzer_samples(id) ON DELETE SET NULL,
    CONSTRAINT fk_anomaly_analyzer FOREIGN KEY (analyzer_id)
        REFERENCES analyzers(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_anomaly_date          ON log_anomaly_records(anomaly_date);
CREATE INDEX IF NOT EXISTS idx_anomaly_analyzer      ON log_anomaly_records(analyzer_id) WHERE analyzer_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_anomaly_type          ON log_anomaly_records(anomaly_type);
CREATE INDEX IF NOT EXISTS idx_anomaly_upload        ON log_anomaly_records(log_upload_id);
CREATE INDEX IF NOT EXISTS idx_anomaly_barcode       ON log_anomaly_records(barcode) WHERE barcode IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_anomaly_service_name  ON log_anomaly_records(service_name) WHERE service_name IS NOT NULL;

-- =============================================================================
-- Table: log_anomaly_daily_summary
-- Aggregated statistics per (date, analyzer) for fast dashboard queries
-- Rebuilt on each log upload parse for the days covered
-- =============================================================================
CREATE TABLE IF NOT EXISTS log_anomaly_daily_summary (
    id                      VARCHAR(36) PRIMARY KEY,

    summary_date            DATE NOT NULL,
    analyzer_id             VARCHAR(36),        -- NULL = cross-analyzer total

    -- Counts from logs
    total_samples           INTEGER NOT NULL DEFAULT 0,  -- all samples in log for that day
    legitimate_count        INTEGER NOT NULL DEFAULT 0,
    anomaly_count           INTEGER NOT NULL DEFAULT 0,  -- suspicious + no_lis_order + error
    suspicious_count        INTEGER NOT NULL DEFAULT 0,
    no_lis_order_count      INTEGER NOT NULL DEFAULT 0,
    error_count             INTEGER NOT NULL DEFAULT 0,
    wash_test_count         INTEGER NOT NULL DEFAULT 0,

    -- From Damumed COMPLETED_LAB_STUDIES_JOURNAL for the same date (for comparison chart)
    damumed_completed_count INTEGER,            -- NULL if not available for this date

    -- Reagent waste estimate (JSON: [{reagentName, quantity, unit}])
    anomaly_reagents_json   TEXT,

    last_updated_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_daily_summary_analyzer FOREIGN KEY (analyzer_id)
        REFERENCES analyzers(id) ON DELETE CASCADE,
    CONSTRAINT uq_daily_summary_date_analyzer UNIQUE (summary_date, analyzer_id)
);

CREATE INDEX IF NOT EXISTS idx_daily_summary_date     ON log_anomaly_daily_summary(summary_date);
CREATE INDEX IF NOT EXISTS idx_daily_summary_analyzer ON log_anomaly_daily_summary(analyzer_id) WHERE analyzer_id IS NOT NULL;

COMMENT ON TABLE log_anomaly_records IS
    'Individual anomaly events from analyzer logs. Unauthorized/suspicious sample runs without LIS registration.';
COMMENT ON TABLE log_anomaly_daily_summary IS
    'Daily aggregated anomaly statistics per analyzer. Used for dashboard charts and trend analysis.';
