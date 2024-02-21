CREATE TABLE IF NOT EXISTS  app_users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS jurisdictions (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255),
    tenant_id BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS remote_hosts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255),
    jurisdiction_id VARCHAR(255) NOT NULL,
    CONSTRAINT FK_JURISDICTION
        FOREIGN KEY (jurisdiction_id)
            REFERENCES jurisdictions (id)
            ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS jurisdiction_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    jurisdiction_id VARCHAR(255) NOT NULL,
    isUserAdmin BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE,
    FOREIGN KEY (jurisdiction_id) REFERENCES jurisdictions(id)
);

CREATE TABLE IF NOT EXISTS services (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    service_code VARCHAR(255) NOT NULL,
    jurisdiction_id VARCHAR(255) NOT NULL,
    service_definition_json TEXT,
    service_name TEXT NOT NULL,
    description TEXT,
    metadata BOOLEAN DEFAULT FALSE,
    type VARCHAR(255),
    FOREIGN KEY (jurisdiction_id) REFERENCES jurisdictions(id)
);

CREATE TABLE IF NOT EXISTS service_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    jurisdiction_id VARCHAR(255) NOT NULL,
    services_id BIGINT NOT NULL,
    attributes_json TEXT,
    latitude VARCHAR(255) NOT NULL,
    longitude VARCHAR(255) NOT NULL,
    address_string VARCHAR(255),
    address_id VARCHAR(255),
    email VARCHAR(255),
    device_id VARCHAR(255),
    account_id VARCHAR(255),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    phone VARCHAR(255),
    description TEXT,
    media_url TEXT,
    status VARCHAR(50) NOT NULL,
    status_notes TEXT,
    agency_responsible VARCHAR(255),
    service_notice VARCHAR(255),
    zipcode VARCHAR(10),
    agency_email VARCHAR(255),
    priority VARCHAR(255),
    expected_date TIMESTAMP,
    closed_date TIMESTAMP,
    date_created TIMESTAMP NOT NULL,
    date_updated TIMESTAMP NOT NULL,
    FOREIGN KEY (services_id) REFERENCES services(id),
    FOREIGN KEY (jurisdiction_id) REFERENCES jurisdictions(id)
);







