-- Update the view to include the requestCount column that Hibernate expects for the native query mapping
CREATE OR REPLACE VIEW open_projects AS
SELECT p.*, (SELECT COUNT(*) FROM service_requests sr WHERE sr.project_id = p.id) AS requestCount
FROM projects p
WHERE p.closed_date IS NULL AND p.end_date >= CURRENT_TIMESTAMP;
