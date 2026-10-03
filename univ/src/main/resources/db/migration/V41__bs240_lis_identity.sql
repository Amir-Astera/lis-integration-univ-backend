-- V41: observed LIS identity for the physical Mindray BS-240.
-- Evidence: real BS-240 Applogs Save string JSON (WorkPlaceID 20747,
-- AnalyzerId 38) and errors.xml Device@SystemName=20747.

UPDATE analyzers
SET lis_device_system_name = '20747',
    lis_analyzer_id = 38,
    lis_device_name = COALESCE(lis_device_name, 'BS-240'),
    notes = CONCAT_WS(E'\n', NULLIF(notes, ''), 'Observed LIS identity: WorkPlaceID=20747, AnalyzerId=38.'),
    updated_at = CURRENT_TIMESTAMP
WHERE id = 'mindray-bs-240';
