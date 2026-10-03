-- V35: Verified Damumed service norms and 1C inventory links
--
-- Evidence boundary:
--   * Each activated service norm is sourced from a pre-existing PATIENT_TEST
--   * analyzer_reagent_rates row with its source document preserved below.
--   * Each CONFIRMED 1C mapping has an explicit package size in the 1C item
--   * name, so the conversion from analyzer consumption to packages is known.
--   * 1C items without a package size remain SUGGESTED. They are visible as
--   * semantic links but intentionally excluded from stock-coverage arithmetic.

-- ============================================================================
-- 1. Exact Damumed service → analyzer → per-test consumption norms
-- ============================================================================
WITH norm_seed (
    id,
    service_name,
    service_category,
    analyzer_id,
    analyzer_rate_id,
    consumable_id
) AS (
    VALUES
        -- Hematology: CBC with 5-part differential
        ('norm-b02-110-002-cbc-diluent', 'B02.110.002, Общий анализ крови на анализаторе с дифференцировкой 5 классов клеток', 'HEMATOLOGY', 'mindray-bc-5000', 'rate-bc5000-diluent-cbc-diff', NULL),
        ('norm-b02-110-002-cbc-diff',    'B02.110.002, Общий анализ крови на анализаторе с дифференцировкой 5 классов клеток', 'HEMATOLOGY', 'mindray-bc-5000', 'rate-bc5000-diff-lyse-cbc-diff', NULL),
        ('norm-b02-110-002-cbc-lh',      'B02.110.002, Общий анализ крови на анализаторе с дифференцировкой 5 классов клеток', 'HEMATOLOGY', 'mindray-bc-5000', 'rate-bc5000-lh-lyse-cbc-diff', NULL),
        ('norm-b02-110-002-cbc-rinse',   'B02.110.002, Общий анализ крови на анализаторе с дифференцировкой 5 классов клеток', 'HEMATOLOGY', 'mindray-bc-5000', 'rate-bc5000-rinse', NULL),

        -- Biochemistry: the exact analyzer-labelled Damumed lines
        ('norm-b03-335-002-glu',  'B03.335.002, Определение глюкозы в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 'bs230-glu-test', NULL),
        ('norm-b03-398-002-bilt', 'B03.398.002, Определение общего билирубина в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 'bs230-bil-t-test', NULL),
        ('norm-b03-386-002-urea', 'B03.386.002, Определение мочевины в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 'bs230-urea-test', NULL),
        ('norm-b03-435-002-bild', 'B03.435.002, Определение прямого билирубина в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 'bs230-bil-d-test', NULL),
        ('norm-b03-293-002-ast',  'B03.293.002, Определение аспартатаминотрансферазы (АСаТ) в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 'bs230-ast-test', NULL),
        ('norm-b03-155-002-alt',  'B03.155.002, Определение аланинаминотрансферазы (АЛаТ) в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 'bs230-alt-test', NULL),
        ('norm-b03-363-002-crea', 'B03.363.002, Определение креатинина в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 'bs230-crea-test', NULL),
        ('norm-b03-397-002-tp',   'B03.397.002, Определение общего белка в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 'bs230-tp-test', NULL),
        ('norm-b03-403-002-amy',  'B03.403.002, Определение общей альфа-амилазы в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 'bs230-amy-test', NULL),
        ('norm-b03-401-002-tc',   'B03.401.002, Определение общего холестерина в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 'bs230-tc-test', NULL),
        ('norm-b03-526-002-alp',  'B03.526.002, Определение щелочной фосфатазы в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 'bs230-alp-test', NULL),
        ('norm-b03-486-002-tg',   'B03.486.002, Определение триглицеридов в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 'bs230-tg-test', NULL),
        ('norm-b03-372-002-ldl',  'B03.372.002, Определение липопротеидов низкой плотности в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 'bs230-ldl-test', NULL),
        ('norm-b03-156-002-alb',  'B03.156.002, Определение альбумина в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 'bs230-alb-test', NULL),
        ('norm-b03-371-002-hdl',  'B03.371.002, Определение липопротеидов высокой плотности в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 'bs230-hdl-test', NULL),
        ('norm-b03-316-002-ggt',  'B03.316.002, Определение гаммаглютамилтранспептидазы (ГГТП) в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 'bs230-ggt-test', NULL),
        ('norm-b03-340-002-fe',   'B03.340.002, Определение железа (Fe) в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 'bs230-fe-test', NULL),
        ('norm-b03-437-002-rf',   'B03.437.002, Определение ревматоидного фактора в сыворотке крови количественно на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 'bs230-rf-test', NULL),
        ('norm-b03-387-002-ua',   'B03.387.002, Определение мочевой кислоты в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 'bs230-ua-test', NULL),
        ('norm-b03-353-002-ca',   'B03.353.002, Определение кальция (Ca) в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 'bs230-ca-test', NULL),
        ('norm-b03-367-002-ldh',  'B03.367.002, Определение лактатдегидрогиназы (ЛДГ) в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 'bs230-ldh-test', NULL),
        ('norm-b03-370-002-lip',  'B03.370.002, Определение липазы в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 'bs230-lip-test', NULL),
        ('norm-b03-364-002-ck',   'B03.364.002, Определение креатинфосфокиназы (КФК) в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 'bs230-ck-test', NULL),
        ('norm-b03-206-002-aso',  'B03.206.002, Определение антистрептолизина «O» в сыворотке крови количественно на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 'bs230-aso-test', NULL),

        -- Urinalysis
        ('norm-b01-077-002-strip', 'B01.077.002, Исследование общего анализа мочи на анализаторе (физико-химические свойства с подсчетом количества клеточных элементов мочевого осадка)', 'URINALYSIS', 'mission-u500', 'rate-mission-u500-strip', 'cartridge-u500-10'),

        -- Coagulation: all reagent components and the disposable cuvette
        ('norm-b04-379-002-pt',  'B04.379.002, Определение протромбинового времени (ПВ) с последующим расчетом протромбинового индекса (ПТИ) и международного нормализованного отношения (МНО) в плазме крови на анализаторе (ПВ-ПТИ-МНО)', 'COAGULATION', 'mindray-c3100', 'rate-c3100-pt-reagent', NULL),
        ('norm-b04-379-002-cuv', 'B04.379.002, Определение протромбинового времени (ПВ) с последующим расчетом протромбинового индекса (ПТИ) и международного нормализованного отношения (МНО) в плазме крови на анализаторе (ПВ-ПТИ-МНО)', 'COAGULATION', 'mindray-c3100', 'rate-c3100-cuvette', 'cuvette-coagulation'),
        ('norm-b04-149-002-aptt', 'B04.149.002, Определение активированного частичного тромбопластинового времени (АЧТВ) в плазме крови на анализаторе', 'COAGULATION', 'mindray-c3100', 'rate-c3100-aptt-reagent', NULL),
        ('norm-b04-149-002-ca',   'B04.149.002, Определение активированного частичного тромбопластинового времени (АЧТВ) в плазме крови на анализаторе', 'COAGULATION', 'mindray-c3100', 'rate-c3100-aptt-cacl2', NULL),
        ('norm-b04-149-002-cuv',  'B04.149.002, Определение активированного частичного тромбопластинового времени (АЧТВ) в плазме крови на анализаторе', 'COAGULATION', 'mindray-c3100', 'rate-c3100-cuvette', 'cuvette-coagulation'),
        ('norm-b04-501-002-fib',  'B04.501.002, Определение фибриногена в плазме крови на анализаторе', 'COAGULATION', 'mindray-c3100', 'rate-c3100-fib-reagent', NULL),
        ('norm-b04-501-002-buf',  'B04.501.002, Определение фибриногена в плазме крови на анализаторе', 'COAGULATION', 'mindray-c3100', 'rate-c3100-fib-buffer', NULL),
        ('norm-b04-501-002-cuv',  'B04.501.002, Определение фибриногена в плазме крови на анализаторе', 'COAGULATION', 'mindray-c3100', 'rate-c3100-cuvette', 'cuvette-coagulation'),
        ('norm-b04-487-002-tt',   'B04.487.002, Определение тромбинового времени (ТВ) в плазме крови на анализаторе', 'COAGULATION', 'mindray-c3100', 'rate-c3100-tt-reagent', NULL),
        ('norm-b04-487-002-cuv',  'B04.487.002, Определение тромбинового времени (ТВ) в плазме крови на анализаторе', 'COAGULATION', 'mindray-c3100', 'rate-c3100-cuvette', 'cuvette-coagulation'),
        ('norm-b04-358-002-dd',   'B04.358.002, Определение количественного D - димер в плазме крови на анализаторе', 'COAGULATION', 'mindray-c3100', 'rate-c3100-dd-reagent', NULL),
        ('norm-b04-358-002-dil',  'B04.358.002, Определение количественного D - димер в плазме крови на анализаторе', 'COAGULATION', 'mindray-c3100', 'rate-c3100-dd-diluent', NULL),
        ('norm-b04-358-002-cuv',  'B04.358.002, Определение количественного D - димер в плазме крови на анализаторе', 'COAGULATION', 'mindray-c3100', 'rate-c3100-cuvette', 'cuvette-coagulation')
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
    s.service_category,
    s.analyzer_id,
    r.reagent_name,
    s.consumable_id,
    COALESCE(r.volume_per_operation_ml, r.units_per_operation::NUMERIC),
    CASE
        WHEN r.unit_type IN ('ML', 'PIECE', 'TEST_POSITION') THEN r.unit_type
        ELSE 'TEST_POSITION'
    END,
    'CALCULATED_FROM_ANALYZER_RATE',
    r.source_document,
    'Exact Damumed service code; amount copied from analyzer_reagent_rates.id=' || r.id,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM norm_seed s
INNER JOIN analyzer_reagent_rates r ON r.id = s.analyzer_rate_id
WHERE COALESCE(r.volume_per_operation_ml, r.units_per_operation::NUMERIC) > 0
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

-- ============================================================================
-- 2. Exact service code → analyzer mappings
--
-- Existing legacy mappings were short labels such as "АЛТ" and did not match
-- full Damumed service lines. These exact codes remove the ambiguity between
-- manual and analyzer versions of the same study.
-- ============================================================================
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
    ('map-dam-b02-110-002', 'B02.110.002, Общий анализ крови на анализаторе с дифференцировкой 5 классов клеток', 'HEMATOLOGY', 'mindray-bc-5000', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b03-335-002', 'B03.335.002, Определение глюкозы в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b03-398-002', 'B03.398.002, Определение общего билирубина в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b03-386-002', 'B03.386.002, Определение мочевины в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b03-435-002', 'B03.435.002, Определение прямого билирубина в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b03-293-002', 'B03.293.002, Определение аспартатаминотрансферазы (АСаТ) в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b03-155-002', 'B03.155.002, Определение аланинаминотрансферазы (АЛаТ) в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b03-363-002', 'B03.363.002, Определение креатинина в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b03-397-002', 'B03.397.002, Определение общего белка в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b03-403-002', 'B03.403.002, Определение общей альфа-амилазы в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b03-401-002', 'B03.401.002, Определение общего холестерина в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b03-526-002', 'B03.526.002, Определение щелочной фосфатазы в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b03-486-002', 'B03.486.002, Определение триглицеридов в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b03-372-002', 'B03.372.002, Определение липопротеидов низкой плотности в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b03-156-002', 'B03.156.002, Определение альбумина в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b03-371-002', 'B03.371.002, Определение липопротеидов высокой плотности в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b03-316-002', 'B03.316.002, Определение гаммаглютамилтранспептидазы (ГГТП) в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b03-340-002', 'B03.340.002, Определение железа (Fe) в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b03-437-002', 'B03.437.002, Определение ревматоидного фактора в сыворотке крови количественно на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b03-387-002', 'B03.387.002, Определение мочевой кислоты в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b03-353-002', 'B03.353.002, Определение кальция (Ca) в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b03-367-002', 'B03.367.002, Определение лактатдегидрогиназы (ЛДГ) в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b03-370-002', 'B03.370.002, Определение липазы в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b03-364-002', 'B03.364.002, Определение креатинфосфокиназы (КФК) в сыворотке крови на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b03-206-002', 'B03.206.002, Определение антистрептолизина «O» в сыворотке крови количественно на анализаторе', 'BIOCHEMISTRY', 'mindray-bs-230', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b01-077-002', 'B01.077.002, Исследование общего анализа мочи на анализаторе (физико-химические свойства с подсчетом количества клеточных элементов мочевого осадка)', 'URINALYSIS', 'mission-u500', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b04-379-002', 'B04.379.002, Определение протромбинового времени (ПВ) с последующим расчетом протромбинового индекса (ПТИ) и международного нормализованного отношения (МНО) в плазме крови на анализаторе (ПВ-ПТИ-МНО)', 'COAGULATION', 'mindray-c3100', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b04-149-002', 'B04.149.002, Определение активированного частичного тромбопластинового времени (АЧТВ) в плазме крови на анализаторе', 'COAGULATION', 'mindray-c3100', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b04-501-002', 'B04.501.002, Определение фибриногена в плазме крови на анализаторе', 'COAGULATION', 'mindray-c3100', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b04-487-002', 'B04.487.002, Определение тромбинового времени (ТВ) в плазме крови на анализаторе', 'COAGULATION', 'mindray-c3100', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('map-dam-b04-358-002', 'B04.358.002, Определение количественного D - димер в плазме крови на анализаторе', 'COAGULATION', 'mindray-c3100', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO UPDATE SET
    service_name_pattern = EXCLUDED.service_name_pattern,
    service_category = EXCLUDED.service_category,
    analyzer_id = EXCLUDED.analyzer_id,
    matching_priority = EXCLUDED.matching_priority,
    is_active = EXCLUDED.is_active,
    updated_at = CURRENT_TIMESTAMP;

-- ============================================================================
-- 3. 1C snapshot → LIMS item links
--
-- The 1C export has NULL measurement units for all current nomenclature and
-- stock rows. Therefore only package quantities explicitly encoded in an item
-- name are used for stock coverage. Semantic matches without that conversion
-- remain SUGGESTED and never affect a coverage calculation.
-- ============================================================================
WITH latest_snapshot AS (
    SELECT id, payload_json::JSONB AS payload
    FROM onec_readonly_snapshots
    ORDER BY imported_at DESC
    LIMIT 1
),
mapping_seed (
    id,
    onec_item_code,
    onec_item_name,
    target_kind,
    target_name,
    target_unit,
    conversion_factor,
    mapping_status,
    notes
) AS (
    VALUES
        -- Confirmed: 1C item names include the package capacity.
        ('onec-map-n00011546-dil', 'Н00011546', 'Дилюент М-52 Diluent (20L×1)', 'REAGENT', 'M-52D Diluent', 'PACK', 0.000050, 'CONFIRMED', 'Exact BC-5000 reagent match. 1 pack = 20 L = 20,000 mL; factor converts expected mL to packs.'),
        ('onec-map-00000008244-df', '00000008244', 'Реагент лизирующий M-52DIFF (500мл)', 'REAGENT', 'M-52 DIFF Lyse', 'PACK', 0.002000, 'CONFIRMED', 'Exact BC-5000 reagent match. 1 pack = 500 mL; factor converts expected mL to packs.'),
        ('onec-map-n00015978-lh', 'Н00015978', 'Реагент лизирующий M-52LH (100мл)', 'REAGENT', 'M-52 LH Lyse', 'PACK', 0.010000, 'CONFIRMED', 'Exact BC-5000 reagent match. 1 pack = 100 mL; factor converts expected mL to packs.'),
        ('onec-map-n00015789-pt', 'Н00015789', 'Протромбиновое время (ПВ) 10*4 мл', 'REAGENT', 'PT Reagent (Протромбиновое время)', 'PACK', 0.025000, 'CONFIRMED', 'Exact coagulation reagent match. 1 pack = 10 × 4 mL = 40 mL; factor converts expected mL to packs.'),
        ('onec-map-n00018753-cd80', 'Н00018753', 'Моющий р-р CD80 (1л*1)', 'REAGENT', 'Mindray CD80 Detergent (Моющий раствор)', 'PACK', 0.001000, 'CONFIRMED', 'Exact detergent match. 1 pack = 1,000 mL. It has no per-service norm because usage is operational, not a patient-test norm.'),

        -- Suggested: analyte identity is exact, but 1C provides neither a
        -- unit nor package volume/test count. These rows are deliberately
        -- excluded from stock coverage until a source document supplies it.
        ('onec-map-n00018413-alt', 'Н00018413', 'Диагностический набор для определения АЛТ', 'REAGENT', 'Mindray ALT (Аланинаминотрансфераза)', 'PACK', 1.000000, 'SUGGESTED', 'Exact analyte match; 1C package capacity is absent, so no coverage conversion is asserted.'),
        ('onec-map-n00018415-ast', 'Н00018415', 'Диагностический набор для определения АСТ', 'REAGENT', 'Mindray AST (Аспартатаминотрансфераза)', 'PACK', 1.000000, 'SUGGESTED', 'Exact analyte match; 1C package capacity is absent, so no coverage conversion is asserted.'),
        ('onec-map-n00018554-glu', 'Н00018554', 'Диагностический набор для определения Глюкозы', 'REAGENT', 'Mindray Glu-G (Глюкоза GOD)', 'PACK', 1.000000, 'SUGGESTED', 'Exact analyte match; 1C package capacity is absent, so no coverage conversion is asserted.'),
        ('onec-map-n00018039-cre', 'Н00018039', 'Диагностический набор для определения Креатинин', 'REAGENT', 'Mindray CREA-S (Креатинин)', 'PACK', 1.000000, 'SUGGESTED', 'Exact analyte match; 1C package capacity is absent, so no coverage conversion is asserted.'),
        ('onec-map-n00017959-ure', 'Н00017959', 'Диагностический набор для определения Мочевина', 'REAGENT', 'Mindray UREA (Мочевина)', 'PACK', 1.000000, 'SUGGESTED', 'Exact analyte match; 1C package capacity is absent, so no coverage conversion is asserted.'),
        ('onec-map-n00017957-bt', 'Н00017957', 'Диагностический набор для определения Общий билирубин', 'REAGENT', 'Mindray Bil-T (Билирубин общий VOX)', 'PACK', 1.000000, 'SUGGESTED', 'Exact analyte match; 1C package capacity is absent, so no coverage conversion is asserted.'),
        ('onec-map-n00017958-bd', 'Н00017958', 'Диагностический набор реагентов для определения Прямого билирубина', 'REAGENT', 'Mindray Bil-D (Билирубин прямой VOX)', 'PACK', 1.000000, 'SUGGESTED', 'Exact analyte match; 1C package capacity is absent, so no coverage conversion is asserted.'),
        ('onec-map-n00018040-tp', 'Н00018040', 'Диагностический набор реагентов для определения Общего белка', 'REAGENT', 'Mindray TP (Общий белок)', 'PACK', 1.000000, 'SUGGESTED', 'Exact analyte match; 1C package capacity is absent, so no coverage conversion is asserted.'),
        ('onec-map-n00019230-amy', 'Н00019230', 'Диагностический набор для определения Альфа- Амилаза', 'REAGENT', 'Mindray α-AMY (Альфа-амилаза)', 'PACK', 1.000000, 'SUGGESTED', 'Exact analyte match; 1C package capacity is absent, so no coverage conversion is asserted.'),
        ('onec-map-n00019337-ggt', 'Н00019337', 'Диагностический набор для определения Гаммаглутамилтрансфераза ГГТП', 'REAGENT', 'Mindray GGT (Гамма-глутамилтрансфераза)', 'PACK', 1.000000, 'SUGGESTED', 'Exact analyte match; 1C package capacity is absent, so no coverage conversion is asserted.'),
        ('onec-map-n00018043-fe', 'Н00018043', 'Диагностический набор для определения Железа', 'REAGENT', 'Mindray Fe (Железо колориметрическое)', 'PACK', 1.000000, 'SUGGESTED', 'Exact analyte match; 1C package capacity is absent, so no coverage conversion is asserted.'),
        ('onec-map-n00018042-ca', 'Н00018042', 'Диагностический набор для определения Кальция', 'REAGENT', 'Mindray Ca (Кальций)', 'PACK', 1.000000, 'SUGGESTED', 'Exact analyte match; 1C package capacity is absent, so no coverage conversion is asserted.'),
        ('onec-map-n00018538-hdl', 'Н00018538', 'Диагностический набор для определения HDL-C', 'REAGENT', 'Mindray HDL-C (Холестерин ЛПВП)', 'PACK', 1.000000, 'SUGGESTED', 'Exact analyte match; 1C package capacity is absent, so no coverage conversion is asserted.'),
        ('onec-map-n00018416-ldl', 'Н00018416', 'Диагностический набор для определения Липопротеиды низкой плотности LDL-C', 'REAGENT', 'Mindray LDL-C (Холестерин ЛПНП)', 'PACK', 1.000000, 'SUGGESTED', 'Exact analyte match; 1C package capacity is absent, so no coverage conversion is asserted.'),
        ('onec-map-n00018038-tc', 'Н00018038', 'Диагностический набор для определения Общий холестерин', 'REAGENT', 'Mindray TC (Общий холестерин)', 'PACK', 1.000000, 'SUGGESTED', 'Exact analyte match; 1C package capacity is absent, so no coverage conversion is asserted.'),
        ('onec-map-n00018749-tg', 'Н00018749', 'Диагностический набор для определения Триглицериды', 'REAGENT', 'Mindray TG (Триглицериды)', 'PACK', 1.000000, 'SUGGESTED', 'Exact analyte match; 1C package capacity is absent, so no coverage conversion is asserted.'),
        ('onec-map-n00018048-alb', 'Н00018048', 'Диагностический набор для определния Альбумина', 'REAGENT', 'Mindray ALB (Альбумин)', 'PACK', 1.000000, 'SUGGESTED', 'Exact analyte match; 1C package capacity is absent, so no coverage conversion is asserted.'),
        ('onec-map-n00013446-ldh', 'Н00013446', 'Диагностический набор реагентов для определения Лактатдегидрогеназы', 'REAGENT', 'Mindray LDH (Лактатдегидрогеназа)', 'PACK', 1.000000, 'SUGGESTED', 'Exact analyte match; 1C package capacity is absent, so no coverage conversion is asserted.'),
        ('onec-map-n00015948-ua', 'Н00015948', 'Диагностический набор реагентов для определения Мочевой кислоты', 'REAGENT', 'Mindray UA (Мочевая кислота)', 'PACK', 1.000000, 'SUGGESTED', 'Exact analyte match; 1C package capacity is absent, so no coverage conversion is asserted.'),
        ('onec-map-n00018055-alp', 'Н00018055', 'Диагностический набор реагентов для определения Щелочной фосфатазы', 'REAGENT', 'Mindray ALP (Щелочная фосфотаза)', 'PACK', 1.000000, 'SUGGESTED', 'Exact analyte match; 1C package capacity is absent, so no coverage conversion is asserted.'),
        ('onec-map-n00018751-rf', 'Н00018751', 'Диагностический набор для определения Ревматоидного фактора', 'REAGENT', 'Mindray RF (Ревматоидный фактор)', 'PACK', 1.000000, 'SUGGESTED', 'Exact analyte match; 1C package capacity is absent, so no coverage conversion is asserted.'),
        ('onec-map-n00018412-aso', 'Н00018412', 'Диагностический набор АСЛО', 'REAGENT', 'Mindray ASO (Антистрептолизин O)', 'PACK', 1.000000, 'SUGGESTED', 'Exact analyte match; 1C package capacity is absent, so no coverage conversion is asserted.'),
        ('onec-map-n00013343-aptt', 'Н00013343', 'Реагент АПТВ', 'REAGENT', 'APTT Reagent (Ellagic Acid)', 'PACK', 1.000000, 'SUGGESTED', 'Exact test identity; 1C package capacity is absent, so no coverage conversion is asserted.'),
        ('onec-map-n00018425-fib', 'Н00018425', 'Техфибриноген по Клаусу', 'REAGENT', 'FIB Assay Kit (Фибриноген)', 'PACK', 1.000000, 'SUGGESTED', 'Exact test identity; 1C package capacity is absent, so no coverage conversion is asserted.'),
        ('onec-map-n00015923-tt', 'Н00015923', 'Тромбиновое время', 'REAGENT', 'TT Reagent (Тромбиновое время)', 'PACK', 1.000000, 'SUGGESTED', 'Exact test identity; 1C package capacity is absent, so no coverage conversion is asserted.'),
        ('onec-map-n00019315-dd', 'Н00019315', 'Тест набор для определения D-Dimer уровень', 'REAGENT', 'D-Dimer Assay Kit (Д-димер)', 'PACK', 1.000000, 'SUGGESTED', 'Exact test identity; 1C package capacity is absent, so no coverage conversion is asserted.'),
        ('onec-map-n00013118-u500', 'Н00013118', 'Реагентные тест-полоски для анализа мочи', 'CONSUMABLE', 'Mission Urinalysis Reagent Strip', 'PACK', 1.000000, 'SUGGESTED', 'Exact consumable class; the 1C item does not state strips per pack, so no coverage conversion is asserted.'),
        ('onec-map-n00018752-cuv', 'Н00018752', 'Реакционная кювета', 'CONSUMABLE', 'Auto Cuvette + Steel Ball (Кювета)', 'PACK', 1.000000, 'SUGGESTED', 'Exact consumable class; the 1C item does not state pieces per pack, so no coverage conversion is asserted.')
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
    m.id,
    s.id,
    NULL,
    m.onec_item_code,
    m.onec_item_name,
    m.target_kind,
    m.target_name,
    m.target_unit,
    m.conversion_factor,
    m.mapping_status,
    m.notes,
    CURRENT_TIMESTAMP,
    'system-v35',
    CURRENT_TIMESTAMP,
    'system-v35',
    0
FROM mapping_seed m
CROSS JOIN latest_snapshot s
WHERE EXISTS (
    SELECT 1
    FROM JSONB_ARRAY_ELEMENTS(s.payload -> 'inventory') inventory(item)
    WHERE BTRIM(inventory.item ->> 'nomenclatureCode') = m.onec_item_code
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
    updated_by = 'system-v35';
