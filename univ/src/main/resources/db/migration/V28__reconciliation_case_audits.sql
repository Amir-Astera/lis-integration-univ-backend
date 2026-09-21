-- Persistent human audit layer over automatically generated reconciliation rows.
-- The parsed sample identifier is intentionally not a foreign key: a log may be
-- re-parsed during a correction, while the auditor's decision history must remain.

CREATE TABLE IF NOT EXISTS reconciliation_cases (
    id                  VARCHAR(36) PRIMARY KEY,
    parsed_sample_id    VARCHAR(36) NOT NULL UNIQUE,
    workflow_status     VARCHAR(32) NOT NULL DEFAULT 'OPEN'
        CHECK (workflow_status IN ('OPEN', 'IN_REVIEW', 'CLOSED')),
    conclusion          VARCHAR(64),
    assigned_to         VARCHAR(255),
    latest_comment      TEXT,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(255),
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(255),
    version             BIGINT
);

CREATE INDEX IF NOT EXISTS idx_reconciliation_cases_status
    ON reconciliation_cases(workflow_status, updated_at DESC);

CREATE TABLE IF NOT EXISTS reconciliation_case_events (
    id                  VARCHAR(36) PRIMARY KEY,
    case_id             VARCHAR(36) NOT NULL
        REFERENCES reconciliation_cases(id) ON DELETE CASCADE,
    workflow_status     VARCHAR(32) NOT NULL
        CHECK (workflow_status IN ('OPEN', 'IN_REVIEW', 'CLOSED')),
    conclusion          VARCHAR(64),
    comment             TEXT,
    assigned_to         VARCHAR(255),
    changed_by          VARCHAR(255),
    changed_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_reconciliation_case_events_case
    ON reconciliation_case_events(case_id, changed_at DESC);

COMMENT ON TABLE reconciliation_cases IS
    'Current human workflow state for a reconciliation case. Automatic source facts remain immutable in analyzer and journal tables.';

COMMENT ON TABLE reconciliation_case_events IS
    'Append-only history of auditor decisions and comments for reconciliation cases.';
