-- Migration: V24 - Reconciliation Daily Summary
-- Purpose: Materialized per-day per-analyzer per-service reconciliation counts.
-- Rebuilt by ReconciliationSummaryService after each log upload or LIS report ingestion.
-- Provides sub-millisecond dashboard queries without runtime aggregation.

CREATE TABLE IF NOT EXISTS reconciliation_daily_summary (
    id                              VARCHAR(36)     PRIMARY KEY,

    -- Grouping dimensions
    summary_date                    DATE            NOT NULL,
    analyzer_id                     VARCHAR(36),            -- NULL = cross-analyzer rollup
    service_catalog_id              VARCHAR(36),            -- NULL = unknown service
    service_name_canonical          VARCHAR(300)    NOT NULL DEFAULT '',

    -- Category mirrors service_catalog.category for fast filtering without JOIN
    category                        VARCHAR(50),

    -- ─── Core counters ─────────────────────────────────────────────────────
    -- logs_count: total non-wash/non-qc analyzer samples for this service/date/analyzer
    logs_count                      INTEGER         NOT NULL DEFAULT 0,

    -- lis_count: how many times LIS (Damumed) recorded this service on this date
    lis_count                       INTEGER         NOT NULL DEFAULT 0,

    -- reconciled_count: logs samples confirmed in LIS (LEGITIMATE classification)
    reconciled_count                INTEGER         NOT NULL DEFAULT 0,

    -- discrepancy_count: logs_count - reconciled_count - pending_grace_count
    -- = samples that ran on analyzer but have NO LIS record and grace window expired/not applicable
    discrepancy_count               INTEGER         NOT NULL DEFAULT 0,

    -- pending_grace_count: in grace window (e.g. immunology hepatitis within 72h)
    -- treated as "to be confirmed" — not yet counted as waste
    pending_grace_count             INTEGER         NOT NULL DEFAULT 0,

    -- wash_test_count: wash/blank/QC samples (excluded from waste calculation)
    wash_test_count                 INTEGER         NOT NULL DEFAULT 0,

    -- ─── Financial estimates ────────────────────────────────────────────────
    -- Based on service_catalog.lis_price_tenge × discrepancy_count
    estimated_wasted_cost_tenge     NUMERIC(15,2)   NOT NULL DEFAULT 0,

    -- Snapshot of price used for calculation
    lis_price_per_test_tenge        NUMERIC(12,2),

    -- ─── Metadata ───────────────────────────────────────────────────────────
    last_rebuilt_at                 TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version                         BIGINT,

    CONSTRAINT fk_recon_analyzer FOREIGN KEY (analyzer_id)
        REFERENCES analyzers(id) ON DELETE SET NULL,

    CONSTRAINT fk_recon_service_catalog FOREIGN KEY (service_catalog_id)
        REFERENCES service_catalog(id) ON DELETE SET NULL,

    -- One row per (date, analyzer, service_catalog). NULL analyzer/catalog treated as distinct group.
    CONSTRAINT uq_recon_daily UNIQUE (summary_date, analyzer_id, service_catalog_id)
);

-- Fast range queries for dashboard (date always the primary filter)
CREATE INDEX IF NOT EXISTS idx_recon_daily_date
    ON reconciliation_daily_summary(summary_date);

-- Filter by analyzer (drill-down)
CREATE INDEX IF NOT EXISTS idx_recon_daily_analyzer
    ON reconciliation_daily_summary(analyzer_id)
    WHERE analyzer_id IS NOT NULL;

-- Fast "show only problem rows" filter — the most common dashboard use-case
CREATE INDEX IF NOT EXISTS idx_recon_daily_discrepancy
    ON reconciliation_daily_summary(discrepancy_count)
    WHERE discrepancy_count > 0;

-- Category filter for department breakdown
CREATE INDEX IF NOT EXISTS idx_recon_daily_category
    ON reconciliation_daily_summary(category)
    WHERE category IS NOT NULL;

-- Composite: date range + discrepancy (most dashboard queries use both)
CREATE INDEX IF NOT EXISTS idx_recon_daily_date_discrepancy
    ON reconciliation_daily_summary(summary_date, discrepancy_count DESC);

COMMENT ON TABLE reconciliation_daily_summary IS
    'Materialized daily reconciliation metrics: analyzer log runs vs LIS records per service. '
    'Rebuilt by ReconciliationSummaryService on each log upload or LIS report ingestion. '
    'discrepancy_count > 0 = reagent waste — analyzer ran test without corresponding LIS order.';

COMMENT ON COLUMN reconciliation_daily_summary.discrepancy_count IS
    'Core business metric: analyzer ran this service but LIS has no record. '
    'Formula: logs_count - reconciled_count - pending_grace_count. '
    'Only logs→LIS direction is a discrepancy; LIS→logs deficit is NOT flagged.';

COMMENT ON COLUMN reconciliation_daily_summary.pending_grace_count IS
    'Samples in grace window (immunology hepatitis ≤72h). '
    'These are NOT counted as discrepancy until grace expires or LIS record appears.';
