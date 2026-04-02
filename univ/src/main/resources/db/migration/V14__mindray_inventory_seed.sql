-- Migration: V14 - Mindray Reagent Inventory Seed Data
-- Purpose: Seed initial reagent inventory for Mindray analyzers
-- Created: 2026-04-03

-- =============================================================================
-- Section 1: Basic Biochemistry Reagents Inventory
-- =============================================================================

-- ALT (Аланинаминотрансфераза) - 4 kits in stock
INSERT INTO reagent_inventory (id, analyzer_id, reagent_name, lot_number, manufacturer, expiry_date_sealed, stability_days_after_opening, total_volume_ml, unit_type, unit_price_tenge, status, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('inv-alt-bs230-001', 'mindray-bs-230', 'Mindray ALT (Аланинаминотрансфераза)', 'LOT240301', 'Mindray', '2026-12-31', 90, 176.0, 'ML', 45000.0, 'IN_STOCK', '2026-04-01', 'admin', 'R1:4×35mL+R2:2×18mL комплект', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('inv-alt-bs240-001', 'mindray-bs-240', 'Mindray ALT (Аланинаминотрансфераза)', 'LOT240302', 'Mindray', '2026-12-31', 90, 176.0, 'ML', 45000.0, 'IN_STOCK', '2026-04-01', 'admin', 'R1:4×35mL+R2:2×18mL комплект', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('inv-alt-bs240pro-001', 'mindray-bs-240pro', 'Mindray ALT (Аланинаминотрансфераза)', 'LOT240303', 'Mindray', '2026-12-31', 90, 176.0, 'ML', 45000.0, 'IN_STOCK', '2026-04-01', 'admin', 'R1:4×35mL+R2:2×18mL комплект', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('inv-alt-bs430-001', 'mindray-bs-430', 'Mindray ALT (Аланинаминотрансфераза)', 'LOT240304', 'Mindray', '2026-12-31', 90, 176.0, 'ML', 45000.0, 'OPENED', '2026-04-01', 'admin', 'R1:4×35mL+R2:2×18mL комплект, открыт', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- AST (Аспартатаминотрансфераза)
INSERT INTO reagent_inventory (id, analyzer_id, reagent_name, lot_number, manufacturer, expiry_date_sealed, stability_days_after_opening, total_volume_ml, unit_type, unit_price_tenge, status, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('inv-ast-bs230-001', 'mindray-bs-230', 'Mindray AST (Аспартатаминотрансфераза)', 'LOT240305', 'Mindray', '2026-12-31', 90, 176.0, 'ML', 45000.0, 'IN_STOCK', '2026-04-01', 'admin', 'R1:4×35mL+R2:2×18mL комплект', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('inv-ast-bs240-001', 'mindray-bs-240', 'Mindray AST (Аспартатаминотрансфераза)', 'LOT240306', 'Mindray', '2026-12-31', 90, 176.0, 'ML', 45000.0, 'IN_STOCK', '2026-04-01', 'admin', 'R1:4×35mL+R2:2×18mL комплект', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ALB (Альбумин)
INSERT INTO reagent_inventory (id, analyzer_id, reagent_name, lot_number, manufacturer, expiry_date_sealed, stability_days_after_opening, total_volume_ml, unit_type, unit_price_tenge, status, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('inv-alb-bs230-001', 'mindray-bs-230', 'Mindray ALB (Альбумин)', 'LOT240307', 'Mindray', '2026-12-31', 90, 160.0, 'ML', 38000.0, 'IN_STOCK', '2026-04-01', 'admin', 'R:4×40mL комплект', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('inv-alb-bs240-001', 'mindray-bs-240', 'Mindray ALB (Альбумин)', 'LOT240308', 'Mindray', '2026-12-31', 90, 160.0, 'ML', 38000.0, 'OPENED', '2026-04-01', 'admin', 'R:4×40mL комплект, открыт', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- GGT (Гамма-глутамилтрансфераза)
INSERT INTO reagent_inventory (id, analyzer_id, reagent_name, lot_number, manufacturer, expiry_date_sealed, stability_days_after_opening, total_volume_ml, unit_type, unit_price_tenge, status, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('inv-ggt-bs230-001', 'mindray-bs-230', 'Mindray GGT (Гамма-глутамилтрансфераза)', 'LOT240309', 'Mindray', '2026-12-31', 90, 176.0, 'ML', 45000.0, 'IN_STOCK', '2026-04-01', 'admin', 'R1:4×35mL+R2:2×18mL комплект', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('inv-ggt-bs240-001', 'mindray-bs-240', 'Mindray GGT (Гамма-глутамилтрансфераза)', 'LOT240310', 'Mindray', '2026-12-31', 90, 176.0, 'ML', 45000.0, 'IN_STOCK', '2026-04-01', 'admin', 'R1:4×35mL+R2:2×18mL комплект', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- Glu-G (Глюкоза GOD)
INSERT INTO reagent_inventory (id, analyzer_id, reagent_name, lot_number, manufacturer, expiry_date_sealed, stability_days_after_opening, total_volume_ml, unit_type, unit_price_tenge, status, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('inv-glu-bs230-001', 'mindray-bs-230', 'Mindray Glu-G (Глюкоза GOD)', 'LOT240311', 'Mindray', '2026-12-31', 90, 200.0, 'ML', 42000.0, 'IN_STOCK', '2026-04-01', 'admin', 'R1:4×40mL+R2:2×20mL комплект', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('inv-glu-bs240-001', 'mindray-bs-240', 'Mindray Glu-G (Глюкоза GOD)', 'LOT240312', 'Mindray', '2026-12-31', 90, 200.0, 'ML', 42000.0, 'IN_STOCK', '2026-04-01', 'admin', 'R1:4×40mL+R2:2×20mL комплект', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- CREA-S (Креатинин)
INSERT INTO reagent_inventory (id, analyzer_id, reagent_name, lot_number, manufacturer, expiry_date_sealed, stability_days_after_opening, total_volume_ml, unit_type, unit_price_tenge, status, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('inv-crea-bs230-001', 'mindray-bs-230', 'Mindray CREA-S (Креатинин)', 'LOT240313', 'Mindray', '2026-12-31', 90, 72.0, 'ML', 55000.0, 'IN_STOCK', '2026-04-01', 'admin', 'R1:2×27mL+R2:1×18mL комплект', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('inv-crea-bs240-001', 'mindray-bs-240', 'Mindray CREA-S (Креатинин)', 'LOT240314', 'Mindray', '2026-12-31', 90, 72.0, 'ML', 55000.0, 'IN_STOCK', '2026-04-01', 'admin', 'R1:2×27mL+R2:1×18mL комплект', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- UREA (Мочевина)
INSERT INTO reagent_inventory (id, analyzer_id, reagent_name, lot_number, manufacturer, expiry_date_sealed, stability_days_after_opening, total_volume_ml, unit_type, unit_price_tenge, status, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('inv-urea-bs230-001', 'mindray-bs-230', 'Mindray UREA (Мочевина)', 'LOT240315', 'Mindray', '2026-12-31', 90, 176.0, 'ML', 40000.0, 'IN_STOCK', '2026-04-01', 'admin', 'R1:4×35mL+R2:2×18mL комплект', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('inv-urea-bs240-001', 'mindray-bs-240', 'Mindray UREA (Мочевина)', 'LOT240316', 'Mindray', '2026-12-31', 90, 176.0, 'ML', 40000.0, 'IN_STOCK', '2026-04-01', 'admin', 'R1:4×35mL+R2:2×18mL комплект', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- TP (Общий белок)
INSERT INTO reagent_inventory (id, analyzer_id, reagent_name, lot_number, manufacturer, expiry_date_sealed, stability_days_after_opening, total_volume_ml, unit_type, unit_price_tenge, status, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('inv-tp-bs230-001', 'mindray-bs-230', 'Mindray TP (Общий белок)', 'LOT240317', 'Mindray', '2026-12-31', 90, 160.0, 'ML', 35000.0, 'IN_STOCK', '2026-04-01', 'admin', 'R:4×40mL комплект', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('inv-tp-bs240-001', 'mindray-bs-240', 'Mindray TP (Общий белок)', 'LOT240318', 'Mindray', '2026-12-31', 90, 160.0, 'ML', 35000.0, 'IN_STOCK', '2026-04-01', 'admin', 'R:4×40mL комплект', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- Bil-T (Билирубин общий VOX)
INSERT INTO reagent_inventory (id, analyzer_id, reagent_name, lot_number, manufacturer, expiry_date_sealed, stability_days_after_opening, total_volume_ml, unit_type, unit_price_tenge, status, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('inv-bilt-bs230-001', 'mindray-bs-230', 'Mindray Bil-T (Билирубин общий VOX)', 'LOT240319', 'Mindray', '2026-12-31', 90, 176.0, 'ML', 52000.0, 'IN_STOCK', '2026-04-01', 'admin', 'R1:4×35mL+R2:2×18mL комплект', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- Bil-D (Билирубин прямой VOX)
INSERT INTO reagent_inventory (id, analyzer_id, reagent_name, lot_number, manufacturer, expiry_date_sealed, stability_days_after_opening, total_volume_ml, unit_type, unit_price_tenge, status, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('inv-bild-bs230-001', 'mindray-bs-230', 'Mindray Bil-D (Билирубин прямой VOX)', 'LOT240320', 'Mindray', '2026-12-31', 90, 176.0, 'ML', 52000.0, 'IN_STOCK', '2026-04-01', 'admin', 'R1:4×35mL+R2:2×18mL комплект', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Section 2: Lipid Panel Reagents Inventory
-- =============================================================================

-- TC (Общий холестерин)
INSERT INTO reagent_inventory (id, analyzer_id, reagent_name, lot_number, manufacturer, expiry_date_sealed, stability_days_after_opening, total_volume_ml, unit_type, unit_price_tenge, status, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('inv-tc-bs230-001', 'mindray-bs-230', 'Mindray TC (Общий холестерин)', 'LOT240321', 'Mindray', '2026-12-31', 90, 160.0, 'ML', 38000.0, 'IN_STOCK', '2026-04-01', 'admin', 'R:4×40mL комплект', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- TG (Триглицериды)
INSERT INTO reagent_inventory (id, analyzer_id, reagent_name, lot_number, manufacturer, expiry_date_sealed, stability_days_after_opening, total_volume_ml, unit_type, unit_price_tenge, status, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('inv-tg-bs230-001', 'mindray-bs-230', 'Mindray TG (Триглицериды)', 'LOT240322', 'Mindray', '2026-12-31', 90, 160.0, 'ML', 38000.0, 'IN_STOCK', '2026-04-01', 'admin', 'R:4×40mL комплект', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- HDL-C (Холестерин ЛПВП)
INSERT INTO reagent_inventory (id, analyzer_id, reagent_name, lot_number, manufacturer, expiry_date_sealed, stability_days_after_opening, total_volume_ml, unit_type, unit_price_tenge, status, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('inv-hdl-bs230-001', 'mindray-bs-230', 'Mindray HDL-C (Холестерин ЛПВП)', 'LOT240323', 'Mindray', '2026-12-31', 90, 54.0, 'ML', 68000.0, 'IN_STOCK', '2026-04-01', 'admin', 'R1:1×40mL+R2:1×14mL комплект', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- LDL-C (Холестерин ЛПНП)
INSERT INTO reagent_inventory (id, analyzer_id, reagent_name, lot_number, manufacturer, expiry_date_sealed, stability_days_after_opening, total_volume_ml, unit_type, unit_price_tenge, status, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('inv-ldl-bs230-001', 'mindray-bs-230', 'Mindray LDL-C (Холестерин ЛПНП)', 'LOT240324', 'Mindray', '2026-12-31', 90, 54.0, 'ML', 68000.0, 'IN_STOCK', '2026-04-01', 'admin', 'R1:1×40mL+R2:1×14mL комплект', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Section 3: Service Reagents and Detergents
-- =============================================================================

-- CD80 Detergent (общий для всех BS-серии)
INSERT INTO reagent_inventory (id, analyzer_id, reagent_name, lot_number, manufacturer, expiry_date_sealed, stability_days_after_opening, total_volume_ml, unit_type, unit_price_tenge, status, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('inv-cd80-common-001', NULL, 'Mindray CD80 Detergent (Моющий раствор)', 'LOT240325', 'Mindray', '2027-06-30', 365, 1000.0, 'ML', 15000.0, 'IN_STOCK', '2026-04-01', 'admin', 'Универсальный моющий раствор CD80 1L для BS-серии', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('inv-cd80-common-002', NULL, 'Mindray CD80 Detergent (Моющий раствор)', 'LOT240326', 'Mindray', '2027-06-30', 365, 1000.0, 'ML', 15000.0, 'IN_STOCK', '2026-04-01', 'admin', 'Универсальный моющий раствор CD80 1L для BS-серии', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Section 4: Consumables - Cuvettes
-- =============================================================================

INSERT INTO consumable_inventory (id, name, category, linked_analyzer_types, quantity_total, quantity_remaining, unit_price_tenge, lot_number, expiry_date, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('inv-cuv-bs230-001', 'Mindray Cuvettes For BS-230 (Кюветы)', 'CUVETTE', 'BIOCHEMISTRY', 5000, 4850, 25.0, 'LOT240327', '2027-12-31', '2026-04-01', 'admin', 'Одноразовые кюветы для BS-230, упаковка 5000 шт', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('inv-cuv-bs240-001', 'Mindray Cuvettes For BS-240 (Кюветы)', 'CUVETTE', 'BIOCHEMISTRY', 5000, 4900, 25.0, 'LOT240328', '2027-12-31', '2026-04-01', 'admin', 'Одноразовые кюветы для BS-240, упаковка 5000 шт', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('inv-cuv-bs240pro-001', 'Mindray Cuvettes For BS-240Pro (Кюветы)', 'CUVETTE', 'BIOCHEMISTRY', 5000, 5000, 25.0, 'LOT240329', '2027-12-31', '2026-04-01', 'admin', 'Одноразовые кюветы для BS-240Pro, упаковка 5000 шт', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('inv-cuv-bs430-001', 'Mindray Cuvettes For BS-430 (Кюветы)', 'CUVETTE', 'BIOCHEMISTRY', 10000, 9800, 22.0, 'LOT240330', '2027-12-31', '2026-04-01', 'admin', 'Одноразовые кюветы для BS-430, упаковка 10000 шт', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;
