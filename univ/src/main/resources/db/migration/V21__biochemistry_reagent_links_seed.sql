-- V21: Full seed for analyzer_reagent_links — biochemistry (Mindray BS series) + all analyzers
-- Covers: Mindray BS-240/240Pro/430/600M (biochemistry), Mindray BC-5000 (hematology),
--         Mindray C3100 (coagulation), iFlash 1800 (immunoassay), and other analyzers.
-- Safe to re-run (uses ON CONFLICT DO NOTHING).

-- ============================================================
-- 1. BIOCHEMISTRY reagent inventory seed (Mindray BS analyzers)
-- ============================================================
INSERT INTO reagent_inventory (
    id, analyzer_id, reagent_name, lot_number, manufacturer,
    total_volume_ml, unit_type, status, received_at, notes, created_at, updated_at, version
) VALUES
-- BS-240 / BS-240Pro / BS-430 / BS-600M — common biochemistry reagents
('ri-bs-glucose',           'mindray-bs-240', 'Глюкоза (GOD-PAP)', NULL, 'Mindray', 4000, 'ML', 'IN_STOCK', CURRENT_DATE, 'Гексокиназный/GOD метод. ~5-10 мкл на тест.', NOW(), NOW(), 0),
('ri-bs-total-protein',     'mindray-bs-240', 'Общий белок (Biuret)', NULL, 'Mindray', 4000, 'ML', 'IN_STOCK', CURRENT_DATE, 'Метод Бурета. ~5-10 мкл на тест.', NOW(), NOW(), 0),
('ri-bs-albumin',           'mindray-bs-240', 'Альбумин (BCG)', NULL, 'Mindray', 4000, 'ML', 'IN_STOCK', CURRENT_DATE, 'Бромкрезоловый зелёный метод. ~5 мкл на тест.', NOW(), NOW(), 0),
('ri-bs-urea',              'mindray-bs-240', 'Мочевина (уреазный)', NULL, 'Mindray', 4000, 'ML', 'IN_STOCK', CURRENT_DATE, 'Уреазный GLDH метод. ~5-8 мкл на тест.', NOW(), NOW(), 0),
('ri-bs-creatinine',        'mindray-bs-240', 'Креатинин (Jaffe)', NULL, 'Mindray', 4000, 'ML', 'IN_STOCK', CURRENT_DATE, 'Метод Яффе. ~5 мкл на тест.', NOW(), NOW(), 0),
('ri-bs-uric-acid',         'mindray-bs-240', 'Мочевая кислота (ферментный)', NULL, 'Mindray', 4000, 'ML', 'IN_STOCK', CURRENT_DATE, 'Уриказный метод. ~5 мкл на тест.', NOW(), NOW(), 0),
('ri-bs-alt',               'mindray-bs-240', 'АЛТ (IFCC)', NULL, 'Mindray', 4000, 'ML', 'IN_STOCK', CURRENT_DATE, 'Метод IFCC. ~5 мкл на тест.', NOW(), NOW(), 0),
('ri-bs-ast',               'mindray-bs-240', 'АСТ (IFCC)', NULL, 'Mindray', 4000, 'ML', 'IN_STOCK', CURRENT_DATE, 'Метод IFCC. ~5 мкл на тест.', NOW(), NOW(), 0),
('ri-bs-alp',               'mindray-bs-240', 'Щелочная фосфатаза (ALP)', NULL, 'Mindray', 4000, 'ML', 'IN_STOCK', CURRENT_DATE, 'Метод pNPP. ~5 мкл на тест.', NOW(), NOW(), 0),
('ri-bs-ggt',               'mindray-bs-240', 'ГГТП (GGT)', NULL, 'Mindray', 4000, 'ML', 'IN_STOCK', CURRENT_DATE, 'IFCC метод. ~5 мкл на тест.', NOW(), NOW(), 0),
('ri-bs-bilirubin-total',   'mindray-bs-240', 'Билирубин общий (DCA)', NULL, 'Mindray', 4000, 'ML', 'IN_STOCK', CURRENT_DATE, 'Диазо-метод (Евелин-Мэллой). ~5 мкл на тест.', NOW(), NOW(), 0),
('ri-bs-bilirubin-direct',  'mindray-bs-240', 'Билирубин прямой (DCA)', NULL, 'Mindray', 4000, 'ML', 'IN_STOCK', CURRENT_DATE, 'Диазо-метод. ~5 мкл на тест.', NOW(), NOW(), 0),
('ri-bs-cholesterol',       'mindray-bs-240', 'Холестерин общий (CHOD-PAP)', NULL, 'Mindray', 4000, 'ML', 'IN_STOCK', CURRENT_DATE, 'Ферментный колориметрический метод. ~5 мкл на тест.', NOW(), NOW(), 0),
('ri-bs-triglycerides',     'mindray-bs-240', 'Триглицериды (GPO-PAP)', NULL, 'Mindray', 4000, 'ML', 'IN_STOCK', CURRENT_DATE, 'Ферментный колориметрический метод. ~5 мкл на тест.', NOW(), NOW(), 0),
('ri-bs-hdl',               'mindray-bs-240', 'ЛПВП холестерин (прямой)', NULL, 'Mindray', 4000, 'ML', 'IN_STOCK', CURRENT_DATE, 'Прямой ферментный метод. ~5 мкл на тест.', NOW(), NOW(), 0),
('ri-bs-ldl',               'mindray-bs-240', 'ЛПНП холестерин (прямой)', NULL, 'Mindray', 4000, 'ML', 'IN_STOCK', CURRENT_DATE, 'Прямой ферментный метод. ~5 мкл на тест.', NOW(), NOW(), 0),
('ri-bs-calcium',           'mindray-bs-240', 'Кальций (арсеназо III)', NULL, 'Mindray', 4000, 'ML', 'IN_STOCK', CURRENT_DATE, 'Фотометрический метод. ~5 мкл на тест.', NOW(), NOW(), 0),
('ri-bs-phosphorus',        'mindray-bs-240', 'Фосфор неорганический', NULL, 'Mindray', 4000, 'ML', 'IN_STOCK', CURRENT_DATE, 'Молибдатный метод. ~5 мкл на тест.', NOW(), NOW(), 0),
('ri-bs-iron',              'mindray-bs-240', 'Железо (феррозин)', NULL, 'Mindray', 4000, 'ML', 'IN_STOCK', CURRENT_DATE, 'Феррозиновый метод без депротеинизации. ~5 мкл на тест.', NOW(), NOW(), 0),
('ri-bs-tibc',              'mindray-bs-240', 'Железосвязывающая способность (ОЖСС)', NULL, 'Mindray', 4000, 'ML', 'IN_STOCK', CURRENT_DATE, 'Колориметрический метод. ~5 мкл на тест.', NOW(), NOW(), 0),
('ri-bs-amylase',           'mindray-bs-240', 'Амилаза (EPS-G7)', NULL, 'Mindray', 4000, 'ML', 'IN_STOCK', CURRENT_DATE, 'Ферментный метод. ~5 мкл на тест.', NOW(), NOW(), 0),
('ri-bs-lipase',            'mindray-bs-240', 'Липаза', NULL, 'Mindray', 4000, 'ML', 'IN_STOCK', CURRENT_DATE, 'Турбидиметрический метод. ~5 мкл на тест.', NOW(), NOW(), 0),
('ri-bs-ldh',               'mindray-bs-240', 'ЛДГ (LDH)', NULL, 'Mindray', 4000, 'ML', 'IN_STOCK', CURRENT_DATE, 'IFCC метод. ~5 мкл на тест.', NOW(), NOW(), 0),
('ri-bs-ck',                'mindray-bs-240', 'Креатинкиназа (CK)', NULL, 'Mindray', 4000, 'ML', 'IN_STOCK', CURRENT_DATE, 'IFCC метод. ~5 мкл на тест.', NOW(), NOW(), 0),
('ri-bs-ck-mb',             'mindray-bs-240', 'Креатинкиназа МВ (CK-MB)', NULL, 'Mindray', 4000, 'ML', 'IN_STOCK', CURRENT_DATE, 'Иммуноингибиционный метод. ~5 мкл на тест.', NOW(), NOW(), 0),
('ri-bs-sodium',            'mindray-bs-240', 'Натрий (ISE/фотометрический)', NULL, 'Mindray', 1000, 'ML', 'IN_STOCK', CURRENT_DATE, 'Фотометрический метод. ~5 мкл на тест.', NOW(), NOW(), 0),
('ri-bs-potassium',         'mindray-bs-240', 'Калий (ISE/фотометрический)', NULL, 'Mindray', 1000, 'ML', 'IN_STOCK', CURRENT_DATE, 'Фотометрический метод. ~5 мкл на тест.', NOW(), NOW(), 0),
('ri-bs-chloride',          'mindray-bs-240', 'Хлориды (меркуриметрический)', NULL, 'Mindray', 1000, 'ML', 'IN_STOCK', CURRENT_DATE, 'Фотометрический метод. ~5 мкл на тест.', NOW(), NOW(), 0),
('ri-bs-crp',               'mindray-bs-240', 'С-реактивный белок (CRP, латекс)', NULL, 'Mindray', 2000, 'ML', 'IN_STOCK', CURRENT_DATE, 'Турбидиметрический латекс метод. ~5 мкл на тест.', NOW(), NOW(), 0),
('ri-bs-rf',                'mindray-bs-240', 'Ревматоидный фактор (RF, латекс)', NULL, 'Mindray', 1000, 'ML', 'IN_STOCK', CURRENT_DATE, 'Турбидиметрический латекс метод.', NOW(), NOW(), 0),
('ri-bs-aso',               'mindray-bs-240', 'Антистрептолизин-О (ASO, латекс)', NULL, 'Mindray', 1000, 'ML', 'IN_STOCK', CURRENT_DATE, 'Турбидиметрический латекс метод.', NOW(), NOW(), 0),
('ri-bs-wash',              'mindray-bs-240', 'Промывочный раствор (BS Wash)', NULL, 'Mindray', 20000, 'ML', 'IN_STOCK', CURRENT_DATE, 'Промывочный раствор для биохим. анализатора.', NOW(), NOW(), 0),
('ri-bs-cuv-rinse',         'mindray-bs-240', 'Реагент промывки кювет (Cuvette Rinse)', NULL, 'Mindray', 5000, 'ML', 'IN_STOCK', CURRENT_DATE, 'Специальный раствор для очистки реакционных кювет.', NOW(), NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- 2. BIOCHEMISTRY reagent rates (Mindray BS-240 — типичные объемы)
-- ============================================================
INSERT INTO analyzer_reagent_rates (
    id, analyzer_id, reagent_name, operation_type, test_mode,
    volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version
) VALUES
('rate-bs240-glucose',          'mindray-bs-240', 'Глюкоза (GOD-PAP)',                'PATIENT_TEST', NULL, 0.010, NULL, 'ML', 'Mindray BS-240 reagent insert', NULL, NOW(), 0),
('rate-bs240-total-protein',    'mindray-bs-240', 'Общий белок (Biuret)',             'PATIENT_TEST', NULL, 0.010, NULL, 'ML', 'Mindray BS-240 reagent insert', NULL, NOW(), 0),
('rate-bs240-albumin',          'mindray-bs-240', 'Альбумин (BCG)',                   'PATIENT_TEST', NULL, 0.005, NULL, 'ML', 'Mindray BS-240 reagent insert', NULL, NOW(), 0),
('rate-bs240-urea',             'mindray-bs-240', 'Мочевина (уреазный)',              'PATIENT_TEST', NULL, 0.008, NULL, 'ML', 'Mindray BS-240 reagent insert', NULL, NOW(), 0),
('rate-bs240-creatinine',       'mindray-bs-240', 'Креатинин (Jaffe)',                'PATIENT_TEST', NULL, 0.005, NULL, 'ML', 'Mindray BS-240 reagent insert', NULL, NOW(), 0),
('rate-bs240-uric-acid',        'mindray-bs-240', 'Мочевая кислота (ферментный)',     'PATIENT_TEST', NULL, 0.005, NULL, 'ML', 'Mindray BS-240 reagent insert', NULL, NOW(), 0),
('rate-bs240-alt',              'mindray-bs-240', 'АЛТ (IFCC)',                       'PATIENT_TEST', NULL, 0.005, NULL, 'ML', 'Mindray BS-240 reagent insert', NULL, NOW(), 0),
('rate-bs240-ast',              'mindray-bs-240', 'АСТ (IFCC)',                       'PATIENT_TEST', NULL, 0.005, NULL, 'ML', 'Mindray BS-240 reagent insert', NULL, NOW(), 0),
('rate-bs240-alp',              'mindray-bs-240', 'Щелочная фосфатаза (ALP)',         'PATIENT_TEST', NULL, 0.005, NULL, 'ML', 'Mindray BS-240 reagent insert', NULL, NOW(), 0),
('rate-bs240-ggt',              'mindray-bs-240', 'ГГТП (GGT)',                       'PATIENT_TEST', NULL, 0.005, NULL, 'ML', 'Mindray BS-240 reagent insert', NULL, NOW(), 0),
('rate-bs240-bilirubin-total',  'mindray-bs-240', 'Билирубин общий (DCA)',            'PATIENT_TEST', NULL, 0.005, NULL, 'ML', 'Mindray BS-240 reagent insert', NULL, NOW(), 0),
('rate-bs240-bilirubin-direct', 'mindray-bs-240', 'Билирубин прямой (DCA)',           'PATIENT_TEST', NULL, 0.005, NULL, 'ML', 'Mindray BS-240 reagent insert', NULL, NOW(), 0),
('rate-bs240-cholesterol',      'mindray-bs-240', 'Холестерин общий (CHOD-PAP)',      'PATIENT_TEST', NULL, 0.005, NULL, 'ML', 'Mindray BS-240 reagent insert', NULL, NOW(), 0),
('rate-bs240-triglycerides',    'mindray-bs-240', 'Триглицериды (GPO-PAP)',           'PATIENT_TEST', NULL, 0.005, NULL, 'ML', 'Mindray BS-240 reagent insert', NULL, NOW(), 0),
('rate-bs240-hdl',              'mindray-bs-240', 'ЛПВП холестерин (прямой)',         'PATIENT_TEST', NULL, 0.005, NULL, 'ML', 'Mindray BS-240 reagent insert', NULL, NOW(), 0),
('rate-bs240-ldl',              'mindray-bs-240', 'ЛПНП холестерин (прямой)',         'PATIENT_TEST', NULL, 0.005, NULL, 'ML', 'Mindray BS-240 reagent insert', NULL, NOW(), 0),
('rate-bs240-calcium',          'mindray-bs-240', 'Кальций (арсеназо III)',           'PATIENT_TEST', NULL, 0.005, NULL, 'ML', 'Mindray BS-240 reagent insert', NULL, NOW(), 0),
('rate-bs240-phosphorus',       'mindray-bs-240', 'Фосфор неорганический',            'PATIENT_TEST', NULL, 0.005, NULL, 'ML', 'Mindray BS-240 reagent insert', NULL, NOW(), 0),
('rate-bs240-iron',             'mindray-bs-240', 'Железо (феррозин)',                'PATIENT_TEST', NULL, 0.005, NULL, 'ML', 'Mindray BS-240 reagent insert', NULL, NOW(), 0),
('rate-bs240-amylase',          'mindray-bs-240', 'Амилаза (EPS-G7)',                 'PATIENT_TEST', NULL, 0.005, NULL, 'ML', 'Mindray BS-240 reagent insert', NULL, NOW(), 0),
('rate-bs240-lipase',           'mindray-bs-240', 'Липаза',                           'PATIENT_TEST', NULL, 0.005, NULL, 'ML', 'Mindray BS-240 reagent insert', NULL, NOW(), 0),
('rate-bs240-ldh',              'mindray-bs-240', 'ЛДГ (LDH)',                        'PATIENT_TEST', NULL, 0.005, NULL, 'ML', 'Mindray BS-240 reagent insert', NULL, NOW(), 0),
('rate-bs240-ck',               'mindray-bs-240', 'Креатинкиназа (CK)',               'PATIENT_TEST', NULL, 0.005, NULL, 'ML', 'Mindray BS-240 reagent insert', NULL, NOW(), 0),
('rate-bs240-crp',              'mindray-bs-240', 'С-реактивный белок (CRP, латекс)', 'PATIENT_TEST', NULL, 0.005, NULL, 'ML', 'Mindray BS-240 reagent insert', NULL, NOW(), 0),
('rate-bs240-wash',             'mindray-bs-240', 'Промывочный раствор (BS Wash)',    'WASH',         NULL, 2.0,   NULL, 'ML', 'Mindray BS-240 service manual', 'Промывка между тестами.', NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- 3. ANALYZER_REAGENT_LINKS — biochemistry (BS-240)
-- ============================================================
INSERT INTO analyzer_reagent_links (
    id, analyzer_id, reagent_inventory_id, usage_role, norm_reagent_name,
    estimated_daily_ml, is_active, created_at, updated_at, version
)
SELECT
    gen_random_uuid()::text,
    ri.analyzer_id,
    ri.id,
    'MAIN',
    ri.reagent_name,
    ri.total_volume_ml * 0.01,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM reagent_inventory ri
WHERE ri.analyzer_id IN ('mindray-bs-240', 'mindray-bs-240pro', 'mindray-bs-430', 'mindray-bs-600m')
  AND NOT EXISTS (
      SELECT 1 FROM analyzer_reagent_links arl
      WHERE arl.analyzer_id = ri.analyzer_id
        AND arl.reagent_inventory_id = ri.id
  );

-- 4. ANALYZER_REAGENT_LINKS — auto-link all remaining inventory items that have an analyzer_id
INSERT INTO analyzer_reagent_links (
    id, analyzer_id, reagent_inventory_id, usage_role, norm_reagent_name,
    is_active, created_at, updated_at, version
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
      WHERE arl.analyzer_id = ri.analyzer_id
        AND arl.reagent_inventory_id = ri.id
  );

-- 5. LINKS from analyzer_reagent_rates — auto-create WASH/CALIBRATOR/CONTROL links
INSERT INTO analyzer_reagent_links (
    id, analyzer_id, reagent_inventory_id, usage_role, norm_reagent_name,
    estimated_daily_ml, is_active, created_at, updated_at, version
)
SELECT DISTINCT ON (arr.analyzer_id, ri.id)
    gen_random_uuid()::text,
    arr.analyzer_id,
    ri.id,
    CASE
        WHEN LOWER(arr.operation_type) IN ('wash', 'shutdown', 'startup') THEN 'WASH'
        WHEN LOWER(arr.operation_type) = 'calibration'                    THEN 'CALIBRATOR'
        WHEN LOWER(arr.operation_type) = 'qc_control'                     THEN 'CONTROL'
        ELSE 'MAIN'
    END,
    arr.reagent_name,
    CASE WHEN arr.unit_type = 'ML' THEN arr.volume_per_operation_ml * 100 ELSE NULL END,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM analyzer_reagent_rates arr
JOIN reagent_inventory ri
    ON ri.analyzer_id = arr.analyzer_id
   AND LOWER(ri.reagent_name) LIKE '%' || LOWER(SPLIT_PART(arr.reagent_name, ' ', 1)) || '%'
WHERE NOT EXISTS (
    SELECT 1 FROM analyzer_reagent_links arl
    WHERE arl.analyzer_id = arr.analyzer_id
      AND arl.reagent_inventory_id = ri.id
);
