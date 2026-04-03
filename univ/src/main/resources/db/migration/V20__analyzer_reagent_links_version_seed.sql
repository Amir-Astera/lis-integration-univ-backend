-- V20: Add version column to analyzer_reagent_links + auto-seed from existing inventory
-- The version column enables Spring Data R2DBC optimistic locking (INSERT vs UPDATE detection).

ALTER TABLE analyzer_reagent_links
    ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

-- Auto-seed: create MAIN links for every reagent_inventory item that has an analyzer_id set.
-- Uses INSERT ... ON CONFLICT DO NOTHING so repeated runs are safe.
INSERT INTO analyzer_reagent_links (
    id, analyzer_id, reagent_inventory_id, usage_role,
    norm_reagent_name, is_active, created_at, updated_at, version
)
SELECT
    gen_random_uuid()::text,
    ri.analyzer_id,
    ri.id,
    'MAIN',
    ri.reagent_name,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM reagent_inventory ri
WHERE ri.analyzer_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM analyzer_reagent_links arl
      WHERE arl.analyzer_id         = ri.analyzer_id
        AND arl.reagent_inventory_id = ri.id
        AND arl.usage_role           = 'MAIN'
  );

-- Additionally seed from analyzer_reagent_rates: for each rate entry, find matching
-- reagent_inventory rows by reagent_name (case-insensitive) and link them.
INSERT INTO analyzer_reagent_links (
    id, analyzer_id, reagent_inventory_id, usage_role,
    norm_reagent_name, estimated_daily_ml, is_active, created_at, updated_at, version
)
SELECT DISTINCT ON (arr.analyzer_id, ri.id)
    gen_random_uuid()::text,
    arr.analyzer_id,
    ri.id,
    CASE
        WHEN LOWER(arr.operation_type) = 'wash'       THEN 'WASH'
        WHEN LOWER(arr.operation_type) = 'calibration' THEN 'CALIBRATOR'
        WHEN LOWER(arr.operation_type) = 'qc_control'  THEN 'CONTROL'
        ELSE 'MAIN'
    END,
    arr.reagent_name,
    CASE WHEN arr.unit_type = 'ML' THEN arr.volume_per_operation_ml ELSE NULL END,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM analyzer_reagent_rates arr
JOIN reagent_inventory ri
    ON ri.analyzer_id = arr.analyzer_id
   AND LOWER(ri.reagent_name) = LOWER(arr.reagent_name)
WHERE NOT EXISTS (
    SELECT 1 FROM analyzer_reagent_links arl
    WHERE arl.analyzer_id          = arr.analyzer_id
      AND arl.reagent_inventory_id  = ri.id
      AND arl.usage_role = CASE
            WHEN LOWER(arr.operation_type) = 'wash'        THEN 'WASH'
            WHEN LOWER(arr.operation_type) = 'calibration' THEN 'CALIBRATOR'
            WHEN LOWER(arr.operation_type) = 'qc_control'  THEN 'CONTROL'
            ELSE 'MAIN'
          END
);
