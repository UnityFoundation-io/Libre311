CREATE TABLE service_request_removal_suggestions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    service_request_id BIGINT NOT NULL,
    jurisdiction_id VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    phone VARCHAR(255),
    reason TEXT NOT NULL,
    date_created TIMESTAMP NOT NULL,
    deleted BOOL NOT NULL,
    CONSTRAINT fk_service_request_removal_suggestions_service_request
        FOREIGN KEY (service_request_id)
        REFERENCES service_requests (id)
        ON DELETE CASCADE
);
