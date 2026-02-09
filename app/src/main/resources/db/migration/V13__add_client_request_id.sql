ALTER TABLE service_requests ADD COLUMN client_request_id VARCHAR(36) NULL;
ALTER TABLE service_requests ADD CONSTRAINT uq_client_request_id UNIQUE (client_request_id);
