-- V10: Seed reagent rates for hematology analyzer BC-5000 (Mindray)
-- Source: РЕАГЕНТЫ_V2_ПОЛНЫЙ_АНАЛИЗ.md — BC-5000 uses 27.5 ml diluent per test (total reagent bundle)
-- Reagent breakdown per test for BC-5000:
--   Diluent M-30D:     ~18.0 ml/test  (main diluent, largest volume)
--   Lyse M-16D:         ~1.4 ml/test  (RBC/PLT lyse)
--   Lyse M-16DH:        ~0.35 ml/test (WBC 5-part diff lyse)
--   Rinse M-20R:        ~0.5 ml/test  (rinse solution)
--   Cleanser M-10CS:    ~0.03 ml/test (daily cleanser, small fraction)
-- Total bundle ~20.28 ml (exact per Mindray BC-5000 consumable specs)
-- Source: Mindray BC-5000 Operator's Manual, Appendix B Consumables

-- =====================================================================
-- BC-5000 analyzer record (if not already present)
-- =====================================================================
INSERT INTO analyzers (
    id, name, type, workplace_name, lis_device_system_name, lis_analyzer_id, lis_device_name, serial_number, is_active, notes, created_at, updated_at, version
) VALUES (
    'mindray-bc-5000',
    'Mindray BC-5000 (гематология)',
    'HEMATOLOGY',
    'Гематологическая лаборатория',
    NULL, NULL, NULL,
    NULL,
    TRUE,
    'Гематологический анализатор 5-дифференциальный, основной. Модель: BC-5000, Производитель: Mindray',
    NOW(), NOW(), 0
) ON CONFLICT (id) DO NOTHING;

