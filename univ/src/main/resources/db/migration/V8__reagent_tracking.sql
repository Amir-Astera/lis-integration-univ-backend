CREATE TABLE IF NOT EXISTS analyzers (
    id VARCHAR(128) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(64) NOT NULL,
    workplace_name VARCHAR(255) NOT NULL,
    lis_device_system_name VARCHAR(64) NULL,
    lis_analyzer_id INTEGER NULL,
    lis_device_name VARCHAR(255) NULL,
    serial_number VARCHAR(128) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    notes TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT NULL
);

CREATE INDEX IF NOT EXISTS idx_analyzers_type
    ON analyzers (type);

CREATE INDEX IF NOT EXISTS idx_analyzers_workplace_name
    ON analyzers (workplace_name);

CREATE TABLE IF NOT EXISTS analyzer_reagent_rates (
    id VARCHAR(128) PRIMARY KEY,
    analyzer_id VARCHAR(128) NOT NULL,
    reagent_name VARCHAR(255) NOT NULL,
    operation_type VARCHAR(64) NOT NULL,
    test_mode VARCHAR(64) NULL,
    volume_per_operation_ml DOUBLE PRECISION NULL,
    units_per_operation INTEGER NULL,
    unit_type VARCHAR(32) NOT NULL,
    source_document VARCHAR(512) NULL,
    notes TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT NULL,
    CONSTRAINT fk_analyzer_reagent_rates_analyzer
        FOREIGN KEY (analyzer_id)
        REFERENCES analyzers (id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_analyzer_reagent_rates_analyzer
    ON analyzer_reagent_rates (analyzer_id);

CREATE UNIQUE INDEX IF NOT EXISTS idx_analyzer_reagent_rates_unique
    ON analyzer_reagent_rates (analyzer_id, reagent_name, operation_type, COALESCE(test_mode, ''));

CREATE TABLE IF NOT EXISTS reagent_inventory (
    id VARCHAR(128) PRIMARY KEY,
    analyzer_id VARCHAR(128) NULL,
    reagent_name VARCHAR(255) NOT NULL,
    lot_number VARCHAR(128) NULL,
    manufacturer VARCHAR(255) NULL,
    expiry_date_sealed DATE NULL,
    stability_days_after_opening INTEGER NULL,
    opened_date DATE NULL,
    total_volume_ml DOUBLE PRECISION NULL,
    total_units INTEGER NULL,
    unit_type VARCHAR(32) NOT NULL,
    unit_price_tenge DOUBLE PRECISION NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'IN_STOCK',
    received_at DATE NOT NULL,
    received_by VARCHAR(255) NULL,
    notes TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT NULL,
    CONSTRAINT fk_reagent_inventory_analyzer
        FOREIGN KEY (analyzer_id)
        REFERENCES analyzers (id)
        ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_reagent_inventory_analyzer
    ON reagent_inventory (analyzer_id);

CREATE INDEX IF NOT EXISTS idx_reagent_inventory_status
    ON reagent_inventory (status);

CREATE INDEX IF NOT EXISTS idx_reagent_inventory_expiry
    ON reagent_inventory (expiry_date_sealed);

CREATE TABLE IF NOT EXISTS consumable_inventory (
    id VARCHAR(128) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(64) NOT NULL,
    tube_color VARCHAR(32) NULL,
    linked_analyzer_types TEXT NULL,
    linked_service_keywords TEXT NULL,
    quantity_total INTEGER NOT NULL,
    quantity_remaining INTEGER NOT NULL,
    unit_price_tenge DOUBLE PRECISION NULL,
    lot_number VARCHAR(128) NULL,
    expiry_date DATE NULL,
    received_at DATE NOT NULL,
    received_by VARCHAR(255) NULL,
    notes TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT NULL
);

CREATE INDEX IF NOT EXISTS idx_consumable_inventory_category
    ON consumable_inventory (category);

CREATE TABLE IF NOT EXISTS analyzer_log_uploads (
    id VARCHAR(128) PRIMARY KEY,
    analyzer_id VARCHAR(128) NULL,
    source_type VARCHAR(32) NOT NULL,
    original_file_name VARCHAR(512) NOT NULL,
    stored_file_name VARCHAR(512) NOT NULL,
    storage_path VARCHAR(1024) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    checksum_sha256 VARCHAR(64) NOT NULL,
    parse_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    parse_started_at TIMESTAMP NULL,
    parse_completed_at TIMESTAMP NULL,
    parse_error_message TEXT NULL,
    total_lines_parsed INTEGER NOT NULL DEFAULT 0,
    total_samples_found INTEGER NOT NULL DEFAULT 0,
    legitimate_samples INTEGER NOT NULL DEFAULT 0,
    unauthorized_samples INTEGER NOT NULL DEFAULT 0,
    wash_test_samples INTEGER NOT NULL DEFAULT 0,
    rerun_samples INTEGER NOT NULL DEFAULT 0,
    log_period_start TIMESTAMP NULL,
    log_period_end TIMESTAMP NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT NOW(),
    uploaded_by VARCHAR(255) NULL,
    version BIGINT NULL,
    CONSTRAINT fk_analyzer_log_uploads_analyzer
        FOREIGN KEY (analyzer_id)
        REFERENCES analyzers (id)
        ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_analyzer_log_uploads_analyzer
    ON analyzer_log_uploads (analyzer_id);

CREATE INDEX IF NOT EXISTS idx_analyzer_log_uploads_status
    ON analyzer_log_uploads (parse_status);

CREATE TABLE IF NOT EXISTS parsed_analyzer_samples (
    id VARCHAR(128) PRIMARY KEY,
    log_upload_id VARCHAR(128) NOT NULL,
    analyzer_id VARCHAR(128) NULL,
    sample_timestamp TIMESTAMP NOT NULL,
    barcode VARCHAR(128) NOT NULL,
    device_system_name VARCHAR(64) NULL,
    device_name VARCHAR(255) NULL,
    lis_analyzer_id INTEGER NULL,
    test_mode VARCHAR(64) NULL,
    blood_mode VARCHAR(16) NULL,
    take_mode VARCHAR(16) NULL,
    order_research_id BIGINT NULL,
    order_id BIGINT NULL,
    service_id INTEGER NULL,
    service_name TEXT NULL,
    has_lis_order BOOLEAN NOT NULL,
    sample_request_count INTEGER NOT NULL DEFAULT 0,
    wbc_value DOUBLE PRECISION NULL,
    rbc_value DOUBLE PRECISION NULL,
    hgb_value DOUBLE PRECISION NULL,
    plt_value DOUBLE PRECISION NULL,
    classification VARCHAR(32) NOT NULL,
    classification_reason TEXT NULL,
    correlated_legitimate_sample_id VARCHAR(128) NULL,
    estimated_diluent_ml DOUBLE PRECISION NULL,
    estimated_diff_lyse_ml DOUBLE PRECISION NULL,
    estimated_lh_lyse_ml DOUBLE PRECISION NULL,
    estimated_cost_tenge DOUBLE PRECISION NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT NULL,
    CONSTRAINT fk_parsed_analyzer_samples_log_upload
        FOREIGN KEY (log_upload_id)
        REFERENCES analyzer_log_uploads (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_parsed_analyzer_samples_analyzer
        FOREIGN KEY (analyzer_id)
        REFERENCES analyzers (id)
        ON DELETE SET NULL,
    CONSTRAINT fk_parsed_analyzer_samples_correlated
        FOREIGN KEY (correlated_legitimate_sample_id)
        REFERENCES parsed_analyzer_samples (id)
        ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_parsed_analyzer_samples_log_upload
    ON parsed_analyzer_samples (log_upload_id);

CREATE INDEX IF NOT EXISTS idx_parsed_analyzer_samples_timestamp
    ON parsed_analyzer_samples (sample_timestamp);

CREATE INDEX IF NOT EXISTS idx_parsed_analyzer_samples_barcode
    ON parsed_analyzer_samples (barcode);

CREATE INDEX IF NOT EXISTS idx_parsed_analyzer_samples_classification
    ON parsed_analyzer_samples (classification);

CREATE INDEX IF NOT EXISTS idx_parsed_analyzer_samples_analyzer
    ON parsed_analyzer_samples (analyzer_id);

CREATE INDEX IF NOT EXISTS idx_parsed_analyzer_samples_has_lis_order
    ON parsed_analyzer_samples (has_lis_order);

CREATE TABLE IF NOT EXISTS reagent_consumption_reports (
    id VARCHAR(128) PRIMARY KEY,
    analyzer_id VARCHAR(128) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    legitimate_test_count INTEGER NOT NULL DEFAULT 0,
    legitimate_reagent_consumption_json TEXT NULL,
    legitimate_cost_tenge DOUBLE PRECISION NOT NULL DEFAULT 0,
    service_operations_json TEXT NULL,
    service_reagent_consumption_json TEXT NULL,
    service_cost_tenge DOUBLE PRECISION NOT NULL DEFAULT 0,
    suspicious_test_count INTEGER NOT NULL DEFAULT 0,
    rerun_test_count INTEGER NOT NULL DEFAULT 0,
    wash_test_count INTEGER NOT NULL DEFAULT 0,
    unauthorized_reagent_consumption_json TEXT NULL,
    unauthorized_cost_tenge DOUBLE PRECISION NOT NULL DEFAULT 0,
    inventory_start_json TEXT NULL,
    inventory_received_json TEXT NULL,
    inventory_end_expected_json TEXT NULL,
    inventory_end_actual_json TEXT NULL,
    discrepancy_json TEXT NULL,
    discrepancy_total_tenge DOUBLE PRECISION NOT NULL DEFAULT 0,
    generated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    generated_by VARCHAR(255) NULL,
    version BIGINT NULL,
    CONSTRAINT fk_reagent_consumption_reports_analyzer
        FOREIGN KEY (analyzer_id)
        REFERENCES analyzers (id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_reagent_consumption_reports_analyzer
    ON reagent_consumption_reports (analyzer_id);

CREATE INDEX IF NOT EXISTS idx_reagent_consumption_reports_period
    ON reagent_consumption_reports (period_start, period_end);

INSERT INTO analyzers (
    id, name, type, workplace_name, lis_device_system_name, lis_analyzer_id, lis_device_name, serial_number, is_active, notes, created_at, updated_at, version
) VALUES
    ('mindray-bs-240', 'Mindray BS-240', 'BIOCHEMISTRY', 'Клиническая химия (биохимия)', NULL, NULL, NULL, NULL, TRUE, 'Нормы per-test должны подтверждаться вкладышами конкретных реагентных наборов.', NOW(), NOW(), 0),
    ('mindray-bs-240pro', 'Mindray BS-240Pro', 'BIOCHEMISTRY', 'Клиническая химия (биохимия)', NULL, NULL, NULL, NULL, TRUE, 'Нормы per-test должны подтверждаться вкладышами конкретных реагентных наборов.', NOW(), NOW(), 0),
    ('mindray-bs-430', 'Mindray BS-430', 'BIOCHEMISTRY', 'Клиническая химия (биохимия)', NULL, NULL, NULL, NULL, TRUE, 'Нормы per-test должны подтверждаться вкладышами конкретных реагентных наборов.', NOW(), NOW(), 0),
    ('mindray-bs-600m', 'Mindray BS-600M', 'BIOCHEMISTRY', 'Клиническая химия (биохимия)', NULL, NULL, NULL, NULL, TRUE, 'Нормы per-test должны подтверждаться вкладышами конкретных реагентных наборов.', NOW(), NOW(), 0),
    ('mindray-c3100', 'Mindray C3100', 'COAGULATION', 'коагулогия', NULL, NULL, NULL, NULL, TRUE, 'Точные объемы per-test должны подтверждаться вкладышами реагентов и методиками лаборатории.', NOW(), NOW(), 0),
    ('mission-u500', 'Mission U500', 'URINALYSIS', 'Общеклинические методы', NULL, NULL, NULL, NULL, TRUE, 'Дискретный расход: тест-полоски.', NOW(), NOW(), 0),
    ('mindray-bc-5000', 'Mindray BC-5000', 'HEMATOLOGY', 'гематология', NULL, NULL, NULL, NULL, TRUE, 'Подтвержденные нормы расхода из сервисного мануала BC-5150/BC-5000.', NOW(), NOW(), 0),
    ('ditreex-ro-400', 'Ditreex RO-400 с баком 40л', 'INFRASTRUCTURE', 'Инфраструктура', NULL, NULL, NULL, NULL, TRUE, 'Система водоподготовки, не анализатор.', NOW(), NOW(), 0),
    ('ro-f4-4-10gm', 'Фильтр обратного осмоса RO F4-4-10Gm', 'INFRASTRUCTURE', 'Инфраструктура', NULL, NULL, NULL, NULL, TRUE, 'Система фильтрации воды, не анализатор.', NOW(), NOW(), 0),
    ('vision-pro-esr', 'Vision Pro', 'ESR', 'гематология', NULL, NULL, NULL, NULL, TRUE, 'ESR-анализатор без жидких реагентов.', NOW(), NOW(), 0),
    ('wondfo-ocg-102', 'Wondfo OCG-102', 'POCT_COAGULATION', 'коагулогия', NULL, NULL, NULL, NULL, TRUE, 'Дискретный расход: картриджи / тест-кассеты.', NOW(), NOW(), 0),
    ('ortho-workstation', 'ORTHO Workstation', 'IMMUNOHEMATOLOGY', 'Иммуногематология', NULL, NULL, NULL, NULL, TRUE, 'Учет через гель-карты и расходные материалы; норма зависит от сценария теста.', NOW(), NOW(), 0),
    ('edan-i15', 'Edan i15', 'BLOOD_GAS', 'POCT / Реаниматология', NULL, NULL, NULL, NULL, TRUE, 'Дискретный расход: одноразовые картриджи.', NOW(), NOW(), 0),
    ('fluorecare', 'Fluorecare', 'POCT_IMMUNOASSAY', 'Иммунология', NULL, NULL, NULL, NULL, TRUE, 'Дискретный расход: одноразовые тест-картриджи.', NOW(), NOW(), 0),
    ('iflash-1800', 'iFlash 1800', 'IMMUNOASSAY', 'иммунология', NULL, NULL, NULL, NULL, TRUE, 'Дискретный расход кассетных позиций + системные расходники.', NOW(), NOW(), 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO analyzer_reagent_rates (
    id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version
) VALUES
    ('rate-bc5000-diluent-cbc-diff', 'mindray-bc-5000', 'M-52D Diluent', 'PATIENT_TEST', 'CBC+DIFF', 27.5, NULL, 'ML', 'BC-5150/BC-5000 Service Manual V3.0, Table 2-10 / 7.4', NULL, NOW(), 0),
    ('rate-bc5000-diluent-cbc', 'mindray-bc-5000', 'M-52D Diluent', 'PATIENT_TEST', 'CBC', 23.2, NULL, 'ML', 'BC-5150/BC-5000 Service Manual V3.0, Table 2-10 / 7.4', NULL, NOW(), 0),
    ('rate-bc5000-diff-lyse-cbc-diff', 'mindray-bc-5000', 'M-52 DIFF Lyse', 'PATIENT_TEST', 'CBC+DIFF', 1.0, NULL, 'ML', 'BC-5150/BC-5000 Service Manual V3.0, Table 2-10 / 7.4', NULL, NOW(), 0),
    ('rate-bc5000-lh-lyse-cbc-diff', 'mindray-bc-5000', 'M-52 LH Lyse', 'PATIENT_TEST', 'CBC+DIFF', 0.2, NULL, 'ML', 'BC-5150/BC-5000 Service Manual V3.0, Table 2-10 / 7.4', NULL, NOW(), 0),
    ('rate-bc5000-lh-lyse-cbc', 'mindray-bc-5000', 'M-52 LH Lyse', 'PATIENT_TEST', 'CBC', 0.2, NULL, 'ML', 'BC-5150/BC-5000 Service Manual V3.0, Table 2-10 / 7.4', NULL, NOW(), 0),
    ('rate-bc5000-startup-diluent', 'mindray-bc-5000', 'M-52D Diluent', 'STARTUP', NULL, 65.0, NULL, 'ML', 'BC-5150/BC-5000 Service Manual V3.0, Table 2-10 / 7.4', NULL, NOW(), 0),
    ('rate-bc5000-startup-diff', 'mindray-bc-5000', 'M-52 DIFF Lyse', 'STARTUP', NULL, 1.0, NULL, 'ML', 'BC-5150/BC-5000 Service Manual V3.0, Table 2-10 / 7.4', NULL, NOW(), 0),
    ('rate-bc5000-startup-lh', 'mindray-bc-5000', 'M-52 LH Lyse', 'STARTUP', NULL, 0.2, NULL, 'ML', 'BC-5150/BC-5000 Service Manual V3.0, Table 2-10 / 7.4', NULL, NOW(), 0),
    ('rate-bc5000-shutdown-diluent', 'mindray-bc-5000', 'M-52D Diluent', 'SHUTDOWN', NULL, 60.9, NULL, 'ML', 'BC-5150/BC-5000 Service Manual V3.0, Table 2-10 / 7.4', NULL, NOW(), 0),
    ('rate-bc5000-shutdown-diff', 'mindray-bc-5000', 'M-52 DIFF Lyse', 'SHUTDOWN', NULL, 1.0, NULL, 'ML', 'BC-5150/BC-5000 Service Manual V3.0, Table 2-10 / 7.4', NULL, NOW(), 0),
    ('rate-bc5000-shutdown-lh', 'mindray-bc-5000', 'M-52 LH Lyse', 'SHUTDOWN', NULL, 0.2, NULL, 'ML', 'BC-5150/BC-5000 Service Manual V3.0, Table 2-10 / 7.4', NULL, NOW(), 0),
    ('rate-bc5000-shutdown-cleanser', 'mindray-bc-5000', 'Probe Cleanser', 'SHUTDOWN', NULL, 2.0, NULL, 'ML', 'BC-5150/BC-5000 Service Manual V3.0, Table 2-10 / 7.4', NULL, NOW(), 0),
    ('rate-bc5000-standby-short-diluent', 'mindray-bc-5000', 'M-52D Diluent', 'STANDBY_EXIT_SHORT', NULL, 3.7, NULL, 'ML', 'BC-5150/BC-5000 Service Manual V3.0, Table 2-10 / 7.4', NULL, NOW(), 0),
    ('rate-bc5000-standby-medium-diluent', 'mindray-bc-5000', 'M-52D Diluent', 'STANDBY_EXIT_MEDIUM', NULL, 16.9, NULL, 'ML', 'BC-5150/BC-5000 Service Manual V3.0, Table 2-10 / 7.4', NULL, NOW(), 0),
    ('rate-bc5000-standby-long-diluent', 'mindray-bc-5000', 'M-52D Diluent', 'STANDBY_EXIT_LONG', NULL, 65.0, NULL, 'ML', 'BC-5150/BC-5000 Service Manual V3.0, Table 2-10 / 7.4', NULL, NOW(), 0),
    ('rate-bc5000-standby-long-diff', 'mindray-bc-5000', 'M-52 DIFF Lyse', 'STANDBY_EXIT_LONG', NULL, 3.0, NULL, 'ML', 'BC-5150/BC-5000 Service Manual V3.0, Table 2-10 / 7.4', NULL, NOW(), 0),
    ('rate-bc5000-standby-long-lh', 'mindray-bc-5000', 'M-52 LH Lyse', 'STANDBY_EXIT_LONG', NULL, 0.2, NULL, 'ML', 'BC-5150/BC-5000 Service Manual V3.0, Table 2-10 / 7.4', NULL, NOW(), 0),
    ('rate-mission-u500-strip', 'mission-u500', 'Mission Urinalysis Reagent Strip', 'PATIENT_TEST', NULL, NULL, 1, 'PIECE', 'Mission U500 product specifications', 'Дискретный расход: одна полоска на один тест.', NOW(), 0),
    ('rate-wondfo-ocg-102-cartridge', 'wondfo-ocg-102', 'OCG-102 Test Cartridge', 'PATIENT_TEST', NULL, NULL, 1, 'PIECE', 'Wondfo OCG-102 product specifications', 'Дискретный расход: один картридж на один тест.', NOW(), 0),
    ('rate-edan-i15-cartridge', 'edan-i15', 'Edan i15 Disposable Cartridge', 'PATIENT_TEST', NULL, NULL, 1, 'PIECE', 'Edan i15 product specifications', 'Дискретный расход: один картридж на один запуск панели.', NOW(), 0),
    ('rate-fluorecare-cartridge', 'fluorecare', 'Fluorecare Test Cartridge', 'PATIENT_TEST', NULL, NULL, 1, 'PIECE', 'Fluorecare product specifications', 'Дискретный расход: один тест-картридж на один параметр.', NOW(), 0),
    ('rate-iflash-cassette-position', 'iflash-1800', 'iFlash Cassette Position', 'PATIENT_TEST', NULL, NULL, 1, 'TEST_POSITION', 'iFlash 1800 product specifications', 'Одна кассетная позиция на один тест.', NOW(), 0),
    ('rate-iflash-reaction-cup', 'iflash-1800', 'iFlash Reaction Cup', 'PATIENT_TEST', NULL, NULL, 1, 'PIECE', 'iFlash 1800 product specifications', 'Одна реакционная кювета на один тест.', NOW(), 0),
    ('rate-iflash-sample-tip', 'iflash-1800', 'iFlash Sample Tip', 'PATIENT_TEST', NULL, NULL, 1, 'PIECE', 'iFlash 1800 product specifications', 'Один наконечник на один тест.', NOW(), 0)
ON CONFLICT (id) DO NOTHING;
