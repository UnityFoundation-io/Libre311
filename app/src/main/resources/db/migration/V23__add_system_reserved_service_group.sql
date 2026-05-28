INSERT INTO service_groups (name, jurisdiction_id)
    SELECT 'System Reserved', id
    FROM jurisdictions;
