DELETE FROM user_role;
DELETE FROM role_permission;
DELETE FROM tenant_service;
DELETE FROM user;
DELETE FROM tenant;
DELETE FROM service;
DELETE FROM permission;
DELETE FROM role;

-- Create a tenant
INSERT IGNORE INTO tenant (id, name, description, status) VALUES(1, 'stl', 'St. Louis Metro Area', 'ENABLED');
-- Create Libre311 Service
INSERT IGNORE INTO service (id, name, description, status) VALUES(1, 'Libre311', 'Libre311', 'ENABLED');

-- Add Libre311 Service to stl tenant
INSERT IGNORE INTO tenant_service (tenant_id, service_id, status) VALUES(1, 1, 'ENABLED');


INSERT IGNORE INTO permission (id, name, description, scope)
VALUES (1, 'AUTH_SERVICE_EDIT-SYSTEM', NULL, 'SYSTEM'),
       (2, 'AUTH_SERVICE_VIEW-SYSTEM', NULL, 'SYSTEM'),
       (3, 'AUTH_SERVICE_EDIT-TENANT', NULL, 'TENANT'),
       (4, 'AUTH_SERVICE_VIEW-TENANT', NULL, 'TENANT'),
       (5, 'LIBRE311_ADMIN_EDIT-SYSTEM', NULL, 'SYSTEM'),
       (6, 'LIBRE311_ADMIN_VIEW-SYSTEM', NULL, 'SYSTEM'),
       (7, 'LIBRE311_ADMIN_EDIT-TENANT', NULL, 'TENANT'),
       (8, 'LIBRE311_ADMIN_VIEW-TENANT', NULL, 'TENANT'),
       (9, 'LIBRE311_ADMIN_EDIT-SUBTENANT', NULL, 'SUBTENANT'),
       (10, 'LIBRE311_ADMIN_VIEW-SUBTENANT', NULL, 'SUBTENANT'),
       (11, 'LIBRE311_REQUEST_EDIT-SYSTEM', NULL, 'SYSTEM'),
       (12, 'LIBRE311_REQUEST_VIEW-SYSTEM', NULL, 'SYSTEM'),
       (13, 'LIBRE311_REQUEST_EDIT-TENANT', NULL, 'TENANT'),
       (14, 'LIBRE311_REQUEST_VIEW-TENANT', NULL, 'TENANT'),
       (15, 'LIBRE311_REQUEST_EDIT-SUBTENANT', NULL, 'SUBTENANT'),
       (16, 'LIBRE311_REQUEST_VIEW-SUBTENANT', NULL, 'SUBTENANT');


INSERT IGNORE INTO role (id, name, description)
VALUES (1, 'Unity Administrator', 'An administrator of the Unity Platform. A user with this role can perform any operation.'),
       (2, 'Tenant Administrator', 'An administrator for a tenant. A user with this role can perform any operation for the tenant.'),
       (3, 'Libre311 Administrator', 'An administrator for Libre311. A user with this role can perform any operation in Libre311 on behalf of their tenant.'),
       (4, 'Libre311 Request Manager', 'A service request manager for Libre311. A user with this role can update and manage service requests.'),
       (5, 'Libre311 Jurisdiction Administrator', 'An administrator for Libre311 that is scoped to specific jurisdictions. Additional access must be granted in Libre311 to enable access for specific jurisdictions.'),
       (6, 'Libre311 Jurisdiction Request Manager', 'A service request manager for Libre311 that is scoped to specific jurisdictions. A user with this role can update and manage service requests. Additional access must be granted in Libre311 to enable access for specific jurisdictions.');


-- Unity Administrator
INSERT IGNORE INTO role_permission (role_id, permission_id)
VALUES (1, 1),  -- AUTH_SERVICE_EDIT-SYSTEM
       (1, 2),  -- AUTH_SERVICE_VIEW-SYSTEM
       (1, 5),  -- LIBRE311_ADMIN_EDIT-SYSTEM
       (1, 6),  -- LIBRE311_ADMIN_VIEW-SYSTEM
       (1, 11), -- LIBRE311_REQUEST_EDIT-SYSTEM
       (1, 12); -- LIBRE311_REQUEST_VIEW-SYSTEM

-- Tenant Administrator
INSERT IGNORE INTO role_permission (role_id, permission_id)
VALUES (2, 3),  -- AUTH_SERVICE_EDIT-TENANT
       (2, 4),  -- AUTH_SERVICE_VIEW-TENANT
       (2, 7),  -- LIBRE311_ADMIN_EDIT-TENANT
       (2, 8),  -- LIBRE311_ADMIN_VIEW-TENANT
       (2, 13), -- LIBRE311_REQUEST_EDIT-TENANT
       (2, 14); -- LIBRE311_REQUEST_VIEW-TENANT

