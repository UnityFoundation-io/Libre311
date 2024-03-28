-- Stl subtenant jurisdiction admin
INSERT IGNORE INTO app_users (id, email)
VALUES (1, 'stl_subtenant_admin@example.com');

-- Add St. Louis Metro Area Jurisdiction (tenant_id must match the Unity Auth data)
INSERT IGNORE INTO jurisdictions (id, name, tenant_id)
VALUES ('stlma', 'St. Louis Metro Area', 1);

-- Add jurisdiction boundary (POLYGON fn
INSERT INTO jurisdiction_boundary (boundary, jurisdiction_id)
VALUES (ST_GeomFromText('POLYGON((
        38.88908245157475, -90.82207996696539,
        38.28511105115126, -90.32668241294714,
        38.73098601356233, -89.86006757704696,
        39.04413540068816, -90.36058752072049,
        38.88908245157475, -90.82207996696539))', 4326),
        'stlma');


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
INSERT IGNORE INTO services (id, service_code, jurisdiction_id, service_name, description, type, service_group_id)
VALUES (1, '202', 'stlma', 'Bus Stop', 'For problems with bus stops', 'REALTIME', 1);

INSERT IGNORE INTO service_definition_attributes (id, attribute_order, code, datatype, datatype_description, description, required, variable, service_id)
VALUES (1, 1, 'BUS_STOP', 'MULTIVALUELIST', 'Please select one or more items.', 'Please select one or more items that best describe the issue. If Other, please elaborate in the Description field below.', true, true, 1);

INSERT IGNORE INTO service_definition_attribute_values (id, value_name, service_definition_attribute_id)
VALUES (1, 'Unsafe location', 1),
       (2, 'No sidewalk', 1),
       (3, 'Sign missing', 1),
       (4, 'No Shelter', 1),
       (5, 'Other', 1);


-- Crosswalk Service
INSERT IGNORE INTO services (id, service_code, jurisdiction_id, service_name, description, type, service_group_id)
VALUES (2, '204', 'stlma', 'Crosswalk', 'For issues with the crosswalks', 'REALTIME', 1);

INSERT IGNORE INTO service_definition_attributes (id, attribute_order, code, datatype, datatype_description, description, required, variable, service_id)
VALUES (2, 1, 'CRSWLK', 'MULTIVALUELIST', 'Please select one or more items.', 'Please select one or more items that best describe the issue. If Other, please elaborate in the Description field below.', true, true, 2);

INSERT IGNORE INTO service_definition_attribute_values (id, value_name, service_definition_attribute_id)
VALUES (6, 'ADA Access', 2),
       (7, 'Missing', 2),
       (8, 'Faded or worn paint', 2),
       (9, 'Drivers failing to yieldr', 2),
       (10, 'Other', 2);


-- Other Service
INSERT IGNORE INTO services (id, service_code, jurisdiction_id, service_name, description, type, service_group_id)
VALUES (3, '206', 'stlma', 'Other', 'Other', 'REALTIME', 1);
