-- Add columns to control how many days closed service requests remain visible
ALTER TABLE jurisdictions ADD COLUMN closed_request_days_visible_user INT DEFAULT 7;
ALTER TABLE jurisdictions ADD COLUMN closed_request_days_visible_admin INT DEFAULT 90;
