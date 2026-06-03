-- Add standalone index so the jurisdiction_id FK retains index support
-- when we drop the compound index from V17
ALTER TABLE projects ADD INDEX idx_projects_jurisdiction_id (jurisdiction_id);

-- Drop the V17 compound index, then the virtual column
DROP INDEX idx_projects_jurisdiction_slug ON projects;
ALTER TABLE projects DROP COLUMN slug;

-- Add a real slug column
ALTER TABLE projects ADD COLUMN slug VARCHAR(255) NOT NULL DEFAULT '';

-- Backfill existing rows using the counter approach:
-- partition by (jurisdiction_id, base_slug) ordered by id so the first project
-- gets the plain slug and subsequent ones get slug-1, slug-2, etc.
UPDATE projects p
JOIN (
    SELECT id,
           base_slug,
           ROW_NUMBER() OVER (PARTITION BY jurisdiction_id, base_slug ORDER BY id) - 1 AS cnt
    FROM (
        SELECT id,
               jurisdiction_id,
               LOWER(REGEXP_REPLACE(REGEXP_REPLACE(name, '[^a-zA-Z0-9 ]', ''), ' +', '-')) AS base_slug
        FROM projects
    ) base
) sub ON p.id = sub.id
SET p.slug = CASE WHEN sub.cnt = 0 THEN sub.base_slug ELSE CONCAT(sub.base_slug, '-', sub.cnt) END;

ALTER TABLE projects ALTER COLUMN slug DROP DEFAULT;

-- uk_projects_jurisdiction_slug covers jurisdiction_id as its leftmost prefix,
-- so the temporary standalone index is no longer needed
ALTER TABLE projects ADD UNIQUE KEY uk_projects_jurisdiction_slug (jurisdiction_id, slug);
DROP INDEX idx_projects_jurisdiction_id ON projects;
