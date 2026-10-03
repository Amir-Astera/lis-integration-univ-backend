CREATE TABLE IF NOT EXISTS onec_lims_item_mappings (
    id                    VARCHAR(36) PRIMARY KEY,
    snapshot_id           VARCHAR(36) NOT NULL
        REFERENCES onec_readonly_snapshots(id) ON DELETE CASCADE,
    onec_item_ref         VARCHAR(128),
    onec_item_code        VARCHAR(255),
    onec_item_name        VARCHAR(500) NOT NULL,
    target_kind           VARCHAR(32) NOT NULL
        CHECK (target_kind IN ('REAGENT', 'CONSUMABLE')),
    target_name           VARCHAR(500) NOT NULL,
    target_unit           VARCHAR(32) NOT NULL,
    conversion_factor     NUMERIC(18,6) NOT NULL DEFAULT 1,
    mapping_status        VARCHAR(32) NOT NULL DEFAULT 'SUGGESTED'
        CHECK (mapping_status IN ('SUGGESTED', 'CONFIRMED', 'DISABLED')),
    notes                 TEXT,
    created_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by            VARCHAR(255),
    updated_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by            VARCHAR(255),
    version               BIGINT,
    CONSTRAINT uq_onec_lims_mapping UNIQUE (snapshot_id, onec_item_ref, target_name)
);

CREATE INDEX IF NOT EXISTS idx_onec_lims_item_mappings_snapshot_status
    ON onec_lims_item_mappings(snapshot_id, mapping_status);

COMMENT ON TABLE onec_lims_item_mappings IS
    'Explicitly confirmed mapping from sanitized 1C item snapshots to LIMS reagents/consumables. No fuzzy mapping is trusted for planning.';
