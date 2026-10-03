ALTER TABLE sample_reconciliation
    ADD COLUMN IF NOT EXISTS comparison_availability VARCHAR(64) NOT NULL DEFAULT 'UNKNOWN',
    ADD COLUMN IF NOT EXISTS comparison_reason TEXT;

CREATE INDEX IF NOT EXISTS idx_sample_reconciliation_comparison_availability
    ON sample_reconciliation(comparison_availability, sample_date DESC);
