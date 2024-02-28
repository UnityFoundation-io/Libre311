CREATE TABLE IF NOT EXISTS service_groups (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    jurisdiction_id VARCHAR(255) NOT NULL,
    FOREIGN KEY (jurisdiction_id) REFERENCES jurisdictions(id)
);

-- create a default service group for each jurisdiction
INSERT INTO service_groups (name, jurisdiction_id)
    SELECT 'Default Service Group', id
    FROM jurisdictions;

ALTER TABLE services
    ADD COLUMN service_group_id BIGINT;

UPDATE services s
    SET service_group_id = (SELECT id FROM service_groups sg WHERE sg.jurisdiction_id = s.jurisdiction_id AND sg.name = 'Default Service Group')
    WHERE service_group_id IS NULL;


ALTER TABLE services
    MODIFY COLUMN service_group_id BIGINT NOT NULL,
    ADD FOREIGN KEY (service_group_id) REFERENCES service_groups(id);