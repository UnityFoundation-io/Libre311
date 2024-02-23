DELETE FROM jurisdiction_user;
DELETE FROM services;
DELETE FROM remote_hosts;
DELETE FROM jurisdictions;
DELETE FROM app_users;

-- Stl subtenant jurisdiction admin
INSERT INTO app_users (id, email)
VALUES (1, 'stl_subtenant_admin@example.com');

-- Add St. Louis Metro Area Jurisdiction (tenant_id must match the Unity Auth data)
INSERT INTO jurisdictions (id, name, tenant_id)
VALUES ('stlma', 'St. Louis Metro Area', 1);

-- map the host to the jurisdiction
INSERT INTO remote_hosts (name, jurisdiction_id)
VALUES ('localhost', 'stlma');

-- make the app user 'stl_subtenant_admin@example.com' an admin for the stlma jurisdiction
INSERT INTO jurisdiction_user (user_id, jurisdiction_id, isUserAdmin)
VALUES (1, 'stlma', TRUE);


-- Bus Stop Service
INSERT INTO services (service_code, jurisdiction_id, service_name, description, type, service_definition_json, metadata)
VALUES ('202', 'stlma', 'Bus Stop', 'For problems with bus stops', 'REALTIME',
        '{"service_code": "202", "attributes": [{"variable": true, "code": "BUS_STOP", "datatype": "multivaluelist", "required": true, "datatype_description": "Please select one or more items.", "order": 1, "description": "Please select one or more items that best describe the issue. If Other, please elaborate in the Description field below.", "values": [{"key": "UNSAFE", "name": "Unsafe location"}, {"key": "NO_SDWLK", "name": "No sidewalk"}, {"key": "MISSING_SIGN", "name": "Sign missing"}, {"key": "NO_SHELTER", "name": "No Shelter"}, {"key": "OTHER", "name": "Other"}]}]}',
        true);

-- Crosswalk Service
INSERT INTO services (service_code, jurisdiction_id, service_name, description, type, service_definition_json, metadata)
VALUES ('204', 'stlma', 'Crosswalk', 'For issues with the crosswalks', 'REALTIME',
        '{"service_code": "204", "attributes": [{"variable": true, "code": "CRSWLK", "datatype": "multivaluelist", "required": true, "datatype_description": "Please select one or more items.", "order": 1, "description": "Please select one or more items that best describe the issue. If Other, please elaborate in the Description field below.", "values": [{"key": "ADA_ACCESS", "name": "ADA Access"}, {"key": "MISSING", "name": "Missing"}, {"key": "FADED", "name": "Faded or worn paint"}, {"key": "DRIVERS", "name": "Drivers failing to yield"}, {"key": "OTHER", "name": "Other"}]}]}',
        true);

-- Other Service
INSERT INTO services (service_code, jurisdiction_id, service_name, description, type, service_definition_json, metadata)
VALUES ('206', 'stlma', 'Other', 'Other', 'REALTIME', '{"service_code": "206", "attributes": []}', true);
