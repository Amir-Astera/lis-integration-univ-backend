-- V40: Directly observed BS-240 service identity.
--
-- Evidence source: real BS-240 Applogs Save string JSON. Each listed NMU code
-- was parsed from OrderResearch.ServiceMo.Code together with ServiceID and
-- OrderResearchID. This replaces prior generic BS-230 assignments only for
-- codes observed on the physical BS-240; it does not generalize to all
-- biochemistry services.

WITH observed_norm (
    id,
    service_name,
    rate_id
) AS (
    VALUES
        ('norm-b03-115-002-crp',  'B03.115.002, Определение «C» реактивного белка (СРБ) в сыворотке крови количественно', 'bs240-crp-test'),
        ('norm-b03-293-002-ast',  'B03.293.002, Определение аспартатаминотрансферазы (АСаТ) в сыворотке крови на анализаторе', 'bs240-ast-test'),
        ('norm-b03-335-002-glu',  'B03.335.002, Определение глюкозы в сыворотке крови на анализаторе', 'bs240-glu-test'),
        ('norm-b03-363-002-crea', 'B03.363.002, Определение креатинина в сыворотке крови на анализаторе', 'bs240-crea-test'),
        ('norm-b03-386-002-urea', 'B03.386.002, Определение мочевины в сыворотке крови на анализаторе', 'bs240-urea-test'),
        ('norm-b03-398-002-bilt', 'B03.398.002, Определение общего билирубина в сыворотке крови на анализаторе', 'bs240-bilt-test'),
        ('norm-b03-401-002-tc',   'B03.401.002, Определение общего холестерина в сыворотке крови на анализаторе', 'bs240-tc-test'),
        ('norm-b03-500-002-fer',  'B03.500.002, Определение ферритина в сыворотке крови на анализаторе', 'bs240-fer-test'),
        ('norm-b03-160-002-amy',  'B03.160.002, Определение амилазы панкреатической в сыворотке крови на анализаторе', 'bs240-amy-test'),
        ('norm-b03-335-003-glue', 'B03.335.003, Определение глюкозы в сыворотке крови экспресс методом', 'bs240-glu-test')
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
    o.id,
    o.service_name,
    LOWER(REGEXP_REPLACE(REGEXP_REPLACE(o.service_name, '[.,;:!?«»]', '', 'g'), '\s+', ' ', 'g')),
    'BIOCHEMISTRY',
    r.analyzer_id,
    r.reagent_name,
    NULL,
    COALESCE(r.volume_per_operation_ml, r.units_per_operation::NUMERIC),
    CASE
        WHEN r.unit_type IN ('ML', 'PIECE', 'TEST', 'TEST_POSITION') THEN r.unit_type
        ELSE 'TEST'
    END,
    'CALCULATED_FROM_ANALYZER_RATE',
    r.source_document,
    'Observed in BS-240 Applogs with ServiceMo.Code; amount copied from analyzer_reagent_rates.id=' || r.id,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM observed_norm o
INNER JOIN analyzer_reagent_rates r ON r.id = o.rate_id
WHERE r.analyzer_id = 'mindray-bs-240'
  AND r.operation_type = 'PATIENT_TEST'
  AND COALESCE(r.volume_per_operation_ml, r.units_per_operation::NUMERIC) > 0
ON CONFLICT (id) DO UPDATE SET
    service_name = EXCLUDED.service_name,
    service_name_normalized = EXCLUDED.service_name_normalized,
    service_category = EXCLUDED.service_category,
    analyzer_id = EXCLUDED.analyzer_id,
    reagent_name = EXCLUDED.reagent_name,
    quantity_per_service = EXCLUDED.quantity_per_service,
    unit_type = EXCLUDED.unit_type,
    source = EXCLUDED.source,
    source_document = EXCLUDED.source_document,
    notes = EXCLUDED.notes,
    is_active = EXCLUDED.is_active,
    updated_at = CURRENT_TIMESTAMP;

-- The old exact mappings used BS-230 because no physical analyzer evidence
-- existed then. Observed ServiceMo.Code now proves these services on BS-240.
UPDATE service_to_analyzer_mappings
SET analyzer_id = 'mindray-bs-240',
    matching_priority = 1,
    updated_at = CURRENT_TIMESTAMP
WHERE service_name_pattern IN (
    'B03.115.002, Определение «C» реактивного белка (СРБ) в сыворотке крови количественно',
    'B03.293.002, Определение аспартатаминотрансферазы (АСаТ) в сыворотке крови на анализаторе',
    'B03.335.002, Определение глюкозы в сыворотке крови на анализаторе',
    'B03.363.002, Определение креатинина в сыворотке крови на анализаторе',
    'B03.386.002, Определение мочевины в сыворотке крови на анализаторе',
    'B03.398.002, Определение общего билирубина в сыворотке крови на анализаторе',
    'B03.401.002, Определение общего холестерина в сыворотке крови на анализаторе',
    'B03.500.002, Определение ферритина в сыворотке крови на анализаторе'
);

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
    ('map-dam-b03-160-002-bs240', 'B03.160.002, Определение амилазы панкреатической в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-240', 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b03-335-003-bs240', 'B03.335.003, Определение глюкозы в сыворотке крови экспресс методом', 'BIOCHEMISTRY', 'mindray-bs-240', 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO UPDATE SET
    service_name_pattern = EXCLUDED.service_name_pattern,
    service_category = EXCLUDED.service_category,
    analyzer_id = EXCLUDED.analyzer_id,
    matching_priority = EXCLUDED.matching_priority,
    is_active = EXCLUDED.is_active,
    updated_at = CURRENT_TIMESTAMP;
