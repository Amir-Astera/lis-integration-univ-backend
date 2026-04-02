-- V9: Seed reagent rates for biochemistry analyzers (BS-240, BS-240Pro, BS-430, BS-600M)
-- Source: "Биохимия Миндрай 2026.doc" — Mindray diagnostic reagent kit catalog
-- Formula: volume_per_test_ml = total_kit_volume_ml / max_tests_for_model
-- BS-240 uses BS-240Pro rates (confirmed by user)

-- =====================================================================
-- BS-240Pro reagent rates (also used for BS-240)
-- =====================================================================
INSERT INTO analyzer_reagent_rates (
    id, analyzer_id, reagent_name, operation_type, test_mode,
    volume_per_operation_ml, units_per_operation, unit_type,
    source_document, notes, created_at, version
) VALUES
    -- Basic chemistry panel
    ('rate-bs240pro-alt',        'mindray-bs-240pro', 'ALT (Аланинаминотрансфераза)', 'PATIENT_TEST', NULL, 0.187, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL kit / 942 tests', NOW(), 0),
    ('rate-bs240pro-alb',        'mindray-bs-240pro', 'ALB (Альбумин)', 'PATIENT_TEST', NULL, 0.220, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '160mL kit / 728 tests', NOW(), 0),
    ('rate-bs240pro-amy',        'mindray-bs-240pro', 'α-AMY (Альфа-амилаза)', 'PATIENT_TEST', NULL, 0.198, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '48mL kit / 242 tests', NOW(), 0),
    ('rate-bs240pro-ast',        'mindray-bs-240pro', 'AST (Аспартатаминотрансфераза)', 'PATIENT_TEST', NULL, 0.187, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL kit / 942 tests', NOW(), 0),
    ('rate-bs240pro-ggt',        'mindray-bs-240pro', 'GGT (Гамма-глутамилтрансфераза)', 'PATIENT_TEST', NULL, 0.187, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL kit / 942 tests', NOW(), 0),
    ('rate-bs240pro-glu',        'mindray-bs-240pro', 'Glu (Глюкоза)', 'PATIENT_TEST', NULL, 0.243, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '200mL kit / 822 tests', NOW(), 0),
    ('rate-bs240pro-fe',         'mindray-bs-240pro', 'Fe (Железо)', 'PATIENT_TEST', NULL, 0.199, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '96mL kit / 483 tests', NOW(), 0),
    ('rate-bs240pro-ca',         'mindray-bs-240pro', 'Ca (Кальций)', 'PATIENT_TEST', NULL, 0.220, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '160mL kit / 728 tests', NOW(), 0),
    ('rate-bs240pro-crea',       'mindray-bs-240pro', 'CREA (Креатинин)', 'PATIENT_TEST', NULL, 0.197, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '72mL kit / 366 tests', NOW(), 0),
    ('rate-bs240pro-ck',         'mindray-bs-240pro', 'CK (Креатинкиназа)', 'PATIENT_TEST', NULL, 0.187, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '88mL kit / 471 tests', NOW(), 0),
    ('rate-bs240pro-ldh',        'mindray-bs-240pro', 'LDH (Лактатдегидрогеназа)', 'PATIENT_TEST', NULL, 0.187, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL kit / 942 tests', NOW(), 0),
    ('rate-bs240pro-mg',         'mindray-bs-240pro', 'Mg (Магний)', 'PATIENT_TEST', NULL, 0.220, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '160mL kit / 728 tests', NOW(), 0),
    ('rate-bs240pro-ua',         'mindray-bs-240pro', 'UA (Мочевая кислота)', 'PATIENT_TEST', NULL, 0.249, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '200mL kit / 804 tests', NOW(), 0),
    ('rate-bs240pro-urea',       'mindray-bs-240pro', 'UREA (Мочевина)', 'PATIENT_TEST', NULL, 0.293, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL kit / 600 tests', NOW(), 0),
    ('rate-bs240pro-tp',         'mindray-bs-240pro', 'TP (Общий белок)', 'PATIENT_TEST', NULL, 0.112, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '160mL kit / 1428 tests', NOW(), 0),
    ('rate-bs240pro-bil-t',      'mindray-bs-240pro', 'Bil-T (Билирубин общий)', 'PATIENT_TEST', NULL, 0.214, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL kit / 824 tests', NOW(), 0),
    ('rate-bs240pro-bil-d',      'mindray-bs-240pro', 'Bil-D (Билирубин прямой)', 'PATIENT_TEST', NULL, 0.214, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL kit / 824 tests', NOW(), 0),
    ('rate-bs240pro-tc',         'mindray-bs-240pro', 'TC (Общий холестерин)', 'PATIENT_TEST', NULL, 0.220, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '160mL kit / 728 tests', NOW(), 0),
    ('rate-bs240pro-tg',         'mindray-bs-240pro', 'TG (Триглицериды)', 'PATIENT_TEST', NULL, 0.220, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '160mL kit / 728 tests', NOW(), 0),
    ('rate-bs240pro-p',          'mindray-bs-240pro', 'P (Фосфор)', 'PATIENT_TEST', NULL, 0.220, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '160mL kit / 728 tests', NOW(), 0),
    ('rate-bs240pro-alp',        'mindray-bs-240pro', 'ALP (Щелочная фосфатаза)', 'PATIENT_TEST', NULL, 0.187, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL kit / 942 tests', NOW(), 0),
    ('rate-bs240pro-hbdh',       'mindray-bs-240pro', 'α-HBDH (Гидроксибутиратдегидрогеназа)', 'PATIENT_TEST', NULL, 0.187, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL kit / 942 tests', NOW(), 0),
    -- Immunology / specific proteins
    ('rate-bs240pro-iga',        'mindray-bs-240pro', 'IgA (Иммуноглобулин А)', 'PATIENT_TEST', NULL, 0.245, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '54mL kit / 220 tests', NOW(), 0),
    ('rate-bs240pro-igg',        'mindray-bs-240pro', 'IgG (Иммуноглобулин G)', 'PATIENT_TEST', NULL, 0.245, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '54mL kit / 220 tests', NOW(), 0),
    ('rate-bs240pro-igm',        'mindray-bs-240pro', 'IgM (Иммуноглобулин M)', 'PATIENT_TEST', NULL, 0.236, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '50mL kit / 212 tests', NOW(), 0),
    ('rate-bs240pro-c3',         'mindray-bs-240pro', 'C3 (Комплемент С3)', 'PATIENT_TEST', NULL, 0.245, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '54mL kit / 220 tests', NOW(), 0),
    ('rate-bs240pro-c4',         'mindray-bs-240pro', 'C4 (Комплемент С4)', 'PATIENT_TEST', NULL, 0.266, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '55mL kit / 207 tests', NOW(), 0),
    ('rate-bs240pro-crp',        'mindray-bs-240pro', 'CRP (С-реактивный белок)', 'PATIENT_TEST', NULL, 0.266, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '50mL kit / 188 tests', NOW(), 0),
    ('rate-bs240pro-ck-mb',      'mindray-bs-240pro', 'CK-MB (Креатинкиназа МВ)', 'PATIENT_TEST', NULL, 0.187, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '88mL kit / 471 tests', NOW(), 0),
    -- Lipid panel
    ('rate-bs240pro-apo-b',      'mindray-bs-240pro', 'Apo-B (Аполипопротеин B)', 'PATIENT_TEST', NULL, 0.324, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '47mL kit / 145 tests', NOW(), 0),
    ('rate-bs240pro-apo-a1',     'mindray-bs-240pro', 'Apo-A1 (Аполипопротеин А1)', 'PATIENT_TEST', NULL, 0.324, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '47mL kit / 145 tests', NOW(), 0),
    ('rate-bs240pro-hdl-c',      'mindray-bs-240pro', 'HDL-C (ЛПВП холестерин)', 'PATIENT_TEST', NULL, 0.238, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '54mL kit / 227 tests', NOW(), 0),
    ('rate-bs240pro-ldl-c',      'mindray-bs-240pro', 'LDL-C (ЛПНП холестерин)', 'PATIENT_TEST', NULL, 0.238, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '54mL kit / 227 tests', NOW(), 0),
    ('rate-bs240pro-lpa',        'mindray-bs-240pro', 'Lp(a) (Липопротеин а)', 'PATIENT_TEST', NULL, 0.273, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '30mL kit / 110 tests', NOW(), 0),
    -- Rheumatology / inflammation
    ('rate-bs240pro-aso',        'mindray-bs-240pro', 'ASO (Антистрептолизин О)', 'PATIENT_TEST', NULL, 0.348, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '46mL kit / 132 tests', NOW(), 0),
    ('rate-bs240pro-rf',         'mindray-bs-240pro', 'RF (Ревматоидный фактор)', 'PATIENT_TEST', NULL, 0.252, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '51mL kit / 202 tests', NOW(), 0),
    ('rate-bs240pro-hs-crp',     'mindray-bs-240pro', 'HS-CRP (Высокочувствительный СРБ)', 'PATIENT_TEST', NULL, 0.278, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '80mL kit / 288 tests', NOW(), 0),
    -- Special chemistry
    ('rate-bs240pro-pa',         'mindray-bs-240pro', 'PA (Преальбумин)', 'PATIENT_TEST', NULL, 0.266, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '55mL kit / 207 tests', NOW(), 0),
    ('rate-bs240pro-fun',        'mindray-bs-240pro', 'FUN (Фруктозамин)', 'PATIENT_TEST', NULL, 0.195, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '75mL kit / 385 tests', NOW(), 0),
    ('rate-bs240pro-lip',        'mindray-bs-240pro', 'LIP (Липаза)', 'PATIENT_TEST', NULL, 0.278, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '44mL kit / 158 tests', NOW(), 0),
    ('rate-bs240pro-fer',        'mindray-bs-240pro', 'FER (Ферритин)', 'PATIENT_TEST', NULL, 0.226, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '19mL kit / 84 tests', NOW(), 0),
    ('rate-bs240pro-ige',        'mindray-bs-240pro', 'IgE (Иммуноглобулин E)', 'PATIENT_TEST', NULL, 0.226, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '31mL kit / 137 tests', NOW(), 0),
    ('rate-bs240pro-ace',        'mindray-bs-240pro', 'ACE (Ангиотензинпревращающий фермент)', 'PATIENT_TEST', NULL, 0.179, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '20mL kit / 112 tests', NOW(), 0),
    ('rate-bs240pro-b2mg',       'mindray-bs-240pro', 'β2-mG (β2 Микроглобулин)', 'PATIENT_TEST', NULL, 0.229, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '52mL kit / 227 tests', NOW(), 0),
    ('rate-bs240pro-cys-c',      'mindray-bs-240pro', 'Cys-C (Цистатин С)', 'PATIENT_TEST', NULL, 0.173, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '52mL kit / 300 tests', NOW(), 0),
    ('rate-bs240pro-bhb',        'mindray-bs-240pro', 'β-HB (Бета-гидроксибутират)', 'PATIENT_TEST', NULL, 0.193, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '27mL kit / 140 tests', NOW(), 0),
    ('rate-bs240pro-g6pd',       'mindray-bs-240pro', 'G6PD (Глюкозо-6-фосфатдегидрогеназа)', 'PATIENT_TEST', NULL, 0.193, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '27mL kit / 140 tests', NOW(), 0),
    ('rate-bs240pro-malb',       'mindray-bs-240pro', 'MALB (Микроальбумин)', 'PATIENT_TEST', NULL, 0.175, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '37mL kit / 211 tests', NOW(), 0),
    ('rate-bs240pro-rbp',        'mindray-bs-240pro', 'RBP (Ретинол-связывающий белок)', 'PATIENT_TEST', NULL, 0.298, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '28mL kit / 94 tests', NOW(), 0),
    ('rate-bs240pro-tpuc',       'mindray-bs-240pro', 'TPUC (Общий белок в моче)', 'PATIENT_TEST', NULL, 0.143, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '54mL kit / 378 tests', NOW(), 0),
    ('rate-bs240pro-trf',        'mindray-bs-240pro', 'TRF (Трансферрин)', 'PATIENT_TEST', NULL, 0.264, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '37mL kit / 140 tests', NOW(), 0),
    ('rate-bs240pro-hcy',        'mindray-bs-240pro', 'HCY (Гомоцистеин)', 'PATIENT_TEST', NULL, 0.280, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '33mL kit / 118 tests', NOW(), 0),
    ('rate-bs240pro-uibc',       'mindray-bs-240pro', 'UIBC (Ненасыщенная железосвяз. способность)', 'PATIENT_TEST', NULL, 0.318, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '27mL kit / 85 tests', NOW(), 0),
    ('rate-bs240pro-tba',        'mindray-bs-240pro', 'TBA (Общие желчные кислоты)', 'PATIENT_TEST', NULL, 0.218, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '72mL kit / 330 tests', NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- =====================================================================
-- BS-240 rates (same as BS-240Pro per user confirmation)
-- =====================================================================
INSERT INTO analyzer_reagent_rates (
    id, analyzer_id, reagent_name, operation_type, test_mode,
    volume_per_operation_ml, units_per_operation, unit_type,
    source_document, notes, created_at, version
) VALUES
    ('rate-bs240-alt',        'mindray-bs-240', 'ALT (Аланинаминотрансфераза)', 'PATIENT_TEST', NULL, 0.187, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', 'Rates same as BS-240Pro; 176mL/942 tests', NOW(), 0),
    ('rate-bs240-alb',        'mindray-bs-240', 'ALB (Альбумин)', 'PATIENT_TEST', NULL, 0.220, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', 'Rates same as BS-240Pro; 160mL/728 tests', NOW(), 0),
    ('rate-bs240-amy',        'mindray-bs-240', 'α-AMY (Альфа-амилаза)', 'PATIENT_TEST', NULL, 0.198, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '48mL/242 tests', NOW(), 0),
    ('rate-bs240-ast',        'mindray-bs-240', 'AST (Аспартатаминотрансфераза)', 'PATIENT_TEST', NULL, 0.187, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL/942 tests', NOW(), 0),
    ('rate-bs240-ggt',        'mindray-bs-240', 'GGT (Гамма-глутамилтрансфераза)', 'PATIENT_TEST', NULL, 0.187, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL/942 tests', NOW(), 0),
    ('rate-bs240-glu',        'mindray-bs-240', 'Glu (Глюкоза)', 'PATIENT_TEST', NULL, 0.243, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '200mL/822 tests', NOW(), 0),
    ('rate-bs240-fe',         'mindray-bs-240', 'Fe (Железо)', 'PATIENT_TEST', NULL, 0.199, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '96mL/483 tests', NOW(), 0),
    ('rate-bs240-ca',         'mindray-bs-240', 'Ca (Кальций)', 'PATIENT_TEST', NULL, 0.220, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '160mL/728 tests', NOW(), 0),
    ('rate-bs240-crea',       'mindray-bs-240', 'CREA (Креатинин)', 'PATIENT_TEST', NULL, 0.197, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '72mL/366 tests', NOW(), 0),
    ('rate-bs240-ck',         'mindray-bs-240', 'CK (Креатинкиназа)', 'PATIENT_TEST', NULL, 0.187, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '88mL/471 tests', NOW(), 0),
    ('rate-bs240-ldh',        'mindray-bs-240', 'LDH (Лактатдегидрогеназа)', 'PATIENT_TEST', NULL, 0.187, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL/942 tests', NOW(), 0),
    ('rate-bs240-mg',         'mindray-bs-240', 'Mg (Магний)', 'PATIENT_TEST', NULL, 0.220, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '160mL/728 tests', NOW(), 0),
    ('rate-bs240-ua',         'mindray-bs-240', 'UA (Мочевая кислота)', 'PATIENT_TEST', NULL, 0.249, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '200mL/804 tests', NOW(), 0),
    ('rate-bs240-urea',       'mindray-bs-240', 'UREA (Мочевина)', 'PATIENT_TEST', NULL, 0.293, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL/600 tests', NOW(), 0),
    ('rate-bs240-tp',         'mindray-bs-240', 'TP (Общий белок)', 'PATIENT_TEST', NULL, 0.112, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '160mL/1428 tests', NOW(), 0),
    ('rate-bs240-bil-t',      'mindray-bs-240', 'Bil-T (Билирубин общий)', 'PATIENT_TEST', NULL, 0.214, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL/824 tests', NOW(), 0),
    ('rate-bs240-bil-d',      'mindray-bs-240', 'Bil-D (Билирубин прямой)', 'PATIENT_TEST', NULL, 0.214, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL/824 tests', NOW(), 0),
    ('rate-bs240-tc',         'mindray-bs-240', 'TC (Общий холестерин)', 'PATIENT_TEST', NULL, 0.220, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '160mL/728 tests', NOW(), 0),
    ('rate-bs240-tg',         'mindray-bs-240', 'TG (Триглицериды)', 'PATIENT_TEST', NULL, 0.220, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '160mL/728 tests', NOW(), 0),
    ('rate-bs240-p',          'mindray-bs-240', 'P (Фосфор)', 'PATIENT_TEST', NULL, 0.220, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '160mL/728 tests', NOW(), 0),
    ('rate-bs240-alp',        'mindray-bs-240', 'ALP (Щелочная фосфатаза)', 'PATIENT_TEST', NULL, 0.187, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL/942 tests', NOW(), 0),
    ('rate-bs240-hbdh',       'mindray-bs-240', 'α-HBDH (Гидроксибутиратдегидрогеназа)', 'PATIENT_TEST', NULL, 0.187, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL/942 tests', NOW(), 0),
    ('rate-bs240-iga',        'mindray-bs-240', 'IgA (Иммуноглобулин А)', 'PATIENT_TEST', NULL, 0.245, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '54mL/220 tests', NOW(), 0),
    ('rate-bs240-igg',        'mindray-bs-240', 'IgG (Иммуноглобулин G)', 'PATIENT_TEST', NULL, 0.245, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '54mL/220 tests', NOW(), 0),
    ('rate-bs240-igm',        'mindray-bs-240', 'IgM (Иммуноглобулин M)', 'PATIENT_TEST', NULL, 0.236, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '50mL/212 tests', NOW(), 0),
    ('rate-bs240-c3',         'mindray-bs-240', 'C3 (Комплемент С3)', 'PATIENT_TEST', NULL, 0.245, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '54mL/220 tests', NOW(), 0),
    ('rate-bs240-c4',         'mindray-bs-240', 'C4 (Комплемент С4)', 'PATIENT_TEST', NULL, 0.266, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '55mL/207 tests', NOW(), 0),
    ('rate-bs240-crp',        'mindray-bs-240', 'CRP (С-реактивный белок)', 'PATIENT_TEST', NULL, 0.266, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '50mL/188 tests', NOW(), 0),
    ('rate-bs240-ck-mb',      'mindray-bs-240', 'CK-MB (Креатинкиназа МВ)', 'PATIENT_TEST', NULL, 0.187, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '88mL/471 tests', NOW(), 0),
    ('rate-bs240-apo-b',      'mindray-bs-240', 'Apo-B (Аполипопротеин B)', 'PATIENT_TEST', NULL, 0.324, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '47mL/145 tests', NOW(), 0),
    ('rate-bs240-apo-a1',     'mindray-bs-240', 'Apo-A1 (Аполипопротеин А1)', 'PATIENT_TEST', NULL, 0.324, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '47mL/145 tests', NOW(), 0),
    ('rate-bs240-hdl-c',      'mindray-bs-240', 'HDL-C (ЛПВП холестерин)', 'PATIENT_TEST', NULL, 0.238, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '54mL/227 tests', NOW(), 0),
    ('rate-bs240-ldl-c',      'mindray-bs-240', 'LDL-C (ЛПНП холестерин)', 'PATIENT_TEST', NULL, 0.238, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '54mL/227 tests', NOW(), 0),
    ('rate-bs240-hs-crp',     'mindray-bs-240', 'HS-CRP (Высокочувствительный СРБ)', 'PATIENT_TEST', NULL, 0.278, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '80mL/288 tests', NOW(), 0),
    ('rate-bs240-rf',         'mindray-bs-240', 'RF (Ревматоидный фактор)', 'PATIENT_TEST', NULL, 0.252, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '51mL/202 tests', NOW(), 0),
    ('rate-bs240-aso',        'mindray-bs-240', 'ASO (Антистрептолизин О)', 'PATIENT_TEST', NULL, 0.348, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '46mL/132 tests', NOW(), 0),
    ('rate-bs240-tba',        'mindray-bs-240', 'TBA (Общие желчные кислоты)', 'PATIENT_TEST', NULL, 0.218, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '72mL/330 tests', NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- =====================================================================
-- BS-430 reagent rates (uses "430" column from catalog)
-- Where 430 differs from 240Pro, different rate is used
-- =====================================================================
INSERT INTO analyzer_reagent_rates (
    id, analyzer_id, reagent_name, operation_type, test_mode,
    volume_per_operation_ml, units_per_operation, unit_type,
    source_document, notes, created_at, version
) VALUES
    ('rate-bs430-alt',        'mindray-bs-430', 'ALT (Аланинаминотрансфераза)', 'PATIENT_TEST', NULL, 0.187, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL/942 tests', NOW(), 0),
    ('rate-bs430-alb',        'mindray-bs-430', 'ALB (Альбумин)', 'PATIENT_TEST', NULL, 0.166, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '160mL/964 tests', NOW(), 0),
    ('rate-bs430-amy',        'mindray-bs-430', 'α-AMY (Альфа-амилаза)', 'PATIENT_TEST', NULL, 0.198, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '48mL/242 tests', NOW(), 0),
    ('rate-bs430-ast',        'mindray-bs-430', 'AST (Аспартатаминотрансфераза)', 'PATIENT_TEST', NULL, 0.187, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL/942 tests', NOW(), 0),
    ('rate-bs430-ggt',        'mindray-bs-430', 'GGT (Гамма-глутамилтрансфераза)', 'PATIENT_TEST', NULL, 0.187, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL/942 tests', NOW(), 0),
    ('rate-bs430-glu',        'mindray-bs-430', 'Glu (Глюкоза)', 'PATIENT_TEST', NULL, 0.243, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '200mL/822 tests', NOW(), 0),
    ('rate-bs430-fe',         'mindray-bs-430', 'Fe (Железо)', 'PATIENT_TEST', NULL, 0.199, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '96mL/483 tests', NOW(), 0),
    ('rate-bs430-ca',         'mindray-bs-430', 'Ca (Кальций)', 'PATIENT_TEST', NULL, 0.220, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '160mL/728 tests', NOW(), 0),
    ('rate-bs430-crea',       'mindray-bs-430', 'CREA (Креатинин)', 'PATIENT_TEST', NULL, 0.184, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '72mL/392 tests', NOW(), 0),
    ('rate-bs430-ck',         'mindray-bs-430', 'CK (Креатинкиназа)', 'PATIENT_TEST', NULL, 0.187, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '88mL/471 tests', NOW(), 0),
    ('rate-bs430-ldh',        'mindray-bs-430', 'LDH (Лактатдегидрогеназа)', 'PATIENT_TEST', NULL, 0.187, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL/942 tests', NOW(), 0),
    ('rate-bs430-mg',         'mindray-bs-430', 'Mg (Магний)', 'PATIENT_TEST', NULL, 0.220, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '160mL/728 tests', NOW(), 0),
    ('rate-bs430-ua',         'mindray-bs-430', 'UA (Мочевая кислота)', 'PATIENT_TEST', NULL, 0.249, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '200mL/804 tests', NOW(), 0),
    ('rate-bs430-urea',       'mindray-bs-430', 'UREA (Мочевина)', 'PATIENT_TEST', NULL, 0.293, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL/600 tests', NOW(), 0),
    ('rate-bs430-tp',         'mindray-bs-430', 'TP (Общий белок)', 'PATIENT_TEST', NULL, 0.112, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '160mL/1428 tests', NOW(), 0),
    ('rate-bs430-bil-t',      'mindray-bs-430', 'Bil-T (Билирубин общий)', 'PATIENT_TEST', NULL, 0.214, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL/824 tests', NOW(), 0),
    ('rate-bs430-bil-d',      'mindray-bs-430', 'Bil-D (Билирубин прямой)', 'PATIENT_TEST', NULL, 0.214, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL/824 tests', NOW(), 0),
    ('rate-bs430-tc',         'mindray-bs-430', 'TC (Общий холестерин)', 'PATIENT_TEST', NULL, 0.220, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '160mL/728 tests', NOW(), 0),
    ('rate-bs430-tg',         'mindray-bs-430', 'TG (Триглицериды)', 'PATIENT_TEST', NULL, 0.220, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '160mL/728 tests', NOW(), 0),
    ('rate-bs430-p',          'mindray-bs-430', 'P (Фосфор)', 'PATIENT_TEST', NULL, 0.220, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '160mL/728 tests', NOW(), 0),
    ('rate-bs430-alp',        'mindray-bs-430', 'ALP (Щелочная фосфатаза)', 'PATIENT_TEST', NULL, 0.187, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL/942 tests', NOW(), 0),
    ('rate-bs430-hbdh',       'mindray-bs-430', 'α-HBDH (Гидроксибутиратдегидрогеназа)', 'PATIENT_TEST', NULL, 0.187, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL/942 tests', NOW(), 0),
    ('rate-bs430-iga',        'mindray-bs-430', 'IgA (Иммуноглобулин А)', 'PATIENT_TEST', NULL, 0.245, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '54mL/220 tests', NOW(), 0),
    ('rate-bs430-igg',        'mindray-bs-430', 'IgG (Иммуноглобулин G)', 'PATIENT_TEST', NULL, 0.245, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '54mL/220 tests', NOW(), 0),
    ('rate-bs430-igm',        'mindray-bs-430', 'IgM (Иммуноглобулин M)', 'PATIENT_TEST', NULL, 0.325, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '50mL/154 tests', NOW(), 0),
    ('rate-bs430-c3',         'mindray-bs-430', 'C3 (Комплемент С3)', 'PATIENT_TEST', NULL, 0.245, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '54mL/220 tests', NOW(), 0),
    ('rate-bs430-c4',         'mindray-bs-430', 'C4 (Комплемент С4)', 'PATIENT_TEST', NULL, 0.266, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '55mL/207 tests', NOW(), 0),
    ('rate-bs430-crp',        'mindray-bs-430', 'CRP (С-реактивный белок)', 'PATIENT_TEST', NULL, 0.266, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '50mL/188 tests', NOW(), 0),
    ('rate-bs430-ck-mb',      'mindray-bs-430', 'CK-MB (Креатинкиназа МВ)', 'PATIENT_TEST', NULL, 0.187, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '88mL/471 tests', NOW(), 0),
    ('rate-bs430-hdl-c',      'mindray-bs-430', 'HDL-C (ЛПВП холестерин)', 'PATIENT_TEST', NULL, 0.238, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '54mL/227 tests', NOW(), 0),
    ('rate-bs430-ldl-c',      'mindray-bs-430', 'LDL-C (ЛПНП холестерин)', 'PATIENT_TEST', NULL, 0.238, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '54mL/227 tests', NOW(), 0),
    ('rate-bs430-hs-crp',     'mindray-bs-430', 'HS-CRP (Высокочувствительный СРБ)', 'PATIENT_TEST', NULL, 0.203, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '80mL/394 tests', NOW(), 0),
    ('rate-bs430-aso',        'mindray-bs-430', 'ASO (Антистрептолизин О)', 'PATIENT_TEST', NULL, 0.280, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '46mL/164 tests', NOW(), 0),
    ('rate-bs430-rf',         'mindray-bs-430', 'RF (Ревматоидный фактор)', 'PATIENT_TEST', NULL, 0.252, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '51mL/202 tests', NOW(), 0),
    ('rate-bs430-tba',        'mindray-bs-430', 'TBA (Общие желчные кислоты)', 'PATIENT_TEST', NULL, 0.218, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '72mL/330 tests', NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- =====================================================================
-- BS-600M reagent rates (uses "600M" column from catalog)
-- =====================================================================
INSERT INTO analyzer_reagent_rates (
    id, analyzer_id, reagent_name, operation_type, test_mode,
    volume_per_operation_ml, units_per_operation, unit_type,
    source_document, notes, created_at, version
) VALUES
    ('rate-bs600m-alt',        'mindray-bs-600m', 'ALT (Аланинаминотрансфераза)', 'PATIENT_TEST', NULL, 0.187, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL/942 tests', NOW(), 0),
    ('rate-bs600m-alb',        'mindray-bs-600m', 'ALB (Альбумин)', 'PATIENT_TEST', NULL, 0.220, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '160mL/728 tests', NOW(), 0),
    ('rate-bs600m-amy',        'mindray-bs-600m', 'α-AMY (Альфа-амилаза)', 'PATIENT_TEST', NULL, 0.198, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '48mL/242 tests', NOW(), 0),
    ('rate-bs600m-ast',        'mindray-bs-600m', 'AST (Аспартатаминотрансфераза)', 'PATIENT_TEST', NULL, 0.187, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL/942 tests', NOW(), 0),
    ('rate-bs600m-ggt',        'mindray-bs-600m', 'GGT (Гамма-глутамилтрансфераза)', 'PATIENT_TEST', NULL, 0.187, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL/942 tests', NOW(), 0),
    ('rate-bs600m-glu',        'mindray-bs-600m', 'Glu (Глюкоза)', 'PATIENT_TEST', NULL, 0.243, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '200mL/822 tests', NOW(), 0),
    ('rate-bs600m-fe',         'mindray-bs-600m', 'Fe (Железо)', 'PATIENT_TEST', NULL, 0.199, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '96mL/483 tests', NOW(), 0),
    ('rate-bs600m-ca',         'mindray-bs-600m', 'Ca (Кальций)', 'PATIENT_TEST', NULL, 0.220, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '160mL/728 tests', NOW(), 0),
    ('rate-bs600m-crea',       'mindray-bs-600m', 'CREA (Креатинин)', 'PATIENT_TEST', NULL, 0.197, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '72mL/366 tests', NOW(), 0),
    ('rate-bs600m-ck',         'mindray-bs-600m', 'CK (Креатинкиназа)', 'PATIENT_TEST', NULL, 0.187, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '88mL/471 tests', NOW(), 0),
    ('rate-bs600m-ldh',        'mindray-bs-600m', 'LDH (Лактатдегидрогеназа)', 'PATIENT_TEST', NULL, 0.187, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL/942 tests', NOW(), 0),
    ('rate-bs600m-mg',         'mindray-bs-600m', 'Mg (Магний)', 'PATIENT_TEST', NULL, 0.220, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '160mL/728 tests', NOW(), 0),
    ('rate-bs600m-ua',         'mindray-bs-600m', 'UA (Мочевая кислота)', 'PATIENT_TEST', NULL, 0.249, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '200mL/804 tests', NOW(), 0),
    ('rate-bs600m-urea',       'mindray-bs-600m', 'UREA (Мочевина)', 'PATIENT_TEST', NULL, 0.293, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL/600 tests', NOW(), 0),
    ('rate-bs600m-tp',         'mindray-bs-600m', 'TP (Общий белок)', 'PATIENT_TEST', NULL, 0.112, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '160mL/1428 tests', NOW(), 0),
    ('rate-bs600m-bil-t',      'mindray-bs-600m', 'Bil-T (Билирубин общий)', 'PATIENT_TEST', NULL, 0.214, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL/824 tests', NOW(), 0),
    ('rate-bs600m-bil-d',      'mindray-bs-600m', 'Bil-D (Билирубин прямой)', 'PATIENT_TEST', NULL, 0.214, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL/824 tests', NOW(), 0),
    ('rate-bs600m-tc',         'mindray-bs-600m', 'TC (Общий холестерин)', 'PATIENT_TEST', NULL, 0.220, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '160mL/728 tests', NOW(), 0),
    ('rate-bs600m-tg',         'mindray-bs-600m', 'TG (Триглицериды)', 'PATIENT_TEST', NULL, 0.220, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '160mL/728 tests', NOW(), 0),
    ('rate-bs600m-p',          'mindray-bs-600m', 'P (Фосфор)', 'PATIENT_TEST', NULL, 0.220, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '160mL/728 tests', NOW(), 0),
    ('rate-bs600m-alp',        'mindray-bs-600m', 'ALP (Щелочная фосфатаза)', 'PATIENT_TEST', NULL, 0.187, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL/942 tests', NOW(), 0),
    ('rate-bs600m-hbdh',       'mindray-bs-600m', 'α-HBDH (Гидроксибутиратдегидрогеназа)', 'PATIENT_TEST', NULL, 0.187, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '176mL/942 tests', NOW(), 0),
    ('rate-bs600m-iga',        'mindray-bs-600m', 'IgA (Иммуноглобулин А)', 'PATIENT_TEST', NULL, 0.245, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '54mL/220 tests', NOW(), 0),
    ('rate-bs600m-igg',        'mindray-bs-600m', 'IgG (Иммуноглобулин G)', 'PATIENT_TEST', NULL, 0.245, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '54mL/220 tests', NOW(), 0),
    ('rate-bs600m-igm',        'mindray-bs-600m', 'IgM (Иммуноглобулин M)', 'PATIENT_TEST', NULL, 0.325, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '50mL/154 tests', NOW(), 0),
    ('rate-bs600m-c3',         'mindray-bs-600m', 'C3 (Комплемент С3)', 'PATIENT_TEST', NULL, 0.245, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '54mL/220 tests', NOW(), 0),
    ('rate-bs600m-c4',         'mindray-bs-600m', 'C4 (Комплемент С4)', 'PATIENT_TEST', NULL, 0.266, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '55mL/207 tests', NOW(), 0),
    ('rate-bs600m-crp',        'mindray-bs-600m', 'CRP (С-реактивный белок)', 'PATIENT_TEST', NULL, 0.266, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '50mL/188 tests', NOW(), 0),
    ('rate-bs600m-ck-mb',      'mindray-bs-600m', 'CK-MB (Креатинкиназа МВ)', 'PATIENT_TEST', NULL, 0.187, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '88mL/471 tests', NOW(), 0),
    ('rate-bs600m-hdl-c',      'mindray-bs-600m', 'HDL-C (ЛПВП холестерин)', 'PATIENT_TEST', NULL, 0.238, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '54mL/227 tests', NOW(), 0),
    ('rate-bs600m-ldl-c',      'mindray-bs-600m', 'LDL-C (ЛПНП холестерин)', 'PATIENT_TEST', NULL, 0.238, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '54mL/227 tests', NOW(), 0),
    ('rate-bs600m-hs-crp',     'mindray-bs-600m', 'HS-CRP (Высокочувствительный СРБ)', 'PATIENT_TEST', NULL, 0.203, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '80mL/394 tests', NOW(), 0),
    ('rate-bs600m-aso',        'mindray-bs-600m', 'ASO (Антистрептолизин О)', 'PATIENT_TEST', NULL, 0.280, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '46mL/164 tests', NOW(), 0),
    ('rate-bs600m-rf',         'mindray-bs-600m', 'RF (Ревматоидный фактор)', 'PATIENT_TEST', NULL, 0.252, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '51mL/202 tests', NOW(), 0),
    ('rate-bs600m-tba',        'mindray-bs-600m', 'TBA (Общие желчные кислоты)', 'PATIENT_TEST', NULL, 0.218, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '72mL/330 tests', NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- =====================================================================
-- BS-series common consumable: detergent CD80
-- 15 mL per unit; used for cuvette washing across all BS analyzers
-- =====================================================================

-- =====================================================================
-- C3100 Coagulation Analyzer reagent rates
-- Source: C3100 Auto Coagulation Analyzer Operator Manual, Table 5-1
-- =====================================================================
INSERT INTO analyzer_reagent_rates (
    id, analyzer_id, reagent_name, operation_type, test_mode,
    volume_per_operation_ml, units_per_operation, unit_type,
    source_document, notes, created_at, version
) VALUES
    -- PT: 100 µL reagent + 50 µL sample
    ('rate-c3100-pt-reagent',     'mindray-c3100', 'PT Reagent (Протромбиновое время)', 'PATIENT_TEST', 'PT', 0.100, NULL, 'ML', 'C3100 Operator Manual, Table 5-1', 'Reagent: 100 µL per test', NOW(), 0),
    -- APTT: 50 µL APTT reagent + 50 µL CaCl2 + 50 µL sample
    ('rate-c3100-aptt-reagent',   'mindray-c3100', 'APTT Reagent (Ellagic Acid)', 'PATIENT_TEST', 'APTT', 0.050, NULL, 'ML', 'C3100 Operator Manual, Table 5-1', 'APTT Reagent: 50 µL per test', NOW(), 0),
    ('rate-c3100-aptt-cacl2',     'mindray-c3100', 'CaCl2 Solution (Хлорид кальция)', 'PATIENT_TEST', 'APTT', 0.050, NULL, 'ML', 'C3100 Operator Manual, Table 5-1', 'CaCl2: 50 µL per test', NOW(), 0),
    -- TT: 50 µL reagent + 75 µL sample
    ('rate-c3100-tt-reagent',     'mindray-c3100', 'TT Reagent (Тромбиновое время)', 'PATIENT_TEST', 'TT', 0.050, NULL, 'ML', 'C3100 Operator Manual, Table 5-1', 'Reagent: 50 µL per test', NOW(), 0),
    -- FIB: 90 µL FIB kit + 110 µL buffer + 10 µL sample
    ('rate-c3100-fib-reagent',    'mindray-c3100', 'FIB Assay Kit (Фибриноген)', 'PATIENT_TEST', 'FIB', 0.090, NULL, 'ML', 'C3100 Operator Manual, Table 5-1', 'FIB Kit: 90 µL per test', NOW(), 0),
    ('rate-c3100-fib-buffer',     'mindray-c3100', 'FIB Buffer (Буфер)', 'PATIENT_TEST', 'FIB', 0.110, NULL, 'ML', 'C3100 Operator Manual, Table 5-1', 'Buffer: 110 µL per test', NOW(), 0),
    -- D-Dimer: 110 µL DD kit + 110 µL diluent + 60 µL sample
    ('rate-c3100-dd-reagent',     'mindray-c3100', 'D-Dimer Assay Kit (Д-димер)', 'PATIENT_TEST', 'D-Dimer', 0.110, NULL, 'ML', 'C3100 Operator Manual, Table 5-1', 'DD Kit: 110 µL per test', NOW(), 0),
    ('rate-c3100-dd-diluent',     'mindray-c3100', 'D-Dimer Diluent (Разбавитель)', 'PATIENT_TEST', 'D-Dimer', 0.110, NULL, 'ML', 'C3100 Operator Manual, Table 5-1', 'Diluent: 110 µL per test', NOW(), 0),
    -- FDP: 110 µL FDP kit + 110 µL diluent + 25 µL sample
    ('rate-c3100-fdp-reagent',    'mindray-c3100', 'FDP Assay Kit (ПДФ)', 'PATIENT_TEST', 'FDP', 0.110, NULL, 'ML', 'C3100 Operator Manual, Table 5-1', 'FDP Kit: 110 µL per test', NOW(), 0),
    ('rate-c3100-fdp-diluent',    'mindray-c3100', 'FDP Diluent (Разбавитель)', 'PATIENT_TEST', 'FDP', 0.110, NULL, 'ML', 'C3100 Operator Manual, Table 5-1', 'Diluent: 110 µL per test', NOW(), 0),
    -- Consumable: 1 cuvette with steel ball per test (all test types)
    ('rate-c3100-cuvette',        'mindray-c3100', 'Auto Cuvette + Steel Ball (Кювета)', 'PATIENT_TEST', NULL, NULL, 1, 'PIECE', 'C3100 Operator Manual, Section 2.7.3', 'Одна одноразовая кювета со стальным шариком на каждый тест', NOW(), 0)
ON CONFLICT (id) DO NOTHING;
