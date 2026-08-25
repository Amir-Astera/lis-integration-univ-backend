-- =============================================================================
-- V27 — Additional analyzer instances and parsed-sample deduplication
-- =============================================================================
--
-- Purpose:
--   1. Seed dedicated analyzer rows for each physical workstation.
--      The lab has multiple BC-5000 / BS-230 boxes located in different rooms,
--      and three ABL800 (blood-gas) instruments. The Windows log-uploader sends
--      a separate analyzer_id per DriversManager{N} folder so we can track each
--      machine independently.
--
--   2. Add a unique partial index on parsed_analyzer_samples to make
--      cross-upload deduplication enforceable at the DB layer.
--      A concrete sample is identified by (analyzer_id, barcode, sample_timestamp).
--      Two uploads of the same Applogs.txt (or its rolled archive) will produce
--      the same triplet — the second insert is silently rejected, no duplicates.
-- =============================================================================

-- -------------------------------------------------------------------
-- 1. Workstation-specific analyzer rows
-- -------------------------------------------------------------------
-- These rows mirror the physical machines in DriversManager folders.
-- Naming convention: <model>-<location>. Same reagent rates as the parent model
-- are inherited via shared LIS device system name where possible.
INSERT INTO analyzers (
    id, name, type, workplace_name,
    lis_device_system_name, lis_analyzer_id, lis_device_name,
    serial_number, is_active, notes,
    created_at, updated_at, version
) VALUES
    -- DriversManager2 — Mindray BC-5000 (экстренная биохимия, взрослое отделение)
    ('mindray-bc-5000-er-adult', 'Mindray BC-5000 (Экстренное Взрослое)', 'HEMATOLOGY',
     'Гематология / Экстренное Взрослое',
     NULL, NULL, NULL, NULL, TRUE,
     'Физический BC-5000 в кабинете экстренного приёма (взрослые). DriversManager2.',
     NOW(), NOW(), 0),

    -- DriversManager5 — Mindray BC-5000 (детское отделение)
    ('mindray-bc-5000-pediatric', 'Mindray BC-5000 (Детство)', 'HEMATOLOGY',
     'Гематология / Детство',
     NULL, NULL, NULL, NULL, TRUE,
     'Физический BC-5000 в детском кабинете. DriversManager5.',
     NOW(), NOW(), 0),

    -- DriversManager6 — Mindray BC-5000 (КДЛ)
    ('mindray-bc-5000-kdl', 'Mindray BC-5000 (КДЛ)', 'HEMATOLOGY',
     'Гематология / КДЛ',
     NULL, NULL, NULL, NULL, TRUE,
     'Физический BC-5000 в КДЛ. DriversManager6.',
     NOW(), NOW(), 0),

    -- DriversManager4 — Mindray BS-230 (биохимия Детство), уже есть mindray-bs-230,
    -- но это отдельная физическая машина в детском отделении
    ('mindray-bs-230-pediatric', 'Mindray BS-230 (Детство)', 'BIOCHEMISTRY',
     'Биохимия / Детство',
     NULL, NULL, NULL, NULL, TRUE,
     'Физический BS-230 в детском отделении. DriversManager4.',
     NOW(), NOW(), 0),

    -- DriversManager9 — ABL800 #1
    ('abl-800-1', 'Radiometer ABL800 #1', 'BLOOD_GAS',
     'POCT / Газы крови',
     NULL, NULL, NULL, NULL, TRUE,
     'Анализатор газов крови. DriversManager9.',
     NOW(), NOW(), 0),

    -- DriversManager10 — ABL800 #2
    ('abl-800-2', 'Radiometer ABL800 #2', 'BLOOD_GAS',
     'POCT / Газы крови',
     NULL, NULL, NULL, NULL, TRUE,
     'Анализатор газов крови. DriversManager10.',
     NOW(), NOW(), 0),

    -- DriversManager11 — ABL800 #3
    ('abl-800-3', 'Radiometer ABL800 #3', 'BLOOD_GAS',
     'POCT / Газы крови',
     NULL, NULL, NULL, NULL, TRUE,
     'Анализатор газов крови. DriversManager11.',
     NOW(), NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- -------------------------------------------------------------------
-- 2. Cross-upload deduplication for parsed_analyzer_samples
-- -------------------------------------------------------------------
-- Unique partial index covering: same physical sample (analyzer + barcode + time)
-- can only be inserted once, regardless of which log upload it came from.
--
-- This protects against:
--   - Re-uploading the same Applogs.txt twice (e.g. manual re-upload, agent retry)
--   - Re-uploading a rolled Applogs<timestamp>.log whose content was previously
--     captured as part of a live Applogs.txt snapshot
--   - Multiple snapshots of a growing Applogs.txt (every 30 min the file is bigger
--     and we re-upload it; earlier samples are already in the DB)
--
-- Step A: clean up any pre-existing duplicates (keep the newest by created_at).
-- This makes the index creation safe on existing data.
WITH ranked AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY analyzer_id, barcode, sample_timestamp
               ORDER BY created_at DESC, id DESC
           ) AS rn
    FROM parsed_analyzer_samples
    WHERE analyzer_id IS NOT NULL
      AND barcode IS NOT NULL
      AND barcode <> ''
)
DELETE FROM parsed_analyzer_samples
WHERE id IN (SELECT id FROM ranked WHERE rn > 1);

-- Step B: install the unique partial index.
CREATE UNIQUE INDEX IF NOT EXISTS uq_parsed_sample_dedupe
    ON parsed_analyzer_samples (analyzer_id, barcode, sample_timestamp)
    WHERE analyzer_id IS NOT NULL AND barcode <> '';
