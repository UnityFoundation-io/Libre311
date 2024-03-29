CREATE TABLE IF NOT EXISTS temp_service_request_location_table
AS
SELECT id, latitude, longitude
FROM service_requests;

ALTER TABLE service_requests
DROP COLUMN latitude,
DROP COLUMN longitude;

ALTER TABLE service_requests
ADD location POINT SRID 4326;

UPDATE service_requests AS sr
    JOIN temp_service_request_location_table AS tr
ON sr.id = tr.id
    SET sr.location = ST_SRID(Point(tr.longitude, tr.latitude), 4326);

DROP TABLE IF EXISTS temp_service_request_location_table;

ALTER TABLE service_requests
MODIFY location POINT NOT NULL SRID 4326;