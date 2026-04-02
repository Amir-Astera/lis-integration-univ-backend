-- Migration: V13 - Mindray Reagent Seed Data
-- Purpose: Seed reagent rates for Mindray BS-series biochemistry analyzers
-- Source: reports/биохимия.md (Mindray reagent catalog)
-- Created: 2026-04-02
-- Note: Consumption rates based on official Mindray documentation

-- =============================================================================
-- Section 1: Mindray BS-series Analyzers
-- =============================================================================

INSERT INTO analyzers (id, name, type, workplace_name, lis_device_system_name, lis_analyzer_id, lis_device_name, serial_number, is_active, notes, created_at, updated_at, version) VALUES
    ('mindray-bs-120', 'Mindray BS-120', 'BIOCHEMISTRY', 'Биохимический отдел', 'BS120', 1001, 'Mindray BS-120', NULL, true, 'Компактный биохимический анализатор для малых и средних лабораторий', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('mindray-bs-200', 'Mindray BS-200', 'BIOCHEMISTRY', 'Биохимический отдел', 'BS200', 1002, 'Mindray BS-200', NULL, true, 'Биохимический анализатор средней производительности', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('mindray-bs-230', 'Mindray BS-230', 'BIOCHEMISTRY', 'Биохимический отдел', 'BS230', 1003, 'Mindray BS-230', NULL, true, 'Популярный биохимический анализатор для средних лабораторий', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('mindray-bs-240', 'Mindray BS-240', 'BIOCHEMISTRY', 'Биохимический отдел', 'BS240', 1004, 'Mindray BS-240', NULL, true, 'Биохимический анализатор с расширенным меню', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('mindray-bs-240pro', 'Mindray BS-240Pro', 'BIOCHEMISTRY', 'Биохимический отдел', 'BS240PRO', 1005, 'Mindray BS-240Pro', NULL, true, 'Профессиональная версия BS-240 с поддержкой ISE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('mindray-bs-430', 'Mindray BS-430', 'BIOCHEMISTRY', 'Биохимический отдел', 'BS430', 1006, 'Mindray BS-430', NULL, true, 'Высокопроизводительный биохимический анализатор', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('mindray-bs-600m', 'Mindray BS-600M', 'BIOCHEMISTRY', 'Биохимический отдел', 'BS600M', 1007, 'Mindray BS-600M', NULL, true, 'Флагманский биохимический анализатор Mindray', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Section 2: Basic Biochemistry Reagents - Patient Test Rates
-- =============================================================================

-- ALT (Аланинаминотрансфераза) - R1:4×35mL + R2:2×18mL = 176mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs120-alt-test', 'mindray-bs-120', 'Mindray ALT (Аланинаминотрансфераза)', 'PATIENT_TEST', 'STANDARD', 0.30, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 600 tests max on BS-200', CURRENT_TIMESTAMP, 0),
    ('bs200-alt-test', 'mindray-bs-200', 'Mindray ALT (Аланинаминотрансфераза)', 'PATIENT_TEST', 'STANDARD', 0.30, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 600 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-alt-test', 'mindray-bs-230', 'Mindray ALT (Аланинаминотрансфераза)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 942 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-alt-test', 'mindray-bs-240', 'Mindray ALT (Аланинаминотрансфераза)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 942 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-alt-test', 'mindray-bs-240pro', 'Mindray ALT (Аланинаминотрансфераза)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 942 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-alt-test', 'mindray-bs-430', 'Mindray ALT (Аланинаминотрансфераза)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 942 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-alt-test', 'mindray-bs-600m', 'Mindray ALT (Аланинаминотрансфераза)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 942 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ALB (Альбумин) - R:4×40mL = 160mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs120-alb-test', 'mindray-bs-120', 'Mindray ALB (Альбумин)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs200-alb-test', 'mindray-bs-200', 'Mindray ALB (Альбумин)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-alb-test', 'mindray-bs-230', 'Mindray ALB (Альбумин)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-alb-test', 'mindray-bs-240', 'Mindray ALB (Альбумин)', 'PATIENT_TEST', 'STANDARD', 0.17, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 964 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-alb-test', 'mindray-bs-240pro', 'Mindray ALB (Альбумин)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-alb-test', 'mindray-bs-430', 'Mindray ALB (Альбумин)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-alb-test', 'mindray-bs-600m', 'Mindray ALB (Альбумин)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- α-AMY (Альфа-амилаза) - R1:1×38mL + R2:1×10mL = 48mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs120-amy-test', 'mindray-bs-120', 'Mindray α-AMY (Альфа-амилаза)', 'PATIENT_TEST', 'STANDARD', 0.31, NULL, 'ML', 'reports/биохимия.md', 'R1:1×38mL+R2:1×10mL, 154 tests max', CURRENT_TIMESTAMP, 0),
    ('bs200-amy-test', 'mindray-bs-200', 'Mindray α-AMY (Альфа-амилаза)', 'PATIENT_TEST', 'STANDARD', 0.20, NULL, 'ML', 'reports/биохимия.md', 'R1:1×38mL+R2:1×10mL, 242 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-amy-test', 'mindray-bs-230', 'Mindray α-AMY (Альфа-амилаза)', 'PATIENT_TEST', 'STANDARD', 0.20, NULL, 'ML', 'reports/биохимия.md', 'R1:1×38mL+R2:1×10mL, 242 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-amy-test', 'mindray-bs-240', 'Mindray α-AMY (Альфа-амилаза)', 'PATIENT_TEST', 'STANDARD', 0.20, NULL, 'ML', 'reports/биохимия.md', 'R1:1×38mL+R2:1×10mL, 242 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-amy-test', 'mindray-bs-240pro', 'Mindray α-AMY (Альфа-амилаза)', 'PATIENT_TEST', 'STANDARD', 0.20, NULL, 'ML', 'reports/биохимия.md', 'R1:1×38mL+R2:1×10mL, 242 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-amy-test', 'mindray-bs-430', 'Mindray α-AMY (Альфа-амилаза)', 'PATIENT_TEST', 'STANDARD', 0.20, NULL, 'ML', 'reports/биохимия.md', 'R1:1×38mL+R2:1×10mL, 242 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-amy-test', 'mindray-bs-600m', 'Mindray α-AMY (Альфа-амилаза)', 'PATIENT_TEST', 'STANDARD', 0.20, NULL, 'ML', 'reports/биохимия.md', 'R1:1×38mL+R2:1×10mL, 242 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- AST (Аспартатаминотрансфераза) - R1:4×35mL + R2:2×18mL = 176mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs120-ast-test', 'mindray-bs-120', 'Mindray AST (Аспартатаминотрансфераза)', 'PATIENT_TEST', 'STANDARD', 0.30, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 600 tests max', CURRENT_TIMESTAMP, 0),
    ('bs200-ast-test', 'mindray-bs-200', 'Mindray AST (Аспартатаминотрансфераза)', 'PATIENT_TEST', 'STANDARD', 0.30, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 600 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-ast-test', 'mindray-bs-230', 'Mindray AST (Аспартатаминотрансфераза)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 942 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-ast-test', 'mindray-bs-240', 'Mindray AST (Аспартатаминотрансфераза)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 942 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-ast-test', 'mindray-bs-240pro', 'Mindray AST (Аспартатаминотрансфераза)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 942 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-ast-test', 'mindray-bs-430', 'Mindray AST (Аспартатаминотрансфераза)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 942 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-ast-test', 'mindray-bs-600m', 'Mindray AST (Аспартатаминотрансфераза)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 942 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- GGT (Гамма-глутамилтрансфераза) - R1:4×35mL + R2:2×18mL = 176mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs120-ggt-test', 'mindray-bs-120', 'Mindray GGT (Гамма-глутамилтрансфераза)', 'PATIENT_TEST', 'STANDARD', 0.30, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 600 tests max', CURRENT_TIMESTAMP, 0),
    ('bs200-ggt-test', 'mindray-bs-200', 'Mindray GGT (Гамма-глутамилтрансфераза)', 'PATIENT_TEST', 'STANDARD', 0.30, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 600 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-ggt-test', 'mindray-bs-230', 'Mindray GGT (Гамма-глутамилтрансфераза)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 942 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-ggt-test', 'mindray-bs-240', 'Mindray GGT (Гамма-глутамилтрансфераза)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 942 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-ggt-test', 'mindray-bs-240pro', 'Mindray GGT (Гамма-глутамилтрансфераза)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 942 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-ggt-test', 'mindray-bs-430', 'Mindray GGT (Гамма-глутамилтрансфераза)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 942 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-ggt-test', 'mindray-bs-600m', 'Mindray GGT (Гамма-глутамилтрансфераза)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 942 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- Glu-G (Глюкоза GOD) - R1:4×40mL + R2:2×20mL = 200mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs120-glu-test', 'mindray-bs-120', 'Mindray Glu-G (Глюкоза GOD)', 'PATIENT_TEST', 'STANDARD', 0.35, NULL, 'ML', 'reports/биохимия.md', 'R1:4×40mL+R2:2×20mL, 569 tests max', CURRENT_TIMESTAMP, 0),
    ('bs200-glu-test', 'mindray-bs-200', 'Mindray Glu-G (Глюкоза GOD)', 'PATIENT_TEST', 'STANDARD', 0.24, NULL, 'ML', 'reports/биохимия.md', 'R1:4×40mL+R2:2×20mL, 822 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-glu-test', 'mindray-bs-230', 'Mindray Glu-G (Глюкоза GOD)', 'PATIENT_TEST', 'STANDARD', 0.24, NULL, 'ML', 'reports/биохимия.md', 'R1:4×40mL+R2:2×20mL, 822 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-glu-test', 'mindray-bs-240', 'Mindray Glu-G (Глюкоза GOD)', 'PATIENT_TEST', 'STANDARD', 0.24, NULL, 'ML', 'reports/биохимия.md', 'R1:4×40mL+R2:2×20mL, 822 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-glu-test', 'mindray-bs-240pro', 'Mindray Glu-G (Глюкоза GOD)', 'PATIENT_TEST', 'STANDARD', 0.24, NULL, 'ML', 'reports/биохимия.md', 'R1:4×40mL+R2:2×20mL, 822 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-glu-test', 'mindray-bs-430', 'Mindray Glu-G (Глюкоза GOD)', 'PATIENT_TEST', 'STANDARD', 0.24, NULL, 'ML', 'reports/биохимия.md', 'R1:4×40mL+R2:2×20mL, 822 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-glu-test', 'mindray-bs-600m', 'Mindray Glu-G (Глюкоза GOD)', 'PATIENT_TEST', 'STANDARD', 0.24, NULL, 'ML', 'reports/биохимия.md', 'R1:4×40mL+R2:2×20mL, 822 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- Fe (Железо) - R1:2×40mL + R2:1×16mL + Calibrator:1×1.5mL = 96mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs120-fe-test', 'mindray-bs-120', 'Mindray Fe (Железо колориметрическое)', 'PATIENT_TEST', 'STANDARD', 0.20, NULL, 'ML', 'reports/биохимия.md', 'R1:2×40mL+R2:1×16mL+Cal:1×1.5mL, 483 tests max', CURRENT_TIMESTAMP, 0),
    ('bs200-fe-test', 'mindray-bs-200', 'Mindray Fe (Железо колориметрическое)', 'PATIENT_TEST', 'STANDARD', 0.20, NULL, 'ML', 'reports/биохимия.md', 'R1:2×40mL+R2:1×16mL+Cal:1×1.5mL, 483 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-fe-test', 'mindray-bs-230', 'Mindray Fe (Железо колориметрическое)', 'PATIENT_TEST', 'STANDARD', 0.20, NULL, 'ML', 'reports/биохимия.md', 'R1:2×40mL+R2:1×16mL+Cal:1×1.5mL, 483 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-fe-test', 'mindray-bs-240', 'Mindray Fe (Железо колориметрическое)', 'PATIENT_TEST', 'STANDARD', 0.20, NULL, 'ML', 'reports/биохимия.md', 'R1:2×40mL+R2:1×16mL+Cal:1×1.5mL, 483 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-fe-test', 'mindray-bs-240pro', 'Mindray Fe (Железо колориметрическое)', 'PATIENT_TEST', 'STANDARD', 0.20, NULL, 'ML', 'reports/биохимия.md', 'R1:2×40mL+R2:1×16mL+Cal:1×1.5mL, 483 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-fe-test', 'mindray-bs-430', 'Mindray Fe (Железо колориметрическое)', 'PATIENT_TEST', 'STANDARD', 0.20, NULL, 'ML', 'reports/биохимия.md', 'R1:2×40mL+R2:1×16mL+Cal:1×1.5mL, 483 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-fe-test', 'mindray-bs-600m', 'Mindray Fe (Железо колориметрическое)', 'PATIENT_TEST', 'STANDARD', 0.20, NULL, 'ML', 'reports/биохимия.md', 'R1:2×40mL+R2:1×16mL+Cal:1×1.5mL, 483 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- Ca (Кальций) - R:4×40mL = 160mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs120-ca-test', 'mindray-bs-120', 'Mindray Ca (Кальций)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs200-ca-test', 'mindray-bs-200', 'Mindray Ca (Кальций)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-ca-test', 'mindray-bs-230', 'Mindray Ca (Кальций)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-ca-test', 'mindray-bs-240', 'Mindray Ca (Кальций)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-ca-test', 'mindray-bs-240pro', 'Mindray Ca (Кальций)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-ca-test', 'mindray-bs-430', 'Mindray Ca (Кальций)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-ca-test', 'mindray-bs-600m', 'Mindray Ca (Кальций)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- CREA-S (Креатинин) - R1:2×27mL + R2:1×18mL = 72mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs120-crea-test', 'mindray-bs-120', 'Mindray CREA-S (Креатинин)', 'PATIENT_TEST', 'STANDARD', 0.28, NULL, 'ML', 'reports/биохимия.md', 'R1:2×27mL+R2:1×18mL, 253 tests max', CURRENT_TIMESTAMP, 0),
    ('bs200-crea-test', 'mindray-bs-200', 'Mindray CREA-S (Креатинин)', 'PATIENT_TEST', 'STANDARD', 0.20, NULL, 'ML', 'reports/биохимия.md', 'R1:2×27mL+R2:1×18mL, 366 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-crea-test', 'mindray-bs-230', 'Mindray CREA-S (Креатинин)', 'PATIENT_TEST', 'STANDARD', 0.18, NULL, 'ML', 'reports/биохимия.md', 'R1:2×27mL+R2:1×18mL, 392 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-crea-test', 'mindray-bs-240', 'Mindray CREA-S (Креатинин)', 'PATIENT_TEST', 'STANDARD', 0.20, NULL, 'ML', 'reports/биохимия.md', 'R1:2×27mL+R2:1×18mL, 366 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-crea-test', 'mindray-bs-240pro', 'Mindray CREA-S (Креатинин)', 'PATIENT_TEST', 'STANDARD', 0.20, NULL, 'ML', 'reports/биохимия.md', 'R1:2×27mL+R2:1×18mL, 366 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-crea-test', 'mindray-bs-430', 'Mindray CREA-S (Креатинин)', 'PATIENT_TEST', 'STANDARD', 0.20, NULL, 'ML', 'reports/биохимия.md', 'R1:2×27mL+R2:1×18mL, 366 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-crea-test', 'mindray-bs-600m', 'Mindray CREA-S (Креатинин)', 'PATIENT_TEST', 'STANDARD', 0.20, NULL, 'ML', 'reports/биохимия.md', 'R1:2×27mL+R2:1×18mL, 366 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- CK (Креатинкиназа) - R1:2×35mL + R2:1×18mL = 88mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs120-ck-test', 'mindray-bs-120', 'Mindray CK (Креатинкиназа)', 'PATIENT_TEST', 'STANDARD', 0.29, NULL, 'ML', 'reports/биохимия.md', 'R1:2×35mL+R2:1×18mL, 300 tests max', CURRENT_TIMESTAMP, 0),
    ('bs200-ck-test', 'mindray-bs-200', 'Mindray CK (Креатинкиназа)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:2×35mL+R2:1×18mL, 471 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-ck-test', 'mindray-bs-230', 'Mindray CK (Креатинкиназа)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:2×35mL+R2:1×18mL, 471 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-ck-test', 'mindray-bs-240', 'Mindray CK (Креатинкиназа)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:2×35mL+R2:1×18mL, 471 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-ck-test', 'mindray-bs-240pro', 'Mindray CK (Креатинкиназа)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:2×35mL+R2:1×18mL, 471 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-ck-test', 'mindray-bs-430', 'Mindray CK (Креатинкиназа)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:2×35mL+R2:1×18mL, 471 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-ck-test', 'mindray-bs-600m', 'Mindray CK (Креатинкиназа)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:2×35mL+R2:1×18mL, 471 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- LDH (Лактатдегидрогеназа) - R1:4×35mL + R2:2×18mL = 176mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs120-ldh-test', 'mindray-bs-120', 'Mindray LDH (Лактатдегидрогеназа)', 'PATIENT_TEST', 'STANDARD', 0.28, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 622 tests max', CURRENT_TIMESTAMP, 0),
    ('bs200-ldh-test', 'mindray-bs-200', 'Mindray LDH (Лактатдегидрогеназа)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 942 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-ldh-test', 'mindray-bs-230', 'Mindray LDH (Лактатдегидрогеназа)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 942 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-ldh-test', 'mindray-bs-240', 'Mindray LDH (Лактатдегидрогеназа)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 942 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-ldh-test', 'mindray-bs-240pro', 'Mindray LDH (Лактатдегидрогеназа)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 942 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-ldh-test', 'mindray-bs-430', 'Mindray LDH (Лактатдегидрогеназа)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 942 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-ldh-test', 'mindray-bs-600m', 'Mindray LDH (Лактатдегидрогеназа)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 942 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- Mg (Магний) - R:4×40mL = 160mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs120-mg-test', 'mindray-bs-120', 'Mindray Mg (Магний)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs200-mg-test', 'mindray-bs-200', 'Mindray Mg (Магний)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-mg-test', 'mindray-bs-230', 'Mindray Mg (Магний)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-mg-test', 'mindray-bs-240', 'Mindray Mg (Магний)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-mg-test', 'mindray-bs-240pro', 'Mindray Mg (Магний)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-mg-test', 'mindray-bs-430', 'Mindray Mg (Магний)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-mg-test', 'mindray-bs-600m', 'Mindray Mg (Магний)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- UA (Мочевая кислота) - R1:4×40mL + R2:2×20mL = 200mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs120-ua-test', 'mindray-bs-120', 'Mindray UA (Мочевая кислота)', 'PATIENT_TEST', 'STANDARD', 0.32, NULL, 'ML', 'reports/биохимия.md', 'R1:4×40mL+R2:2×20mL, 622 tests max', CURRENT_TIMESTAMP, 0),
    ('bs200-ua-test', 'mindray-bs-200', 'Mindray UA (Мочевая кислота)', 'PATIENT_TEST', 'STANDARD', 0.25, NULL, 'ML', 'reports/биохимия.md', 'R1:4×40mL+R2:2×20mL, 804 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-ua-test', 'mindray-bs-230', 'Mindray UA (Мочевая кислота)', 'PATIENT_TEST', 'STANDARD', 0.25, NULL, 'ML', 'reports/биохимия.md', 'R1:4×40mL+R2:2×20mL, 804 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-ua-test', 'mindray-bs-240', 'Mindray UA (Мочевая кислота)', 'PATIENT_TEST', 'STANDARD', 0.25, NULL, 'ML', 'reports/биохимия.md', 'R1:4×40mL+R2:2×20mL, 804 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-ua-test', 'mindray-bs-240pro', 'Mindray UA (Мочевая кислота)', 'PATIENT_TEST', 'STANDARD', 0.25, NULL, 'ML', 'reports/биохимия.md', 'R1:4×40mL+R2:2×20mL, 804 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-ua-test', 'mindray-bs-430', 'Mindray UA (Мочевая кислота)', 'PATIENT_TEST', 'STANDARD', 0.25, NULL, 'ML', 'reports/биохимия.md', 'R1:4×40mL+R2:2×20mL, 804 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-ua-test', 'mindray-bs-600m', 'Mindray UA (Мочевая кислота)', 'PATIENT_TEST', 'STANDARD', 0.25, NULL, 'ML', 'reports/биохимия.md', 'R1:4×40mL+R2:2×20mL, 804 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- UREA (Мочевина) - R1:4×35mL + R2:2×18mL = 176mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs120-urea-test', 'mindray-bs-120', 'Mindray UREA (Мочевина)', 'PATIENT_TEST', 'STANDARD', 0.29, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 600 tests max', CURRENT_TIMESTAMP, 0),
    ('bs200-urea-test', 'mindray-bs-200', 'Mindray UREA (Мочевина)', 'PATIENT_TEST', 'STANDARD', 0.29, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 600 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-urea-test', 'mindray-bs-230', 'Mindray UREA (Мочевина)', 'PATIENT_TEST', 'STANDARD', 0.29, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 600 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-urea-test', 'mindray-bs-240', 'Mindray UREA (Мочевина)', 'PATIENT_TEST', 'STANDARD', 0.29, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 600 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-urea-test', 'mindray-bs-240pro', 'Mindray UREA (Мочевина)', 'PATIENT_TEST', 'STANDARD', 0.29, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 600 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-urea-test', 'mindray-bs-430', 'Mindray UREA (Мочевина)', 'PATIENT_TEST', 'STANDARD', 0.29, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 600 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-urea-test', 'mindray-bs-600m', 'Mindray UREA (Мочевина)', 'PATIENT_TEST', 'STANDARD', 0.29, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 600 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- TP (Общий белок) - R:4×40mL = 160mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs120-tp-test', 'mindray-bs-120', 'Mindray TP (Общий белок)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 732 tests max', CURRENT_TIMESTAMP, 0),
    ('bs200-tp-test', 'mindray-bs-200', 'Mindray TP (Общий белок)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 732 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-tp-test', 'mindray-bs-230', 'Mindray TP (Общий белок)', 'PATIENT_TEST', 'STANDARD', 0.11, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 1428 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-tp-test', 'mindray-bs-240', 'Mindray TP (Общий белок)', 'PATIENT_TEST', 'STANDARD', 0.11, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 1428 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-tp-test', 'mindray-bs-240pro', 'Mindray TP (Общий белок)', 'PATIENT_TEST', 'STANDARD', 0.11, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 1428 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-tp-test', 'mindray-bs-430', 'Mindray TP (Общий белок)', 'PATIENT_TEST', 'STANDARD', 0.11, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 1428 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-tp-test', 'mindray-bs-600m', 'Mindray TP (Общий белок)', 'PATIENT_TEST', 'STANDARD', 0.11, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 1428 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- Bil-T (Билирубин общий VOX) - R1:4×35mL + R2:2×18mL = 176mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs120-bilt-test', 'mindray-bs-120', 'Mindray Bil-T (Билирубин общий VOX)', 'PATIENT_TEST', 'STANDARD', 0.29, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 610 tests max', CURRENT_TIMESTAMP, 0),
    ('bs200-bilt-test', 'mindray-bs-200', 'Mindray Bil-T (Билирубин общий VOX)', 'PATIENT_TEST', 'STANDARD', 0.21, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 824 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-bilt-test', 'mindray-bs-230', 'Mindray Bil-T (Билирубин общий VOX)', 'PATIENT_TEST', 'STANDARD', 0.21, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 824 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-bilt-test', 'mindray-bs-240', 'Mindray Bil-T (Билирубин общий VOX)', 'PATIENT_TEST', 'STANDARD', 0.21, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 824 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-bilt-test', 'mindray-bs-240pro', 'Mindray Bil-T (Билирубин общий VOX)', 'PATIENT_TEST', 'STANDARD', 0.21, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 824 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-bilt-test', 'mindray-bs-430', 'Mindray Bil-T (Билирубин общий VOX)', 'PATIENT_TEST', 'STANDARD', 0.21, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 824 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-bilt-test', 'mindray-bs-600m', 'Mindray Bil-T (Билирубин общий VOX)', 'PATIENT_TEST', 'STANDARD', 0.21, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 824 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- Bil-D (Билирубин прямой VOX) - R1:4×35mL + R2:2×18mL = 176mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs120-bild-test', 'mindray-bs-120', 'Mindray Bil-D (Билирубин прямой VOX)', 'PATIENT_TEST', 'STANDARD', 0.29, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 610 tests max', CURRENT_TIMESTAMP, 0),
    ('bs200-bild-test', 'mindray-bs-200', 'Mindray Bil-D (Билирубин прямой VOX)', 'PATIENT_TEST', 'STANDARD', 0.21, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 824 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-bild-test', 'mindray-bs-230', 'Mindray Bil-D (Билирубин прямой VOX)', 'PATIENT_TEST', 'STANDARD', 0.21, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 824 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-bild-test', 'mindray-bs-240', 'Mindray Bil-D (Билирубин прямой VOX)', 'PATIENT_TEST', 'STANDARD', 0.21, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 824 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-bild-test', 'mindray-bs-240pro', 'Mindray Bil-D (Билирубин прямой VOX)', 'PATIENT_TEST', 'STANDARD', 0.21, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 824 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-bild-test', 'mindray-bs-430', 'Mindray Bil-D (Билирубин прямой VOX)', 'PATIENT_TEST', 'STANDARD', 0.21, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 824 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-bild-test', 'mindray-bs-600m', 'Mindray Bil-D (Билирубин прямой VOX)', 'PATIENT_TEST', 'STANDARD', 0.21, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 824 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- TC (Общий холестерин) - R:4×40mL = 160mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs120-tc-test', 'mindray-bs-120', 'Mindray TC (Общий холестерин)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs200-tc-test', 'mindray-bs-200', 'Mindray TC (Общий холестерин)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-tc-test', 'mindray-bs-230', 'Mindray TC (Общий холестерин)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-tc-test', 'mindray-bs-240', 'Mindray TC (Общий холестерин)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-tc-test', 'mindray-bs-240pro', 'Mindray TC (Общий холестерин)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-tc-test', 'mindray-bs-430', 'Mindray TC (Общий холестерин)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-tc-test', 'mindray-bs-600m', 'Mindray TC (Общий холестерин)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- TG (Триглицериды) - R:4×40mL = 160mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs120-tg-test', 'mindray-bs-120', 'Mindray TG (Триглицериды)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs200-tg-test', 'mindray-bs-200', 'Mindray TG (Триглицериды)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-tg-test', 'mindray-bs-230', 'Mindray TG (Триглицериды)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-tg-test', 'mindray-bs-240', 'Mindray TG (Триглицериды)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-tg-test', 'mindray-bs-240pro', 'Mindray TG (Триглицериды)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-tg-test', 'mindray-bs-430', 'Mindray TG (Триглицериды)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-tg-test', 'mindray-bs-600m', 'Mindray TG (Триглицериды)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- P (Фосфор) - R:4×40mL = 160mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs120-p-test', 'mindray-bs-120', 'Mindray P (Фосфор)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs200-p-test', 'mindray-bs-200', 'Mindray P (Фосфор)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-p-test', 'mindray-bs-230', 'Mindray P (Фосфор)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-p-test', 'mindray-bs-240', 'Mindray P (Фосфор)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-p-test', 'mindray-bs-240pro', 'Mindray P (Фосфор)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-p-test', 'mindray-bs-430', 'Mindray P (Фосфор)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-p-test', 'mindray-bs-600m', 'Mindray P (Фосфор)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R:4×40mL, 728 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ALP (Щелочная фосфотаза) - R1:4×35mL + R2:2×18mL = 176mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs120-alp-test', 'mindray-bs-120', 'Mindray ALP (Щелочная фосфотаза)', 'PATIENT_TEST', 'STANDARD', 0.30, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 600 tests max', CURRENT_TIMESTAMP, 0),
    ('bs200-alp-test', 'mindray-bs-200', 'Mindray ALP (Щелочная фосфотаза)', 'PATIENT_TEST', 'STANDARD', 0.30, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 600 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-alp-test', 'mindray-bs-230', 'Mindray ALP (Щелочная фосфотаза)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 942 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-alp-test', 'mindray-bs-240', 'Mindray ALP (Щелочная фосфотаза)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 942 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-alp-test', 'mindray-bs-240pro', 'Mindray ALP (Щелочная фосфотаза)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 942 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-alp-test', 'mindray-bs-430', 'Mindray ALP (Щелочная фосфотаза)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 942 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-alp-test', 'mindray-bs-600m', 'Mindray ALP (Щелочная фосфотаза)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 942 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- α-HBDH (Альфа-Гидроксибутират Дегидрогеназа) - R1:4×35mL + R2:2×18mL = 176mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs120-hbdh-test', 'mindray-bs-120', 'Mindray α-HBDH (Альфа-Гидроксибутират Дегидрогеназа)', 'PATIENT_TEST', 'STANDARD', 0.35, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 508 tests max', CURRENT_TIMESTAMP, 0),
    ('bs200-hbdh-test', 'mindray-bs-200', 'Mindray α-HBDH (Альфа-Гидроксибутират Дегидрогеназа)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 942 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-hbdh-test', 'mindray-bs-230', 'Mindray α-HBDH (Альфа-Гидроксибутират Дегидрогеназа)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 942 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-hbdh-test', 'mindray-bs-240', 'Mindray α-HBDH (Альфа-Гидроксибутират Дегидрогеназа)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 942 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-hbdh-test', 'mindray-bs-240pro', 'Mindray α-HBDH (Альфа-Гидроксибутират Дегидрогеназа)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 942 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-hbdh-test', 'mindray-bs-430', 'Mindray α-HBDH (Альфа-Гидроксибутират Дегидрогеназа)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 942 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-hbdh-test', 'mindray-bs-600m', 'Mindray α-HBDH (Альфа-Гидроксибутират Дегидрогеназа)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:4×35mL+R2:2×18mL, 942 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Section 3: Service Operations (Startup, Shutdown, QC)
-- =============================================================================

-- Daily startup wash for all BS series
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs120-startup', 'mindray-bs-120', 'Mindray CD80 Detergent (Моющий раствор)', 'STARTUP', 'DAILY', 15.0, NULL, 'ML', 'reports/биохимия.md', 'Ежедневная промывка при старте, CD80 1L', CURRENT_TIMESTAMP, 0),
    ('bs200-startup', 'mindray-bs-200', 'Mindray CD80 Detergent (Моющий раствор)', 'STARTUP', 'DAILY', 15.0, NULL, 'ML', 'reports/биохимия.md', 'Ежедневная промывка при старте, CD80 1L', CURRENT_TIMESTAMP, 0),
    ('bs230-startup', 'mindray-bs-230', 'Mindray CD80 Detergent (Моющий раствор)', 'STARTUP', 'DAILY', 15.0, NULL, 'ML', 'reports/биохимия.md', 'Ежедневная промывка при старте, CD80 1L', CURRENT_TIMESTAMP, 0),
    ('bs240-startup', 'mindray-bs-240', 'Mindray CD80 Detergent (Моющий раствор)', 'STARTUP', 'DAILY', 15.0, NULL, 'ML', 'reports/биохимия.md', 'Ежедневная промывка при старте, CD80 1L', CURRENT_TIMESTAMP, 0),
    ('bs240pro-startup', 'mindray-bs-240pro', 'Mindray CD80 Detergent (Моющий раствор)', 'STARTUP', 'DAILY', 15.0, NULL, 'ML', 'reports/биохимия.md', 'Ежедневная промывка при старте, CD80 1L', CURRENT_TIMESTAMP, 0),
    ('bs430-startup', 'mindray-bs-430', 'Mindray CD80 Detergent (Моющий раствор)', 'STARTUP', 'DAILY', 15.0, NULL, 'ML', 'reports/биохимия.md', 'Ежедневная промывка при старте, CD80 1L', CURRENT_TIMESTAMP, 0),
    ('bs600m-startup', 'mindray-bs-600m', 'Mindray CD80 Detergent (Моющий раствор)', 'STARTUP', 'DAILY', 15.0, NULL, 'ML', 'reports/биохимия.md', 'Ежедневная промывка при старте, CD80 1L', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- Daily shutdown wash
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs120-shutdown', 'mindray-bs-120', 'Mindray CD80 Detergent (Моющий раствор)', 'SHUTDOWN', 'DAILY', 20.0, NULL, 'ML', 'reports/биохимия.md', 'Ежедневная промывка при выключении', CURRENT_TIMESTAMP, 0),
    ('bs200-shutdown', 'mindray-bs-200', 'Mindray CD80 Detergent (Моющий раствор)', 'SHUTDOWN', 'DAILY', 20.0, NULL, 'ML', 'reports/биохимия.md', 'Ежедневная промывка при выключении', CURRENT_TIMESTAMP, 0),
    ('bs230-shutdown', 'mindray-bs-230', 'Mindray CD80 Detergent (Моющий раствор)', 'SHUTDOWN', 'DAILY', 20.0, NULL, 'ML', 'reports/биохимия.md', 'Ежедневная промывка при выключении', CURRENT_TIMESTAMP, 0),
    ('bs240-shutdown', 'mindray-bs-240', 'Mindray CD80 Detergent (Моющий раствор)', 'SHUTDOWN', 'DAILY', 20.0, NULL, 'ML', 'reports/биохимия.md', 'Ежедневная промывка при выключении', CURRENT_TIMESTAMP, 0),
    ('bs240pro-shutdown', 'mindray-bs-240pro', 'Mindray CD80 Detergent (Моющий раствор)', 'SHUTDOWN', 'DAILY', 20.0, NULL, 'ML', 'reports/биохимия.md', 'Ежедневная промывка при выключении', CURRENT_TIMESTAMP, 0),
    ('bs430-shutdown', 'mindray-bs-430', 'Mindray CD80 Detergent (Моющий раствор)', 'SHUTDOWN', 'DAILY', 20.0, NULL, 'ML', 'reports/биохимия.md', 'Ежедневная промывка при выключении', CURRENT_TIMESTAMP, 0),
    ('bs600m-shutdown', 'mindray-bs-600m', 'Mindray CD80 Detergent (Моющий раствор)', 'SHUTDOWN', 'DAILY', 20.0, NULL, 'ML', 'reports/биохимия.md', 'Ежедневная промывка при выключении', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- Weekly enhanced cleaning
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs120-enhanced', 'mindray-bs-120', 'Mindray CD80 Detergent (Моющий раствор)', 'ENHANCED_CLEAN', 'WEEKLY', 50.0, NULL, 'ML', 'reports/биохимия.md', 'Усиленная промывка еженедельно', CURRENT_TIMESTAMP, 0),
    ('bs200-enhanced', 'mindray-bs-200', 'Mindray CD80 Detergent (Моющий раствор)', 'ENHANCED_CLEAN', 'WEEKLY', 50.0, NULL, 'ML', 'reports/биохимия.md', 'Усиленная промывка еженедельно', CURRENT_TIMESTAMP, 0),
    ('bs230-enhanced', 'mindray-bs-230', 'Mindray CD80 Detergent (Моющий раствор)', 'ENHANCED_CLEAN', 'WEEKLY', 50.0, NULL, 'ML', 'reports/биохимия.md', 'Усиленная промывка еженедельно', CURRENT_TIMESTAMP, 0),
    ('bs240-enhanced', 'mindray-bs-240', 'Mindray CD80 Detergent (Моющий раствор)', 'ENHANCED_CLEAN', 'WEEKLY', 50.0, NULL, 'ML', 'reports/биохимия.md', 'Усиленная промывка еженедельно', CURRENT_TIMESTAMP, 0),
    ('bs240pro-enhanced', 'mindray-bs-240pro', 'Mindray CD80 Detergent (Моющий раствор)', 'ENHANCED_CLEAN', 'WEEKLY', 50.0, NULL, 'ML', 'reports/биохимия.md', 'Усиленная промывка еженедельно', CURRENT_TIMESTAMP, 0),
    ('bs430-enhanced', 'mindray-bs-430', 'Mindray CD80 Detergent (Моющий раствор)', 'ENHANCED_CLEAN', 'WEEKLY', 50.0, NULL, 'ML', 'reports/биохимия.md', 'Усиленная промывка еженедельно', CURRENT_TIMESTAMP, 0),
    ('bs600m-enhanced', 'mindray-bs-600m', 'Mindray CD80 Detergent (Моющий раствор)', 'ENHANCED_CLEAN', 'WEEKLY', 50.0, NULL, 'ML', 'reports/биохимия.md', 'Усиленная промывка еженедельно', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- Cuvettes consumable
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs230-cuvettes', 'mindray-bs-230', 'Mindray Cuvettes For BS-230 (Кюветы)', 'PATIENT_TEST', 'STANDARD', NULL, 1, 'PIECE', 'reports/биохимия.md', 'Одноразовые кюветы для BS-230', CURRENT_TIMESTAMP, 0),
    ('bs240-cuvettes', 'mindray-bs-240', 'Mindray Cuvettes For BS-240 (Кюветы)', 'PATIENT_TEST', 'STANDARD', NULL, 1, 'PIECE', 'reports/биохимия.md', 'Одноразовые кюветы для BS-240', CURRENT_TIMESTAMP, 0),
    ('bs240pro-cuvettes', 'mindray-bs-240pro', 'Mindray Cuvettes For BS-240Pro (Кюветы)', 'PATIENT_TEST', 'STANDARD', NULL, 1, 'PIECE', 'reports/биохимия.md', 'Одноразовые кюветы для BS-240Pro', CURRENT_TIMESTAMP, 0),
    ('bs430-cuvettes', 'mindray-bs-430', 'Mindray Cuvettes For BS-430 (Кюветы)', 'PATIENT_TEST', 'STANDARD', NULL, 1, 'PIECE', 'reports/биохимия.md', 'Одноразовые кюветы для BS-430', CURRENT_TIMESTAMP, 0),
    ('bs600m-cuvettes', 'mindray-bs-600m', 'Mindray Cuvettes For BS-600M (Кюветы)', 'PATIENT_TEST', 'STANDARD', NULL, 1, 'PIECE', 'reports/биохимия.md', 'Одноразовые кюветы для BS-600M', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Section 4: Lipid Panel (HDL-C, LDL-C, ApoA1, ApoB, Lp(a))
-- =============================================================================

-- HDL-C (Холестерин ЛПВП) - R1:1×40mL + R2:1×14mL = 54mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs200-hdl-test', 'mindray-bs-200', 'Mindray HDL-C (Холестерин ЛПВП)', 'PATIENT_TEST', 'STANDARD', 0.24, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×14mL, 227 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-hdl-test', 'mindray-bs-230', 'Mindray HDL-C (Холестерин ЛПВП)', 'PATIENT_TEST', 'STANDARD', 0.24, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×14mL, 227 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-hdl-test', 'mindray-bs-240', 'Mindray HDL-C (Холестерин ЛПВП)', 'PATIENT_TEST', 'STANDARD', 0.24, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×14mL, 227 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-hdl-test', 'mindray-bs-240pro', 'Mindray HDL-C (Холестерин ЛПВП)', 'PATIENT_TEST', 'STANDARD', 0.24, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×14mL, 227 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-hdl-test', 'mindray-bs-430', 'Mindray HDL-C (Холестерин ЛПВП)', 'PATIENT_TEST', 'STANDARD', 0.24, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×14mL, 227 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-hdl-test', 'mindray-bs-600m', 'Mindray HDL-C (Холестерин ЛПВП)', 'PATIENT_TEST', 'STANDARD', 0.24, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×14mL, 227 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- LDL-C (Холестерин ЛПНП) - R1:1×40mL + R2:1×14mL = 54mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs200-ldl-test', 'mindray-bs-200', 'Mindray LDL-C (Холестерин ЛПНП)', 'PATIENT_TEST', 'STANDARD', 0.24, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×14mL, 227 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-ldl-test', 'mindray-bs-230', 'Mindray LDL-C (Холестерин ЛПНП)', 'PATIENT_TEST', 'STANDARD', 0.24, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×14mL, 227 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-ldl-test', 'mindray-bs-240', 'Mindray LDL-C (Холестерин ЛПНП)', 'PATIENT_TEST', 'STANDARD', 0.24, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×14mL, 227 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-ldl-test', 'mindray-bs-240pro', 'Mindray LDL-C (Холестерин ЛПНП)', 'PATIENT_TEST', 'STANDARD', 0.24, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×14mL, 227 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-ldl-test', 'mindray-bs-430', 'Mindray LDL-C (Холестерин ЛПНП)', 'PATIENT_TEST', 'STANDARD', 0.24, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×14mL, 227 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-ldl-test', 'mindray-bs-600m', 'Mindray LDL-C (Холестерин ЛПНП)', 'PATIENT_TEST', 'STANDARD', 0.24, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×14mL, 227 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- Apo-A1 (Аполипопротеин А1) - R1:1×35mL + R2:1×12mL = 47mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs200-apoa1-test', 'mindray-bs-200', 'Mindray Apo-A1 (Аполипопротеин А1)', 'PATIENT_TEST', 'STANDARD', 0.32, NULL, 'ML', 'reports/биохимия.md', 'R1:1×35mL+R2:1×12mL, 145 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-apoa1-test', 'mindray-bs-230', 'Mindray Apo-A1 (Аполипопротеин А1)', 'PATIENT_TEST', 'STANDARD', 0.32, NULL, 'ML', 'reports/биохимия.md', 'R1:1×35mL+R2:1×12mL, 145 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-apoa1-test', 'mindray-bs-240', 'Mindray Apo-A1 (Аполипопротеин А1)', 'PATIENT_TEST', 'STANDARD', 0.32, NULL, 'ML', 'reports/биохимия.md', 'R1:1×35mL+R2:1×12mL, 145 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-apoa1-test', 'mindray-bs-240pro', 'Mindray Apo-A1 (Аполипопротеин А1)', 'PATIENT_TEST', 'STANDARD', 0.32, NULL, 'ML', 'reports/биохимия.md', 'R1:1×35mL+R2:1×12mL, 145 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-apoa1-test', 'mindray-bs-430', 'Mindray Apo-A1 (Аполипопротеин А1)', 'PATIENT_TEST', 'STANDARD', 0.32, NULL, 'ML', 'reports/биохимия.md', 'R1:1×35mL+R2:1×12mL, 145 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-apoa1-test', 'mindray-bs-600m', 'Mindray Apo-A1 (Аполипопротеин А1)', 'PATIENT_TEST', 'STANDARD', 0.32, NULL, 'ML', 'reports/биохимия.md', 'R1:1×35mL+R2:1×12mL, 145 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- Apo-B (Аполипопротеин B) - R1:1×35mL + R2:1×12mL = 47mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs200-apob-test', 'mindray-bs-200', 'Mindray Apo-B (Аполипопротеин B)', 'PATIENT_TEST', 'STANDARD', 0.32, NULL, 'ML', 'reports/биохимия.md', 'R1:1×35mL+R2:1×12mL, 145 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-apob-test', 'mindray-bs-230', 'Mindray Apo-B (Аполипопротеин B)', 'PATIENT_TEST', 'STANDARD', 0.32, NULL, 'ML', 'reports/биохимия.md', 'R1:1×35mL+R2:1×12mL, 145 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-apob-test', 'mindray-bs-240', 'Mindray Apo-B (Аполипопротеин B)', 'PATIENT_TEST', 'STANDARD', 0.32, NULL, 'ML', 'reports/биохимия.md', 'R1:1×35mL+R2:1×12mL, 145 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-apob-test', 'mindray-bs-240pro', 'Mindray Apo-B (Аполипопротеин B)', 'PATIENT_TEST', 'STANDARD', 0.32, NULL, 'ML', 'reports/биохимия.md', 'R1:1×35mL+R2:1×12mL, 145 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-apob-test', 'mindray-bs-430', 'Mindray Apo-B (Аполипопротеин B)', 'PATIENT_TEST', 'STANDARD', 0.32, NULL, 'ML', 'reports/биохимия.md', 'R1:1×35mL+R2:1×12mL, 145 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-apob-test', 'mindray-bs-600m', 'Mindray Apo-B (Аполипопротеин B)', 'PATIENT_TEST', 'STANDARD', 0.32, NULL, 'ML', 'reports/биохимия.md', 'R1:1×35mL+R2:1×12mL, 145 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- Lp(a) (Липопротеин (a)) - R1:1×23mL + R2:1×7mL = 30mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs200-lpa-test', 'mindray-bs-200', 'Mindray Lp(a) (Липопротеин (a))', 'PATIENT_TEST', 'STANDARD', 0.27, NULL, 'ML', 'reports/биохимия.md', 'R1:1×23mL+R2:1×7mL, 110 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-lpa-test', 'mindray-bs-230', 'Mindray Lp(a) (Липопротеин (a))', 'PATIENT_TEST', 'STANDARD', 0.27, NULL, 'ML', 'reports/биохимия.md', 'R1:1×23mL+R2:1×7mL, 110 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-lpa-test', 'mindray-bs-240', 'Mindray Lp(a) (Липопротеин (a))', 'PATIENT_TEST', 'STANDARD', 0.27, NULL, 'ML', 'reports/биохимия.md', 'R1:1×23mL+R2:1×7mL, 110 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-lpa-test', 'mindray-bs-240pro', 'Mindray Lp(a) (Липопротеин (a))', 'PATIENT_TEST', 'STANDARD', 0.27, NULL, 'ML', 'reports/биохимия.md', 'R1:1×23mL+R2:1×7mL, 110 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-lpa-test', 'mindray-bs-430', 'Mindray Lp(a) (Липопротеин (a))', 'PATIENT_TEST', 'STANDARD', 0.27, NULL, 'ML', 'reports/биохимия.md', 'R1:1×23mL+R2:1×7mL, 110 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-lpa-test', 'mindray-bs-600m', 'Mindray Lp(a) (Липопротеин (a))', 'PATIENT_TEST', 'STANDARD', 0.27, NULL, 'ML', 'reports/биохимия.md', 'R1:1×23mL+R2:1×7mL, 110 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Section 5: Cardiac Markers (CK-MB)
-- =============================================================================

-- CK-MB (Креатинкиназа МВ) - R1:2×35mL + R2:1×18mL = 88mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs200-ckmb-test', 'mindray-bs-200', 'Mindray CK-MB (Креатинкиназа МВ)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:2×35mL+R2:1×18mL, 471 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-ckmb-test', 'mindray-bs-230', 'Mindray CK-MB (Креатинкиназа МВ)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:2×35mL+R2:1×18mL, 471 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-ckmb-test', 'mindray-bs-240', 'Mindray CK-MB (Креатинкиназа МВ)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:2×35mL+R2:1×18mL, 471 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-ckmb-test', 'mindray-bs-240pro', 'Mindray CK-MB (Креатинкиназа МВ)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:2×35mL+R2:1×18mL, 471 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-ckmb-test', 'mindray-bs-430', 'Mindray CK-MB (Креатинкиназа МВ)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:2×35mL+R2:1×18mL, 471 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-ckmb-test', 'mindray-bs-600m', 'Mindray CK-MB (Креатинкиназа МВ)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:2×35mL+R2:1×18mL, 471 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Section 6: Immunology (IgA, IgG, IgM, C3, C4, CRP, HS-CRP)
-- =============================================================================

-- IgA (Иммуноглобулин A) - R1:1×36mL + R2:1×18mL = 54mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs200-iga-test', 'mindray-bs-200', 'Mindray IgA (Иммуноглобулин A)', 'PATIENT_TEST', 'STANDARD', 0.34, NULL, 'ML', 'reports/биохимия.md', 'R1:1×36mL+R2:1×18mL, 157 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-iga-test', 'mindray-bs-230', 'Mindray IgA (Иммуноглобулин A)', 'PATIENT_TEST', 'STANDARD', 0.25, NULL, 'ML', 'reports/биохимия.md', 'R1:1×36mL+R2:1×18mL, 220 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-iga-test', 'mindray-bs-240', 'Mindray IgA (Иммуноглобулин A)', 'PATIENT_TEST', 'STANDARD', 0.25, NULL, 'ML', 'reports/биохимия.md', 'R1:1×36mL+R2:1×18mL, 220 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-iga-test', 'mindray-bs-240pro', 'Mindray IgA (Иммуноглобулин A)', 'PATIENT_TEST', 'STANDARD', 0.25, NULL, 'ML', 'reports/биохимия.md', 'R1:1×36mL+R2:1×18mL, 220 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-iga-test', 'mindray-bs-430', 'Mindray IgA (Иммуноглобулин A)', 'PATIENT_TEST', 'STANDARD', 0.25, NULL, 'ML', 'reports/биохимия.md', 'R1:1×36mL+R2:1×18mL, 220 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-iga-test', 'mindray-bs-600m', 'Mindray IgA (Иммуноглобулин A)', 'PATIENT_TEST', 'STANDARD', 0.25, NULL, 'ML', 'reports/биохимия.md', 'R1:1×36mL+R2:1×18mL, 220 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- IgG (Иммуноглобулин G) - R1:1×36mL + R2:1×18mL = 54mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs200-igg-test', 'mindray-bs-200', 'Mindray IgG (Иммуноглобулин G)', 'PATIENT_TEST', 'STANDARD', 0.48, NULL, 'ML', 'reports/биохимия.md', 'R1:1×36mL+R2:1×18mL, 113 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-igg-test', 'mindray-bs-230', 'Mindray IgG (Иммуноглобулин G)', 'PATIENT_TEST', 'STANDARD', 0.25, NULL, 'ML', 'reports/биохимия.md', 'R1:1×36mL+R2:1×18mL, 220 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-igg-test', 'mindray-bs-240', 'Mindray IgG (Иммуноглобулин G)', 'PATIENT_TEST', 'STANDARD', 0.25, NULL, 'ML', 'reports/биохимия.md', 'R1:1×36mL+R2:1×18mL, 220 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-igg-test', 'mindray-bs-240pro', 'Mindray IgG (Иммуноглобулин G)', 'PATIENT_TEST', 'STANDARD', 0.25, NULL, 'ML', 'reports/биохимия.md', 'R1:1×36mL+R2:1×18mL, 220 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-igg-test', 'mindray-bs-430', 'Mindray IgG (Иммуноглобулин G)', 'PATIENT_TEST', 'STANDARD', 0.25, NULL, 'ML', 'reports/биохимия.md', 'R1:1×36mL+R2:1×18mL, 220 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-igg-test', 'mindray-bs-600m', 'Mindray IgG (Иммуноглобулин G)', 'PATIENT_TEST', 'STANDARD', 0.25, NULL, 'ML', 'reports/биохимия.md', 'R1:1×36mL+R2:1×18mL, 220 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- IgM (Иммуноглобулин M) - R1:1×40mL + R2:1×10mL = 50mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs200-igm-test', 'mindray-bs-200', 'Mindray IgM (Иммуноглобулин M)', 'PATIENT_TEST', 'STANDARD', 0.32, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×10mL, 155 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-igm-test', 'mindray-bs-230', 'Mindray IgM (Иммуноглобулин M)', 'PATIENT_TEST', 'STANDARD', 0.24, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×10mL, 212 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-igm-test', 'mindray-bs-240', 'Mindray IgM (Иммуноглобулин M)', 'PATIENT_TEST', 'STANDARD', 0.32, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×10mL, 154 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-igm-test', 'mindray-bs-240pro', 'Mindray IgM (Иммуноглобулин M)', 'PATIENT_TEST', 'STANDARD', 0.32, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×10mL, 154 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-igm-test', 'mindray-bs-430', 'Mindray IgM (Иммуноглобулин M)', 'PATIENT_TEST', 'STANDARD', 0.32, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×10mL, 154 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-igm-test', 'mindray-bs-600m', 'Mindray IgM (Иммуноглобулин M)', 'PATIENT_TEST', 'STANDARD', 0.32, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×10mL, 154 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- C3 (Комплимент C3) - R1:1×36mL + R2:1×18mL = 54mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs200-c3-test', 'mindray-bs-200', 'Mindray C3 (Комплимент C3)', 'PATIENT_TEST', 'STANDARD', 0.34, NULL, 'ML', 'reports/биохимия.md', 'R1:1×36mL+R2:1×18mL, 157 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-c3-test', 'mindray-bs-230', 'Mindray C3 (Комплимент C3)', 'PATIENT_TEST', 'STANDARD', 0.25, NULL, 'ML', 'reports/биохимия.md', 'R1:1×36mL+R2:1×18mL, 220 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-c3-test', 'mindray-bs-240', 'Mindray C3 (Комплимент C3)', 'PATIENT_TEST', 'STANDARD', 0.25, NULL, 'ML', 'reports/биохимия.md', 'R1:1×36mL+R2:1×18mL, 220 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-c3-test', 'mindray-bs-240pro', 'Mindray C3 (Комплимент C3)', 'PATIENT_TEST', 'STANDARD', 0.25, NULL, 'ML', 'reports/биохимия.md', 'R1:1×36mL+R2:1×18mL, 220 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-c3-test', 'mindray-bs-430', 'Mindray C3 (Комплимент C3)', 'PATIENT_TEST', 'STANDARD', 0.25, NULL, 'ML', 'reports/биохимия.md', 'R1:1×36mL+R2:1×18mL, 220 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-c3-test', 'mindray-bs-600m', 'Mindray C3 (Комплимент C3)', 'PATIENT_TEST', 'STANDARD', 0.25, NULL, 'ML', 'reports/биохимия.md', 'R1:1×36mL+R2:1×18mL, 220 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- C4 (Комплимент C4) - R1:1×40mL + R2:1×15mL = 55mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs200-c4-test', 'mindray-bs-200', 'Mindray C4 (Комплимент C4)', 'PATIENT_TEST', 'STANDARD', 0.33, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×15mL, 168 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-c4-test', 'mindray-bs-230', 'Mindray C4 (Комплимент C4)', 'PATIENT_TEST', 'STANDARD', 0.27, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×15mL, 207 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-c4-test', 'mindray-bs-240', 'Mindray C4 (Комплимент C4)', 'PATIENT_TEST', 'STANDARD', 0.27, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×15mL, 207 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-c4-test', 'mindray-bs-240pro', 'Mindray C4 (Комплимент C4)', 'PATIENT_TEST', 'STANDARD', 0.27, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×15mL, 207 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-c4-test', 'mindray-bs-430', 'Mindray C4 (Комплимент C4)', 'PATIENT_TEST', 'STANDARD', 0.27, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×15mL, 207 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-c4-test', 'mindray-bs-600m', 'Mindray C4 (Комплимент C4)', 'PATIENT_TEST', 'STANDARD', 0.27, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×15mL, 207 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- CRP (С-реактивный белок) - R1:1×40mL + R2:1×10mL = 50mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs200-crp-test', 'mindray-bs-200', 'Mindray CRP (С-реактивный белок)', 'PATIENT_TEST', 'STANDARD', 0.32, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×10mL, 154 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-crp-test', 'mindray-bs-230', 'Mindray CRP (С-реактивный белок)', 'PATIENT_TEST', 'STANDARD', 0.27, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×10mL, 188 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-crp-test', 'mindray-bs-240', 'Mindray CRP (С-реактивный белок)', 'PATIENT_TEST', 'STANDARD', 0.27, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×10mL, 188 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-crp-test', 'mindray-bs-240pro', 'Mindray CRP (С-реактивный белок)', 'PATIENT_TEST', 'STANDARD', 0.27, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×10mL, 188 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-crp-test', 'mindray-bs-430', 'Mindray CRP (С-реактивный белок)', 'PATIENT_TEST', 'STANDARD', 0.27, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×10mL, 188 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-crp-test', 'mindray-bs-600m', 'Mindray CRP (С-реактивный белок)', 'PATIENT_TEST', 'STANDARD', 0.27, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×10mL, 188 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- HS-CRP (Высокочувствительный С-реактивный белок) - R1:1×40mL + R2:1×40mL + Cal:5×0.5mL = 80mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs200-hscrp-test', 'mindray-bs-200', 'Mindray HS-CRP (Высокочувствительный СРБ)', 'PATIENT_TEST', 'STANDARD', 0.33, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×40mL+Cal:5×0.5mL, 241 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-hscrp-test', 'mindray-bs-230', 'Mindray HS-CRP (Высокочувствительный СРБ)', 'PATIENT_TEST', 'STANDARD', 0.28, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×40mL+Cal:5×0.5mL, 288 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-hscrp-test', 'mindray-bs-240', 'Mindray HS-CRP (Высокочувствительный СРБ)', 'PATIENT_TEST', 'STANDARD', 0.28, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×40mL+Cal:5×0.5mL, 288 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-hscrp-test', 'mindray-bs-240pro', 'Mindray HS-CRP (Высокочувствительный СРБ)', 'PATIENT_TEST', 'STANDARD', 0.20, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×40mL+Cal:5×0.5mL, 394 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-hscrp-test', 'mindray-bs-430', 'Mindray HS-CRP (Высокочувствительный СРБ)', 'PATIENT_TEST', 'STANDARD', 0.20, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×40mL+Cal:5×0.5mL, 394 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-hscrp-test', 'mindray-bs-600m', 'Mindray HS-CRP (Высокочувствительный СРБ)', 'PATIENT_TEST', 'STANDARD', 0.20, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×40mL+Cal:5×0.5mL, 394 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Section 7: Specific Proteins (Prealbumin, Transferrin, Ferritin, β2-MG, Cystatin C)
-- =============================================================================

-- PA (Преальбумин) - R1:1×40mL + R2:1×15mL = 55mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs200-pa-test', 'mindray-bs-200', 'Mindray PA (Преальбумин)', 'PATIENT_TEST', 'STANDARD', 0.33, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×15mL, 168 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-pa-test', 'mindray-bs-230', 'Mindray PA (Преальбумин)', 'PATIENT_TEST', 'STANDARD', 0.27, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×15mL, 207 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-pa-test', 'mindray-bs-240', 'Mindray PA (Преальбумин)', 'PATIENT_TEST', 'STANDARD', 0.33, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×15mL, 168 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-pa-test', 'mindray-bs-240pro', 'Mindray PA (Преальбумин)', 'PATIENT_TEST', 'STANDARD', 0.27, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×15mL, 207 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-pa-test', 'mindray-bs-430', 'Mindray PA (Преальбумин)', 'PATIENT_TEST', 'STANDARD', 0.27, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×15mL, 207 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-pa-test', 'mindray-bs-600m', 'Mindray PA (Преальбумин)', 'PATIENT_TEST', 'STANDARD', 0.27, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×15mL, 207 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- TRF (Трансферрин) - R1:1×32mL + R2:1×5mL = 37mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs200-trf-test', 'mindray-bs-200', 'Mindray TRF (Трансферрин)', 'PATIENT_TEST', 'STANDARD', 0.26, NULL, 'ML', 'reports/биохимия.md', 'R1:1×32mL+R2:1×5mL, 140 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-trf-test', 'mindray-bs-230', 'Mindray TRF (Трансферрин)', 'PATIENT_TEST', 'STANDARD', 0.26, NULL, 'ML', 'reports/биохимия.md', 'R1:1×32mL+R2:1×5mL, 140 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-trf-test', 'mindray-bs-240', 'Mindray TRF (Трансферрин)', 'PATIENT_TEST', 'STANDARD', 0.26, NULL, 'ML', 'reports/биохимия.md', 'R1:1×32mL+R2:1×5mL, 140 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-trf-test', 'mindray-bs-240pro', 'Mindray TRF (Трансферрин)', 'PATIENT_TEST', 'STANDARD', 0.26, NULL, 'ML', 'reports/биохимия.md', 'R1:1×32mL+R2:1×5mL, 140 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-trf-test', 'mindray-bs-430', 'Mindray TRF (Трансферрин)', 'PATIENT_TEST', 'STANDARD', 0.26, NULL, 'ML', 'reports/биохимия.md', 'R1:1×32mL+R2:1×5mL, 140 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-trf-test', 'mindray-bs-600m', 'Mindray TRF (Трансферрин)', 'PATIENT_TEST', 'STANDARD', 0.26, NULL, 'ML', 'reports/биохимия.md', 'R1:1×32mL+R2:1×5mL, 140 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- FER (Ферритин) - R1:1×12mL + R2:1×7mL = 19mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs200-fer-test', 'mindray-bs-200', 'Mindray FER (Ферритин)', 'PATIENT_TEST', 'STANDARD', 0.26, NULL, 'ML', 'reports/биохимия.md', 'R1:1×12mL+R2:1×7mL, 72 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-fer-test', 'mindray-bs-230', 'Mindray FER (Ферритин)', 'PATIENT_TEST', 'STANDARD', 0.23, NULL, 'ML', 'reports/биохимия.md', 'R1:1×12mL+R2:1×7mL, 84 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-fer-test', 'mindray-bs-240', 'Mindray FER (Ферритин)', 'PATIENT_TEST', 'STANDARD', 0.23, NULL, 'ML', 'reports/биохимия.md', 'R1:1×12mL+R2:1×7mL, 84 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-fer-test', 'mindray-bs-240pro', 'Mindray FER (Ферритин)', 'PATIENT_TEST', 'STANDARD', 0.23, NULL, 'ML', 'reports/биохимия.md', 'R1:1×12mL+R2:1×7mL, 84 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-fer-test', 'mindray-bs-430', 'Mindray FER (Ферритин)', 'PATIENT_TEST', 'STANDARD', 0.23, NULL, 'ML', 'reports/биохимия.md', 'R1:1×12mL+R2:1×7mL, 84 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-fer-test', 'mindray-bs-600m', 'Mindray FER (Ферритин)', 'PATIENT_TEST', 'STANDARD', 0.23, NULL, 'ML', 'reports/биохимия.md', 'R1:1×12mL+R2:1×7mL, 84 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- β2-MG (β2 Микроглобулин) - R1:1×40mL + R2:1×12mL = 52mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs200-b2mg-test', 'mindray-bs-200', 'Mindray β2-MG (β2 Микроглобулин)', 'PATIENT_TEST', 'STANDARD', 0.23, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×12mL, 227 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-b2mg-test', 'mindray-bs-230', 'Mindray β2-MG (β2 Микроглобулин)', 'PATIENT_TEST', 'STANDARD', 0.23, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×12mL, 227 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-b2mg-test', 'mindray-bs-240', 'Mindray β2-MG (β2 Микроглобулин)', 'PATIENT_TEST', 'STANDARD', 0.23, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×12mL, 227 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-b2mg-test', 'mindray-bs-240pro', 'Mindray β2-MG (β2 Микроглобулин)', 'PATIENT_TEST', 'STANDARD', 0.23, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×12mL, 227 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-b2mg-test', 'mindray-bs-430', 'Mindray β2-MG (β2 Микроглобулин)', 'PATIENT_TEST', 'STANDARD', 0.23, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×12mL, 227 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-b2mg-test', 'mindray-bs-600m', 'Mindray β2-MG (β2 Микроглобулин)', 'PATIENT_TEST', 'STANDARD', 0.23, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×12mL, 227 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- Cys-C (Цистатин C) - R1:1×40mL + R2:1×12mL = 52mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs200-cysc-test', 'mindray-bs-200', 'Mindray Cys-C (Цистатин C)', 'PATIENT_TEST', 'STANDARD', 0.26, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×12mL, 202 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-cysc-test', 'mindray-bs-230', 'Mindray Cys-C (Цистатин C)', 'PATIENT_TEST', 'STANDARD', 0.17, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×12mL, 300 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-cysc-test', 'mindray-bs-240', 'Mindray Cys-C (Цистатин C)', 'PATIENT_TEST', 'STANDARD', 0.17, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×12mL, 300 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-cysc-test', 'mindray-bs-240pro', 'Mindray Cys-C (Цистатин C)', 'PATIENT_TEST', 'STANDARD', 0.17, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×12mL, 300 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-cysc-test', 'mindray-bs-430', 'Mindray Cys-C (Цистатин C)', 'PATIENT_TEST', 'STANDARD', 0.17, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×12mL, 300 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-cysc-test', 'mindray-bs-600m', 'Mindray Cys-C (Цистатин C)', 'PATIENT_TEST', 'STANDARD', 0.17, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×12mL, 300 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Section 8: Diabetes Markers (Microalbumin, HCY, β-HB)
-- =============================================================================

-- MALB (Микроальбумин) - R1:1×30mL + R2:1×7mL + Cal:5×1mL = 37mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs200-malb-test', 'mindray-bs-200', 'Mindray MALB (Микроальбумин)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R1:1×30mL+R2:1×7mL+Cal:5×1mL, 170 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-malb-test', 'mindray-bs-230', 'Mindray MALB (Микроальбумин)', 'PATIENT_TEST', 'STANDARD', 0.18, NULL, 'ML', 'reports/биохимия.md', 'R1:1×30mL+R2:1×7mL+Cal:5×1mL, 211 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-malb-test', 'mindray-bs-240', 'Mindray MALB (Микроальбумин)', 'PATIENT_TEST', 'STANDARD', 0.18, NULL, 'ML', 'reports/биохимия.md', 'R1:1×30mL+R2:1×7mL+Cal:5×1mL, 211 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-malb-test', 'mindray-bs-240pro', 'Mindray MALB (Микроальбумин)', 'PATIENT_TEST', 'STANDARD', 0.18, NULL, 'ML', 'reports/биохимия.md', 'R1:1×30mL+R2:1×7mL+Cal:5×1mL, 211 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-malb-test', 'mindray-bs-430', 'Mindray MALB (Микроальбумин)', 'PATIENT_TEST', 'STANDARD', 0.18, NULL, 'ML', 'reports/биохимия.md', 'R1:1×30mL+R2:1×7mL+Cal:5×1mL, 211 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-malb-test', 'mindray-bs-600m', 'Mindray MALB (Микроальбумин)', 'PATIENT_TEST', 'STANDARD', 0.18, NULL, 'ML', 'reports/биохимия.md', 'R1:1×30mL+R2:1×7mL+Cal:5×1mL, 211 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- HCY (Гомоцистеин) - R1:1×25mL + R2:1×8mL + Cal:5×1mL = 33mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs200-hcy-test', 'mindray-bs-200', 'Mindray HCY (Гомоцистеин)', 'PATIENT_TEST', 'STANDARD', 0.28, NULL, 'ML', 'reports/биохимия.md', 'R1:1×25mL+R2:1×8mL+Cal:5×1mL, 118 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-hcy-test', 'mindray-bs-230', 'Mindray HCY (Гомоцистеин)', 'PATIENT_TEST', 'STANDARD', 0.28, NULL, 'ML', 'reports/биохимия.md', 'R1:1×25mL+R2:1×8mL+Cal:5×1mL, 118 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-hcy-test', 'mindray-bs-240', 'Mindray HCY (Гомоцистеин)', 'PATIENT_TEST', 'STANDARD', 0.28, NULL, 'ML', 'reports/биохимия.md', 'R1:1×25mL+R2:1×8mL+Cal:5×1mL, 118 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-hcy-test', 'mindray-bs-240pro', 'Mindray HCY (Гомоцистеин)', 'PATIENT_TEST', 'STANDARD', 0.28, NULL, 'ML', 'reports/биохимия.md', 'R1:1×25mL+R2:1×8mL+Cal:5×1mL, 118 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-hcy-test', 'mindray-bs-430', 'Mindray HCY (Гомоцистеин)', 'PATIENT_TEST', 'STANDARD', 0.28, NULL, 'ML', 'reports/биохимия.md', 'R1:1×25mL+R2:1×8mL+Cal:5×1mL, 118 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-hcy-test', 'mindray-bs-600m', 'Mindray HCY (Гомоцистеин)', 'PATIENT_TEST', 'STANDARD', 0.28, NULL, 'ML', 'reports/биохимия.md', 'R1:1×25mL+R2:1×8mL+Cal:5×1mL, 118 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- β-HB (Бета Гидроксибутират) - R1:1×20mL + R2:1×7mL + Cal:1×1mL = 27mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs200-bhb-test', 'mindray-bs-200', 'Mindray β-HB (Бета Гидроксибутират)', 'PATIENT_TEST', 'STANDARD', 0.32, NULL, 'ML', 'reports/биохимия.md', 'R1:1×20mL+R2:1×7mL+Cal:1×1mL, 85 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-bhb-test', 'mindray-bs-230', 'Mindray β-HB (Бета Гидроксибутират)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:1×20mL+R2:1×7mL+Cal:1×1mL, 140 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-bhb-test', 'mindray-bs-240', 'Mindray β-HB (Бета Гидроксибутират)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:1×20mL+R2:1×7mL+Cal:1×1mL, 140 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-bhb-test', 'mindray-bs-240pro', 'Mindray β-HB (Бета Гидроксибутират)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:1×20mL+R2:1×7mL+Cal:1×1mL, 140 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-bhb-test', 'mindray-bs-430', 'Mindray β-HB (Бета Гидроксибутират)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:1×20mL+R2:1×7mL+Cal:1×1mL, 140 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-bhb-test', 'mindray-bs-600m', 'Mindray β-HB (Бета Гидроксибутират)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:1×20mL+R2:1×7mL+Cal:1×1mL, 140 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Section 9: Rheumatology and Autoimmune (ASO, RF)
-- =============================================================================

-- ASO (Антистрептолизин O) - R1:1×23mL + R2:1×23mL + Cal:1×0.5mL = 46mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs200-aso-test', 'mindray-bs-200', 'Mindray ASO (Антистрептолизин O)', 'PATIENT_TEST', 'STANDARD', 0.35, NULL, 'ML', 'reports/биохимия.md', 'R1:1×23mL+R2:1×23mL+Cal:1×0.5mL, 132 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-aso-test', 'mindray-bs-230', 'Mindray ASO (Антистрептолизин O)', 'PATIENT_TEST', 'STANDARD', 0.35, NULL, 'ML', 'reports/биохимия.md', 'R1:1×23mL+R2:1×23mL+Cal:1×0.5mL, 132 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-aso-test', 'mindray-bs-240', 'Mindray ASO (Антистрептолизин O)', 'PATIENT_TEST', 'STANDARD', 0.28, NULL, 'ML', 'reports/биохимия.md', 'R1:1×23mL+R2:1×23mL+Cal:1×0.5mL, 164 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-aso-test', 'mindray-bs-240pro', 'Mindray ASO (Антистрептолизин O)', 'PATIENT_TEST', 'STANDARD', 0.28, NULL, 'ML', 'reports/биохимия.md', 'R1:1×23mL+R2:1×23mL+Cal:1×0.5mL, 164 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-aso-test', 'mindray-bs-430', 'Mindray ASO (Антистрептолизин O)', 'PATIENT_TEST', 'STANDARD', 0.28, NULL, 'ML', 'reports/биохимия.md', 'R1:1×23mL+R2:1×23mL+Cal:1×0.5mL, 164 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-aso-test', 'mindray-bs-600m', 'Mindray ASO (Антистрептолизин O)', 'PATIENT_TEST', 'STANDARD', 0.28, NULL, 'ML', 'reports/биохимия.md', 'R1:1×23mL+R2:1×23mL+Cal:1×0.5mL, 164 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- RF (Ревматоидный фактор) - R1:1×40mL + R2:1×11mL = 51mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs200-rf-test', 'mindray-bs-200', 'Mindray RF (Ревматоидный фактор)', 'PATIENT_TEST', 'STANDARD', 0.25, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×11mL, 202 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-rf-test', 'mindray-bs-230', 'Mindray RF (Ревматоидный фактор)', 'PATIENT_TEST', 'STANDARD', 0.25, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×11mL, 202 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-rf-test', 'mindray-bs-240', 'Mindray RF (Ревматоидный фактор)', 'PATIENT_TEST', 'STANDARD', 0.25, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×11mL, 202 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-rf-test', 'mindray-bs-240pro', 'Mindray RF (Ревматоидный фактор)', 'PATIENT_TEST', 'STANDARD', 0.25, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×11mL, 202 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-rf-test', 'mindray-bs-430', 'Mindray RF (Ревматоидный фактор)', 'PATIENT_TEST', 'STANDARD', 0.25, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×11mL, 202 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-rf-test', 'mindray-bs-600m', 'Mindray RF (Ревматоидный фактор)', 'PATIENT_TEST', 'STANDARD', 0.25, NULL, 'ML', 'reports/биохимия.md', 'R1:1×40mL+R2:1×11mL, 202 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Section 10: Other Specialized Reagents (TBA, Fructosamine, Lipase, RBP, TPUC, G6PD, UIBC)
-- =============================================================================

-- TBA (Общие желчные кислоты) - R1:2×27mL + R2:1×18mL + Cal:1×1.5mL = 72mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs200-tba-test', 'mindray-bs-200', 'Mindray TBA (Общие желчные кислоты)', 'PATIENT_TEST', 'STANDARD', 0.32, NULL, 'ML', 'reports/биохимия.md', 'R1:2×27mL+R2:1×18mL+Cal:1×1.5mL, 226 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-tba-test', 'mindray-bs-230', 'Mindray TBA (Общие желчные кислоты)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R1:2×27mL+R2:1×18mL+Cal:1×1.5mL, 330 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-tba-test', 'mindray-bs-240', 'Mindray TBA (Общие желчные кислоты)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R1:2×27mL+R2:1×18mL+Cal:1×1.5mL, 330 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-tba-test', 'mindray-bs-240pro', 'Mindray TBA (Общие желчные кислоты)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R1:2×27mL+R2:1×18mL+Cal:1×1.5mL, 330 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-tba-test', 'mindray-bs-430', 'Mindray TBA (Общие желчные кислоты)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R1:2×27mL+R2:1×18mL+Cal:1×1.5mL, 330 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-tba-test', 'mindray-bs-600m', 'Mindray TBA (Общие желчные кислоты)', 'PATIENT_TEST', 'STANDARD', 0.22, NULL, 'ML', 'reports/биохимия.md', 'R1:2×27mL+R2:1×18mL+Cal:1×1.5mL, 330 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- FUN (Фруктозамин) - R1:2×30mL + R2:1×15mL + Cal:1×1.5mL = 75mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs200-fun-test', 'mindray-bs-200', 'Mindray FUN (Фруктозамин)', 'PATIENT_TEST', 'STANDARD', 0.31, NULL, 'ML', 'reports/биохимия.md', 'R1:2×30mL+R2:1×15mL+Cal:1×1.5mL, 245 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-fun-test', 'mindray-bs-230', 'Mindray FUN (Фруктозамин)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:2×30mL+R2:1×15mL+Cal:1×1.5mL, 385 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-fun-test', 'mindray-bs-240', 'Mindray FUN (Фруктозамин)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:2×30mL+R2:1×15mL+Cal:1×1.5mL, 385 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-fun-test', 'mindray-bs-240pro', 'Mindray FUN (Фруктозамин)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:2×30mL+R2:1×15mL+Cal:1×1.5mL, 385 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-fun-test', 'mindray-bs-430', 'Mindray FUN (Фруктозамин)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:2×30mL+R2:1×15mL+Cal:1×1.5mL, 385 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-fun-test', 'mindray-bs-600m', 'Mindray FUN (Фруктозамин)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:2×30mL+R2:1×15mL+Cal:1×1.5mL, 385 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- LIP (Липаза) - R1:1×35mL + R2:1×9mL = 44mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs200-lip-test', 'mindray-bs-200', 'Mindray LIP (Липаза)', 'PATIENT_TEST', 'STANDARD', 0.28, NULL, 'ML', 'reports/биохимия.md', 'R1:1×35mL+R2:1×9mL, 158 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-lip-test', 'mindray-bs-230', 'Mindray LIP (Липаза)', 'PATIENT_TEST', 'STANDARD', 0.28, NULL, 'ML', 'reports/биохимия.md', 'R1:1×35mL+R2:1×9mL, 158 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-lip-test', 'mindray-bs-240', 'Mindray LIP (Липаза)', 'PATIENT_TEST', 'STANDARD', 0.28, NULL, 'ML', 'reports/биохимия.md', 'R1:1×35mL+R2:1×9mL, 158 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-lip-test', 'mindray-bs-240pro', 'Mindray LIP (Липаза)', 'PATIENT_TEST', 'STANDARD', 0.28, NULL, 'ML', 'reports/биохимия.md', 'R1:1×35mL+R2:1×9mL, 158 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-lip-test', 'mindray-bs-430', 'Mindray LIP (Липаза)', 'PATIENT_TEST', 'STANDARD', 0.28, NULL, 'ML', 'reports/биохимия.md', 'R1:1×35mL+R2:1×9mL, 158 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-lip-test', 'mindray-bs-600m', 'Mindray LIP (Липаза)', 'PATIENT_TEST', 'STANDARD', 0.28, NULL, 'ML', 'reports/биохимия.md', 'R1:1×35mL+R2:1×9mL, 158 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- RBP (Ретинол-связывающий белок) - R1:1×20mL + R2:1×8mL + Cal:1×1mL = 28mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs200-rbp-test', 'mindray-bs-200', 'Mindray RBP (Ретинол-связывающий белок)', 'PATIENT_TEST', 'STANDARD', 0.30, NULL, 'ML', 'reports/биохимия.md', 'R1:1×20mL+R2:1×8mL+Cal:1×1mL, 94 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-rbp-test', 'mindray-bs-230', 'Mindray RBP (Ретинол-связывающий белок)', 'PATIENT_TEST', 'STANDARD', 0.30, NULL, 'ML', 'reports/биохимия.md', 'R1:1×20mL+R2:1×8mL+Cal:1×1mL, 94 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-rbp-test', 'mindray-bs-240', 'Mindray RBP (Ретинол-связывающий белок)', 'PATIENT_TEST', 'STANDARD', 0.30, NULL, 'ML', 'reports/биохимия.md', 'R1:1×20mL+R2:1×8mL+Cal:1×1mL, 94 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-rbp-test', 'mindray-bs-240pro', 'Mindray RBP (Ретинол-связывающий белок)', 'PATIENT_TEST', 'STANDARD', 0.30, NULL, 'ML', 'reports/биохимия.md', 'R1:1×20mL+R2:1×8mL+Cal:1×1mL, 94 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-rbp-test', 'mindray-bs-430', 'Mindray RBP (Ретинол-связывающий белок)', 'PATIENT_TEST', 'STANDARD', 0.30, NULL, 'ML', 'reports/биохимия.md', 'R1:1×20mL+R2:1×8mL+Cal:1×1mL, 94 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-rbp-test', 'mindray-bs-600m', 'Mindray RBP (Ретинол-связывающий белок)', 'PATIENT_TEST', 'STANDARD', 0.30, NULL, 'ML', 'reports/биохимия.md', 'R1:1×20mL+R2:1×8mL+Cal:1×1mL, 94 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- TPUC (Общий белок в моче) - R:3×18mL + Cal:1×1mL = 54mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs200-tpuc-test', 'mindray-bs-200', 'Mindray TPUC (Общий белок в моче)', 'PATIENT_TEST', 'STANDARD', 0.23, NULL, 'ML', 'reports/биохимия.md', 'R:3×18mL+Cal:1×1mL, 240 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-tpuc-test', 'mindray-bs-230', 'Mindray TPUC (Общий белок в моче)', 'PATIENT_TEST', 'STANDARD', 0.14, NULL, 'ML', 'reports/биохимия.md', 'R:3×18mL+Cal:1×1mL, 378 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-tpuc-test', 'mindray-bs-240', 'Mindray TPUC (Общий белок в моче)', 'PATIENT_TEST', 'STANDARD', 0.11, NULL, 'ML', 'reports/биохимия.md', 'R:3×18mL+Cal:1×1mL, 471 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-tpuc-test', 'mindray-bs-240pro', 'Mindray TPUC (Общий белок в моче)', 'PATIENT_TEST', 'STANDARD', 0.11, NULL, 'ML', 'reports/биохимия.md', 'R:3×18mL+Cal:1×1mL, 471 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-tpuc-test', 'mindray-bs-430', 'Mindray TPUC (Общий белок в моче)', 'PATIENT_TEST', 'STANDARD', 0.11, NULL, 'ML', 'reports/биохимия.md', 'R:3×18mL+Cal:1×1mL, 471 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-tpuc-test', 'mindray-bs-600m', 'Mindray TPUC (Общий белок в моче)', 'PATIENT_TEST', 'STANDARD', 0.11, NULL, 'ML', 'reports/биохимия.md', 'R:3×18mL+Cal:1×1mL, 471 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- G6PD (Глюкозо-6-фосфат дегидрогеназа) - R1:1×20mL + R2:1×7mL = 27mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs200-g6pd-test', 'mindray-bs-200', 'Mindray G6PD (Глюкозо-6-фосфат дегидрогеназа)', 'PATIENT_TEST', 'STANDARD', 0.32, NULL, 'ML', 'reports/биохимия.md', 'R1:1×20mL+R2:1×7mL, 85 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-g6pd-test', 'mindray-bs-230', 'Mindray G6PD (Глюкозо-6-фосфат дегидрогеназа)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:1×20mL+R2:1×7mL, 140 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-g6pd-test', 'mindray-bs-240', 'Mindray G6PD (Глюкозо-6-фосфат дегидрогеназа)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:1×20mL+R2:1×7mL, 140 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-g6pd-test', 'mindray-bs-240pro', 'Mindray G6PD (Глюкозо-6-фосфат дегидрогеназа)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:1×20mL+R2:1×7mL, 140 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-g6pd-test', 'mindray-bs-430', 'Mindray G6PD (Глюкозо-6-фосфат дегидрогеназа)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:1×20mL+R2:1×7mL, 140 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-g6pd-test', 'mindray-bs-600m', 'Mindray G6PD (Глюкозо-6-фосфат дегидрогеназа)', 'PATIENT_TEST', 'STANDARD', 0.19, NULL, 'ML', 'reports/биохимия.md', 'R1:1×20mL+R2:1×7mL, 140 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- UIBC (НЖСС - ненасыщенная железосвязывающая способность) - R1:1×20mL + R2:1×7mL + Cal:1×1mL = 27mL total
INSERT INTO analyzer_reagent_rates (id, analyzer_id, reagent_name, operation_type, test_mode, volume_per_operation_ml, units_per_operation, unit_type, source_document, notes, created_at, version) VALUES
    ('bs200-uibc-test', 'mindray-bs-200', 'Mindray UIBC (НЖСС)', 'PATIENT_TEST', 'STANDARD', 0.32, NULL, 'ML', 'reports/биохимия.md', 'R1:1×20mL+R2:1×7mL+Cal:1×1mL, 85 tests max', CURRENT_TIMESTAMP, 0),
    ('bs230-uibc-test', 'mindray-bs-230', 'Mindray UIBC (НЖСС)', 'PATIENT_TEST', 'STANDARD', 0.32, NULL, 'ML', 'reports/биохимия.md', 'R1:1×20mL+R2:1×7mL+Cal:1×1mL, 85 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240-uibc-test', 'mindray-bs-240', 'Mindray UIBC (НЖСС)', 'PATIENT_TEST', 'STANDARD', 0.32, NULL, 'ML', 'reports/биохимия.md', 'R1:1×20mL+R2:1×7mL+Cal:1×1mL, 85 tests max', CURRENT_TIMESTAMP, 0),
    ('bs240pro-uibc-test', 'mindray-bs-240pro', 'Mindray UIBC (НЖСС)', 'PATIENT_TEST', 'STANDARD', 0.32, NULL, 'ML', 'reports/биохимия.md', 'R1:1×20mL+R2:1×7mL+Cal:1×1mL, 85 tests max', CURRENT_TIMESTAMP, 0),
    ('bs430-uibc-test', 'mindray-bs-430', 'Mindray UIBC (НЖСС)', 'PATIENT_TEST', 'STANDARD', 0.32, NULL, 'ML', 'reports/биохимия.md', 'R1:1×20mL+R2:1×7mL+Cal:1×1mL, 85 tests max', CURRENT_TIMESTAMP, 0),
    ('bs600m-uibc-test', 'mindray-bs-600m', 'Mindray UIBC (НЖСС)', 'PATIENT_TEST', 'STANDARD', 0.32, NULL, 'ML', 'reports/биохимия.md', 'R1:1×20mL+R2:1×7mL+Cal:1×1mL, 85 tests max', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;
