
CREATE TABLE IF NOT EXISTS service_groups (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    jurisdiction_id VARCHAR(255) NOT NULL,
    FOREIGN KEY (jurisdiction_id) REFERENCES jurisdictions(id)
);

ALTER TABLE services
    ADD COLUMN service_group_id BIGINT NOT NULL,
    ADD FOREIGN KEY (service_group_id) REFERENCES service_groups(id);