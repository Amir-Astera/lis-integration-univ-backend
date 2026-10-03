-- V37: Service norms for journal lines that already have a patient-test rate
--
-- Evidence boundary:
--   * Every service name below is a distinct Damumed journal line.
--   * Every quantity is copied from an existing PATIENT_TEST row in
--     analyzer_reagent_rates. No manual-method line is given an analyzer norm.
--   * CBC on 6 parameters uses the BC-5000 test_mode CBC rates
--     (diluent 23.2 mL, LH lyse 0.2 mL, rinse 0.5 mL), not the 5-diff profile.
--   * Sodium, potassium and chloride have stock names but no per-test rate,
--     so they are not given a consumption norm.

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
    service_category,
    analyzer_rate_id,
    consumable_id,
    unit_override
) AS (
    VALUES
        (
            'norm-b03-398-002-bilt',
            'B03.398.002, Определение общего билирубина в сыворотке крови на анализаторе',
            'BIOCHEMISTRY',
            'bs230-bilt-test',
            NULL,
            NULL
        ),
        (
            'norm-b03-435-002-bild',
            'B03.435.002, Определение прямого билирубина в сыворотке крови на анализаторе',
            'BIOCHEMISTRY',
            'bs230-bild-test',
            NULL,
            NULL
        ),
        (
            'norm-b03-115-002-crp',
            'B03.115.002, Определение «C» реактивного белка (СРБ) в сыворотке крови количественно',
            'BIOCHEMISTRY',
            'bs230-crp-test',
            NULL,
            NULL
        ),
        (
            'norm-b03-500-002-fer',
            'B03.500.002, Определение ферритина в сыворотке крови на анализаторе',
            'BIOCHEMISTRY',
            'bs230-fer-test',
            NULL,
            NULL
        ),
        (
            'norm-b03-850-002-p',
            'B03.850.002, Определение фосфора (P) в сыворотке крови на анализаторе',
            'BIOCHEMISTRY',
            'bs230-p-test',
            NULL,
            NULL
        ),
        (
            'norm-b02-114-002-cbc-diluent',
            'B02.114.002, Общий анализ крови 6 параметров на анализаторе',
            'HEMATOLOGY',
            'rate-bc5000-diluent-cbc',
            NULL,
            NULL
        ),
        (
            'norm-b02-114-002-cbc-lh',
            'B02.114.002, Общий анализ крови 6 параметров на анализаторе',
            'HEMATOLOGY',
            'rate-bc5000-lh-lyse-cbc',
            NULL,
            NULL
        ),
        (
            'norm-b02-114-002-cbc-rinse',
            'B02.114.002, Общий анализ крови 6 параметров на анализаторе',
            'HEMATOLOGY',
            'rate-bc5000-rinse',
            NULL,
            NULL
        ),
        (
            'norm-b03-328-002-hba1c',
            'B03.328.002, Определение гликозилированного гемоглобина в крови на анализаторе',
            'BIOCHEMISTRY',
            'rate-vision-pro-cartridge',
            NULL,
            'PIECE'
        ),
        (
            'norm-b03-318-002-blood-gas',
            'B03.318.002, Определение газов и электролитов крови с добавочными тестами (лактат, глюкоза, карбоксигемоглобин) на анализаторе',
            'POCT',
            'rate-edan-i15-cartridge',
            NULL,
            'PIECE'
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
    LOWER(REGEXP_REPLACE(REGEXP_REPLACE(s.service_name, '[.,;:!?«»]', '', 'g'), '\s+', ' ', 'g')),
    s.service_category,
    r.analyzer_id,
    r.reagent_name,
    s.consumable_id,
    COALESCE(r.volume_per_operation_ml, r.units_per_operation::NUMERIC),
    COALESCE(
        s.unit_override,
        CASE
            WHEN r.unit_type IN ('ML', 'PIECE', 'TEST', 'TEST_POSITION') THEN r.unit_type
            ELSE 'TEST'
        END
    ),
    'CALCULATED_FROM_ANALYZER_RATE',
    r.source_document,
    'Journal service line; amount copied from analyzer_reagent_rates.id=' || r.id
        || CASE
            WHEN r.test_mode IS NOT NULL THEN '; test_mode=' || r.test_mode
            ELSE ''
           END,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM norm_seed s
INNER JOIN analyzer_reagent_rates r ON r.id = s.analyzer_rate_id
WHERE r.operation_type = 'PATIENT_TEST'
  AND COALESCE(r.volume_per_operation_ml, r.units_per_operation::NUMERIC) > 0
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
    ('map-dam-b03-398-002', 'B03.398.002, Определение общего билирубина в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b03-435-002', 'B03.435.002, Определение прямого билирубина в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b03-115-002', 'B03.115.002, Определение «C» реактивного белка (СРБ) в сыворотке крови количественно', 'BIOCHEMISTRY', 'mindray-bs-230', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b03-500-002', 'B03.500.002, Определение ферритина в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b03-850-002', 'B03.850.002, Определение фосфора (P) в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b02-114-002', 'B02.114.002, Общий анализ крови 6 параметров на анализаторе', 'HEMATOLOGY', 'mindray-bc-5000', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b03-328-002', 'B03.328.002, Определение гликозилированного гемоглобина в крови на анализаторе', 'BIOCHEMISTRY', 'vision-pro', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b03-318-002', 'B03.318.002, Определение газов и электролитов крови с добавочными тестами (лактат, глюкоза, карбоксигемоглобин) на анализаторе', 'POCT', 'edan-i15', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO UPDATE SET
    service_name_pattern = EXCLUDED.service_name_pattern,
    service_category = EXCLUDED.service_category,
    analyzer_id = EXCLUDED.analyzer_id,
    matching_priority = EXCLUDED.matching_priority,
    is_active = EXCLUDED.is_active,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO service_catalog (
    id,
    canonical_name,
    category,
    lis_aliases,
    analyzer_service_ids,
    analyzer_parameters,
    analyzer_types,
    grace_hours,
    is_active,
    notes,
    created_at,
    updated_at,
    version
) VALUES
    (
        'sc-bio-017',
        'Фосфор неорганический',
        'BIOCHEMISTRY',
        '["b03.850.002","фосфора (p)","inorganic phosphorus"]',
        '[]',
        '["P","PHOS"]',
        '["BIOCHEMISTRY"]',
        0,
        TRUE,
        'Сывороточный фосфор. Норма расхода берётся из Mindray P на BS-230.',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        0
    ),
    (
        'sc-bio-018',
        'Гликозилированный гемоглобин (HbA1c)',
        'BIOCHEMISTRY',
        '["b03.328.002","hba1c","гликозилированного гемоглобина"]',
        '[]',
        '["HbA1c","A1C"]',
        '["HBA1C"]',
        0,
        TRUE,
        'Один картридж Vision Pro на одно исследование.',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        0
    ),
    (
        'sc-poc-001',
        'Газы и электролиты крови',
        'POCT',
        '["b03.318.002","газов и электролитов"]',
        '[]',
        '["pH","pCO2","pO2","Lactate","Glucose","COHb"]',
        '["BLOOD_GAS"]',
        0,
        TRUE,
        'Панель Edan i15. Одна строка журнала — один картридж, без деления на отдельные аналиты.',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        0
    )
ON CONFLICT (id) DO UPDATE SET
    canonical_name = EXCLUDED.canonical_name,
    category = EXCLUDED.category,
    lis_aliases = EXCLUDED.lis_aliases,
    analyzer_parameters = EXCLUDED.analyzer_parameters,
    analyzer_types = EXCLUDED.analyzer_types,
    notes = EXCLUDED.notes,
    is_active = EXCLUDED.is_active,
    updated_at = CURRENT_TIMESTAMP;

UPDATE service_catalog AS c
SET lis_aliases = (
        SELECT jsonb_agg(alias ORDER BY alias)::text
        FROM (
            SELECT DISTINCT alias
            FROM (
                SELECT jsonb_array_elements_text(c.lis_aliases::jsonb) AS alias
                UNION ALL
                SELECT added.alias
                FROM (VALUES
                    ('sc-bio-003', 'b03.398.002'),
                    ('sc-bio-004', 'b03.435.002'),
                    ('sc-imm-010', 'b03.115.002'),
                    ('sc-imm-009', 'b03.500.002'),
                    ('sc-hem-001', 'b02.114.002'),
                    ('sc-hem-002', 'b02.110.002'),
                    ('sc-hem-004', 'b02.061.002'),
                    ('sc-imm-004', 'b06.484.006'),
                    ('sc-imm-005', 'b06.445.006'),
                    ('sc-imm-006', 'b06.446.006'),
                    ('sc-ura-001', 'b01.077.002')
                ) AS added(catalog_id, alias)
                WHERE added.catalog_id = c.id
            ) merged
        ) distinct_aliases
    ),
    updated_at = CURRENT_TIMESTAMP
WHERE c.id IN (
    'sc-bio-003', 'sc-bio-004', 'sc-imm-010', 'sc-imm-009',
    'sc-hem-001', 'sc-hem-002', 'sc-hem-004',
    'sc-imm-004', 'sc-imm-005', 'sc-imm-006', 'sc-ura-001'
);