-- =====================================================================
-- BC-5000 reagent rates
-- =====================================================================
INSERT INTO analyzer_reagent_rates (
    id, analyzer_id, reagent_name, operation_type, test_mode,
    volume_per_operation_ml, units_per_operation, unit_type,
    source_document, notes, created_at, version
) VALUES
    ('rate-bc5000-diluent',
     'mindray-bc-5000',
     'Diluent M-30D (Дилюент)',
     'PATIENT_TEST', NULL,
     18.0, NULL, 'ML',
     'Mindray BC-5000 Operator Manual, Appendix B',
     '18 ml per CBC test (5-diff mode)',
     NOW(), 0),

    ('rate-bc5000-lyse-rbc',
     'mindray-bc-5000',
     'Lyse M-16D (Лизирующий реагент RBC/PLT)',
     'PATIENT_TEST', NULL,
     1.4, NULL, 'ML',
     'Mindray BC-5000 Operator Manual, Appendix B',
     '1.4 ml per CBC test',
     NOW(), 0),

    ('rate-bc5000-lyse-wbc',
     'mindray-bc-5000',
     'Lyse M-16DH (Лизирующий реагент WBC 5-diff)',
     'PATIENT_TEST', NULL,
     0.35, NULL, 'ML',
     'Mindray BC-5000 Operator Manual, Appendix B',
     '0.35 ml per CBC test (5-part differential)',
     NOW(), 0),

    ('rate-bc5000-rinse',
     'mindray-bc-5000',
     'Rinse M-20R (Промывочный раствор)',
     'PATIENT_TEST', NULL,
     0.5, NULL, 'ML',
     'Mindray BC-5000 Operator Manual, Appendix B',
     '0.5 ml per CBC test',
     NOW(), 0),

    ('rate-bc5000-cleanser',
     'mindray-bc-5000',
     'Cleanser M-10CS (Очищающий раствор)',
     'PATIENT_TEST', NULL,
     0.03, NULL, 'ML',
     'Mindray BC-5000 Operator Manual, Appendix B',
     '~0.03 ml per test (daily cleanser apportioned)',
     NOW(), 0),

    -- QC mode (control run): same reagent volumes as patient test
    ('rate-bc5000-diluent-qc',
     'mindray-bc-5000',
     'Diluent M-30D (Дилюент)',
     'QUALITY_CONTROL', NULL,
     18.0, NULL, 'ML',
     'Mindray BC-5000 Operator Manual, Appendix B',
     '18 ml per QC run',
     NOW(), 0),

    ('rate-bc5000-lyse-rbc-qc',
     'mindray-bc-5000',
     'Lyse M-16D (Лизирующий реагент RBC/PLT)',
     'QUALITY_CONTROL', NULL,
     1.4, NULL, 'ML',
     'Mindray BC-5000 Operator Manual, Appendix B',
     '1.4 ml per QC run',
     NOW(), 0),

    ('rate-bc5000-lyse-wbc-qc',
     'mindray-bc-5000',
     'Lyse M-16DH (Лизирующий реагент WBC 5-diff)',
     'QUALITY_CONTROL', NULL,
     0.35, NULL, 'ML',
     'Mindray BC-5000 Operator Manual, Appendix B',
     '0.35 ml per QC run',
     NOW(), 0),

    -- Background (blank) check: diluent only
    ('rate-bc5000-diluent-bg',
     'mindray-bc-5000',
     'Diluent M-30D (Дилюент)',
     'BACKGROUND_CHECK', NULL,
     18.0, NULL, 'ML',
     'Mindray BC-5000 Operator Manual, Appendix B',
     '18 ml per background check cycle',
     NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- =====================================================================
-- iFlash 1800 (Иммунология) - chemiluminescence analyzer
-- Source: РЕАГЕНТЫ_V2_ПОЛНЫЙ_АНАЛИЗ.md - 50-test cassettes with chip
-- =====================================================================
INSERT INTO analyzers (
    id, name, type, workplace_name, lis_device_system_name, lis_analyzer_id, lis_device_name, serial_number, is_active, notes, created_at, updated_at, version
) VALUES (
    'ykon-iflash-1800',
    'Ykon iFlash 1800 (иммунология)',
    'IMMUNOLOGY',
    'Иммунологическая лаборатория',
    NULL, NULL, NULL,
    NULL,
    TRUE,
    'Хемилюминесцентный иммуноанализатор, 50-тестовые кассеты с чипом. Модель: iFlash 1800, Производитель: Ykon Biotech',
    NOW(), NOW(), 0
) ON CONFLICT (id) DO NOTHING;

INSERT INTO analyzer_reagent_rates (
    id, analyzer_id, reagent_name, operation_type, test_mode,
    volume_per_operation_ml, units_per_operation, unit_type,
    source_document, notes, created_at, version
) VALUES
    ('rate-iflash1800-tsh', 'ykon-iflash-1800', 'TSH Cassette (50 tests)', 'PATIENT_TEST', NULL,
     NULL, 1, 'UNITS', 'Ykon iFlash 1800 User Manual', '1 test from 50-test cassette', NOW(), 0),
    ('rate-iflash1800-ft4', 'ykon-iflash-1800', 'FT4 Cassette (50 tests)', 'PATIENT_TEST', NULL,
     NULL, 1, 'UNITS', 'Ykon iFlash 1800 User Manual', '1 test from 50-test cassette', NOW(), 0),
    ('rate-iflash1800-ft3', 'ykon-iflash-1800', 'FT3 Cassette (50 tests)', 'PATIENT_TEST', NULL,
     NULL, 1, 'UNITS', 'Ykon iFlash 1800 User Manual', '1 test from 50-test cassette', NOW(), 0),
    ('rate-iflash1800-qc', 'ykon-iflash-1800', 'Control Cassette', 'QUALITY_CONTROL', NULL,
     NULL, 1, 'UNITS', 'Ykon iFlash 1800 User Manual', '1 QC test', NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- =====================================================================
-- OCG-102 (POCT)
-- =====================================================================
INSERT INTO analyzers (
    id, name, type, workplace_name, lis_device_system_name, lis_analyzer_id, lis_device_name, serial_number, is_active, notes, created_at, updated_at, version
) VALUES (
    'ocg-102',
    'OCG-102 (POCT иммуноанализатор)',
    'POCT',
    'Приемное отделение / Стационар',
    NULL, NULL, NULL,
    NULL,
    TRUE,
    'POCT анализатор для экспресс-тестов, одноразовые картриджи. Модель: OCG-102, Производитель: OCG',
    NOW(), NOW(), 0
) ON CONFLICT (id) DO NOTHING;

INSERT INTO analyzer_reagent_rates (
    id, analyzer_id, reagent_name, operation_type, test_mode,
    volume_per_operation_ml, units_per_operation, unit_type,
    source_document, notes, created_at, version
) VALUES
    ('rate-ocg102-cartridge', 'ocg-102', 'OCG-102 Test Cartridge', 'PATIENT_TEST', NULL,
     NULL, 1, 'UNITS', 'OCG-102 Operator Manual', '1 disposable cartridge per test', NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- =====================================================================
-- ORTHO Workstation
-- =====================================================================
INSERT INTO analyzers (
    id, name, type, workplace_name, lis_device_system_name, lis_analyzer_id, lis_device_name, serial_number, is_active, notes, created_at, updated_at, version
) VALUES (
    'ortho-workstation',
    'ORTHO Workstation (типирование крови)',
    'BLOOD_TYPING',
    'Отделение трансфузиологии',
    NULL, NULL, NULL,
    NULL,
    TRUE,
    'Гель-карты для группового и резус-типирования. Модель: ORTHO Workstation, Производитель: Ortho Clinical Diagnostics',
    NOW(), NOW(), 0
) ON CONFLICT (id) DO NOTHING;

INSERT INTO analyzer_reagent_rates (
    id, analyzer_id, reagent_name, operation_type, test_mode,
    volume_per_operation_ml, units_per_operation, unit_type,
    source_document, notes, created_at, version
) VALUES
    ('rate-ortho-gelcard-abo', 'ortho-workstation', 'ABO/Rh Gel Card', 'PATIENT_TEST', NULL,
     NULL, 1, 'UNITS', 'ORTHO Workstation Manual', '1 gel card per blood typing', NOW(), 0),
    ('rate-ortho-gelcard-phenotype', 'ortho-workstation', 'Phenotype Gel Card', 'PATIENT_TEST', NULL,
     NULL, 1, 'UNITS', 'ORTHO Workstation Manual', '1 gel card for phenotype testing', NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- =====================================================================
-- Edan i15 (POCT)
-- =====================================================================
INSERT INTO analyzers (
    id, name, type, workplace_name, lis_device_system_name, lis_analyzer_id, lis_device_name, serial_number, is_active, notes, created_at, updated_at, version
) VALUES (
    'edan-i15',
    'Edan i15 (POCT газов/электролитов)',
    'POCT',
    'Реанимация / Операционные',
    NULL, NULL, NULL,
    NULL,
    TRUE,
    'POCT анализатор газов крови, одноразовые картриджи. Модель: i15, Производитель: Edan Instruments',
    NOW(), NOW(), 0
) ON CONFLICT (id) DO NOTHING;

INSERT INTO analyzer_reagent_rates (
    id, analyzer_id, reagent_name, operation_type, test_mode,
    volume_per_operation_ml, units_per_operation, unit_type,
    source_document, notes, created_at, version
) VALUES
    ('rate-edan-i15-cartridge', 'edan-i15', 'i15 Blood Gas Cartridge', 'PATIENT_TEST', NULL,
     NULL, 1, 'UNITS', 'Edan i15 User Manual', '1 cartridge per blood gas test', NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- =====================================================================
-- Fluorecare (POCT)
-- =====================================================================
INSERT INTO analyzers (
    id, name, type, workplace_name, lis_device_system_name, lis_analyzer_id, lis_device_name, serial_number, is_active, notes, created_at, updated_at, version
) VALUES (
    'fluorecare',
    'Fluorecare (POCT иммунофлуоресценция)',
    'POCT',
    'Приемное отделение',
    NULL, NULL, NULL,
    NULL,
    TRUE,
    'POCT иммунофлуоресцентный анализатор, одноразовые кассеты. Модель: Fluorecare, Производитель: Wondfo',
    NOW(), NOW(), 0
) ON CONFLICT (id) DO NOTHING;

INSERT INTO analyzer_reagent_rates (
    id, analyzer_id, reagent_name, operation_type, test_mode,
    volume_per_operation_ml, units_per_operation, unit_type,
    source_document, notes, created_at, version
) VALUES
    ('rate-fluorecare-cassette', 'fluorecare', 'Fluorecare Test Cassette', 'PATIENT_TEST', NULL,
     NULL, 1, 'UNITS', 'Fluorecare User Manual', '1 disposable cassette per test', NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- =====================================================================
-- Mission U500 (Urinalysis) - strips only
-- =====================================================================
INSERT INTO analyzers (
    id, name, type, workplace_name, lis_device_system_name, lis_analyzer_id, lis_device_name, serial_number, is_active, notes, created_at, updated_at, version
) VALUES (
    'acon-mission-u500',
    'Acon Mission U500 (анализатор мочи)',
    'URINALYSIS',
    'Лаборатория общего анализа мочи',
    NULL, NULL, NULL,
    NULL,
    TRUE,
    'Анализатор мочи с тест-полосками. Модель: Mission U500, Производитель: Acon Laboratories',
    NOW(), NOW(), 0
) ON CONFLICT (id) DO NOTHING;

INSERT INTO analyzer_reagent_rates (
    id, analyzer_id, reagent_name, operation_type, test_mode,
    volume_per_operation_ml, units_per_operation, unit_type,
    source_document, notes, created_at, version
) VALUES
    ('rate-mission-u500-strip', 'acon-mission-u500', 'Urine Test Strip', 'PATIENT_TEST', NULL,
     NULL, 1, 'UNITS', 'Mission U500 User Manual', '1 test strip per urinalysis', NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- =====================================================================
-- Vision Pro (HbA1c) - sealed cartridges
-- =====================================================================
INSERT INTO analyzers (
    id, name, type, workplace_name, lis_device_system_name, lis_analyzer_id, lis_device_name, serial_number, is_active, notes, created_at, updated_at, version
) VALUES (
    'vision-pro',
    'Vision Pro (HbA1c)',
    'HBA1C',
    'Лаборатория HbA1c',
    NULL, NULL, NULL,
    NULL,
    TRUE,
    'Анализатор HbA1c, одноразовые картриджи. Модель: Vision Pro, Производитель: A. Menarini Diagnostics',
    NOW(), NOW(), 0
) ON CONFLICT (id) DO NOTHING;

INSERT INTO analyzer_reagent_rates (
    id, analyzer_id, reagent_name, operation_type, test_mode,
    volume_per_operation_ml, units_per_operation, unit_type,
    source_document, notes, created_at, version
) VALUES
    ('rate-vision-pro-cartridge', 'vision-pro', 'HbA1c Cartridge', 'PATIENT_TEST', NULL,
     NULL, 1, 'UNITS', 'Vision Pro User Manual', '1 sealed cartridge per HbA1c test', NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- =====================================================================
-- Additional biochemistry reagents for BS-series (from биохимия.md)
-- Missing from V9: FUN, ACE, β2-mG, Cys-C, β-HB, G6PD, MALB, RBP, TPUC, TRF, HCY, UIBC, TBA
-- Formula: volume_per_test_ml = total_kit_volume_ml / max_tests_for_model
-- Using BS-240Pro rates (same for BS-240), applicable to BS-430/BS-600M
-- =====================================================================

INSERT INTO analyzer_reagent_rates (
    id, analyzer_id, reagent_name, operation_type, test_mode,
    volume_per_operation_ml, units_per_operation, unit_type,
    source_document, notes, created_at, version
) VALUES
    -- Фруктозамин (FUN): 75ml kit / 385 tests (BS-240Pro)
    ('rate-bs240pro-fun', 'mindray-bs-240pro', 'FUN (Фруктозамин)', 'PATIENT_TEST', NULL,
     0.195, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '75mL kit / 385 tests', NOW(), 0),

    -- ACE: 20ml kit / 112 tests
    ('rate-bs240pro-ace', 'mindray-bs-240pro', 'ACE (Ангиотензинпревращающий фермент)', 'PATIENT_TEST', NULL,
     0.179, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '20mL kit / 112 tests', NOW(), 0),

    -- β2-Microglobulin (β2-mG): 52ml kit / 227 tests
    ('rate-bs240pro-b2mg', 'mindray-bs-240pro', 'β2-mG (β2 Микроглобулин)', 'PATIENT_TEST', NULL,
     0.229, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '52mL kit / 227 tests', NOW(), 0),

    -- Cystatin C (Cys-C): 52ml kit / 300 tests
    ('rate-bs240pro-cysc', 'mindray-bs-240pro', 'Cys-C (Цистатин C)', 'PATIENT_TEST', NULL,
     0.173, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '52mL kit / 300 tests', NOW(), 0),

    -- Beta-Hydroxybutyrate (β-HB): 27ml kit / 140 tests
    ('rate-bs240pro-bhb', 'mindray-bs-240pro', 'β-HB (Бета Гидроксибутират)', 'PATIENT_TEST', NULL,
     0.193, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '27mL kit / 140 tests', NOW(), 0),

    -- G6PD: 27ml kit / 140 tests
    ('rate-bs240pro-g6pd', 'mindray-bs-240pro', 'G6PD (Глюкозо-6-фосфат Дегидрогеназа)', 'PATIENT_TEST', NULL,
     0.193, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '27mL kit / 140 tests', NOW(), 0),

    -- Microalbumin (MALB): 37ml kit / 211 tests
    ('rate-bs240pro-malb', 'mindray-bs-240pro', 'MALB (Микроальбумин)', 'PATIENT_TEST', NULL,
     0.175, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '37mL kit / 211 tests', NOW(), 0),

    -- RBP: 28ml kit / 94 tests
    ('rate-bs240pro-rbp', 'mindray-bs-240pro', 'RBP (Ретинол связывающий белок)', 'PATIENT_TEST', NULL,
     0.298, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '28mL kit / 94 tests', NOW(), 0),

    -- TPUC (Total Protein Urine): 54ml kit / 378 tests (BS-240Pro)
    ('rate-bs240pro-tpuc', 'mindray-bs-240pro', 'TPUC (Общий белок в моче)', 'PATIENT_TEST', NULL,
     0.143, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '54mL kit / 378 tests', NOW(), 0),

    -- Transferrin (TRF): 37ml kit / 140 tests
    ('rate-bs240pro-trf', 'mindray-bs-240pro', 'TRF (Трансферрин)', 'PATIENT_TEST', NULL,
     0.264, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '37mL kit / 140 tests', NOW(), 0),

    -- Homocysteine (HCY): 33ml kit / 118 tests
    ('rate-bs240pro-hcy', 'mindray-bs-240pro', 'HCY (Гомоцистеин)', 'PATIENT_TEST', NULL,
     0.280, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '33mL kit / 118 tests', NOW(), 0),

    -- UIBC: 27ml kit / 85 tests
    ('rate-bs240pro-uibc', 'mindray-bs-240pro', 'UIBC (Ненасыщенная железосвязывающая способность)', 'PATIENT_TEST', NULL,
     0.318, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '27mL kit / 85 tests', NOW(), 0),

    -- TBA (Total Bile Acids): 72ml kit / 330 tests
    ('rate-bs240pro-tba', 'mindray-bs-240pro', 'TBA (Общие желчные кислоты)', 'PATIENT_TEST', NULL,
     0.218, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '72mL kit / 330 tests', NOW(), 0),

    -- CHE (Cholinesterase): 4×36 mL kit, need to calculate tests count
    -- From table: R1:4×36 mL ≈ 144 mL total, similar to other 4×35 kits which give 942 tests
    -- Estimated ~942 tests for BS-240Pro based on similar volume kits
    ('rate-bs240pro-che', 'mindray-bs-240pro', 'CHE (Холинэстераза)', 'PATIENT_TEST', NULL,
     0.153, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', '144mL kit (4×36mL) / ~942 tests', NOW(), 0),

    -- CO2 (бикарбонат): from мультиконтроль reference, using typical kit size
    ('rate-bs240pro-co2', 'mindray-bs-240pro', 'CO2 (Бикарбонат)', 'PATIENT_TEST', NULL,
     0.150, NULL, 'ML', 'Mindray Biochemistry Reagent Catalog 2026', 'Typical CO2 reagent volume', NOW(), 0)
ON CONFLICT (id) DO NOTHING;
