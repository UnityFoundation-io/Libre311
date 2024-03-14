ALTER TABLE services DROP COLUMN service_definition_json;
ALTER TABLE services DROP COLUMN metadata;

CREATE TABLE IF NOT EXISTS service_definitions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    service_id BIGINT NOT NULL,
    FOREIGN KEY (service_id) REFERENCES services(id)
);

CREATE TABLE IF NOT EXISTS service_definition_attributes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    attribute_order INTEGER NOT NULL,
    code VARCHAR(255),
    datatype VARCHAR(255),
    datatype_description VARCHAR(255),
    description VARCHAR(255),
    required BIT NOT NULL,
    variable BIT NOT NULL,
    service_definition_id BIGINT NOT NULL,
    FOREIGN KEY (service_definition_id) REFERENCES service_definitions(id)
);

CREATE TABLE IF NOT EXISTS service_definition_attribute_values (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    value_name VARCHAR(255),
    service_definition_attribute_id BIGINT NOT NULL,
    FOREIGN KEY (service_definition_attribute_id) REFERENCES service_definition_attributes(id)
);
