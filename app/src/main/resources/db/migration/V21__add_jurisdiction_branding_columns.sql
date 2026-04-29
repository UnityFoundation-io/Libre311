-- These columns were missing from Flyway and were being silently created by
-- hbm2ddl.auto=update. Conditional adds for idempotency on databases where
-- hbm2ddl already created them.

SET @s = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'jurisdictions' AND COLUMN_NAME = 'abbreviated_name') = 0,
    'ALTER TABLE jurisdictions ADD COLUMN abbreviated_name VARCHAR(8)',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'jurisdictions' AND COLUMN_NAME = 'primary_color') = 0,
    'ALTER TABLE jurisdictions ADD COLUMN primary_color VARCHAR(255)',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'jurisdictions' AND COLUMN_NAME = 'primary_hover_color') = 0,
    'ALTER TABLE jurisdictions ADD COLUMN primary_hover_color VARCHAR(255)',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'jurisdictions' AND COLUMN_NAME = 'logo_media_url') = 0,
    'ALTER TABLE jurisdictions ADD COLUMN logo_media_url VARCHAR(255)',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
