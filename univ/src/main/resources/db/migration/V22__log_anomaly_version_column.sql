-- Migration: V22 - Add optimistic locking version columns to log anomaly tables
ALTER TABLE log_anomaly_records
    ADD COLUMN IF NOT EXISTS version BIGINT;

ALTER TABLE log_anomaly_daily_summary
    ADD COLUMN IF NOT EXISTS version BIGINT;
