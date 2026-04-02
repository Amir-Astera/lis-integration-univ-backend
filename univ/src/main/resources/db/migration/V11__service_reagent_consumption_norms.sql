-- Migration: V11 - Service Reagent Consumption Norms
-- Purpose: Link Damumed services (lab tests) to reagents and consumables for automatic consumption calculation
-- Created: 2026-04-02

-- =============================================================================
-- Table: service_reagent_consumption_norms
-- Maps laboratory services (from Damumed reports) to reagents/consumables consumption
-- =============================================================================

CREATE TABLE IF NOT EXISTS service_reagent_consumption_norms (
    -- Primary key
    id VARCHAR(36) PRIMARY KEY,

    -- Service identification (from Damumed report)
    service_name VARCHAR(500) NOT NULL,           -- Exact name from Damumed (e.g., "Общий анализ крови на анализаторе...")
    service_name_normalized VARCHAR(500) NOT NULL,  -- Normalized for matching
    service_category VARCHAR(100),                  -- Гематология, Биохимия, etc.

    -- Analyzer reference (optional - some services are manual)
    analyzer_id VARCHAR(36),                        -- FK to analyzers (NULL for manual methods)

    -- Reagent/Consumable identification
    reagent_name VARCHAR(255) NOT NULL,             -- Name of reagent or consumable
    consumable_id VARCHAR(36),                      -- FK to consumable_inventory (if applicable)

    -- Consumption per service execution
    quantity_per_service DECIMAL(10, 4) NOT NULL,   -- How much is consumed per 1 test
    unit_type VARCHAR(20) NOT NULL                  -- ML, PIECE, TEST_POSITION
        CHECK (unit_type IN ('ML', 'PIECE', 'TEST_POSITION')),

    -- Source and validation
    source VARCHAR(50) NOT NULL DEFAULT 'MANUAL',   -- MANUAL, CALCULATED_FROM_ANALYZER_RATE, DOCUMENT_BASED
    source_document VARCHAR(500),                   -- Reference to document/methodology
    notes TEXT,                                     -- Additional notes
    is_active BOOLEAN NOT NULL DEFAULT TRUE,        -- Can be disabled if obsolete

    -- Timestamps and versioning
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,

    -- Constraints
    CONSTRAINT chk_quantity_positive CHECK (quantity_per_service > 0),
    CONSTRAINT fk_service_norm_analyzer FOREIGN KEY (analyzer_id)
        REFERENCES analyzers(id) ON DELETE SET NULL,
    CONSTRAINT fk_service_norm_consumable FOREIGN KEY (consumable_id)
        REFERENCES consumable_inventory(id) ON DELETE SET NULL
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_service_norms_service_name
    ON service_reagent_consumption_norms(service_name_normalized);

CREATE INDEX IF NOT EXISTS idx_service_norms_category
    ON service_reagent_consumption_norms(service_category);

CREATE INDEX IF NOT EXISTS idx_service_norms_analyzer
    ON service_reagent_consumption_norms(analyzer_id) WHERE analyzer_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_service_norms_active
    ON service_reagent_consumption_norms(is_active) WHERE is_active = TRUE;

CREATE INDEX IF NOT EXISTS idx_service_norms_reagent
    ON service_reagent_consumption_norms(reagent_name);

-- Composite index for the most common query pattern
CREATE INDEX IF NOT EXISTS idx_service_norms_lookup
    ON service_reagent_consumption_norms(service_name_normalized, is_active, analyzer_id);

-- =============================================================================
-- Table: damumed_report_reagent_consumption
-- Stores calculated reagent consumption derived from Damumed reports
-- Links normalized_facts to calculated consumption
-- =============================================================================

CREATE TABLE IF NOT EXISTS damumed_report_reagent_consumption (
    id VARCHAR(36) PRIMARY KEY,

    -- Link to report upload
    upload_id VARCHAR(36) NOT NULL,

    -- Link to normalized fact (the specific service entry)
    fact_id VARCHAR(36),                            -- FK to damumed_report_normalized_facts

    -- Service details
    service_name VARCHAR(500) NOT NULL,
    service_category VARCHAR(100),
    completed_count INTEGER NOT NULL DEFAULT 1,   -- How many times service was performed

    -- Calculated consumption (JSON for flexibility - can contain multiple reagents)
    consumption_json TEXT NOT NULL,                 -- Array of consumption entries

    -- Cost calculation
    total_estimated_cost_tenge DECIMAL(15, 2) DEFAULT 0,

    -- Analyzer reference (if auto-detected from service name)
    detected_analyzer_id VARCHAR(36),
    detection_confidence VARCHAR(20),               -- HIGH, MEDIUM, LOW, MANUAL

    -- Timestamps
    calculated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    calculated_by VARCHAR(100),                     -- User or system process

    -- Versioning
    version BIGINT DEFAULT 0,

    -- Constraints
    CONSTRAINT chk_completed_count_positive CHECK (completed_count >= 0),
    CONSTRAINT chk_cost_non_negative CHECK (total_estimated_cost_tenge >= 0),
    CONSTRAINT fk_consumption_upload FOREIGN KEY (upload_id)
        REFERENCES damumed_report_uploads(id) ON DELETE CASCADE,
    CONSTRAINT fk_consumption_fact FOREIGN KEY (fact_id)
        REFERENCES damumed_report_normalized_facts(id) ON DELETE SET NULL,
    CONSTRAINT fk_consumption_analyzer FOREIGN KEY (detected_analyzer_id)
        REFERENCES analyzers(id) ON DELETE SET NULL
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_damumed_consumption_upload
    ON damumed_report_reagent_consumption(upload_id);

CREATE INDEX IF NOT EXISTS idx_damumed_consumption_fact
    ON damumed_report_reagent_consumption(fact_id) WHERE fact_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_damumed_consumption_service
    ON damumed_report_reagent_consumption(service_name);

CREATE INDEX IF NOT EXISTS idx_damumed_consumption_calculated
    ON damumed_report_reagent_consumption(calculated_at);

-- =============================================================================
-- Table: service_to_analyzer_mappings
-- Helper table for auto-detecting which analyzer performs which service
-- =============================================================================

CREATE TABLE IF NOT EXISTS service_to_analyzer_mappings (
    id VARCHAR(36) PRIMARY KEY,

    service_name_pattern VARCHAR(500) NOT NULL,     -- Pattern to match (can use wildcards)
    service_category VARCHAR(100),
    analyzer_id VARCHAR(36) NOT NULL,               -- FK to analyzers

    matching_priority INTEGER DEFAULT 100,            -- Lower = higher priority for conflicts
    is_active BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,

    CONSTRAINT fk_mapping_analyzer FOREIGN KEY (analyzer_id)
        REFERENCES analyzers(id) ON DELETE CASCADE,
    CONSTRAINT chk_priority_positive CHECK (matching_priority > 0)
);

CREATE INDEX IF NOT EXISTS idx_service_analyzer_mapping_pattern
    ON service_to_analyzer_mappings(service_name_pattern);

CREATE INDEX IF NOT EXISTS idx_service_analyzer_mapping_category
    ON service_to_analyzer_mappings(service_category) WHERE service_category IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_service_analyzer_mapping_active
    ON service_to_analyzer_mappings(is_active) WHERE is_active = TRUE;

-- =============================================================================
-- Comments for documentation
-- =============================================================================

COMMENT ON TABLE service_reagent_consumption_norms IS
    'Defines how much reagent/consumable is used per laboratory service execution. Links Damumed services to inventory items.';

COMMENT ON TABLE damumed_report_reagent_consumption IS
    'Calculated reagent consumption derived from Damumed report data. Stores per-fact or aggregated consumption.';

COMMENT ON TABLE service_to_analyzer_mappings IS
    'Auto-mapping rules to determine which analyzer performs a given service based on service name patterns.';
