-- Migration: V23 - Service Catalog
-- Purpose: Canonical service registry bridging LIS report names and analyzer log names.
-- One entry = one logical laboratory test (e.g. CBC, Glucose, HBsAg).
-- Used by ServiceMatchingService to reconcile LIS vs analyzer log service counts.

CREATE TABLE IF NOT EXISTS service_catalog (
    id                      VARCHAR(36)    PRIMARY KEY,
    canonical_name          VARCHAR(300)   NOT NULL,
    category                VARCHAR(50)    NOT NULL  -- HEMATOLOGY|BIOCHEMISTRY|IMMUNOLOGY|COAGULATION|URINALYSIS|MICROBIOLOGY|POCT|OTHER
        CHECK (category IN ('HEMATOLOGY','BIOCHEMISTRY','IMMUNOLOGY','COAGULATION','URINALYSIS','MICROBIOLOGY','POCT','OTHER')),

    -- JSON array of substrings matched against LIS service names (case-insensitive contains)
    lis_aliases             TEXT           NOT NULL  DEFAULT '[]',

    -- JSON array of integer service IDs from HL7/analyzer driver
    analyzer_service_ids    TEXT           NOT NULL  DEFAULT '[]',

    -- JSON array of parameter codes from analyzer (e.g. ["WBC","RBC","HGB","PLT"] for CBC)
    analyzer_parameters     TEXT           NOT NULL  DEFAULT '[]',

    -- JSON array of AnalyzerType values this service is performed on
    analyzer_types          TEXT           NOT NULL  DEFAULT '[]',

    -- Price from LIS (unit price per test, tenge) — used for lost-revenue estimate
    lis_price_tenge         NUMERIC(12,2),

    -- Grace window: if sample is in logs but not yet in LIS, treat as PENDING_GRACE for N hours
    -- 0 = no grace (immediate discrepancy). Immunology hepatitis: 72h.
    grace_hours             INTEGER        NOT NULL  DEFAULT 0,

    is_active               BOOLEAN        NOT NULL  DEFAULT TRUE,
    notes                   TEXT,
    created_at              TIMESTAMP      NOT NULL  DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP      NOT NULL  DEFAULT CURRENT_TIMESTAMP,
    version                 BIGINT
);

CREATE INDEX IF NOT EXISTS idx_service_catalog_category  ON service_catalog(category);
CREATE INDEX IF NOT EXISTS idx_service_catalog_is_active ON service_catalog(is_active);

COMMENT ON TABLE service_catalog IS
    'Canonical service dictionary. Bridges Damumed LIS service names and analyzer log service names for reconciliation.';

-- =============================================================================
-- Seed: Common Kazakhstan laboratory services
-- Grace periods:
--   Hematology/Biochemistry/Coagulation/Urinalysis: 0 (immediate)
--   Immunology - hormones/basic: 24h
--   Immunology - hepatitis/HIV: 72h (day-long confirmation cycle in Damumed)
-- =============================================================================

-- ─── ГЕМАТОЛОГИЯ ─────────────────────────────────────────────────────────────
INSERT INTO service_catalog (id, canonical_name, category, lis_aliases, analyzer_service_ids, analyzer_parameters, analyzer_types, grace_hours, notes)
VALUES
('sc-hem-001', 'Общий анализ крови (автоматический)', 'HEMATOLOGY',
 '["общий анализ крови","оак","общ. анализ крови","кровь клинический","клинический анализ крови","cbc","полный анализ крови"]',
 '[1,2,3,100,101,102]', '["WBC","RBC","HGB","HCT","PLT","MCV","MCH","MCHC"]', '["HEMATOLOGY"]', 0,
 'CBC — Общий анализ крови. Выполняется на гематологических анализаторах (BC-5000, Mindray, Sysmex).'),

('sc-hem-002', 'Лейкоцитарная формула (дифференциальный подсчёт)', 'HEMATOLOGY',
 '["лейкоцитарная формула","лейкоформула","дифф","diff count","5-part diff","3-part diff"]',
 '[4,5,103,104]', '["NEUT","LYMPH","MONO","EOS","BASO"]', '["HEMATOLOGY"]', 0,
 'Пятипараметрный дифференциальный подсчёт лейкоцитов.'),

('sc-hem-003', 'Ретикулоциты', 'HEMATOLOGY',
 '["ретикулоцит","retic"]',
 '[6,105]', '["RET","RET#","%RET"]', '["HEMATOLOGY"]', 0, NULL),

('sc-hem-004', 'СОЭ (скорость оседания эритроцитов)', 'HEMATOLOGY',
 '["соэ","сoe","esr","скорость оседания"]',
 '[7,106]', '["ESR"]', '["HEMATOLOGY","ESR"]', 0, NULL),

