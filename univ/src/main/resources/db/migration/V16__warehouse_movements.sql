-- Migration: V16 - Warehouse Movements
-- Purpose: Full-featured warehouse ledger for reagent & consumable movements (receipts, write-offs, transfers, returns)

CREATE TABLE IF NOT EXISTS warehouse_movements (
    id                  VARCHAR(36) PRIMARY KEY,
    movement_type       VARCHAR(30) NOT NULL
        CHECK (movement_type IN ('RECEIPT', 'WRITE_OFF', 'ADJUSTMENT', 'TRANSFER', 'RETURN', 'INVENTORY_CORRECTION')),
    item_type           VARCHAR(20) NOT NULL
        CHECK (item_type IN ('REAGENT', 'CONSUMABLE')),
    reagent_id          VARCHAR(128) NULL,
    consumable_id       VARCHAR(128) NULL,
    analyzer_id         VARCHAR(128) NULL,

    quantity            DOUBLE PRECISION NOT NULL,
    unit_type           VARCHAR(32) NOT NULL DEFAULT 'UNITS',
    unit_price_tenge    DOUBLE PRECISION NULL,
    total_cost_tenge    DOUBLE PRECISION GENERATED ALWAYS AS (quantity * unit_price_tenge) STORED,

    lot_number          VARCHAR(128) NULL,
    expiry_date         DATE NULL,
    supplier            VARCHAR(255) NULL,
    invoice_number      VARCHAR(128) NULL,
    reference_id        VARCHAR(36) NULL,
    reason              TEXT NULL,
    notes               TEXT NULL,

    performed_by        VARCHAR(255) NOT NULL,
    movement_date       DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_wm_reagent FOREIGN KEY (reagent_id)
        REFERENCES reagent_inventory (id) ON DELETE SET NULL,
    CONSTRAINT fk_wm_consumable FOREIGN KEY (consumable_id)
        REFERENCES consumable_inventory (id) ON DELETE SET NULL,
    CONSTRAINT fk_wm_analyzer FOREIGN KEY (analyzer_id)
        REFERENCES analyzers (id) ON DELETE SET NULL,
    CONSTRAINT chk_wm_item_set
        CHECK (reagent_id IS NOT NULL OR consumable_id IS NOT NULL)
);

CREATE INDEX IF NOT EXISTS idx_wm_movement_date ON warehouse_movements (movement_date DESC);
CREATE INDEX IF NOT EXISTS idx_wm_movement_type ON warehouse_movements (movement_type);
CREATE INDEX IF NOT EXISTS idx_wm_reagent ON warehouse_movements (reagent_id) WHERE reagent_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_wm_consumable ON warehouse_movements (consumable_id) WHERE consumable_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_wm_analyzer ON warehouse_movements (analyzer_id) WHERE analyzer_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_wm_item_type ON warehouse_movements (item_type);

-- Daily snapshot table for fast warehouse dashboard queries
CREATE TABLE IF NOT EXISTS warehouse_daily_snapshots (
    id                      VARCHAR(36) PRIMARY KEY,
    snapshot_date           DATE NOT NULL,
    item_type               VARCHAR(20) NOT NULL CHECK (item_type IN ('REAGENT', 'CONSUMABLE')),
    reagent_id              VARCHAR(128) NULL,
    consumable_id           VARCHAR(128) NULL,
    item_name               VARCHAR(255) NOT NULL,
    analyzer_id             VARCHAR(128) NULL,

    opening_quantity        DOUBLE PRECISION NOT NULL DEFAULT 0,
    receipts_quantity       DOUBLE PRECISION NOT NULL DEFAULT 0,
    write_offs_quantity     DOUBLE PRECISION NOT NULL DEFAULT 0,
    adjustments_quantity    DOUBLE PRECISION NOT NULL DEFAULT 0,
    closing_quantity        DOUBLE PRECISION NOT NULL DEFAULT 0,
    unit_type               VARCHAR(32) NOT NULL DEFAULT 'UNITS',
    closing_cost_tenge      DOUBLE PRECISION NULL,

    low_stock_flag          BOOLEAN NOT NULL DEFAULT FALSE,
    expiry_warning_flag     BOOLEAN NOT NULL DEFAULT FALSE,

    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_wds_reagent FOREIGN KEY (reagent_id)
        REFERENCES reagent_inventory (id) ON DELETE CASCADE,
    CONSTRAINT fk_wds_consumable FOREIGN KEY (consumable_id)
        REFERENCES consumable_inventory (id) ON DELETE CASCADE,
    CONSTRAINT uq_wds_date_reagent
        UNIQUE (snapshot_date, reagent_id) DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT uq_wds_date_consumable
        UNIQUE (snapshot_date, consumable_id) DEFERRABLE INITIALLY DEFERRED
);

CREATE INDEX IF NOT EXISTS idx_wds_snapshot_date ON warehouse_daily_snapshots (snapshot_date DESC);
CREATE INDEX IF NOT EXISTS idx_wds_low_stock ON warehouse_daily_snapshots (low_stock_flag) WHERE low_stock_flag = TRUE;
CREATE INDEX IF NOT EXISTS idx_wds_expiry_warning ON warehouse_daily_snapshots (expiry_warning_flag) WHERE expiry_warning_flag = TRUE;
