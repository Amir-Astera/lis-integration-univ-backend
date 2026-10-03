-- V36: Verified iFlash hormone service norms
--
-- Evidence boundary:
--   * The three full Damumed service names below occur in the normalized
--     completed-studies journal.
--   * Ykon iFlash 1800 has a distinct PATIENT_TEST analyzer_reagent_rates row
--     for each analyte, sourced from the iFlash operator manual.
--   * The current 1C snapshot identifies an iFlash FT4 item, but omits both
--     its unit and pack capacity. Its mapping is therefore SUGGESTED only and
--     cannot participate in stock-coverage arithmetic.

-- `UNITS` was used by early seed rows, but is not a ReagentUnitType in the
-- application. A cassette count is TEST. A named cartridge is one PIECE.
UPDATE analyzer_reagent_rates
SET unit_type = 'PIECE'
WHERE unit_type = 'UNITS'
  AND reagent_name ~* 'cartridge|картридж';

UPDATE analyzer_reagent_rates
SET unit_type = 'TEST'
WHERE unit_type = 'UNITS';

ALTER TABLE service_reagent_consumption_norms
    DROP CONSTRAINT IF EXISTS service_reagent_consumption_norms_unit_type_check;

ALTER TABLE service_reagent_consumption_norms
    DROP CONSTRAINT IF EXISTS chk_service_norm_unit_type;

ALTER TABLE service_reagent_consumption_norms
    ADD CONSTRAINT chk_service_norm_unit_type
    CHECK (unit_type IN ('ML', 'PIECE', 'TEST', 'TEST_POSITION'));

WITH norm_seed (
    id,
    service_name,
    analyzer_rate_id
) AS (
    VALUES
        (
            'norm-b06-446-006-iflash-ft3',
            'B06.446.006, Определение свободного трийодтиронина (T3) в сыворотке крови методом иммунохемилюминесценции',
            'rate-iflash1800-ft3'
        ),
        (
            'norm-b06-445-006-iflash-ft4',
            'B06.445.006, Определение свободного тироксина (T4) в сыворотке крови методом иммунохемилюминесценции',
            'rate-iflash1800-ft4'
        ),
        (
            'norm-b06-484-006-iflash-tsh',
            'B06.484.006, Определение тиреотропного гормона (ТТГ) в сыворотке крови методом иммунохемилюминесценции',
            'rate-iflash1800-tsh'
        )
)
INSERT INTO service_reagent_consumption_norms (
    id,
    service_name,
    service_name_normalized,
    service_category,
    analyzer_id,
    reagent_name,
    consumable_id,
    quantity_per_service,
    unit_type,
    source,
    source_document,
    notes,
    is_active,
    created_at,
    updated_at,
    version
)
SELECT
    s.id,
    s.service_name,
    LOWER(REGEXP_REPLACE(REGEXP_REPLACE(s.service_name, '[.,;:!?]', '', 'g'), '\s+', ' ', 'g')),
    'IMMUNOLOGY',
    r.analyzer_id,
    r.reagent_name,
    NULL,
    r.units_per_operation::NUMERIC,
    'TEST',
    'CALCULATED_FROM_ANALYZER_RATE',
    r.source_document,
    'Exact Damumed service code; one test from the iFlash 50-test cassette; analyzer_reagent_rates.id=' || r.id,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM norm_seed s
INNER JOIN analyzer_reagent_rates r ON r.id = s.analyzer_rate_id
WHERE r.operation_type = 'PATIENT_TEST'
  AND r.units_per_operation > 0
ON CONFLICT (id) DO UPDATE SET
    service_name = EXCLUDED.service_name,
    service_name_normalized = EXCLUDED.service_name_normalized,
    service_category = EXCLUDED.service_category,
    analyzer_id = EXCLUDED.analyzer_id,
    reagent_name = EXCLUDED.reagent_name,
    consumable_id = EXCLUDED.consumable_id,
    quantity_per_service = EXCLUDED.quantity_per_service,
    unit_type = EXCLUDED.unit_type,
    source = EXCLUDED.source,
    source_document = EXCLUDED.source_document,
    notes = EXCLUDED.notes,
    is_active = EXCLUDED.is_active,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO service_to_analyzer_mappings (
    id,
    service_name_pattern,
    service_category,
    analyzer_id,
    matching_priority,
    is_active,
    created_at,
    updated_at,
    version
) VALUES
    (
        'map-dam-b06-446-006-iflash-ft3',
        'B06.446.006, Определение свободного трийодтиронина (T3) в сыворотке крови методом иммунохемилюминесценции',
        'IMMUNOLOGY',
        'ykon-iflash-1800',
        5,
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        0
    ),
    (
        'map-dam-b06-445-006-iflash-ft4',
        'B06.445.006, Определение свободного тироксина (T4) в сыворотке крови методом иммунохемилюминесценции',
        'IMMUNOLOGY',
        'ykon-iflash-1800',
        5,
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        0
    ),
    (
        'map-dam-b06-484-006-iflash-tsh',
        'B06.484.006, Определение тиреотропного гормона (ТТГ) в сыворотке крови методом иммунохемилюминесценции',
        'IMMUNOLOGY',
        'ykon-iflash-1800',
        5,
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        0
    )
ON CONFLICT (id) DO UPDATE SET
    service_name_pattern = EXCLUDED.service_name_pattern,
    service_category = EXCLUDED.service_category,
    analyzer_id = EXCLUDED.analyzer_id,
    matching_priority = EXCLUDED.matching_priority,
    is_active = EXCLUDED.is_active,
    updated_at = CURRENT_TIMESTAMP;

WITH latest_snapshot AS (
    SELECT id, payload_json::JSONB AS payload
    FROM onec_readonly_snapshots
    ORDER BY imported_at DESC
    LIMIT 1
)
INSERT INTO onec_lims_item_mappings (
    id,
    snapshot_id,
    onec_item_ref,
    onec_item_code,
    onec_item_name,
    target_kind,
    target_name,
    target_unit,
    conversion_factor,
    mapping_status,
    notes,
    created_at,
    created_by,
    updated_at,
    updated_by,
    version
)
SELECT
    'onec-map-n00018050-iflash-ft4',
    s.id,
    NULL,
    'Н00018050',
    'Тест для определения iFlash-FT4',
    'REAGENT',
    'FT4 Cassette (50 tests)',
    'PACK',
    1.000000,
    'SUGGESTED',
    'Exact iFlash FT4 item name, but 1C provides no unit or pack capacity. Excluded from stock coverage until the package capacity is sourced.',
    CURRENT_TIMESTAMP,
    'system-v36',
    CURRENT_TIMESTAMP,
    'system-v36',
    0
FROM latest_snapshot s
WHERE EXISTS (
    SELECT 1
    FROM JSONB_ARRAY_ELEMENTS(s.payload -> 'inventory') inventory(item)
    WHERE BTRIM(inventory.item ->> 'nomenclatureCode') = 'Н00018050'
)
ON CONFLICT (id) DO UPDATE SET
    snapshot_id = EXCLUDED.snapshot_id,
    onec_item_code = EXCLUDED.onec_item_code,
    onec_item_name = EXCLUDED.onec_item_name,
    target_kind = EXCLUDED.target_kind,
    target_name = EXCLUDED.target_name,
    target_unit = EXCLUDED.target_unit,
    conversion_factor = EXCLUDED.conversion_factor,
    mapping_status = EXCLUDED.mapping_status,
    notes = EXCLUDED.notes,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system-v36';
