-- Migration: V25 - Sample-Level Reconciliation
-- Purpose: Per-sample reconciliation status (one row per parsed analyzer sample).
--          Enables drill-down: "show me the 47 specific samples that caused this discrepancy".
-- Parent: reconciliation_daily_summary (V24) is the aggregate; this is the raw per-sample layer.

CREATE TABLE IF NOT EXISTS sample_reconciliation (
    id                          VARCHAR(36)     PRIMARY KEY,
    parsed_sample_id            VARCHAR(36)     NOT NULL,    -- FK → parsed_analyzer_samples.id

    -- Denormalized grouping dimensions for fast filter (avoids JOIN on dashboard reads)
    sample_date                 DATE            NOT NULL,
    analyzer_id                 VARCHAR(36),
    service_catalog_id          VARCHAR(36),                 -- matched catalog entry; NULL = unknown
    service_name_raw            VARCHAR(300),                -- original service name from sample
    service_name_canonical      VARCHAR(300),                -- canonical name from matched catalog entry
    category                    VARCHAR(50),                 -- denormalized from service_catalog.category

    -- Classification as resolved (may differ from sample.classification if grace expired)
    -- Values: LEGITIMATE | DISCREPANCY | PENDING_GRACE | WASH_TEST | RERUN
    reconciliation_status       VARCHAR(32)     NOT NULL
        CHECK (reconciliation_status IN ('LEGITIMATE','DISCREPANCY','PENDING_GRACE','WASH_TEST','RERUN')),

    -- Why this status was assigned — human readable
    reason                      TEXT,

    -- Grace window metadata (for PENDING_GRACE → DISCREPANCY promotion by scheduler)
    grace_hours                 INTEGER         NOT NULL DEFAULT 0,
    grace_deadline_at           TIMESTAMP,                   -- sample_timestamp + grace_hours; NULL if no grace

    -- Matching provenance
    match_confidence            VARCHAR(32),                 -- BY_SERVICE_ID|BY_EXACT_NAME|BY_LIS_ALIAS|BY_FUZZY|NO_MATCH

    -- Financial impact (only for DISCREPANCY)
    lis_price_per_test_tenge    NUMERIC(12,2),
    estimated_waste_tenge       NUMERIC(12,2)   NOT NULL DEFAULT 0,

    -- Link to LIS referral if this sample was reconciled to one (for drill-down from LIS side)
    lis_referral_key            VARCHAR(100),

    reconciled_at               TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version                     BIGINT,

    CONSTRAINT fk_samp_recon_sample FOREIGN KEY (parsed_sample_id)
        REFERENCES parsed_analyzer_samples(id) ON DELETE CASCADE,

    CONSTRAINT fk_samp_recon_analyzer FOREIGN KEY (analyzer_id)
        REFERENCES analyzers(id) ON DELETE SET NULL,

    CONSTRAINT fk_samp_recon_catalog FOREIGN KEY (service_catalog_id)
        REFERENCES service_catalog(id) ON DELETE SET NULL,

    -- One reconciliation row per sample
    CONSTRAINT uq_samp_recon_sample UNIQUE (parsed_sample_id)
);

-- Drill-down by date+catalog (primary dashboard use-case)
CREATE INDEX IF NOT EXISTS idx_samp_recon_date_catalog
    ON sample_reconciliation(sample_date, service_catalog_id)
    WHERE reconciliation_status = 'DISCREPANCY';

-- Drill-down by analyzer
CREATE INDEX IF NOT EXISTS idx_samp_recon_analyzer_date
    ON sample_reconciliation(analyzer_id, sample_date)
    WHERE analyzer_id IS NOT NULL;

-- Scheduler: find samples whose grace has expired and need promotion
CREATE INDEX IF NOT EXISTS idx_samp_recon_pending_grace
    ON sample_reconciliation(grace_deadline_at)
    WHERE reconciliation_status = 'PENDING_GRACE';

-- Status filter (most dashboard queries filter by status)
CREATE INDEX IF NOT EXISTS idx_samp_recon_status_date
    ON sample_reconciliation(reconciliation_status, sample_date DESC);

COMMENT ON TABLE sample_reconciliation IS
    'Per-sample reconciliation ledger. Source of truth for drill-down: which individual samples '
    'caused each discrepancy unit in reconciliation_daily_summary. Rebuilt alongside the daily summary.';

COMMENT ON COLUMN sample_reconciliation.grace_deadline_at IS
    'When PENDING_GRACE expires (sample_timestamp + grace_hours). Scheduler promotes to DISCREPANCY '
    'when CURRENT_TIMESTAMP > grace_deadline_at AND still no LIS record found.';

COMMENT ON COLUMN sample_reconciliation.estimated_waste_tenge IS
    'Monetary estimate of wasted reagents: lis_price_per_test_tenge if DISCREPANCY, else 0.';