-- ─── БИОХИМИЯ ─────────────────────────────────────────────────────────────────
('sc-bio-001', 'Глюкоза', 'BIOCHEMISTRY',
 '["глюкоз","glucose","сахар крови","glu"]',
 '[10,200,201]', '["GLU","Glucose"]', '["BIOCHEMISTRY"]', 0, NULL),

('sc-bio-002', 'Общий белок', 'BIOCHEMISTRY',
 '["общий белок","total protein","белок общий","tp"]',
 '[11,202]', '["TP","TotalProtein"]', '["BIOCHEMISTRY"]', 0, NULL),

('sc-bio-003', 'Билирубин общий', 'BIOCHEMISTRY',
 '["билирубин общий","total bilirubin","tbi","bilirub"]',
 '[12,203]', '["TBIL","TotalBilirubin"]', '["BIOCHEMISTRY"]', 0, NULL),

('sc-bio-004', 'Билирубин прямой', 'BIOCHEMISTRY',
 '["билирубин прямой","direct bilirubin","dbi","bil прям"]',
 '[13,204]', '["DBIL","DirectBilirubin"]', '["BIOCHEMISTRY"]', 0, NULL),

('sc-bio-005', 'АЛТ (аланинаминотрансфераза)', 'BIOCHEMISTRY',
 '["алт","alt","аланинаминотрансфераза","аланин"]',
 '[14,205]', '["ALT"]', '["BIOCHEMISTRY"]', 0, NULL),

('sc-bio-006', 'АСТ (аспартатаминотрансфераза)', 'BIOCHEMISTRY',
 '["аст","ast","аспартатаминотрансфераза","аспартат"]',
 '[15,206]', '["AST"]', '["BIOCHEMISTRY"]', 0, NULL),

('sc-bio-007', 'Мочевина', 'BIOCHEMISTRY',
 '["мочевина","urea","urea nitrogen","bun"]',
 '[16,207]', '["UREA","BUN"]', '["BIOCHEMISTRY"]', 0, NULL),

('sc-bio-008', 'Креатинин', 'BIOCHEMISTRY',
 '["креатинин","creatinine","cr","cre"]',
 '[17,208]', '["CREA","Creatinine"]', '["BIOCHEMISTRY"]', 0, NULL),

('sc-bio-009', 'Общий холестерин', 'BIOCHEMISTRY',
 '["холестерин","cholesterol","chol","тотальный холестерин"]',
 '[18,209]', '["CHOL","Cholesterol"]', '["BIOCHEMISTRY"]', 0, NULL),

('sc-bio-010', 'Щелочная фосфатаза', 'BIOCHEMISTRY',
 '["щелочная фосфатаза","alkaline phosphatase","alp","щф"]',
 '[19,210]', '["ALP"]', '["BIOCHEMISTRY"]', 0, NULL),

('sc-bio-011', 'Амилаза', 'BIOCHEMISTRY',
 '["амилаза","amylase","amy"]',
 '[20,211]', '["AMY","Amylase"]', '["BIOCHEMISTRY"]', 0, NULL),

('sc-bio-012', 'Мочевая кислота', 'BIOCHEMISTRY',
 '["мочевая кислота","uric acid","ua","урат"]',
 '[21,212]', '["UA","UricAcid"]', '["BIOCHEMISTRY"]', 0, NULL),

('sc-bio-013', 'Триглицериды', 'BIOCHEMISTRY',
 '["триглицерид","triglyceride","tg","trig"]',
 '[22,213]', '["TG","Triglyceride"]', '["BIOCHEMISTRY"]', 0, NULL),

('sc-bio-014', 'Кальций', 'BIOCHEMISTRY',
 '["кальций","calcium","ca"]',
 '[23,214]', '["CA","Calcium"]', '["BIOCHEMISTRY"]', 0, NULL),

('sc-bio-015', 'Железо сывороточное', 'BIOCHEMISTRY',
 '["железо","iron","fe","сывороточное железо"]',
 '[24,215]', '["FE","Iron"]', '["BIOCHEMISTRY"]', 0, NULL),

('sc-bio-016', 'Фибриноген', 'BIOCHEMISTRY',
 '["фибриноген","fibrinogen","fib"]',
 '[25,216]', '["FIB","Fibrinogen"]', '["BIOCHEMISTRY","COAGULATION"]', 0, NULL),

-- ─── КОАГУЛОЛОГИЯ ─────────────────────────────────────────────────────────────
('sc-coa-001', 'ПТИ / МНО (протромбиновый тест)', 'COAGULATION',
 '["пти","мно","протромбин","prothrombin","pt","inr","мно ","пт "]',
 '[30,300,301]', '["PT","INR","PT%"]', '["COAGULATION"]', 0, NULL),

('sc-coa-002', 'АЧТВ (активированное частичное тромбопластиновое время)', 'COAGULATION',
 '["ачтв","aptt","ачт ","частичное тромбопластиновое","activated partial thromboplastin"]',
 '[31,302]', '["APTT","aPTT"]', '["COAGULATION"]', 0, NULL),

