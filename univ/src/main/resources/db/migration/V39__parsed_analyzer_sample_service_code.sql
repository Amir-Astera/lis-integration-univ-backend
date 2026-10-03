-- V39: persist the Damumed/NMU service code carried by analyzer Save string.
-- The field is sourced from OrderResearch.ServiceMo.Code, for example
-- B03.115.002. It is not inferred from a name or tariff lookup.

ALTER TABLE parsed_analyzer_samples
    ADD COLUMN IF NOT EXISTS service_code VARCHAR(64);

CREATE INDEX IF NOT EXISTS idx_parsed_analyzer_samples_service_code
    ON parsed_analyzer_samples(service_code)
    WHERE service_code IS NOT NULL;