-- Libre311 Administrator
INSERT IGNORE INTO role_permission (role_id, permission_id)
VALUES (3, 7),  -- LIBRE311_ADMIN_EDIT-TENANT
       (3, 8),  -- LIBRE311_ADMIN_VIEW-TENANT
       (3, 13), -- LIBRE311_REQUEST_EDIT-TENANT
       (3, 14); -- LIBRE311_REQUEST_VIEW-TENANT

-- Libre311 Request Manager
INSERT IGNORE INTO role_permission (role_id, permission_id)
VALUES (4, 13), -- LIBRE311_REQUEST_EDIT-TENANT
       (4, 14); -- LIBRE311_REQUEST_VIEW-TENANT

-- Libre311 Jurisdiction Administrator
INSERT IGNORE INTO role_permission (role_id, permission_id)
VALUES (5, 9),  -- LIBRE311_ADMIN_EDIT-SUBTENANT
       (5, 10), -- LIBRE311_ADMIN_VIEW-SUBTENANT
       (5, 15), -- LIBRE311_REQUEST_EDIT-SUBTENANT
       (5, 16); -- LIBRE311_REQUEST_VIEW-SUBTENANT

-- Libre311 Jurisdiction Request Manager
INSERT IGNORE INTO role_permission (role_id, permission_id)
VALUES (6, 15), -- LIBRE311_REQUEST_EDIT-SUBTENANT
       (6, 16); -- LIBRE311_REQUEST_VIEW-SUBTENANT


-- Password for all the following accounts is 'test'
-- Unity Administrator
INSERT IGNORE INTO user (id, email, first_name, last_name, password, status) VALUES
    (1, 'unity_admin@example.com', 'Unity', 'Admin', '$2a$10$YJetsyoS.EzlVlb249w07uBR8uSqgtlqVH9Hl7bsHtvvwdKAhJp82', 'ENABLED');

-- Tenant Administrator
INSERT IGNORE INTO user (id, email, first_name, last_name, password, status) VALUES
    (2, 'tenant_admin@example.com', 'Tenant', 'Admin', '$2a$10$YJetsyoS.EzlVlb249w07uBR8uSqgtlqVH9Hl7bsHtvvwdKAhJp82', 'ENABLED');

-- Libre311 Administrator
INSERT IGNORE INTO user (id, email, first_name, last_name, password, status) VALUES
    (3, 'libre311_admin@example.com', 'Libre', 'Admin', '$2a$10$YJetsyoS.EzlVlb249w07uBR8uSqgtlqVH9Hl7bsHtvvwdKAhJp82', 'ENABLED');

-- Libre311 Request Manager
INSERT IGNORE INTO user (id, email, first_name, last_name, password, status) VALUES
    (4, 'libre311_request_manager@example.com', 'Request', 'Manager', '$2a$10$YJetsyoS.EzlVlb249w07uBR8uSqgtlqVH9Hl7bsHtvvwdKAhJp82', 'ENABLED');

-- Libre311 Jurisdiction Administrator
INSERT IGNORE INTO user (id, email, first_name, last_name, password, status) VALUES
    (5, 'libre311_jurisdiction_admin@example.com', 'Jurisdiction', 'Admin', '$2a$10$YJetsyoS.EzlVlb249w07uBR8uSqgtlqVH9Hl7bsHtvvwdKAhJp82', 'ENABLED');

-- Libre311 Jurisdiction Request Manager
INSERT IGNORE INTO user (id, email, first_name, last_name, password, status) VALUES
    (6, 'libre311_jurisdiction_request_manager@example.com', 'Jurisdiction', 'Request Manager', '$2a$10$YJetsyoS.EzlVlb249w07uBR8uSqgtlqVH9Hl7bsHtvvwdKAhJp82', 'ENABLED');

-- Stl sub-tenant admin
INSERT IGNORE INTO user (id, email, first_name, last_name, password, status) VALUES
    (7, 'stl_subtenant_admin@example.com', 'Subtenant', 'Admin', '$2a$10$YJetsyoS.EzlVlb249w07uBR8uSqgtlqVH9Hl7bsHtvvwdKAhJp82', 'ENABLED');


-- Unity Administrator
INSERT IGNORE INTO user_role (tenant_id, user_id, role_id) VALUES
    (1, 1, 1);

-- Tenant Administrator
INSERT IGNORE INTO user_role (tenant_id, user_id, role_id) VALUES
    (1, 2, 2);

-- Libre311 Administrator
INSERT IGNORE INTO user_role (tenant_id, user_id, role_id) VALUES
    (1, 3, 3);

-- Libre311 Request Manager
INSERT IGNORE INTO user_role (tenant_id, user_id, role_id) VALUES
    (1, 4, 4);

-- Libre311 Jurisdiction Administrator
INSERT IGNORE INTO user_role (tenant_id, user_id, role_id) VALUES
    (1, 5, 5);

-- Libre311 Jurisdiction Request Manager
INSERT IGNORE INTO user_role (tenant_id, user_id, role_id) VALUES
    (1, 6, 6);


-- Stl sub-tenant admin
INSERT IGNORE INTO user_role (tenant_id, user_id, role_id) VALUES
    (1, 7, 5);
