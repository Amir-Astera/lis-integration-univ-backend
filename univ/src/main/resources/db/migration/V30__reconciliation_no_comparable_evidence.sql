-- Missing source coverage is not a discrepancy. It is retained as a separate
-- state so activity can be counted without inventing a reconciliation fact.

ALTER TABLE reconciliation_daily_summary
    ADD COLUMN IF NOT EXISTS no_comparable_evidence_count INTEGER NOT NULL DEFAULT 0;

ALTER TABLE sample_reconciliation
    DROP CONSTRAINT IF EXISTS sample_reconciliation_reconciliation_status_check;

ALTER TABLE sample_reconciliation
    ADD CONSTRAINT sample_reconciliation_reconciliation_status_check
    CHECK (reconciliation_status IN (
        'LEGITIMATE',
        'DISCREPANCY',
        'PENDING_GRACE',
        'NO_COMPARABLE_EVIDENCE',
        'WASH_TEST',
        'RERUN'
    ));

CREATE INDEX IF NOT EXISTS idx_samp_recon_no_comparable_evidence
    ON sample_reconciliation(sample_date DESC)
    WHERE reconciliation_status = 'NO_COMPARABLE_EVIDENCE';
