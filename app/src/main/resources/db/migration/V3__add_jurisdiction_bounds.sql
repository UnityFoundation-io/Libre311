CREATE TABLE IF NOT EXISTS boundary_coordinates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    jurisdiction_id VARCHAR(255) NOT NULL,
    FOREIGN KEY (jurisdiction_id) REFERENCES jurisdictions(id)
);