('sc-coa-003', 'Тромбиновое время', 'COAGULATION',
 '["тромбиновое время","thrombin time","tt","тромб. врем"]',
 '[32,303]', '["TT","ThrombinTime"]', '["COAGULATION"]', 0, NULL),

('sc-coa-004', 'D-димер', 'COAGULATION',
 '["d-димер","d-dimer","ddimer","д-димер"]',
 '[33,304]', '["DDIM","D-Dimer"]', '["COAGULATION","IMMUNOLOGY"]', 0, NULL),

-- ─── ИММУНОЛОГИЯ / ИФА ────────────────────────────────────────────────────────
-- Grace period 72h for hepatitis/HIV (confirmation takes 24-72h in Damumed workflow)
('sc-imm-001', 'HBsAg (гепатит B поверхностный антиген)', 'IMMUNOLOGY',
 '["hbsag","гепатит b","hepatitis b","антиген гепатита b","hbs"]',
 '[40,400]', '["HBsAg"]', '["IMMUNOLOGY"]', 72,
 'Гепатит B. Подтверждение в ЛИС занимает 1-3 дня. Grace period 72h.'),

('sc-imm-002', 'Anti-HCV (антитела к гепатиту C)', 'IMMUNOLOGY',
 '["anti-hcv","hcv","антитела к гепатиту c","гепатит c","hepatitis c","анти-hcv"]',
 '[41,401]', '["HCV","Anti-HCV"]', '["IMMUNOLOGY"]', 72,
 'Гепатит C. Grace period 72h.'),

('sc-imm-003', 'ВИЧ (антиген/антитела, скрининг)', 'IMMUNOLOGY',
 '["вич","hiv","спид","антитела к вич","ag/at вич"]',
 '[42,402]', '["HIV","HIV1+2"]', '["IMMUNOLOGY"]', 72,
 'ВИЧ скрининг. Grace period 72h.'),

('sc-imm-004', 'ТТГ (тиреотропный гормон)', 'IMMUNOLOGY',
 '["ттг","tsh","тиреотропный гормон","thyroid stimulating"]',
 '[43,403]', '["TSH"]', '["IMMUNOLOGY"]', 24,
 'Гормон щитовидной железы. Grace 24h.'),

('sc-imm-005', 'Т4 свободный (тироксин)', 'IMMUNOLOGY',
 '["т4 своб","ft4","free t4","тироксин своб","свободный t4","t4 свободный"]',
 '[44,404]', '["FT4"]', '["IMMUNOLOGY"]', 24, NULL),

('sc-imm-006', 'Т3 свободный (трийодтиронин)', 'IMMUNOLOGY',
 '["т3 своб","ft3","free t3","трийодтиронин","t3 свободный"]',
 '[45,405]', '["FT3"]', '["IMMUNOLOGY"]', 24, NULL),

('sc-imm-007', 'ХГЧ (хорионический гонадотропин)', 'IMMUNOLOGY',
 '["хгч","hcg","бета-хгч","β-хгч","хорионический гонадотропин","b-hcg"]',
 '[46,406]', '["HCG","β-HCG"]', '["IMMUNOLOGY"]', 24, NULL),

('sc-imm-008', 'ПСА (простатический специфический антиген)', 'IMMUNOLOGY',
 '["пса","psa","простатический специфический","prostate specific antigen"]',
 '[47,407]', '["PSA"]', '["IMMUNOLOGY"]', 24, NULL),

('sc-imm-009', 'Ферритин', 'IMMUNOLOGY',
 '["ферритин","ferritin","fer"]',
 '[48,408]', '["FERR","Ferritin"]', '["IMMUNOLOGY"]', 24, NULL),

('sc-imm-010', 'С-реактивный белок (СРБ)', 'IMMUNOLOGY',
 '["срб","crp","с-реактивный белок","c-reactive protein","с реактивный"]',
 '[49,409]', '["CRP","hsCRP"]', '["IMMUNOLOGY","BIOCHEMISTRY"]', 0, NULL),

('sc-imm-011', 'Тропонин I (кардиомаркер)', 'IMMUNOLOGY',
 '["тропонин","troponin","tropi","trop i","hs-troponin"]',
 '[50,410]', '["TROP","TropI"]', '["IMMUNOLOGY","POCT"]', 0,
 'Кардиомаркер. Часто на POCT-анализаторах (iFlash, hs-cTnI).'),

-- ─── ОАМ (Урология) ──────────────────────────────────────────────────────────
('sc-ura-001', 'Общий анализ мочи (ОАМ)', 'URINALYSIS',
 '["общий анализ мочи","оам","urinalysis","моча общий","ua общий","анализ мочи"]',
 '[60,500]', '["UA","SG","PH","PRO","GLU","KET","BIL","UBG","RBC_U","WBC_U"]', '["URINALYSIS"]', 0, NULL);
