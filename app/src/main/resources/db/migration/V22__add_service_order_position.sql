-- order_position on services was missing from Flyway and silently added by hbm2ddl.
SET @s = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'services' AND COLUMN_NAME = 'order_position') = 0,
    'ALTER TABLE services ADD COLUMN order_position INT NOT NULL DEFAULT -1',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
