-- Migration: V17 - Service-to-Analyzer Seed Data
-- Adds missing analyzers first (Mindray CL-2000 immunoassay, Mindray BC-5000 hematology variant)
-- Purpose: Map Damumed service names and categories to analyzers by keyword patterns.
-- Source: Damumed service catalog (КР/Услуги КМИСБ), common KZ laboratory nomenclature.
-- Priority: lower number = higher priority (matched first on conflict).

-- =============================================================================
-- Ensure required analyzers exist
-- =============================================================================
INSERT INTO analyzers (id, name, type, workplace_name, lis_device_system_name, lis_analyzer_id, lis_device_name, serial_number, is_active, notes, created_at, updated_at, version) VALUES
    ('mindray-cl-2000',   'Mindray CL-2000', 'IMMUNOASSAY', 'Иммунологический отдел', 'CL2000',  2001, 'Mindray CL-2000',  NULL, true, 'Иммунохимический анализатор (хемилюминесценция)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('mindray-cl-1000i',  'Mindray CL-1000i', 'IMMUNOASSAY', 'Иммунологический отдел', 'CL1000I', 2002, 'Mindray CL-1000i', NULL, true, 'Автоматический иммунохимический анализатор', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Hematology (ОАК, гематология, общий анализ крови)
-- Typical analyzers: Mindray BC-series, Sysmex XP/XN, ABX
-- =============================================================================
INSERT INTO service_to_analyzer_mappings (id, service_name_pattern, service_category, analyzer_id, matching_priority, is_active, created_at, updated_at, version)
SELECT
    gen.id, gen.pattern, gen.category, gen.analyzer_id, gen.priority, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM (VALUES
    ('map-hema-oak-1',        'общий анализ крови',                    'HEMATOLOGY', 'mindray-bc-5000', 10),
    ('map-hema-oak-2',        'ОАК',                                   'HEMATOLOGY', 'mindray-bc-5000', 10),
    ('map-hema-oak-3',        'клинический анализ крови',              'HEMATOLOGY', 'mindray-bc-5000', 10),
    ('map-hema-oak-4',        'анализ крови развернутый',              'HEMATOLOGY', 'mindray-bc-5000', 15),
    ('map-hema-oak-5',        'кровь клиническая',                     'HEMATOLOGY', 'mindray-bc-5000', 15),
    ('map-hema-oac-ru',       'клинический анализ крови + лейкоциты',  'HEMATOLOGY', 'mindray-bc-5000', 20),
    ('map-hema-reticulocyte', 'ретикулоциты',                          'HEMATOLOGY', 'mindray-bc-5000', 30),
    ('map-hema-esr',          'скорость оседания эритроцитов',         'ESR',         'mindray-bc-5000', 40),
    ('map-hema-esr2',         'СОЭ',                                   'ESR',         'mindray-bc-5000', 40),
    ('map-hema-esr3',         'вестергрен',                            'ESR',         'mindray-bc-5000', 50)
) AS gen(id, pattern, category, analyzer_id, priority)
WHERE EXISTS (SELECT 1 FROM analyzers WHERE id = gen.analyzer_id)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Biochemistry (биохимия, liver, kidney, lipids, glucose, electrolytes)
-- Typical analyzers: Mindray BS-series
-- =============================================================================
INSERT INTO service_to_analyzer_mappings (id, service_name_pattern, service_category, analyzer_id, matching_priority, is_active, created_at, updated_at, version)
SELECT gen.id, gen.pattern, gen.category, gen.analyzer_id, gen.priority, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM (VALUES
    ('map-bio-alt',            'АЛТ',                                   'BIOCHEMISTRY', 'mindray-bs-230', 10),
    ('map-bio-alt2',           'аланинаминотрансфераза',                 'BIOCHEMISTRY', 'mindray-bs-230', 10),
    ('map-bio-ast',            'АСТ',                                   'BIOCHEMISTRY', 'mindray-bs-230', 10),
    ('map-bio-ast2',           'аспартатаминотрансфераза',               'BIOCHEMISTRY', 'mindray-bs-230', 10),
    ('map-bio-bili-total',     'билирубин общий',                        'BIOCHEMISTRY', 'mindray-bs-230', 10),
    ('map-bio-bili-direct',    'билирубин прямой',                       'BIOCHEMISTRY', 'mindray-bs-230', 10),
    ('map-bio-bili-indirect',  'билирубин непрямой',                     'BIOCHEMISTRY', 'mindray-bs-230', 10),
    ('map-bio-bili-total-kz',  'билирубин',                              'BIOCHEMISTRY', 'mindray-bs-230', 15),
    ('map-bio-glucose',        'глюкоза',                               'BIOCHEMISTRY', 'mindray-bs-230', 10),
    ('map-bio-glucose2',       'сахар крови',                           'BIOCHEMISTRY', 'mindray-bs-230', 20),
    ('map-bio-creatinine',     'креатинин',                             'BIOCHEMISTRY', 'mindray-bs-230', 10),
    ('map-bio-urea',           'мочевина',                              'BIOCHEMISTRY', 'mindray-bs-230', 10),
    ('map-bio-uric-acid',      'мочевая кислота',                       'BIOCHEMISTRY', 'mindray-bs-230', 10),
    ('map-bio-chol-total',     'холестерин общий',                      'BIOCHEMISTRY', 'mindray-bs-230', 10),
    ('map-bio-chol',           'холестерин',                            'BIOCHEMISTRY', 'mindray-bs-230', 15),
    ('map-bio-ldl',            'ЛПНП',                                  'BIOCHEMISTRY', 'mindray-bs-230', 10),
    ('map-bio-ldl2',           'холестерин LDL',                        'BIOCHEMISTRY', 'mindray-bs-230', 10),
    ('map-bio-hdl',            'ЛПВП',                                  'BIOCHEMISTRY', 'mindray-bs-230', 10),
    ('map-bio-hdl2',           'холестерин HDL',                        'BIOCHEMISTRY', 'mindray-bs-230', 10),
    ('map-bio-triglycerides',  'триглицериды',                          'BIOCHEMISTRY', 'mindray-bs-230', 10),
    ('map-bio-protein-total',  'общий белок',                           'BIOCHEMISTRY', 'mindray-bs-230', 10),
    ('map-bio-albumin',        'альбумин',                              'BIOCHEMISTRY', 'mindray-bs-230', 10),
    ('map-bio-ggtp',           'гамма-ГТ',                              'BIOCHEMISTRY', 'mindray-bs-230', 10),
    ('map-bio-ggtp2',          'ГГТП',                                  'BIOCHEMISTRY', 'mindray-bs-230', 10),
    ('map-bio-alp',            'щелочная фосфатаза',                    'BIOCHEMISTRY', 'mindray-bs-230', 10),
    ('map-bio-alp2',           'ЩФ',                                    'BIOCHEMISTRY', 'mindray-bs-230', 10),
    ('map-bio-amylase',        'амилаза',                               'BIOCHEMISTRY', 'mindray-bs-230', 10),
    ('map-bio-lipase',         'липаза',                                'BIOCHEMISTRY', 'mindray-bs-230', 10),
    ('map-bio-ldh',            'ЛДГ',                                   'BIOCHEMISTRY', 'mindray-bs-230', 10),
    ('map-bio-ck',             'КФК',                                   'BIOCHEMISTRY', 'mindray-bs-230', 10),
    ('map-bio-ck-mb',          'КФК-МВ',                                'BIOCHEMISTRY', 'mindray-bs-230', 10),
    ('map-bio-hba1c',          'гликированный гемоглобин',              'HBA1C',        'mindray-bs-230', 10),
    ('map-bio-hba1c2',         'HbA1c',                                 'HBA1C',        'mindray-bs-230', 10),
    ('map-bio-fe',             'железо сыворотки',                      'BIOCHEMISTRY', 'mindray-bs-230', 10),
    ('map-bio-tibc',           'ОЖСС',                                  'BIOCHEMISTRY', 'mindray-bs-230', 10),
    ('map-bio-ca',             'кальций',                               'BIOCHEMISTRY', 'mindray-bs-230', 20),
    ('map-bio-mg',             'магний',                                 'BIOCHEMISTRY', 'mindray-bs-230', 20),
    ('map-bio-p',              'фосфор неорганический',                  'BIOCHEMISTRY', 'mindray-bs-230', 20),
    ('map-bio-na-k-cl',        'натрий, калий, хлор',                   'BIOCHEMISTRY', 'mindray-bs-240pro', 10),
    ('map-bio-electrolytes',   'электролиты',                           'BIOCHEMISTRY', 'mindray-bs-240pro', 10)
) AS gen(id, pattern, category, analyzer_id, priority)
WHERE EXISTS (SELECT 1 FROM analyzers WHERE id = gen.analyzer_id)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Coagulation (коагулология, свертываемость, гемостаз)
-- Typical analyzers: Stago, STA-R, Helena
-- Fallback to generic coagulation analyzer if not seeded
-- =============================================================================
INSERT INTO service_to_analyzer_mappings (id, service_name_pattern, service_category, analyzer_id, matching_priority, is_active, created_at, updated_at, version)
SELECT gen.id, gen.pattern, gen.category, gen.analyzer_id, gen.priority, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM (VALUES
    ('map-coag-pti',          'протромбиновое время',                   'COAGULATION',  'mindray-bs-230', 20),
    ('map-coag-pti2',         'ПТИ',                                   'COAGULATION',  'mindray-bs-230', 20),
    ('map-coag-inr',          'МНО',                                   'COAGULATION',  'mindray-bs-230', 20),
    ('map-coag-aptt',         'АЧТВ',                                  'COAGULATION',  'mindray-bs-230', 20),
    ('map-coag-fibrinogen',   'фибриноген',                            'COAGULATION',  'mindray-bs-230', 20),
    ('map-coag-ddimer',       'Д-димер',                               'COAGULATION',  'mindray-bs-230', 20),
    ('map-coag-coag-test',    'коагулограмма',                         'COAGULATION',  'mindray-bs-230', 30),
    ('map-coag-thrombin',     'тромбиновое время',                     'COAGULATION',  'mindray-bs-230', 30)
) AS gen(id, pattern, category, analyzer_id, priority)
WHERE EXISTS (SELECT 1 FROM analyzers WHERE id = gen.analyzer_id)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Immunoassay / Hormones / Infection markers
-- Typical analyzers: Mindray CL-series, ARCHITECT
-- =============================================================================
INSERT INTO service_to_analyzer_mappings (id, service_name_pattern, service_category, analyzer_id, matching_priority, is_active, created_at, updated_at, version)
SELECT gen.id, gen.pattern, gen.category, gen.analyzer_id, gen.priority, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM (VALUES
    ('map-immu-hiv',          'ВИЧ',                                    'IMMUNOASSAY', 'mindray-cl-2000', 10),
    ('map-immu-hbsag',        'HBsAg',                                  'IMMUNOASSAY', 'mindray-cl-2000', 10),
    ('map-immu-hbsag2',       'гепатит B',                              'IMMUNOASSAY', 'mindray-cl-2000', 15),
    ('map-immu-hcv',          'гепатит C',                              'IMMUNOASSAY', 'mindray-cl-2000', 10),
    ('map-immu-hcv2',         'анти-ВГС',                               'IMMUNOASSAY', 'mindray-cl-2000', 10),
    ('map-immu-syphilis',     'сифилис',                                'IMMUNOASSAY', 'mindray-cl-2000', 10),
    ('map-immu-trepanema',    'трепонема',                              'IMMUNOASSAY', 'mindray-cl-2000', 10),
    ('map-immu-tsh',          'ТТГ',                                    'IMMUNOASSAY', 'mindray-cl-2000', 10),
    ('map-immu-t3',           'Т3 свободный',                           'IMMUNOASSAY', 'mindray-cl-2000', 10),
    ('map-immu-t4',           'Т4 свободный',                           'IMMUNOASSAY', 'mindray-cl-2000', 10),
    ('map-immu-tsh2',         'тиреотропный гормон',                    'IMMUNOASSAY', 'mindray-cl-2000', 15),
    ('map-immu-psa',          'ПСА',                                    'IMMUNOASSAY', 'mindray-cl-2000', 10),
    ('map-immu-psa2',         'простат-специфический антиген',          'IMMUNOASSAY', 'mindray-cl-2000', 15),
    ('map-immu-crp',          'С-реактивный белок',                     'IMMUNOASSAY', 'mindray-cl-2000', 10),
    ('map-immu-crp2',         'СРБ',                                    'IMMUNOASSAY', 'mindray-cl-2000', 10),
    ('map-immu-troponin',     'тропонин',                               'IMMUNOASSAY', 'mindray-cl-2000', 10),
    ('map-immu-bnp',          'про-МНП',                                'IMMUNOASSAY', 'mindray-cl-2000', 10),
    ('map-immu-procalcitonin','прокальцитонин',                         'IMMUNOASSAY', 'mindray-cl-2000', 10),
    ('map-immu-ferritin',     'ферритин',                               'IMMUNOASSAY', 'mindray-cl-2000', 10),
    ('map-immu-b12',          'витамин B12',                            'IMMUNOASSAY', 'mindray-cl-2000', 10),
    ('map-immu-folate',       'фолиевая кислота',                       'IMMUNOASSAY', 'mindray-cl-2000', 10),
    ('map-immu-vitd',         'витамин D',                              'IMMUNOASSAY', 'mindray-cl-2000', 10),
    ('map-immu-covid',        'COVID-19',                               'IMMUNOASSAY', 'mindray-cl-2000', 10),
    ('map-immu-covid2',       'SARS-CoV-2',                             'IMMUNOASSAY', 'mindray-cl-2000', 10)
) AS gen(id, pattern, category, analyzer_id, priority)
WHERE EXISTS (SELECT 1 FROM analyzers WHERE id = gen.analyzer_id)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Urinalysis (ОАМ, моча, urinalysis)
-- =============================================================================
INSERT INTO service_to_analyzer_mappings (id, service_name_pattern, service_category, analyzer_id, matching_priority, is_active, created_at, updated_at, version)
SELECT gen.id, gen.pattern, gen.category, gen.analyzer_id, gen.priority, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM (VALUES
    ('map-urine-oam',         'общий анализ мочи',                     'URINALYSIS', 'mindray-bs-230', 20),
    ('map-urine-oam2',        'ОАМ',                                   'URINALYSIS', 'mindray-bs-230', 20),
    ('map-urine-micro',       'микроскопия мочи',                      'URINALYSIS', 'mindray-bs-230', 20),
    ('map-urine-protein',     'белок в моче',                          'URINALYSIS', 'mindray-bs-230', 20),
    ('map-urine-glucose-u',   'глюкоза в моче',                        'URINALYSIS', 'mindray-bs-230', 25),
    ('map-urine-creatinine-u','креатинин мочи',                        'URINALYSIS', 'mindray-bs-230', 25),
    ('map-urine-albumin-u',   'микроальбуминурия',                     'URINALYSIS', 'mindray-bs-230', 20)
) AS gen(id, pattern, category, analyzer_id, priority)
WHERE EXISTS (SELECT 1 FROM analyzers WHERE id = gen.analyzer_id)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Seed note: Analyzer IDs reference existing seeds from V13.
-- If your lab uses different analyzer IDs, update analyzer_id values accordingly.
-- Pattern matching is done via ILIKE in application code.
-- =============================================================================
