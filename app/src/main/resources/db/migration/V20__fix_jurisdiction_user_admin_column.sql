-- The original schema created this column as `isUserAdmin` (camelCase).
-- Hibernate's naming strategy expects `is_user_admin`, so hbm2ddl added a
-- second column, leaving both in the table. This migration consolidates them.

-- Carry over any rows where the old column is set but the new one isn't.
UPDATE jurisdiction_user
SET is_user_admin = TRUE
WHERE isUserAdmin = TRUE
  AND is_user_admin = FALSE;

ALTER TABLE jurisdiction_user DROP COLUMN isUserAdmin;
