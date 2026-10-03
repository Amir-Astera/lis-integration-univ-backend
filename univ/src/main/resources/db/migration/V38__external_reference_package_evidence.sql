-- V38: Public package references for suggested 1C ↔ LIMS mappings.
--
-- These references are deliberately informational only. The 1C nomenclature
-- lines do not contain an article, manufacturer, or package capacity, therefore
-- none of these mappings is upgraded to CONFIRMED or used in coverage math.
--
-- Sources:
--   * Mindray BS chemistry reagent listing (public distributor catalogue):
--     ALT/AST/Bil-T/Bil-D/UREA = R1 4x35 mL + R2 2x18 mL = 176 mL;
--     Glu-G = R1 4x40 mL + R2 2x20 mL = 200 mL.
--   * Mission URS10U public product listings: common retail canister = 100
--     strips. The generic 1C item does not state Mission/URS10U or pack size.
--   * Existing LIMS reagent inventory provides a local reference capacity for
--     several remaining analytes, but cannot prove that a generic 1C item is
--     the same vendor/package.

WITH reference_capacity (id, capacity, evidence) AS (
    VALUES
        ('onec-map-n00018413-alt', '176 mL', 'Public Mindray BS reagent listing and local LIMS lots agree on 176 mL.'),
        ('onec-map-n00018415-ast', '176 mL', 'Public Mindray BS reagent listing and local LIMS lots agree on 176 mL.'),
        ('onec-map-n00018554-glu', '200 mL', 'Public Mindray BS reagent listing and local LIMS lots agree on 200 mL.'),
        ('onec-map-n00017959-ure', '176 mL', 'Public Mindray BS reagent listing and local LIMS lots agree on 176 mL.'),
        ('onec-map-n00017957-bt', '176 mL', 'Public Mindray BS reagent listing and local LIMS lots agree on 176 mL.'),
        ('onec-map-n00017958-bd', '176 mL', 'Public Mindray BS reagent listing and local LIMS lots agree on 176 mL.'),
        ('onec-map-n00018048-alb', '160 mL', 'Local LIMS reference lots have 160 mL; no manufacturer/package evidence in 1C.'),
        ('onec-map-n00018039-cre', '72 mL', 'Local LIMS reference lots have 72 mL; no manufacturer/package evidence in 1C.'),
        ('onec-map-n00019337-ggt', '176 mL', 'Local LIMS reference lots have 176 mL; no manufacturer/package evidence in 1C.'),
        ('onec-map-n00018040-tp', '160 mL', 'Local LIMS reference lots have 160 mL; no manufacturer/package evidence in 1C.'),
        ('onec-map-n00018538-hdl', '54 mL', 'Local LIMS reference lots have 54 mL; no manufacturer/package evidence in 1C.'),
        ('onec-map-n00018416-ldl', '54 mL', 'Local LIMS reference lots have 54 mL; no manufacturer/package evidence in 1C.'),
        ('onec-map-n00018038-tc', '160 mL', 'Local LIMS reference lots have 160 mL; no manufacturer/package evidence in 1C.'),
        ('onec-map-n00018749-tg', '160 mL', 'Local LIMS reference lots have 160 mL; no manufacturer/package evidence in 1C.'),
        ('onec-map-n00013118-u500', '100 strips', 'Public Mission URS10U listings commonly specify 100 strips, but the 1C item has no brand or pack count.')
)
UPDATE onec_lims_item_mappings m
SET notes = CONCAT_WS(
        E'\n',
        NULLIF(m.notes, ''),
        'Reference-only capacity: ' || r.capacity || '. ' || r.evidence ||
            ' 1C capacity is absent; mapping remains SUGGESTED and is excluded from stock coverage.'
    ),
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system-v38'
FROM reference_capacity r
WHERE m.id = r.id
  AND m.mapping_status = 'SUGGESTED';

-- A direct single-service analyzer → Damumed link exists for CRP. The 1C item
-- is semantically exact, but has no package capacity and remains non-arithmetic.
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
    'onec-map-n00018417-crp',
    s.id,
    NULL,
    'Н00018417',
    'Диагностический набор реагентов для определения С-реактивного белка',
    'REAGENT',
    'Mindray CRP (С-реактивный белок)',
    'PACK',
    1.000000,
    'SUGGESTED',
    'Exact analyte semantic match. Three dated analyzer samples link to a Damumed referral with CRP as the only service. 1C has no manufacturer/article/package capacity, so this is excluded from coverage arithmetic.',
    CURRENT_TIMESTAMP,
    'system-v38',
    CURRENT_TIMESTAMP,
    'system-v38',
    0
FROM latest_snapshot s
WHERE EXISTS (
    SELECT 1
    FROM JSONB_ARRAY_ELEMENTS(s.payload -> 'inventory') inventory(item)
    WHERE BTRIM(inventory.item ->> 'nomenclatureCode') = 'Н00018417'
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
    updated_by = 'system-v38';
