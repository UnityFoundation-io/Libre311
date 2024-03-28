CREATE TABLE IF NOT EXISTS jurisdiction_boundary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    boundary POLYGON NOT NULL SRID 4326,
    jurisdiction_id VARCHAR(255) NOT NULL,
    FOREIGN KEY (jurisdiction_id) REFERENCES jurisdictions(id)
);

DROP TABLE IF EXISTS boundary_coordinates;