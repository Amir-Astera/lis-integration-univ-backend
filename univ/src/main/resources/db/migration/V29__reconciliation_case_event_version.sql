-- Spring Data R2DBC needs a null @Version value to treat an entity with an
-- application-generated UUID as a new row instead of attempting an UPDATE.
ALTER TABLE reconciliation_case_events
    ADD COLUMN IF NOT EXISTS version BIGINT;
