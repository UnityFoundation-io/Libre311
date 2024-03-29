ALTER TABLE service_requests
DROP COLUMN latitude;

ALTER TABLE service_requests
DROP COLUMN longitude;

ALTER TABLE service_requests
ADD location POINT NOT NULL SRID 4326;

