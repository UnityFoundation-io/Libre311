-- The original schema created this column as `isUserAdmin` (camelCase).
-- Hibernate's naming strategy expects `is_user_admin`, so hbm2ddl added a
-- second column, leaving both in the table. This migration consolidates them.
-- On fresh databases only `isUserAdmin` exists, so we add `is_user_admin` first.

SET @s = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'jurisdiction_user' AND COLUMN_NAME = 'is_user_admin') = 0,
    'ALTER TABLE jurisdiction_user ADD COLUMN is_user_admin BOOLEAN NOT NULL DEFAULT FALSE',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Carry over any rows where the old column is set but the new one isn't.
UPDATE jurisdiction_user
SET is_user_admin = TRUE
WHERE isUserAdmin = TRUE
  AND is_user_admin = FALSE;

ALTER TABLE jurisdiction_user DROP COLUMN isUserAdmin;
