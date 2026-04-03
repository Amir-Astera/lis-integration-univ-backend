-- V19: Analyzer-to-Reagent Inventory Links
-- Purpose: Explicitly map analyzers to their assigned reagent inventory items.
-- This completes the chain: Damumed Service → Analyzer → Reagent Inventory.
-- Previously, reagent_inventory had a single analyzer_id column (1-to-1).
-- This table allows M-to-M: one analyzer can use many reagents, one reagent
-- can be shared across multiple analyzers (e.g., saline for several instruments).

CREATE TABLE IF NOT EXISTS analyzer_reagent_links (
    id                  VARCHAR(36) PRIMARY KEY,
    analyzer_id         VARCHAR(36) NOT NULL,
    reagent_inventory_id VARCHAR(128) NOT NULL,
    -- Role of this reagent for the analyzer
    usage_role          VARCHAR(50) NOT NULL DEFAULT 'MAIN'
        CHECK (usage_role IN ('MAIN', 'DILUENT', 'WASH', 'CALIBRATOR', 'CONTROL', 'OTHER')),
    -- Optional: link to a consumption norm that generated this assignment
    norm_reagent_name   VARCHAR(255),
    -- Estimated daily usage volume
    estimated_daily_ml  DOUBLE PRECISION,
    estimated_daily_units INTEGER,
    -- Whether this assignment is currently active
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    notes               TEXT,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(255),
    CONSTRAINT fk_arl_analyzer FOREIGN KEY (analyzer_id)
        REFERENCES analyzers(id) ON DELETE CASCADE,
    CONSTRAINT fk_arl_reagent FOREIGN KEY (reagent_inventory_id)
        REFERENCES reagent_inventory(id) ON DELETE CASCADE,
    CONSTRAINT uq_arl_analyzer_reagent_role UNIQUE (analyzer_id, reagent_inventory_id, usage_role)
);

CREATE INDEX IF NOT EXISTS idx_arl_analyzer_id ON analyzer_reagent_links (analyzer_id);
CREATE INDEX IF NOT EXISTS idx_arl_reagent_id  ON analyzer_reagent_links (reagent_inventory_id);
CREATE INDEX IF NOT EXISTS idx_arl_active      ON analyzer_reagent_links (analyzer_id, is_active);

COMMENT ON TABLE analyzer_reagent_links IS
    'M-to-M mapping between analyzers and their assigned reagent inventory items.';
COMMENT ON COLUMN analyzer_reagent_links.usage_role IS
    'MAIN=primary reagent, DILUENT=diluent/saline, WASH=cleaning agent, '
    'CALIBRATOR=calibration material, CONTROL=QC control, OTHER=misc.';
