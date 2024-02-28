-- Stl subtenant jurisdiction admin
INSERT IGNORE INTO app_users (id, email)
VALUES (1, 'stl_subtenant_admin@example.com');

-- Add St. Louis Metro Area Jurisdiction (tenant_id must match the Unity Auth data)
INSERT IGNORE INTO jurisdictions (id, name, tenant_id)
VALUES ('stlma', 'St. Louis Metro Area', 1);

-- Add jurisdiction boundary
INSERT IGNORE INTO boundary_coordinates (id, latitude, longitude, jurisdiction_id)
VALUES (1, -90.30025693587594, 38.68777201455936, 'stlma'),
       (2, -90.34433315946103, 38.61729515893717, 'stlma'),
       (3, -90.2360192439154, 38.61061640035771, 'stlma'),
       (4, -90.22097070151844, 38.7066943276854, 'stlma'),
       (5, -90.30025693587594, 38.68777201455936, 'stlma');

-- map the host to the jurisdiction
INSERT IGNORE INTO remote_hosts (id, name, jurisdiction_id)
VALUES (1, 'localhost', 'stlma');

-- make the app user 'stl_subtenant_admin@example.com' an admin for the stlma jurisdiction
INSERT IGNORE INTO jurisdiction_user (id, user_id, jurisdiction_id, isUserAdmin)
VALUES (1, 1, 'stlma', TRUE);

-- Stl subtenant jurisdiction admin
INSERT IGNORE INTO service_groups (id, name, jurisdiction_id)
VALUES (1, 'Infrastructure', 'stlma');

-- Bus Stop Service
INSERT IGNORE INTO services (id, service_code, jurisdiction_id, service_name, description, type, service_definition_json, metadata, service_group_id)
VALUES (1, '202', 'stlma', 'Bus Stop', 'For problems with bus stops', 'REALTIME',
        '{"service_code": "202", "attributes": [{"variable": true, "code": "BUS_STOP", "datatype": "multivaluelist", "required": true, "datatype_description": "Please select one or more items.", "order": 1, "description": "Please select one or more items that best describe the issue. If Other, please elaborate in the Description field below.", "values": [{"key": "UNSAFE", "name": "Unsafe location"}, {"key": "NO_SDWLK", "name": "No sidewalk"}, {"key": "MISSING_SIGN", "name": "Sign missing"}, {"key": "NO_SHELTER", "name": "No Shelter"}, {"key": "OTHER", "name": "Other"}]}]}',
        true, 1);

-- Crosswalk Service
INSERT IGNORE INTO services (id, service_code, jurisdiction_id, service_name, description, type, service_definition_json, metadata, service_group_id)
VALUES (2, '204', 'stlma', 'Crosswalk', 'For issues with the crosswalks', 'REALTIME',
        '{"service_code": "204", "attributes": [{"variable": true, "code": "CRSWLK", "datatype": "multivaluelist", "required": true, "datatype_description": "Please select one or more items.", "order": 1, "description": "Please select one or more items that best describe the issue. If Other, please elaborate in the Description field below.", "values": [{"key": "ADA_ACCESS", "name": "ADA Access"}, {"key": "MISSING", "name": "Missing"}, {"key": "FADED", "name": "Faded or worn paint"}, {"key": "DRIVERS", "name": "Drivers failing to yield"}, {"key": "OTHER", "name": "Other"}]}]}',
        true, 1);

-- Other Service
INSERT IGNORE INTO services (id, service_code, jurisdiction_id, service_name, description, type, service_definition_json, metadata, service_group_id)
VALUES (3, '206', 'stlma', 'Other', 'Other', 'REALTIME', '{"service_code": "206", "attributes": []}', true, 1);
