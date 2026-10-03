CREATE TABLE IF NOT EXISTS reconciliation_source_coverage (
    id                  VARCHAR(36) PRIMARY KEY,
    coverage_date       DATE NOT NULL,
    analyzer_id         VARCHAR(36),
    source_kind         VARCHAR(64) NOT NULL,
    source_upload_id    VARCHAR(36),
    fact_count          INTEGER NOT NULL DEFAULT 0,
    coverage_quality    VARCHAR(64) NOT NULL,
    coverage_reason     TEXT,
    observed_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version             BIGINT
);

CREATE INDEX IF NOT EXISTS idx_reconciliation_source_coverage_date
    ON reconciliation_source_coverage(coverage_date, source_kind);

CREATE INDEX IF NOT EXISTS idx_reconciliation_source_coverage_analyzer
    ON reconciliation_source_coverage(analyzer_id, coverage_date)
    WHERE analyzer_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_reconciliation_source_coverage
    ON reconciliation_source_coverage(
        coverage_date,
        COALESCE(analyzer_id, ''),
        source_kind,
        COALESCE(source_upload_id, '')
    );

COMMENT ON TABLE reconciliation_source_coverage IS
    'Observed source coverage per date. Missing coverage is evidence of an incomparable period, never a discrepancy.';
