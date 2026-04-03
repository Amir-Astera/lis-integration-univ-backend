-- Align log_anomaly_records with LogAnomalyRecordEntity (estimated_reagents_json).
-- Needed when V15 was applied before this column existed in the schema.

ALTER TABLE log_anomaly_records
    ADD COLUMN IF NOT EXISTS estimated_reagents_json TEXT;

COMMENT ON COLUMN log_anomaly_records.estimated_reagents_json IS
    'JSON array of estimated reagent consumption for this anomaly row.';
